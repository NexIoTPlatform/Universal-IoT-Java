/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.mqtt.protocol.third;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.mqtt.protocol.entity.MQTTProductConfig;
import cn.universal.mqtt.protocol.entity.MQTTPublishMessage;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.metrics.MqttMetricsMananer;
import cn.universal.mqtt.protocol.processor.MqttUPProcessorChain;
import cn.universal.mqtt.protocol.system.SysMQTTManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * MQTT服务器管理器 - 重构版
 *
 * <p>专注于MQTT客户端的连接管理和消息处理 配置管理交由MqttConfigService统一处理 系统MQTT交由SystemMqttManager专门处理
 *
 * @version 3.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "mqtt")
@Service
@DependsOn("sysMQTTManager")
public class ThirdMQTTServerManager implements ApplicationListener<ApplicationReadyEvent> {

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    initialize();
  }

  @Autowired private ThirdMQTTConfigService configService;

  @Autowired private SysMQTTManager sysMQTTManager;

  /** 命名虚拟线程执行器 - 用于需要特定命名的任务 */
  @Resource(name = "namedVirtualThreadExecutor")
  private ExecutorService executorService;

  @Autowired private MqttUPProcessorChain processorChain;

  @Autowired private MqttMetricsMananer metricsCollector;
  @Autowired private MQTTTopicManager mqttTopicManager;

  @Value("${mqtt.protocol.enabled:true}")
  private boolean enabled;

  @Value("${mqtt.cfg.third.maxInflight:500}")
  private int maxInflight;

  // === 客户端管理 ===
  private final Map<String, MqttAsyncClient> networkClients = new ConcurrentHashMap<>();
  private final Map<String, MQTTProductConfig> networkConfigs = new ConcurrentHashMap<>();

  // === 连接状态管理 ===
  private final Map<String, Boolean> connectionStatus = new ConcurrentHashMap<>();
  private final Map<String, Long> lastConnectTime = new ConcurrentHashMap<>();

  // === 线程池 ===
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private final ExecutorService startupExecutor =
      Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("MQTT-Startup-", 0).factory());

  // === 重连管理 ===
  private final Map<String, Integer> reconnectRetryMap = new ConcurrentHashMap<>();
  private final int maxReconnectRetry = 15; // 最大重试次数
  private final long maxReconnectDelayMillis = 30000; // 最大重试间隔30秒

  private volatile boolean started = false;

  public void initialize() {
    if (!enabled) {
      log.info("[THIRD_MQTT] MQTT模块已禁用，跳过初始化");
      return;
    }

    log.info("[THIRD_MQTT] 开始初始化MQTT服务器...");

    // 延迟执行，确保配置注入完成
    scheduler.schedule(
        () -> {
          try {
            //        // 1. 初始化系统MQTT
            //        sysMQTTManager.initialize(scheduler);

            // 2. 加载并启动产品客户端
            loadAndStartAllClients();

            // 3. 启动健康检查
            startHealthCheck();

            started = true;
            log.info("[THIRD_MQTT] MQTT服务器初始化完成");

          } catch (Exception e) {
            log.error("[THIRD_MQTT] MQTT服务器初始化失败: ", e);
          }
        },
        2,
        TimeUnit.SECONDS);
  }

  /** MQTT回调处理器 */
  private class MqttCallbackHandler implements MqttCallback {

    private final String networkUnionId;

    public MqttCallbackHandler(String networkUnionId) {
      this.networkUnionId = networkUnionId;
    }

    @Override
    public void connectionLost(Throwable cause) {
      log.warn("[THIRD_MQTT] 产品连接丢失: {}, 原因: {}", networkUnionId, cause.getMessage());
      connectionStatus.put(networkUnionId, false);
      reconnectClient(networkUnionId, networkConfigs.get(networkUnionId));
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
      try {
        String payload = new String(message.getPayload());
        log.info("[THIRD_MQTT] 收到消息: 产品={}, 主题={}, 消息={}", networkUnionId, topic, payload);
        String productKey = mqttTopicManager.extractProductKeyFromTopic(topic);
        boolean isSupport = configService.supportMQTTNetwork(productKey, networkUnionId);
        if (!isSupport) {
          log.warn(
              "[THIRD_MQTT] 消息不支持，产品={},networkUnionId={},topic={},payload={} }",
              productKey,
              networkUnionId,
              topic,
              payload);
          return;
        }
        // 构建UP请求并处理
        MQTTUPRequest request = buildMqttUPRequest(topic, payload, productKey);
        request.setPayload(payload);
        request.setNetworkUnionId(networkUnionId);
        // 通过处理链处理
        executorService.submit(() -> processorChain.process(request));
        metricsCollector.incrementActiveClientCount();

      } catch (Exception e) {
        log.error("[THIRD_MQTT] 消息处理异常: 产品={}, 主题={}, 异常=", networkUnionId, topic, e);
        metricsCollector.incrementErrorCount();
      }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
      log.debug(
          "[THIRD_MQTT] 消息发送完成: 产品={}, topics={} , 消息ID={}",
          networkUnionId,
          token.getTopics(),
          token.getMessageId());
    }

    /** 构建MQTT UP请求 */
    private MQTTUPRequest buildMqttUPRequest(String topic, String payload, String productKey) {
      MQTTUPRequest request =
          MQTTUPRequest.builder()
              .upTopic(topic)
              .productKey(productKey)
              .messageId(IdUtil.simpleUUID())
              .isSysMQTTBroker(false)
              .deviceId(mqttTopicManager.extractDeviceIdFromTopic(topic))
              .build();
      request.setPayload(payload);
      return request;
    }
  }

  /** 加载并启动所有产品客户端 */
  public void loadAndStartAllClients() {
    try {
      // 从配置服务加载所有MQTT配置
      Map<String, MQTTProductConfig> allConfigs = configService.loadAllConfigs();
      networkConfigs.putAll(allConfigs);

      // 启动所有产品客户端
      for (Map.Entry<String, MQTTProductConfig> entry : networkConfigs.entrySet()) {
        String unionId = entry.getKey();
        MQTTProductConfig config = entry.getValue();

        try {
          // 检查是否被系统MQTT覆盖
          if (sysMQTTManager.isProductCovered(unionId)) {
            log.info("[THIRD_MQTT] 产品 {} 已被系统MQTT覆盖，跳过单独启动", unionId);
            continue;
          }

          // 启动产品客户端
          initializeClient(unionId, config);

        } catch (Exception e) {
          log.error("[THIRD_MQTT] 启动产品客户端失败: unionId={}, error={}", unionId, e.getMessage(), e);
        }
      }

      log.info("[THIRD_MQTT] 产品客户端启动完成，数量: {}", networkClients.size());

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 加载和启动客户端失败: ", e);
    }
  }

  //  /** 初始化单个客户端 */
  //  private boolean initializeClient(String unionId, MQTTProductConfig config) {
  //    try {
  //      String clientId = buildClientId(unionId);
  //      MqttAsyncClient client =
  //          new MqttAsyncClient(config.getHost(), clientId, new MemoryPersistence());
  //
  //      // 设置回调
  //      client.setCallback(new MqttCallbackHandler(unionId));
  //
  //      // 配置连接选项
  //      MqttConnectOptions options = buildConnectOptions(config);
  //
  //      // 连接到broker
  //      client.connect(options);
  //
  //      // 订阅主题
  //      subscribeTopics(client, config);
  //
  //      // 保存客户端
  //      networkClients.put(unionId, client);
  //      connectionStatus.put(unionId, true);
  //      lastConnectTime.put(unionId, System.currentTimeMillis());
  //
  //      log.info("[THIRD_MQTT] MQTT客户端启动成功: {} -> {}", unionId, config.getHost());
  //      metricsCollector.incrementActiveClientCount();
  //      return true;
  //
  //    } catch (Exception e) {
  //      log.error("[THIRD_MQTT] 初始化MQTT客户端失败: unionId={}, error={}", unionId, e.getMessage(), e);
  //      connectionStatus.put(unionId, false);
  //      metricsCollector.incrementErrorCount();
  //      return false;
  //    }
  //  }

  /** 构建客户端ID */
  private String buildClientId(String networkUnionId) {
    String instanceId = System.getProperty("server.port", "8080");
    long timestamp = System.currentTimeMillis();
    return String.format("%s-%s-%d", networkUnionId, instanceId, timestamp);
  }

  /** 构建连接选项 */
  private MqttConnectOptions buildConnectOptions(MQTTProductConfig config) {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setMaxInflight(maxInflight);
    options.setCleanSession(config.isCleanSession());
    options.setKeepAliveInterval(config.getKeepAliveInterval());
    options.setConnectionTimeout(config.getConnectTimeout());

    if (config.getUsername() != null && !config.getUsername().isEmpty()) {
      options.setUserName(config.getUsername());
    }
    if (config.getPassword() != null && !config.getPassword().isEmpty()) {
      options.setPassword(config.getPassword().toCharArray());
    }

    options.setAutomaticReconnect(config.isAutoReconnect());

    return options;
  }

  /** 初始化单个客户端 */
  private boolean initializeClient(String unionId, MQTTProductConfig config) {
    try {
      String clientId = buildClientId(unionId);
      MqttAsyncClient client =
          new MqttAsyncClient(config.getHost(), clientId, new MemoryPersistence());

      // 设置回调
      client.setCallback(new MqttCallbackHandler(unionId));

      // 配置连接选项
      MqttConnectOptions options = buildConnectOptions(config);

      // 异步连接，并添加连接结果回调（关键修改）
      client.connect(
          options,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              // 连接成功后执行订阅
              try {
                subscribeTopics(client, config);
                // 连接成功后更新状态
                networkClients.put(unionId, client);
                connectionStatus.put(unionId, true);
                lastConnectTime.put(unionId, System.currentTimeMillis());
                log.info("[THIRD_MQTT] MQTT客户端连接并订阅成功: {} -> {}", unionId, config.getHost());
                metricsCollector.incrementActiveClientCount();
                // 清理重试计数
                reconnectRetryMap.remove(unionId);
              } catch (MqttException e) {
                log.error("[THIRD_MQTT] 连接成功后订阅主题失败: unionId={}", unionId, e);
                connectionStatus.put(unionId, false);
              }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              // 连接失败处理
              log.error(
                  "[THIRD_MQTT] 客户端连接失败: unionId={}, 原因: {}",
                  unionId,
                  exception.getMessage(),
                  exception);
              connectionStatus.put(unionId, false);
              metricsCollector.incrementErrorCount();

              // 自动重连
              int retryCount = reconnectRetryMap.getOrDefault(unionId, 0) + 1;
              MQTTProductConfig config = networkConfigs.get(unionId);
              if (config != null) {
                scheduleReconnect(unionId, config, retryCount);
              }
            }
          });

      return true; // 此处仅表示“发起连接成功”，实际连接结果在回调中处理

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 初始化MQTT客户端失败: unionId={}", unionId, e);
      connectionStatus.put(unionId, false);
      metricsCollector.incrementErrorCount();
      return false;
    }
  }

  /** 订阅主题（保持不变，但确保仅在连接成功后调用） */
  private void subscribeTopics(MqttAsyncClient client, MQTTProductConfig config)
      throws MqttException {
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs = config.getSubscribeTopics();

    String[] topics =
        topicConfigs.stream()
            .map(MQTTProductConfig.MqttTopicConfig::getTopicPattern)
            .toArray(String[]::new);
    int[] qosArray =
        topicConfigs.stream().mapToInt(MQTTProductConfig.MqttTopicConfig::getQos).toArray();

    // 异步订阅（可选：添加订阅结果回调，更细致地处理订阅成功/失败）
    client.subscribe(
        topics,
        qosArray,
        null,
        new IMqttActionListener() {
          @Override
          public void onSuccess(IMqttToken asyncActionToken) {
            log.info("[THIRD_MQTT] 订阅主题成功，主题={} 总数: {}", topics, topics.length);
          }

          @Override
          public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            log.warn("[THIRD_MQTT] 订阅主题失败: {}", Arrays.toString(topics), exception);
          }
        });
  }

  //  /** 订阅主题 */
  //  private void subscribeTopics(MqttAsyncClient client, MQTTProductConfig config)
  //      throws MqttException {
  //    List<MQTTProductConfig.MqttTopicConfig> topicConfigs = config.getSubscribeTopics();
  //
  //    String[] topics =
  //        topicConfigs.stream()
  //            .map(MQTTProductConfig.MqttTopicConfig::getTopicPattern)
  //            .toArray(String[]::new);
  //    int[] qosArray =
  //        topicConfigs.stream().mapToInt(MQTTProductConfig.MqttTopicConfig::getQos).toArray();
  //
  //    client.subscribe(topics, qosArray);
  //
  //    log.debug("[THIRD_MQTT] 订阅主题成功，主题={} 总数: {}", topics, topics.length);
  //  }

  /** 启动健康检查 */
  private void startHealthCheck() {
    scheduler.scheduleWithFixedDelay(
        () -> {
          try {
            performHealthCheck();
          } catch (Exception e) {
            log.error("[THIRD_MQTT] 健康检查异常: ", e);
          }
        },
        30,
        30,
        TimeUnit.SECONDS);
  }

  /** 执行健康检查 */
  private void performHealthCheck() {
    for (Map.Entry<String, MqttAsyncClient> entry : networkClients.entrySet()) {
      String unionId = entry.getKey();
      MqttAsyncClient client = entry.getValue();

      try {
        boolean isConnected = client != null && client.isConnected();
        connectionStatus.put(unionId, isConnected);

        if (!isConnected) {
          log.warn("[THIRD_MQTT] 检测到连接断开: {}", unionId);

          // 尝试重连
          MQTTProductConfig config = networkConfigs.get(unionId);
          if (config != null) {
            reconnectClient(unionId, config);
          }
        }
      } catch (Exception e) {
        log.error("[THIRD_MQTT] 健康检查失败: unionId={}, error={}", unionId, e.getMessage());
        connectionStatus.put(unionId, false);
      }
    }
  }

  /** 重连客户端 */
  private void reconnectClient(String unionId, MQTTProductConfig config) {
    try {
      log.info("[THIRD_MQTT] 尝试重连客户端: {}", unionId);

      // 先关闭旧连接
      stopMqttClient(unionId);

      // 等待片刻
      Thread.sleep(1000);

      // 重新初始化
      if (initializeClient(unionId, config)) {
        log.info("[THIRD_MQTT] 客户端重连成功: {}", unionId);
      }

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 客户端重连失败: unionId={}, error={}", unionId, e.getMessage());
    }
  }

  /** 封装自动重连调度方法 */
  private void scheduleReconnect(String unionId, MQTTProductConfig config, int retryCount) {
    if (retryCount > maxReconnectRetry) {
      log.error("[THIRD_MQTT] unionId={} 超过最大重连次数({})，不再重连", unionId, maxReconnectRetry);
      reconnectRetryMap.remove(unionId);
      return;
    }
    long delay =
        Math.min((1L << (retryCount - 1)) * 2000, maxReconnectDelayMillis); // 2s, 4s, 8s, 16s, 30s
    log.warn("[THIRD_MQTT] unionId={} 第{}次重连，{}ms后重试", unionId, retryCount, delay);

    reconnectRetryMap.put(unionId, retryCount);

    scheduler.schedule(
        () -> {
          try {
            boolean success = initializeClient(unionId, config);
            if (!success) {
              // 递归调度下一次重连
              scheduleReconnect(unionId, config, retryCount + 1);
            }
          } catch (Exception e) {
            log.error("[THIRD_MQTT] unionId={} 重连异常", unionId, e);
            scheduleReconnect(unionId, config, retryCount + 1);
          }
        },
        delay,
        TimeUnit.MILLISECONDS);
  }

  // ==================== 管理API ====================

  /** 启动MQTT客户端 */
  public boolean startMqttClient(String unionId) {
    try {
      // 首先从缓存中获取配置
      MQTTProductConfig config = networkConfigs.get(unionId);

      // 如果缓存中没有配置，尝试从数据库重新加载
      if (config == null) {
        log.info("[THIRD_MQTT] 缓存中未找到产品配置，尝试从数据库重新加载: {}", unionId);

        // 重新加载所有配置
        Map<String, MQTTProductConfig> allConfigs = configService.loadAllConfigs();
        networkConfigs.putAll(allConfigs);

        // 再次尝试获取配置
        config = networkConfigs.get(unionId);

        if (config == null) {
          log.warn("[THIRD_MQTT] 数据库中未找到产品配置: {}", unionId);
          return false;
        }

        log.info("[THIRD_MQTT] 成功从数据库加载配置: {}", unionId);
      }

      // 检查是否已连接
      if (isConnected(unionId)) {
        log.info("[THIRD_MQTT] 客户端已连接: {}", unionId);
        return true;
      }

      // 初始化并启动客户端
      return initializeClient(unionId, config);

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 启动客户端失败: unionId={}, error={}", unionId, e.getMessage(), e);
      return false;
    }
  }

  /** 停止MQTT客户端 */
  public boolean stopMqttClient(String unionId) {
    try {
      MqttAsyncClient client = networkClients.get(unionId);
      if (client != null) {
        if (client.isConnected()) {
          client.disconnect();
        }
        client.close();

        networkClients.remove(unionId);
        connectionStatus.put(unionId, false);
        networkConfigs.remove(unionId);
        log.info("[THIRD_MQTT] 客户端已停止: {}", unionId);
        metricsCollector.decrementActiveClientCount();
        return true;
      }

      // 如果客户端不存在，但配置存在，也认为是停止成功
      if (networkConfigs.containsKey(unionId)) {
        log.info("[THIRD_MQTT] 客户端不存在但配置存在，停止成功: {}", unionId);
        return true;
      }

      log.warn("[THIRD_MQTT] 未找到客户端配置: {}", unionId);
      return false;

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 停止客户端失败: unionId={}, error={}", unionId, e.getMessage(), e);
      return false;
    }
  }

  /** 重启MQTT客户端 */
  public boolean restartMqttClient(String unionId) {
    log.info("[THIRD_MQTT] 重启客户端: {}", unionId);

    // 先停止客户端
    stopMqttClient(unionId);

    try {
      Thread.sleep(1000); // 等待片刻
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // 重新启动客户端（会自动从数据库加载配置）
    return startMqttClient(unionId);
  }

  /** 发布消息 */
  public boolean publishMessage(String unionId, String topic, String payload) {
    try {
      if (StrUtil.isBlank(unionId)) {
        return false;
      }
      // 使用产品客户端发布
      MqttAsyncClient client = networkClients.get(unionId);
      MQTTProductConfig mqttProductConfig = networkConfigs.get(unionId);
      if (client == null || !client.isConnected() || mqttProductConfig == null) {
        log.debug("[THIRD_MQTT] 客户端未连接，无法发布消息: {}", unionId);
        return false;
      }

      MqttMessage mqttMessage = new MqttMessage();
      mqttMessage.setPayload(payload.getBytes());
      mqttMessage.setQos(mqttProductConfig.getDefaultQos());
      mqttMessage.setRetained(false);

      client.publish(
          topic,
          mqttMessage,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              log.info(
                  "[THIRD_MQTT] 消息发布成功 - unionId={}, 主题={},qos={},retained={}",
                  unionId,
                  topic,
                  mqttProductConfig.getDefaultQos(),
                  false);
              metricsCollector.incrementPublishMessageCount();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              log.error("[THIRD_MQTT] 消息发布失败 - 主题: {}", topic, exception);
              metricsCollector.incrementErrorCount();
            }
          });
      metricsCollector.incrementPublishMessageCount();
      return true;

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 消息发布失败 - 主题: {}, 异常: ", topic, e);
      metricsCollector.incrementErrorCount();
      return false;
    }
  }

  /** 发布消息 */
  public boolean publishMessage(String unionId, MQTTPublishMessage message) {
    try {
      if (StrUtil.isBlank(unionId)) {
        log.warn("publishMessage unionId is empty");
        return false;
      }
      // 使用产品客户端发布
      MqttAsyncClient client = networkClients.get(unionId);
      if (client == null || !client.isConnected()) {
        log.debug("[THIRD_MQTT] 客户端未连接，无法发布消息: {}", unionId);
        return false;
      }

      MqttMessage mqttMessage = new MqttMessage();
      mqttMessage.setPayload(message.getPayloadAsBytes());
      mqttMessage.setQos(message.getQos());
      mqttMessage.setRetained(message.isRetained());

      client.publish(
          message.getTopic(),
          mqttMessage,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              log.info(
                  "[THIRD_MQTT] 消息发布成功 - unionId={},主题={},qos={},retained={}",
                  unionId,
                  message.getTopic(),
                  mqttMessage.getQos(),
                  false);
              metricsCollector.incrementPublishMessageCount();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              log.error("[THIRD_MQTT] 消息发布失败 - 主题: {}", message.getTopic(), exception);
              metricsCollector.incrementErrorCount();
            }
          });
      metricsCollector.incrementPublishMessageCount();
      return true;

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 消息发布失败: unionId={}, topic={}", unionId, message.getTopic(), e);
      metricsCollector.incrementErrorCount();
      return false;
    }
  }

  /** 获取连接状态 */
  public boolean isConnected(String unionId) {
    // 检查系统MQTT覆盖
    if (sysMQTTManager.isProductCovered(unionId)) {
      return sysMQTTManager.isConnected();
    }

    // 检查产品客户端
    MqttAsyncClient client = networkClients.get(unionId);
    return client != null && client.isConnected();
  }

  /** 获取所有客户端状态 */
  public Map<String, String> getAllClientStatus() {
    Map<String, String> status = new HashMap<>();

    // 产品客户端状态
    for (String unionId : networkConfigs.keySet()) {
      if (sysMQTTManager.isProductCovered(unionId)) {
        status.put(unionId, "SYSTEM_MQTT_COVERED");
      } else {
        status.put(unionId, isConnected(unionId) ? "CONNECTED" : "DISCONNECTED");
      }
    }

    // 系统MQTT状态
    if (sysMQTTManager.isEnabled()) {
      status.put("SYSTEM_MQTT_BROKER", sysMQTTManager.isConnected() ? "CONNECTED" : "DISCONNECTED");
    }

    return status;
  }

  /** 重新加载配置 */
  public void reloadAllConfigs() {
    log.info("[THIRD_MQTT] 重新加载所有配置...");

    try {
      // 停止所有客户端
      for (String unionId : new HashSet<>(networkClients.keySet())) {
        stopMqttClient(unionId);
      }

      // 清空配置
      networkConfigs.clear();

      // 重新加载和启动
      loadAndStartAllClients();

      log.info("[THIRD_MQTT] 配置重新加载完成");

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 重新加载配置失败: ", e);
    }
  }

  /** 获取统计信息 */
  public String getStatistics() {
    StringBuilder stats = new StringBuilder();
    stats.append("MQTT服务器状态:\n");
    stats.append("服务状态: ").append(started ? "已启动" : "未启动").append("\n");
    stats.append("产品客户端数: ").append(networkClients.size()).append("\n");
    stats.append("活跃连接数: ").append(getActiveConnectionCount()).append("\n");
    stats
        .append("系统MQTT: ")
        .append(sysMQTTManager.isEnabled() ? (sysMQTTManager.isConnected() ? "已连接" : "已断开") : "已禁用")
        .append("\n");

    return stats.toString();
  }

  /** 获取活跃连接数 */
  public int getActiveConnectionCount() {
    int count = 0;
    for (String unionId : networkConfigs.keySet()) {
      if (isConnected(unionId)) {
        count++;
      }
    }
    if (sysMQTTManager.isConnected()) {
      count++;
    }
    return count;
  }

  // ==================== 内部类 ====================

  // ==================== 生命周期管理 ====================

  @PreDestroy
  public void destroy() {
    log.info("[THIRD_MQTT] 开始关闭MQTT服务器...");

    try {
      // 关闭所有产品客户端
      for (String unionId : new HashSet<>(networkClients.keySet())) {
        stopMqttClient(unionId);
      }

      // 关闭系统MQTT
      sysMQTTManager.shutdown();

      // 关闭线程池
      scheduler.shutdown();
      startupExecutor.shutdown();

      started = false;
      log.info("[THIRD_MQTT] MQTT服务器已关闭");

    } catch (Exception e) {
      log.error("[THIRD_MQTT] 关闭MQTT服务器异常: ", e);
    }
  }
}

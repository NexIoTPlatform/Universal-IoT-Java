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

package cn.universal.mqtt.protocol.system;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.IdUtil;
import cn.universal.mqtt.protocol.entity.MQTTProductConfig;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.metrics.MqttMetricsMananer;
import cn.universal.mqtt.protocol.processor.MqttUPProcessorChain;
import cn.universal.mqtt.protocol.third.ThirdMQTTConfigChecker;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Slf4j(topic = "mqtt")
@Service
public class SysMQTTManager
    implements SysMQTTStatusProvider, ApplicationListener<ApplicationReadyEvent> {

  /** 使用内置MQTT的集合 */
  private final Set<String> convertProductKey = new ConcurrentHashSet<>();

  @Value("${mqtt.cfg.maxInflight:500}")
  private int maxInflight;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    initialize();
  }

  @Autowired private MqttUPProcessorChain processorChain;

  @Autowired private MqttMetricsMananer metricsCollector;

  @Autowired private ThirdMQTTConfigChecker thirdMQTTConfigChecker;

  @Autowired private MQTTTopicManager mqttTopicManager;

  @Autowired
  @Qualifier("virtualScheduledExecutor")
  private ScheduledExecutorService scheduledExecutor;

  // === 系统MQTT配置 ===
  @Value("${mqtt.cfg.enable:true}")
  private boolean enabled;

  @Value("${mqtt.cfg.host:tcp://localhost:1883}")
  private String host;

  @Value("${mqtt.cfg.client.username:}")
  private String username;

  @Value("${mqtt.cfg.client.password:}")
  private String password;

  @Value("${mqtt.cfg.clientIdPrefix:univ_iot}")
  private String clientIdPrefix;

  @Value("${mqtt.cfg.keepAliveInterval:60}")
  private int keepAliveInterval;

  @Value("${mqtt.cfg.cleanSession:true}")
  private boolean cleanSession;

  @Value("${mqtt.cfg.autoReconnect:true}")
  private boolean autoReconnect;

  @Value("${mqtt.cfg.defaultQos:1}")
  private int defaultQos;

  // === 运行时状态 ===
  private MqttAsyncClient systemMqttClient;
  private MQTTProductConfig systemMqttConfig;
  private volatile boolean initialized = false;

  /** 系统MQTT回调处理器 */
  private class SystemMqttCallbackHandler implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {
      log.warn("[MQTT] 连接丢失: {}", cause.getMessage());
      reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
      try {
        String payload = new String(message.getPayload());
        log.info("[MQTT] 收到消息 - 主题: {}, 消息: {}", topic, payload);
        // 解析产品Key
        String productKey = mqttTopicManager.extractProductKeyFromTopic(topic);
        if (productKey != null) {
          // 构建UP请求并处理
          MQTTUPRequest request = buildMqttUPRequest(topic, payload, productKey);
          processorChain.process(request);
          metricsCollector.incrementActiveClientCount();
        } else {
          log.warn("[MQTT] 无法从主题中提取产品Key: {}", topic);
        }

      } catch (Exception e) {
        log.error("[MQTT] 消息处理异常 - 主题: {}, 异常: ", topic, e);
        metricsCollector.incrementErrorCount();
      }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
      log.info("[MQTT] 消息发送完成topic: {},messageId={}", token.getTopics(), token.getMessageId());
    }
  }

  /** 初始化系统MQTT */
  public void initialize() {
    // 验证配置是否正确注入
    if (host == null || host.isEmpty()) {
      log.warn("[MQTT] 系统MQTT配置未正确注入，使用默认配置");
    }
    log.info("[MQTT] 系统MQTT配置 - enabled: {}, host: {}, username: {}", enabled, host, username);
    if (!enabled) {
      log.info("[MQTT] 系统MQTT已禁用");
      return;
    }

    try {
      // 构建配置
      buildSystemMqttConfig();

      // 启动客户端（异步连接，通过回调处理后续操作）
      initializeSystemMqttClient();

      // 设置重连调度
      setupReconnectScheduler(scheduledExecutor);

      log.info("[MQTT] 系统MQTT初始化流程启动完成（实际连接状态待回调确认）");

    } catch (Exception e) {
      log.error("[MQTT] 系统MQTT初始化失败: ", e);
    }
  }

  /** 构建系统MQTT配置 */
  private void buildSystemMqttConfig() {
    systemMqttConfig =
        MQTTProductConfig.builder()
            .networkUnionId("SYSTEM_MQTT_BROKER")
            .networkName("系统MQTT Broker")
            .networkType("SYSTEM_MQTT")
            .host(host)
            .username(username.isEmpty() ? null : username)
            .password(password.isEmpty() ? null : password)
            .clientIdPrefix(clientIdPrefix)
            .keepAliveInterval(keepAliveInterval)
            .cleanSession(cleanSession)
            .autoReconnect(autoReconnect)
            .defaultQos(defaultQos)
            .ssl(false)
            .enabled(true)
            .subscribeTopics(parseSystemMqttTopics())
            .createdTime(LocalDateTime.now())
            .updatedTime(LocalDateTime.now())
            .build();
    log.info("[MQTT] 系统MQTT配置构建完成: {}", host);
  }

  /** 解析系统MQTT订阅主题 */
  private List<MQTTProductConfig.MqttTopicConfig> parseSystemMqttTopics() {
    return mqttTopicManager.getAllSubscriptionTopics().stream()
        .map(String::trim)
        .filter(topic -> !topic.isEmpty())
        .map(
            topic ->
                MQTTProductConfig.MqttTopicConfig.builder()
                    .topicPattern(topic)
                    .qos(defaultQos)
                    .enabled(true)
                    .build())
        .collect(Collectors.toList());
  }

  /** 初始化系统MQTT客户端（异步连接，带回调） */
  private void initializeSystemMqttClient() throws MqttException {
    String clientId = generateSystemClientId();
    systemMqttClient = new MqttAsyncClient(host, clientId, new MemoryPersistence());

    // 设置回调
    systemMqttClient.setCallback(new SystemMqttCallbackHandler());

    // 配置连接选项
    MqttConnectOptions options = buildConnectOptions();

    // 异步连接，并通过回调处理连接结果（关键修改）
    systemMqttClient.connect(
        options,
        null,
        new IMqttActionListener() {
          @Override
          public void onSuccess(IMqttToken asyncActionToken) {
            try {
              // 连接成功后执行订阅
              subscribeSystemTopics();
              initialized = true; // 仅在连接+订阅成功后标记为初始化完成
              log.info("[MQTT] 系统MQTT客户端连接并订阅成功 - Broker: {}, ClientId: {}", host, clientId);
            } catch (MqttException e) {
              log.error("[MQTT] 连接成功后订阅主题失败", e);
              initialized = false;
            }
          }

          @Override
          public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            log.error("[MQTT] 系统MQTT客户端连接失败 - Broker: {}, ClientId: {}", host, clientId, exception);
            initialized = false;
          }
        });
  }

  /** 生成系统客户端ID */
  private String generateSystemClientId() {
    String instanceId = System.getProperty("server.port", "8080");
    long timestamp = System.currentTimeMillis();
    return String.format("%s-%s-%d", clientIdPrefix, instanceId, timestamp);
  }

  /** 构建连接选项 */
  private MqttConnectOptions buildConnectOptions() {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setMaxInflight(maxInflight); // 可根据实际并发需求调整
    options.setCleanSession(cleanSession);
    options.setKeepAliveInterval(keepAliveInterval);
    options.setConnectionTimeout(30); // 增加连接超时时间（秒）

    if (username != null && !username.isEmpty()) {
      options.setUserName(username);
    }
    if (password != null && !password.isEmpty()) {
      options.setPassword(password.toCharArray());
    }

    options.setAutomaticReconnect(autoReconnect);
    options.setMaxReconnectDelay(5000);
    return options;
  }

  /** 订阅系统主题（仅在连接成功后调用） */
  private void subscribeSystemTopics() throws MqttException {
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs = systemMqttConfig.getSubscribeTopics();
    if (topicConfigs.isEmpty()) {
      log.warn("[MQTT] 系统MQTT无订阅主题配置，跳过订阅");
      return;
    }

    String[] topics =
        topicConfigs.stream()
            .map(MQTTProductConfig.MqttTopicConfig::getTopicPattern)
            .toArray(String[]::new);
    int[] qosArray =
        topicConfigs.stream().mapToInt(MQTTProductConfig.MqttTopicConfig::getQos).toArray();

    // 异步订阅，添加订阅结果回调（可选，增强可观测性）
    systemMqttClient.subscribe(
        topics,
        qosArray,
        null,
        new IMqttActionListener() {
          @Override
          public void onSuccess(IMqttToken asyncActionToken) {
            log.info("[MQTT] 订阅主题成功，主题={} 总数: {}", topics, topics.length);
          }

          @Override
          public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            log.info("[MQTT] 订阅主题失败: {}", topics, exception);
          }
        });
  }

  /** 设置重连调度器 */
  private void setupReconnectScheduler(ScheduledExecutorService scheduler) {
    scheduler.scheduleWithFixedDelay(
        () -> {
          try {
            if (!initialized || (systemMqttClient != null && !systemMqttClient.isConnected())) {
              // initialized和状态不一致，有假死风险，直接断开
              systemMqttClient.disconnect();
              log.warn("[MQTT] 检测到连接断开，尝试重连...");
              reconnect();
            }
          } catch (Exception e) {
            log.error("[MQTT] 健康检查异常: ", e);
            reconnect();
          }
        },
        30,
        30,
        TimeUnit.SECONDS);
  }

  /** 重新连接（带回调的异步重连） */
  private void reconnect() {
    try {
      log.info("SysMQTT开始进入重连");
      if (systemMqttClient == null) {
        log.warn("[MQTT] 客户端未初始化，跳过重连");
        return;
      }

      if (systemMqttClient.isConnected()) {
        log.debug("[MQTT] 客户端已连接，无需重连");
        return;
      }

      MqttConnectOptions options = buildConnectOptions();
      // 异步重连，连接成功后重新订阅
      systemMqttClient.connect(
          options,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              try {
                subscribeSystemTopics(); // 重连成功后重新订阅
                log.info("[MQTT] 重连成功");
                initialized = true;
              } catch (MqttException e) {
                log.error("[MQTT] 重连成功后订阅失败", e);
                initialized = false;
              }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              log.error("[MQTT] 重连失败", exception);
              initialized = false;
            }
          });
    } catch (Exception e) {
      log.error("[MQTT] 重连操作触发异常", e);
    }
  }

  /** 发布消息（添加连接状态检查） */
  public boolean publishMessage(String topic, byte[] payload, int qos, boolean retained) {
    try {
      if (systemMqttClient == null || !systemMqttClient.isConnected()) {
        log.warn("[MQTT] 系统MQTT未连接，无法发布消息（主题: {}）", topic);
        return false;
      }

      MqttMessage mqttMessage = new MqttMessage();
      mqttMessage.setPayload(payload);
      mqttMessage.setQos(qos);
      mqttMessage.setRetained(retained);

      // 异步发布，添加发布结果回调（可选）
      systemMqttClient.publish(
          topic,
          mqttMessage,
          null,
          new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
              log.info("[MQTT] 消息发布成功 - 主题={},qos={},retained={}", topic, qos, retained);
              metricsCollector.incrementPublishMessageCount();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
              log.error("[MQTT] 消息发布失败 - 主题: {}", topic, exception);
              metricsCollector.incrementErrorCount();
            }
          });

      return true;

    } catch (Exception e) {
      log.error("[MQTT] 消息发布失败 - 主题: {}", topic, e);
      metricsCollector.incrementErrorCount();
      return false;
    }
  }

  /** 检查产品是否被系统MQTT覆盖 */
  public boolean isProductCovered(String productKey) {
    return isUseSysMQTT(productKey);
  }

  /** 获取连接状态 */
  public boolean isConnected() {
    return systemMqttClient != null && systemMqttClient.isConnected();
  }

  /** 获取配置 */
  public MQTTProductConfig getConfig() {
    return systemMqttConfig;
  }

  /** 是否启用 */
  public boolean isEnabled() {
    return enabled;
  }

  /** 关闭系统MQTT */
  public void shutdown() {
    if (systemMqttClient != null) {
      try {
        if (systemMqttClient.isConnected()) {
          // 异步断开连接，带回调
          systemMqttClient.disconnect(
              null,
              new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                  try {
                    systemMqttClient.close();
                    log.info("[MQTT] 系统MQTT客户端已关闭");
                  } catch (MqttException e) {
                    log.error("[MQTT] 关闭客户端失败", e);
                  }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                  log.error("[MQTT] 断开连接失败", exception);
                }
              });
        } else {
          systemMqttClient.close();
          log.info("[MQTT] 系统MQTT客户端已关闭");
        }
      } catch (Exception e) {
        log.error("[MQTT] 关闭系统MQTT客户端异常: ", e);
      }
    }
  }

  /** 构建MQTT UP请求 */
  private MQTTUPRequest buildMqttUPRequest(String topic, String payload, String productKey) {
    MQTTUPRequest request =
        MQTTUPRequest.builder()
            .upTopic(topic)
            .productKey(productKey)
            .messageId(IdUtil.simpleUUID())
            .isSysMQTTBroker(true)
            .deviceId(mqttTopicManager.extractDeviceIdFromTopic(topic))
            .build();
    request.setPayload(payload);
    convertProductKey.add(productKey);
    return request;
  }

  @Override
  public boolean isUseSysMQTT(String productKey) {
    if (productKey == null) {
      return false;
    }
    return convertProductKey.contains(productKey);
  }
}

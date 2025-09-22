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

package cn.universal.dm.device.service.push;

import cn.hutool.json.JSONUtil;
import cn.universal.core.message.UPRequest;
import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.dm.device.service.push.processor.PushRetryProcessor;
import cn.universal.dm.device.service.push.processor.PushStatisticsProcessor;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.entity.bo.UPPushBO;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 推送策略管理器
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/9
 */
@Slf4j
@Component
public class PushStrategyManager {

  @Autowired private HttpPushStrategy httpPushStrategy;

  @Autowired private MqttPushStrategy mqttPushStrategy;

  @Autowired private KafkaPushStrategy kafkaPushStrategy;

  @Autowired private RocketMQPushStrategy rocketMQPushStrategy;

  @Autowired private UPProcessorManager upProcessorManager;

  @Autowired private PushStatisticsProcessor pushStatisticsProcessor;

  @Autowired private PushRetryProcessor pushRetryProcessor;

  /**
   * 执行推送
   *
   * @param request 上行请求
   * @param config 推送配置
   * @return 推送结果列表
   */
  public List<IoTPushResult> executePush(BaseUPRequest request, UPPushBO config) {
    if (request == null || config == null) {
      log.warn("[推送策略管理器] 请求或配置为空，跳过推送");
      return null;
    }

    String messageJson = JSONUtil.toJsonStr((UPRequest) request);
    log.debug("[推送策略管理器] 开始推送消息: {}", request.getIotId());

    List<IoTPushResult> results = new java.util.ArrayList<>();

    // HTTP 推送
    if (config.getHttp() != null && config.getHttp().isEnable()) {
      httpPushStrategy.setConfig(config.getHttp());
      if (httpPushStrategy.isSupported()) {
        IoTPushResult result = executeSinglePush(httpPushStrategy, request, messageJson, "HTTP");
        if (result != null) {
          results.add(result);
        }
      }
    }

    // MQTT 推送
    if (config.getMqtt() != null && config.getMqtt().isEnable()) {
      mqttPushStrategy.setConfig(config.getMqtt());
      if (mqttPushStrategy.isSupported()) {
        IoTPushResult result = executeSinglePush(mqttPushStrategy, request, messageJson, "MQTT");
        if (result != null) {
          results.add(result);
        }
      }
    }

    // Kafka 推送
    if (config.getKafka() != null && config.getKafka().isEnable()) {
      kafkaPushStrategy.setConfig(config.getKafka());
      if (kafkaPushStrategy.isSupported()) {
        IoTPushResult result = executeSinglePush(kafkaPushStrategy, request, messageJson, "Kafka");
        if (result != null) {
          results.add(result);
        }
      }
    }

    // RocketMQ 推送
    if (config.getRocketMQ() != null && config.getRocketMQ().isEnable()) {
      rocketMQPushStrategy.setConfig(config.getRocketMQ());
      if (rocketMQPushStrategy.isSupported()) {
        IoTPushResult result =
            executeSinglePush(rocketMQPushStrategy, request, messageJson, "RocketMQ");
        if (result != null) {
          results.add(result);
        }
      }
    }

    // 异步处理推送结果
    CompletableFuture.runAsync(
        () -> {
          processPushResults(results, request);
        });

    // 执行推送后处理器
    upProcessorManager.executeAfterPush(List.of(request), results);

    log.debug("[推送策略管理器] 推送完成: {}", request.getIotId());
    return results;
  }

  /** 执行单个推送策略 */
  private IoTPushResult executeSinglePush(
      PushStrategy strategy, BaseUPRequest request, String messageJson, String channel) {
    long startTime = System.currentTimeMillis();

    try {
      IoTPushResult result = strategy.execute(request, messageJson);

      // 补充推送结果信息
      if (result != null) {
        result.setDeviceId(request.getIotId());
        result.setProductKey(request.getProductKey());
        result.setChannel(channel);
        result.setResponseTime(System.currentTimeMillis() - startTime);
        result.setRequestId(request.getIotId() + "_" + System.currentTimeMillis());
      }

      return result;
    } catch (Exception e) {
      log.error("[推送策略管理器] {} 推送失败: {}", channel, request.getIotId(), e);

      // 创建失败结果 - 从设备配置中获取平台信息
      String platform = null;
      if (request.getIoTDeviceDTO() != null) {
        platform = request.getIoTDeviceDTO().getThirdPlatform();
      }

      return IoTPushResult.failed(
          platform, // 从设备配置中获取平台信息
          request.getProductKey(),
          request.getIotId(),
          channel,
          messageJson,
          e.getMessage(),
          "PUSH_ERROR");
    }
  }

  /** 处理推送结果 */
  private void processPushResults(List<IoTPushResult> results, BaseUPRequest request) {
    if (results == null || results.isEmpty()) {
      return;
    }

    for (IoTPushResult result : results) {
      try {
        // 只处理非空结果
        if (result != null) {
          // 处理失败推送
          if (!result.isOk()) {
            pushRetryProcessor.addToRetryQueue(result);
            pushRetryProcessor.recordFailedPush(result);
            log.warn(
                "[推送策略管理器] 推送失败，已加入重试队列: deviceId={}, channel={}, error={}",
                result.getDeviceId(),
                result.getChannel(),
                result.getErrorMessage());
          } else {
            log.debug(
                "[推送策略管理器] 推送成功: deviceId={}, channel={}, responseTime={}ms",
                result.getDeviceId(),
                result.getChannel(),
                result.getResponseTime());
          }
        }
      } catch (Exception e) {
        log.error(
            "[推送策略管理器] 处理推送结果失败: deviceId={}, channel={}",
            result.getDeviceId(),
            result.getChannel(),
            e);
      }
    }
  }

  /**
   * 批量推送
   *
   * @param requests 请求列表
   * @param config 推送配置
   * @return 所有推送结果
   */
  public List<IoTPushResult> executeBatchPush(List<BaseUPRequest> requests, UPPushBO config) {
    if (requests == null || requests.isEmpty()) {
      log.warn("[推送策略管理器] 请求列表为空，跳过批量推送");
      return null;
    }

    log.debug("[推送策略管理器] 开始批量推送，消息数量: {}", requests.size());

    // 执行推送前处理
    List<BaseUPRequest> processedRequests = upProcessorManager.executeBeforePush(requests);

    List<IoTPushResult> allResults = new java.util.ArrayList<>();

    for (BaseUPRequest request : processedRequests) {
      try {
        List<IoTPushResult> results = executePush(request, config);
        if (results != null) {
          allResults.addAll(results);
        }
      } catch (Exception e) {
        log.error("[推送策略管理器] 单条消息推送失败: {}", request.getIotId(), e);

        // 创建失败结果
        IoTPushResult failedResult =
            IoTPushResult.failed(
                request.getIoTDeviceDTO().getThirdPlatform(), // 平台信息暂时设为null，后续可以从配置中获取
                request.getProductKey(),
                request.getIotId(),
                "BATCH_PUSH",
                JSONUtil.toJsonStr(request),
                e.getMessage(),
                "BATCH_PUSH_ERROR");
        allResults.add(failedResult);
      }
    }

    log.info("[推送策略管理器] 批量推送完成，消息数量: {}, 结果数量: {}", requests.size(), allResults.size());
    return allResults;
  }
}

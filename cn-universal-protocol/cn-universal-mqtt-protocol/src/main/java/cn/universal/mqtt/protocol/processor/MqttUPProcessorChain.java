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

package cn.universal.mqtt.protocol.processor;

import cn.universal.dm.device.service.plugin.ProcessorExecutor;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.processor.MqttMessageProcessor.ProcessorResult;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQTT 处理器链
 *
 * <p>使用通用的ProcessorExecutor执行处理器逻辑 专注于MQTT业务逻辑，通用逻辑由ProcessorExecutor处理
 *
 * @version 2.0 @Author Aleo
 * @since 2025/01/20
 */
@Slf4j(topic = "mqtt")
@Component
public class MqttUPProcessorChain {

  @Autowired private ProcessorExecutor processorExecutor;

  @Autowired private List<MqttMessageProcessor> processors;

  /**
   * 处理MQTT消息
   *
   * @param request MQTT上行请求
   * @return 处理结果
   */
  public boolean process(MQTTUPRequest request) {
    if (request == null) {
      log.warn("[THIRD_MQTT_UP] 请求为空，跳过处理");
      return false;
    }

    log.debug(
        "[THIRD_MQTT_UP] 开始处理MQTT消息，设备: {}, 主题: {}, 消息ID: {}",
        request.getDeviceId(),
        request.getUpTopic(),
        request.getMessageId());

    // 使用ProcessorExecutor执行处理器链
    boolean success =
        processorExecutor.executeChain(
            processors,
            "THIRD_MQTT_UP",
            processor -> {
              try {
                // 执行前置检查
                if (!processor.preCheck(request)) {
                  log.debug("[THIRD_MQTT_UP] 处理器 {} 预检查失败", processor.getName());
                  return null; // 返回null表示跳过
                }

                // 执行处理器
                ProcessorResult result = processor.process(request);
                // 执行后置处理
                processor.postProcess(request, result);

                return result;
              } catch (Exception e) {
                // 调用处理器的异常处理方法
                processor.onError(request, e);
                log.error("[THIRD_MQTT_UP] 处理器 {} 执行异常: ", processor.getName(), e);
                return null;
              }
            },
            result ->
                result != null
                    && (ProcessorResult.CONTINUE.equals(result)
                        || ProcessorResult.STOP.equals(result)),
            // 成功检查
            processor -> processor.supports(request) // 支持性检查
            );

    log.debug("[THIRD_MQTT_UP] MQTT消息处理完成，设备: {}, 成功: {}", request.getDeviceId(), success);

    return success;
  }

  /**
   * 批量处理MQTT消息
   *
   * @param requests MQTT上行请求列表
   * @return 成功处理的数量
   */
  public int processBatch(List<MQTTUPRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.warn("[THIRD_MQTT_UP] 批量请求列表为空，跳过处理");
      return 0;
    }

    log.info("[THIRD_MQTT_UP] 开始批量处理MQTT消息，请求数量: {}", requests.size());

    int successCount = 0;
    for (MQTTUPRequest request : requests) {
      try {
        if (process(request)) {
          successCount++;
        }
      } catch (Exception e) {
        log.error("[THIRD_MQTT_UP] 批量处理异常，消息ID: {}, 异常: ", request.getMessageId(), e);
      }
    }

    log.info("[THIRD_MQTT_UP] 批量处理完成，总数: {}, 成功: {}", requests.size(), successCount);
    return successCount;
  }

  /** 获取处理器数量 */
  public int getProcessorCount() {
    return processors.size();
  }

  /** 获取处理器名称列表（用于调试） */
  public List<String> getProcessorNames() {
    return processorExecutor.getProcessorNames(processors);
  }

  /** 检查是否有指定名称的处理器 */
  public boolean hasProcessor(String name) {
    return processors.stream().anyMatch(p -> p.getName().equals(name));
  }

  /** 获取启用的处理器数量 */
  public long getEnabledProcessorCount() {
    return processorExecutor.getEnabledProcessorCount(processors);
  }

  /** 检查指定处理器是否启用 */
  public boolean isProcessorEnabled(String name) {
    return processorExecutor.isProcessorEnabled(processors, name);
  }

  /** 获取处理器链健康状态 */
  public boolean isHealthy() {
    return processors.stream()
        .filter(MqttMessageProcessor::isEnabled)
        .allMatch(MqttMessageProcessor::isHealthy);
  }

  /** 获取处理器链统计信息 */
  public String getStatistics() {
    StringBuilder stats = new StringBuilder();
    stats.append("MQTT处理器链统计信息:\n");
    stats.append("总处理器数: ").append(processors.size()).append("\n");
    stats.append("启用处理器数: ").append(getEnabledProcessorCount()).append("\n");

    processors.forEach(
        processor -> {
          stats
              .append("- ")
              .append(processor.getName())
              .append(" (order: ")
              .append(processor.getOrder())
              .append(", enabled: ")
              .append(processor.isEnabled())
              .append(", healthy: ")
              .append(processor.isHealthy())
              .append(")\n");
        });

    return stats.toString();
  }
}

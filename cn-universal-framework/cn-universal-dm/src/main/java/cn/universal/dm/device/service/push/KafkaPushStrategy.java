/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.dm.device.service.push;

import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.entity.bo.UPPushBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Kafka推送策略实现
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Slf4j
@Component
public class KafkaPushStrategy implements PushStrategy {

  private UPPushBO.KafkaPushConfig kafkaConfig;

  public void setConfig(UPPushBO.KafkaPushConfig kafkaConfig) {
    this.kafkaConfig = kafkaConfig;
  }

  @Override
  public IoTPushResult execute(BaseUPRequest request, String messageJson) {
    if (kafkaConfig == null) {
      log.warn("[Kafka推送] 配置为空，跳过推送");
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "Kafka",
          messageJson,
          "配置为空",
          "CONFIG_NULL");
    }

    try {
      log.info("[Kafka推送] 推送消息: {}", request.getIotId());

      // TODO: 实现具体的Kafka推送逻辑
      // 这里可以集成Spring Kafka或其他Kafka客户端，比如：
      // - Spring Kafka Template
      // - Apache Kafka Client
      // - Confluent Kafka Client
      // kafkaTemplate.send("platform-topic", request.getIotId(), messageJson);

      // 暂时返回成功（实际实现时需要根据Kafka推送结果返回）
      return IoTPushResult.success(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "Kafka",
          messageJson,
          System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[Kafka推送] 推送失败: {}", request.getIotId(), e);
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "Kafka",
          messageJson,
          "推送异常: " + e.getMessage(),
          "PUSH_EXCEPTION");
    }
  }

  @Override
  public boolean isSupported() {
    return kafkaConfig != null && kafkaConfig.isSupport() && kafkaConfig.isEnable();
  }
}

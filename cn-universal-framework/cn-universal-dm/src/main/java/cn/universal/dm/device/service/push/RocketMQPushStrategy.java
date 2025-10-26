/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

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
 * RocketMQ推送策略实现
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/1/9
 */
@Slf4j
@Component
public class RocketMQPushStrategy implements PushStrategy {

  private UPPushBO.RocketMQPushConfig rocketMQConfig;

  public void setConfig(UPPushBO.RocketMQPushConfig rocketMQConfig) {
    this.rocketMQConfig = rocketMQConfig;
  }

  @Override
  public IoTPushResult execute(BaseUPRequest request, String messageJson) {
    if (rocketMQConfig == null) {
      log.warn("[RocketMQ推送] 配置为空，跳过推送");
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "RocketMQ",
          messageJson,
          "配置为空",
          "CONFIG_NULL");
    }

    try {
      log.info("[RocketMQ推送] 推送消息: {}", request.getIotId());

      // TODO: 实现具体的RocketMQ推送逻辑
      // 这里可以集成RocketMQ客户端，比如：
      // - RocketMQ Spring Boot Starter
      // - Apache RocketMQ Client
      // - Alibaba RocketMQ Client
      // rocketMQTemplate.syncSend("platform-topic", messageJson);

      // 暂时返回成功（实际实现时需要根据RocketMQ推送结果返回）
      return IoTPushResult.success(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "RocketMQ",
          messageJson,
          System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[RocketMQ推送] 推送失败: {}", request.getIotId(), e);
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "RocketMQ",
          messageJson,
          "推送异常: " + e.getMessage(),
          "PUSH_EXCEPTION");
    }
  }

  @Override
  public boolean isSupported() {
    return rocketMQConfig != null && rocketMQConfig.isSupport() && rocketMQConfig.isEnable();
  }
}

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

import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.entity.bo.UPPushBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * MQTT推送策略实现
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/9
 */
@Slf4j
@Component
public class MqttPushStrategy implements PushStrategy {

  private UPPushBO.MqttPushConfig mqttConfig;

  public void setConfig(UPPushBO.MqttPushConfig mqttConfig) {
    this.mqttConfig = mqttConfig;
  }

  @Autowired
  @Qualifier("sysMQTTManager")
  private MQTTPushService mqttPushService;

  @Value("${mqtt.cfg.defined.thing:$thing}")
  private String thingPrefix;

  @Override
  public IoTPushResult execute(BaseUPRequest request, String messageJson) {
    if (mqttConfig == null) {
      log.warn("[MQTT推送] 配置为空，跳过推送");
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "MQTT",
          messageJson,
          "配置为空",
          "CONFIG_NULL");
    }
    try {
      // 构建完整的topic,不允许修改默认为
      // thingPrefix/${appid}/${productKey}/${deviceId}
      String fullTopic =
          thingPrefix
              + "/"
              + request.getIoTDeviceDTO().getAppId()
              + "/"
              + request.getProductKey()
              + "/"
              + request.getDeviceId();

      log.info("[MQTT推送] 推送到Topic: {}, 消息: {}", fullTopic, request.getIotId());

      mqttPushService.publishMessage(fullTopic, messageJson.getBytes(), 1, false);

      // 暂时返回成功（实际实现时需要根据MQTT推送结果返回）
      return IoTPushResult.success(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "MQTT",
          messageJson,
          System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[MQTT推送] 推送失败: {}", request.getIotId(), e);
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "MQTT",
          messageJson,
          "推送异常: " + e.getMessage(),
          "PUSH_EXCEPTION");
    }
  }

  @Override
  public boolean isSupported() {
    return mqttConfig != null && mqttConfig.isSupport() && mqttConfig.isEnable();
  }
}

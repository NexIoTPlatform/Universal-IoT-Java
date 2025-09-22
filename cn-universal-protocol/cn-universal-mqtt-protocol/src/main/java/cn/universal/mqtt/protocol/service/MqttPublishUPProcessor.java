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

package cn.universal.mqtt.protocol.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.dm.device.service.IoTUPPushAdapter;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.entity.ProcessingStage;
import cn.universal.mqtt.protocol.processor.MqttMessageProcessor;
import cn.universal.persistence.base.BaseUPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MQTT发布处理器
 *
 * <p>处理消息发布和回复 支持主动推送、回复消息、通知发送等
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "mqtt")
@Component
public class MqttPublishUPProcessor extends IoTUPPushAdapter<BaseUPRequest>
    implements MqttMessageProcessor {

  @Override
  public String getName() {
    return "MQTT消息推送处理器";
  }

  @Override
  public String getDescription() {
    return "MQTT消息推送处理器";
  }

  @Override
  public int getOrder() {
    return 1999;
  }

  @Override
  public boolean supports(MQTTUPRequest request) {
    // 支持已完成业务处理的消息
    if (request.getIoTDeviceDTO() != null
        && StrUtil.isNotBlank(request.getIoTDeviceDTO().getApplicationId())) {
      return true;
    }
    if (request.getIoTDeviceDTO() != null && request.getDevSubscribe() != null) {
      return true;
    }
    return false;
  }

  @Override
  public ProcessorResult process(MQTTUPRequest request) {
    try {
      log.debug(
          "[{}] 开始消息处理，设备: {}, 主题: {}", getName(), request.getDeviceId(), request.getUpTopic());
      if (CollUtil.isNotEmpty(request.getUpRequestList())) {
        doUp(request.getUpRequestList());
      }
      // 4. 更新处理阶段
      request.setStage(ProcessingStage.PUBLISH_PROCESSED);
      log.debug("[{}] 消息推送处理完成，设备: {}", getName(), request.getDeviceId());
      return ProcessorResult.STOP; // 处理完成，停止后续处理
    } catch (Exception e) {
      log.error("[{}] 消息推送处理异常，设备: {}, 异常: ", getName(), request.getDeviceId(), e);
      return ProcessorResult.ERROR;
    }
  }
}

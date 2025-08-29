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

package cn.universal.mqtt.protocol.processor.up;

import cn.universal.mqtt.protocol.processor.up.common.BaseReplyProcessor;
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
public class MQTTReplyUPProcessor extends BaseReplyProcessor {

  @Override
  protected String getTopicType() {
    return "MQTT消息回复";
  }
}

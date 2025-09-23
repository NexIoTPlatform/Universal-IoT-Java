/*
 *
 * Copyright (c) 2025, IoT-universal. All Rights Reserved.
 *
 * @Description: 本文件由 AleoXin 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: AleoXin
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */
package cn.universal.dm.device.service.push;

/**
 * mqtt消息推送接口
 *
 * @version 1.0 @Author Aleo
 * @since 2025/08/30
 */
public interface MQTTPushService {

  /**
   * 消息推送
   *
   * @param topic 主题
   * @param payload 有效载荷
   * @param qos qos
   * @param retained 是否保存
   * @return
   */
  boolean publishMessage(String topic, byte[] payload, int qos, boolean retained);
}

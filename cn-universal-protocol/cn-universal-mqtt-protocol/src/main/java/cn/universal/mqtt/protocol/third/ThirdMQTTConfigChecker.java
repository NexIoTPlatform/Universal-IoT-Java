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

/**
 * MQTT配置检查器接口
 *
 * <p>用于解决SystemMqttManager和MqttConfigService之间的循环依赖问题 只暴露必要的配置检查方法，避免直接依赖具体实现
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public interface ThirdMQTTConfigChecker {

  /**
   * @param productKey
   * @return
   */
  boolean supportMQTTNetwork(String productKey);

  /**
   * @param productKey
   * @return
   */
  boolean supportMQTTNetwork(String productKey, String networkUnionId);
}

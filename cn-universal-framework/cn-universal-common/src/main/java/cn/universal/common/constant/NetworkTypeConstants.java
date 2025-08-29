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
package cn.universal.common.constant;

/**
 * 网络类型常量类 用于替换硬编码的网络类型字符串
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public final class NetworkTypeConstants {

  private NetworkTypeConstants() {
    // 私有构造函数，防止实例化
  }

  // TCP相关
  public static final String TCP_CLIENT = "TCP_CLIENT";
  public static final String TCP_SERVER = "TCP_SERVER";

  // MQTT相关
  public static final String MQTT_CLIENT = "MQTT_CLIENT";
  public static final String MQTT_SERVER = "MQTT_SERVER";

  // HTTP相关
  public static final String HTTP_CLIENT = "HTTP_CLIENT";
  public static final String HTTP_SERVER = "HTTP_SERVER";

  // WebSocket相关
  public static final String WEB_SOCKET_CLIENT = "WEB_SOCKET_CLIENT";
  public static final String WEB_SOCKET_SERVER = "WEB_SOCKET_SERVER";

  // UDP相关
  public static final String UDP = "UDP";

  // CoAP相关
  public static final String COAP_CLIENT = "COAP_CLIENT";
  public static final String COAP_SERVER = "COAP_SERVER";
}

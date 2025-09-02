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

package cn.universal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceLog implements Serializable {

  @Id
  private Long id;

  /**
   * 唯一编码
   */
  @Column(name = "iot_id")
  private String iotId;

  /**
   * 设备自身序号
   */
  @Column(name = "device_id")
  private String deviceId;

  /**
   * 第三方设备ID唯一标识符
   */
  // @Transient
  @Column(name = "ext_device_id")
  private String extDeviceId;

  /**
   * 产品ID
   */
  @Column(name = "product_key")
  private String productKey;

  /**
   * 设备名称
   */
  @Column(name = "device_name")
  private String deviceName;

  /**
   * 消息类型
   */
  @Column(name = "message_type")
  private String messageType;

  /**
   * 指令ID
   */
  @Column(name = "command_id")
  private String commandId;

  /**
   * 指令ID
   */
  @Column(name = "command_status")
  private Integer commandStatus;

  /**
   * 创建人
   */
  @Column(name = "create_id")
  private String createId;

  /**
   * 事件名称
   */
  private String event;

  /**
   * 实例名称
   */
  private String instance;

  /**
   * 创建时间
   */
  @Column(name = "create_time")
  private Long createTime;

  /**
   * 内容
   */
  private String content;

  private String point;
  private static final long serialVersionUID = 1L;
}

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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device_log_metadata")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceLogMetadata implements Serializable {

  @Id private Long id;

  @Column(name = "iot_id")
  private String iotId;

  /** 产品唯一标识 */
  @Column(name = "product_key")
  private String productKey;

  /** 设备名称 */
  @Column(name = "device_name", length = 64)
  private String deviceName;

  @Column(name = "device_id", length = 64)
  private String deviceId;

  /** 消息类型 */
  @Column(name = "message_type")
  private String messageType;

  @Column(length = 32)
  private String event;

  /** 属性 */
  private String property;

  private String ext1;

  private String ext2;

  private String ext3;

  /** 发生时间 */
  @Column(name = "create_time")
  private LocalDateTime createTime;

  /** 其他 */
  private String content;

  private static final long serialVersionUID = 1L;
}

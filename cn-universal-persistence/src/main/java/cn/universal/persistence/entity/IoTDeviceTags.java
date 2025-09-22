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

@Table(name = "iot_device_tags")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceTags implements Serializable {

  @Id
  private Long id;

  /**
   * 设备deviceId
   */
  @Column(name = "device_id")
  private String deviceId;

  /**
   * 产品ID或者设备唯一标识
   */
  @Column(name = "product_key")
  private String productKey;

  @Column(name = "iot_id")
  private String iotId;

  @Column(name = "create_time")
  private Long createTime;

  @Column(name = "`name`")
  private String name;

  private String description;

  @Column(name = "`type`")
  private String type;

  @Column(name = "`value`")
  private String value;

  @Column(name = "`key`")
  private String key;

  @Column(name = "`instance`")
  private String instance;

  private static final long serialVersionUID = 1L;
}

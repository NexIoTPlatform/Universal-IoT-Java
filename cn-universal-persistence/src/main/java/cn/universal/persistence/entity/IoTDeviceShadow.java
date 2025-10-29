/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
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
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device_shadow")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceShadow implements Serializable {

  @Id private Long id;

  /** 本平台设备唯一标识符 */
  @Column(name = "iot_id")
  private String iotId;

  /** 产品KEY */
  @Column(name = "product_key")
  private String productKey;

  /** 设备自身序号 */
  @Column(name = "device_id")
  private String deviceId;

  /** 第三方平台设备ID唯一标识符 */
  @Column(name = "ext_device_id")
  private String extDeviceId;

  /** 注册时间 */
  @Column(name = "active_time")
  private Date activeTime;

  /** 激活时间 */
  @Column(name = "online_time")
  private Date onlineTime;

  /** 最后通信时间 */
  @Column(name = "last_time")
  private Date lastTime;

  /** 更新时间 */
  @Column(name = "update_date")
  private Date updateDate;

  /** 影子数据 */
  private String metadata;

  @Column(name = "`instance`")
  private String instance;

  /** 版本号 */
  @Column(name = "version")
  private Long version;

  private static final long serialVersionUID = 1L;
}

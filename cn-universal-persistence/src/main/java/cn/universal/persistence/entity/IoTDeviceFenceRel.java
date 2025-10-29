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

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备和围栏中间表 @Author gitee.com/NexIoT
 *
 * @since 2023/8/5 8:51
 */
@Table(name = "iot_device_fence_rel")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceFenceRel implements Serializable {

  @Id private Long id;

  /** 围栏id */
  private Long fenceId;

  /** 设备唯一标识符 */
  private String iotId;

  /** 设备序列号 */
  private String deviceId;

  /** 创建人 */
  private String creatorId;

  /** 创建时间 */
  private Date createDate;
}

/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

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

@Table(name = "iot_device_rule_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceRuleLog implements Serializable {

  @Id private Long id;

  /** 业务ID */
  @Column(name = "c_id")
  private String cId;

  /** 业务名称 */
  @Column(name = "c_name")
  private String cName;

  /** 执行状态 */
  @Column(name = "c_status")
  private Byte cStatus;

  /** 1-场景联动，2-数据流转 */
  @Column(name = "c_type")
  private Byte cType;

  /** 条件 */
  private String conditions;

  @Column(name = "create_time")
  private Date createTime;

  @Column(name = "update_time")
  private Date updateTime;

  private String content;

  private static final long serialVersionUID = 1L;
}

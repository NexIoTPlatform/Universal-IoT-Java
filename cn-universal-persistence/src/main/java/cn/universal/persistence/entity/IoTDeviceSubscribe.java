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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device_subscribe")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceSubscribe implements Serializable {

  @Id private Long id;

  /** 消息类别：属性（PROPERTIES），指令（REPLY），事件（EVENT），上下线（EVENT：online,offline），所有 */
  @Column(name = "msg_type")
  private String msgType;

  /** 订阅级别：设备级，产品级 */
  @Column(name = "sub_type")
  private String subType;

  /** 设备deviceId */
  @Column(name = "device_id")
  private String deviceId;

  /** 产品ID或者设备唯一标识 */
  @Column(name = "product_key")
  private String productKey;

  /** 产品ID或者设备唯一标识 */
  @Column(name = "iot_id")
  private String iotId;

  /** 订阅地址 */
  private String url;

  /** 主题 */
  private String topic;

  /** 创建时间 */
  @Column(name = "create_date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createDate;

  /** 创建人 */
  private String creater;

  /** 实例编号(应用标识) */
  @Column(name = "`instance`")
  private String instance;

  /** 是否启用 */
  private Boolean enabled;

  private static final long serialVersionUID = 1L;
}

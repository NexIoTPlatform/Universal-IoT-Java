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

package cn.universal.persistence.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "设备日志视图对象")
public class IoTDeviceLogVO {

  private static final long serialVersionUID = 1L;

  /*-----------------日志信息---------------------*/

  /** 日志ID，非自增 */
  private Long id;

  /** 设备编码 */
  private String iotId;

  /** 设备序列号 */
  private String deviceId;

  /** 产品ID */
  private String productKey;

  /** 设备名称 */
  private String deviceName;

  /** 消息类型 */
  private String messageType;

  /** 指令ID */
  private String commandId;

  /** 指令状态 */
  private Integer commandStatus;

  /** 事件名称 */
  private String event;

  /** 经纬度 */
  private String point;

  /** 内容 */
  private String content;

  /** 创建时间 */
  private LocalDateTime createTime;
}

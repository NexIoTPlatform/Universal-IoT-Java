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

package cn.universal.persistence.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设备属性视图对象")
public class IoTDeviceLogMetadataVO {

  private static final long serialVersionUID = 1L;

  /*-----------------日志信息---------------------*/

  /** 日志ID，非自增 */
  //  private Long id;

  /**
   * 设备编码
   */
  private String iotId;

  /**
   * 设备序列号
   */
  private String deviceId;

  /**
   * 产品ID
   */
  private String productKey;

  /**
   * 设备名称
   */
  private String deviceName;

  /**
   * 消息类型
   */
  private String messageType;

  private String event;
  private String property;
  private String content;
  private String ext1;
  private String ext2;
  private String ext3;
  private Integer createTime;
}

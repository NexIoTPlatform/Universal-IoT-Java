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

import lombok.Data;

@Data
public class GatewayDeviceVo {

  private String iotId;
  private String productKey;
  private String deviceId;
  private String deviceName;
  private String concatName;

  public String getConcatName() {
    return deviceId + "(" + deviceName + ")";
  }
}

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

package cn.universal.core.message;

import cn.hutool.json.JSONObject;
import lombok.Data;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2023/10/11
 */
@Data
public class DownCommonData {

  /** imei运营商NB号码，长度15 */
  private String imei;

  /** 长度不超过15 */
  private String imsi;

  /** 水电表 表号 */
  private String meterNo;

  /** 设备型号 */
  private String deviceModel;

  /** 设备名称 */
  private String deviceName;

  /** 公司名称 */
  private String companyNo;

  private JSONObject configuration;

  /** 经度 */
  private String longitude;

  /** 维度 */
  private String latitude;

  /** 安装位置 */
  private String location;

  /** extDeviceId */
  private String extDeviceId;

  /** */
  private String appKey;
}

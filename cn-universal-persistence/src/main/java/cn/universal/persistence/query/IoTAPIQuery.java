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

package cn.universal.persistence.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/11/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class IoTAPIQuery extends BasePage {

  private String applicationId;

  private String application;

  private String iotUnionId;

  private String iotId;

  private String productName;

  private String productKey;

  private String deviceName;

  private String deviceId;

  /** 传输协议 */
  private String transportProtocol;

  private String deviceNode;

  private String companyNo;

  /** 产品型号；例如 WS101-470M */
  private String productId;

  /** 经度 */
  private String longitude;

  /** 维度 */
  private String latitude;

  private String gwProductKey;

  /** 备注说明 */
  private String detail;
}

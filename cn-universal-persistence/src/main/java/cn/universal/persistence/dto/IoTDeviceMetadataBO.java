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

package cn.universal.persistence.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/9/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IoTDeviceMetadataBO implements Serializable {

  private String companyNo;
  private String creatorId;
  private String orgId;
  private String classifiedId;
  private String transportProtocol;
  private String point;
}

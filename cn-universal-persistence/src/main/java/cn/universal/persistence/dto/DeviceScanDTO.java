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

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/7/14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceScanDTO {

  private String productKey;
  private String qrcode;

  public boolean isEmpty() {
    return StrUtil.isEmpty(getProductKey()) || StrUtil.isEmpty(getQrcode());
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ResultScanDTO {

    private String imei;
    private String deviceId;
  }
}

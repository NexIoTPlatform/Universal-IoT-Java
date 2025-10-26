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

package cn.universal.persistence.query;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2023/1/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IoTDeviceQuery {

  private String iotId;
  private String extDeviceId;
  private String deviceId;
  private String gwProductKey;
  private String productKey;
  private String thirdPlatform;

  public boolean emptyParams() {
    return StrUtil.isBlank(getIotId())
        && StrUtil.isBlank(getExtDeviceId())
        && StrUtil.isBlank(getDeviceId())
        && StrUtil.isBlank(getGwProductKey())
        && StrUtil.isBlank(productKey)
        && StrUtil.isBlank(thirdPlatform);
  }
}

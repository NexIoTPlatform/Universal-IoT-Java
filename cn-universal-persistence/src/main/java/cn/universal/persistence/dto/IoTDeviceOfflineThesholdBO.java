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

package cn.universal.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2023/2/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IoTDeviceOfflineThesholdBO {

  private String productKey;
  private String platform;
  private int offlineThreshold;
}

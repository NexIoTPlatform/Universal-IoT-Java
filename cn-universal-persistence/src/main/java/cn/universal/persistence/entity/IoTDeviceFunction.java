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

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IoTDeviceFunction implements Serializable {

  /** 功能标识 */
  private String id;

  /** 功能名称 */
  private String name;

  /** 是否是配置 */
  private boolean config;

  /** 描述 */
  private String description;

  /** 功能来源 */
  private String source;

  /** 输入 */
  private String inputs;
}

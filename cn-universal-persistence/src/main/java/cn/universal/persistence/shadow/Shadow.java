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

package cn.universal.persistence.shadow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备影子
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/9/17
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Shadow {

  /** 状态 */
  private State state;

  private State metadata;

  private Long timestamp;

  private Long version;
}

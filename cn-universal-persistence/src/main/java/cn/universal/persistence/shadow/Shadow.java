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

package cn.universal.persistence.shadow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备影子
 *
 * @version 1.0 @Author Aleo
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

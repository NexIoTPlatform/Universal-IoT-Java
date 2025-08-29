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

package cn.universal.core.engine.convert;

import cn.universal.core.engine.runtime.Variables;

public interface ClassImplicitConvert {

  /** 转换顺序 */
  default int sort() {
    return Integer.MAX_VALUE;
  }

  /** 是否支持隐式自动转换 */
  boolean support(Class<?> from, Class<?> to);

  /** 转换 */
  Object convert(Variables variables, Object source, Class<?> target);
}

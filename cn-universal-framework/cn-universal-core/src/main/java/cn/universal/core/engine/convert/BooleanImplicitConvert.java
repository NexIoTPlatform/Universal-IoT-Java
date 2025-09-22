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

import cn.universal.core.engine.parsing.ast.literal.BooleanLiteral;
import cn.universal.core.engine.runtime.Variables;

/** 任意值到boolean类型的隐式转换 */
public class BooleanImplicitConvert implements ClassImplicitConvert {

  @Override
  public boolean support(Class<?> from, Class<?> to) {
    return to == Boolean.class || to == boolean.class;
  }

  @Override
  public Object convert(Variables variables, Object source, Class<?> target) {
    return BooleanLiteral.isTrue(source);
  }
}

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

package cn.universal.core.metadata.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntType extends NumberType<Integer> {

  public static final String ID = "int";

  public static final IntType GLOBAL = new IntType();

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "整型";
  }

  @Override
  public Integer convert(Object value) {
    return super.convertNumber(value, Number::intValue);
  }
}

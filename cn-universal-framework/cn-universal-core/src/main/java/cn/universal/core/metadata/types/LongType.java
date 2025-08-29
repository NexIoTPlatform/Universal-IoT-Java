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
public class LongType extends NumberType<Long> {

  public static final String ID = "long";
  public static final LongType GLOBAL = new LongType();

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "长整型";
  }

  @Override
  public Long convert(Object value) {
    return super.convertNumber(value, Number::longValue);
  }
}

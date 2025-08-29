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

package cn.universal.core.engine.runtime;

public class ExitValue {

  private final Object[] values;

  public ExitValue() {
    this(new Object[0]);
  }

  public ExitValue(Object[] values) {
    this.values = values;
  }

  public Object[] getValues() {
    return values;
  }

  public int getLength() {
    return values.length;
  }
}

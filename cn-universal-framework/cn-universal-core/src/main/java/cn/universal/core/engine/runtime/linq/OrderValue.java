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

package cn.universal.core.engine.runtime.linq;

public class OrderValue {

  private final Object value;

  private final int order;

  public OrderValue(Object value, int order) {
    this.value = value;
    this.order = order;
  }

  public Object getValue() {
    return value;
  }

  public int getOrder() {
    return order;
  }
}

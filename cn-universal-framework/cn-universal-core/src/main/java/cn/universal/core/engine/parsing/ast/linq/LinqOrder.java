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

package cn.universal.core.engine.parsing.ast.linq;

import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.VarIndex;
import cn.universal.core.engine.parsing.ast.Expression;

public class LinqOrder extends LinqField {

  /** 1 正序 -1 倒序 */
  private final int order;

  public LinqOrder(Span span, Expression expression, VarIndex alias, int order) {
    super(span, expression, alias);
    this.order = order;
  }

  public int getOrder() {
    return order;
  }
}

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
import cn.universal.core.engine.parsing.ast.VariableSetter;

public class LinqField extends LinqExpression implements VariableSetter {

  private final String aliasName;

  private final VarIndex varIndex;

  public LinqField(Span span, Expression expression, VarIndex alias) {
    super(span, expression);
    this.aliasName = alias != null ? alias.getName() : expression.getSpan().getText();
    this.varIndex = alias;
  }

  public VarIndex getVarIndex() {
    return varIndex;
  }

  public String getAlias() {
    return aliasName;
  }
}

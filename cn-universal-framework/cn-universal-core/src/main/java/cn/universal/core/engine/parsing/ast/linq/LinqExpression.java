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

import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.Expression;
import cn.universal.core.engine.parsing.ast.statement.MemberAccess;

public class LinqExpression extends Expression {

  private final Expression expression;

  private String methodName;

  public LinqExpression(Expression expression) {
    this(expression.getSpan(), expression);
  }

  public LinqExpression(Span span, Expression expression) {
    super(span);
    this.expression = expression;
  }

  @Override
  public void visitMethod(MagicScriptCompiler compiler) {
    expression.visitMethod(compiler);
    if (!(expression instanceof WholeLiteral)) {
      this.methodName =
          compiler.visitMethod(
              "linq_expression",
              () ->
                  compiler
                      .compile(
                          expression instanceof MemberAccess
                                  && ((MemberAccess) expression).isWhole()
                              ? ((MemberAccess) expression).getObject()
                              : expression)
                      .insn(ARETURN));
    }
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    if (methodName != null) {
      compiler.load0().lambda(methodName);
    } else {
      compiler.insn(ACONST_NULL);
    }
  }

  public Expression getExpression() {
    return expression;
  }
}

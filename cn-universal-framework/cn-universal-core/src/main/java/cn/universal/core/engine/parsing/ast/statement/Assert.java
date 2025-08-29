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

package cn.universal.core.engine.parsing.ast.statement;

import cn.universal.core.engine.asm.Label;
import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.Expression;
import cn.universal.core.engine.parsing.ast.Node;
import cn.universal.core.engine.runtime.handle.OperatorHandle;
import java.util.List;

/** assert expr : expr[,expr][,expr][,expr] */
public class Assert extends Node {

  private final Expression condition;

  private final List<Expression> expressions;

  public Assert(Span span, Expression condition, List<Expression> expressions) {
    super(span);
    this.condition = condition;
    this.expressions = expressions;
  }

  @Override
  public void visitMethod(MagicScriptCompiler compiler) {
    condition.visitMethod(compiler);
    expressions.forEach(it -> it.visitMethod(compiler));
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    Label end = new Label();
    compiler
        .visit(condition)
        .invoke(INVOKESTATIC, OperatorHandle.class, "isFalse", boolean.class, Object.class)
        .jump(IFEQ, end)
        .compile(new Exit(getSpan(), expressions))
        .label(end);
  }
}

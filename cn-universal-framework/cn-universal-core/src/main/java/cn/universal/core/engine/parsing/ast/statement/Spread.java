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

import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.Expression;
import cn.universal.core.engine.runtime.SpreadValue;

/** 展开语法 Spread syntax (...) */
public class Spread extends Expression {

  private final Expression target;

  public Spread(Span span, Expression target) {
    super(span);
    this.target = target;
  }

  @Override
  public void visitMethod(MagicScriptCompiler compiler) {
    target.visitMethod(compiler);
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    // 对于...xxx 的参数 统一转换为 new SpreadValue(object)
    compiler
        .typeInsn(NEW, SpreadValue.class)
        .insn(DUP)
        .visit(target)
        .invoke(INVOKESPECIAL, SpreadValue.class, "<init>", void.class, Object.class);
  }
}

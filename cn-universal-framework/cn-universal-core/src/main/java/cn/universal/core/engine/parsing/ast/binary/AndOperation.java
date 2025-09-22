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

package cn.universal.core.engine.parsing.ast.binary;

import cn.universal.core.engine.asm.Label;
import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.BinaryOperation;
import cn.universal.core.engine.parsing.ast.Expression;
import cn.universal.core.engine.runtime.handle.OperatorHandle;

/** && 操作 */
public class AndOperation extends BinaryOperation {

  public AndOperation(Expression leftOperand, Span span, Expression rightOperand) {
    super(leftOperand, span, rightOperand);
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    Label end = new Label();
    compiler
        .visit(getLeftOperand())
        .insn(DUP)
        .invoke(INVOKESTATIC, OperatorHandle.class, "isTrue", boolean.class, Object.class)
        .jump(IFEQ, end)
        .insn(POP)
        .visit(getRightOperand())
        .label(end);
  }
}

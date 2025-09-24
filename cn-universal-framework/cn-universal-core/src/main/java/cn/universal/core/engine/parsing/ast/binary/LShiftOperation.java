

package cn.universal.core.engine.parsing.ast.binary;

import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.BinaryOperation;
import cn.universal.core.engine.parsing.ast.Expression;

/** << */
public class LShiftOperation extends BinaryOperation {

  public LShiftOperation(Expression leftOperand, Span span, Expression rightOperand) {
    super(leftOperand, span, rightOperand);
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    compiler
        .visit(getLeftOperand())
        .visit(getRightOperand())
        .lineNumber(getSpan())
        .bit("left_shift");
  }
}

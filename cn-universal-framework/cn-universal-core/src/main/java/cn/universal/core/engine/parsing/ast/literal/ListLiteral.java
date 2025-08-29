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

package cn.universal.core.engine.parsing.ast.literal;

import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.Expression;
import cn.universal.core.engine.parsing.ast.Literal;
import cn.universal.core.engine.parsing.ast.statement.Spread;
import java.util.List;

/** List常量 */
public class ListLiteral extends Literal {

  public final List<Expression> values;

  public ListLiteral(Span span, List<Expression> values) {
    super(span);
    this.values = values;
  }

  @Override
  public void visitMethod(MagicScriptCompiler compiler) {
    values.forEach(expr -> expr.visitMethod(compiler));
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    int size = values.size();
    if (size == 0) {
      compiler.newArrayList();
    } else {
      compiler
          .insn(values.stream().anyMatch(it -> it instanceof Spread) ? ICONST_1 : ICONST_0)
          .asBoolean()
          .newArray(values)
          .call("newArrayList", 2);
    }
  }
}

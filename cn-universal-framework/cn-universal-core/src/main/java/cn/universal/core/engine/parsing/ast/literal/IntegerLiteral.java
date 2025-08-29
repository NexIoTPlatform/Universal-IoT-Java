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

import cn.universal.core.engine.MagicScriptError;
import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.parsing.ast.Literal;

/** int常量 */
public class IntegerLiteral extends Literal {

  public IntegerLiteral(Span literal) {
    super(literal);
    try {
      setValue(Integer.parseInt(literal.getText().replace("_", "")));
    } catch (NumberFormatException e) {
      MagicScriptError.error("定义int变量值不合法", literal, e);
    }
  }

  public IntegerLiteral(Span span, Object value) {
    super(span, value);
  }

  @Override
  public void compile(MagicScriptCompiler context) {
    context
        .visitInt((Integer) value)
        .invoke(INVOKESTATIC, Integer.class, "valueOf", Integer.class, int.class);
  }
}

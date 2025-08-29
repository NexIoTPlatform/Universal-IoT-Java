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

package cn.universal.core.engine.parsing.ast;

import cn.universal.core.engine.MagicScriptContext;
import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.runtime.function.MagicScriptLanguageFunction;

public class LanguageExpression extends Expression {

  private final String language;

  private final String content;

  public LanguageExpression(Span language, Span content) {
    super(new Span(language, content));
    this.language = language.getText();
    this.content = content.getText();
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    // new MagicScriptLanguageFunction(language, content)
    compiler
        .typeInsn(NEW, MagicScriptLanguageFunction.class)
        .insn(DUP)
        .loadContext()
        .ldc(this.language)
        .ldc(this.content)
        .invoke(
            INVOKESPECIAL,
            MagicScriptLanguageFunction.class,
            "<init>",
            void.class,
            MagicScriptContext.class,
            String.class,
            String.class);
  }
}

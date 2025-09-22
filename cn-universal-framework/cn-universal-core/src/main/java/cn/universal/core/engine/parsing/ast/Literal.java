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

import cn.universal.core.engine.compile.MagicScriptCompiler;
import cn.universal.core.engine.parsing.Span;

/** 常量 */
public abstract class Literal extends Expression {

  protected Object value = null;

  public Literal(Span span) {
    super(span);
  }

  public Literal(Span span, Object value) {
    super(span);
    this.value = value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    compiler.ldc(value);
  }
}

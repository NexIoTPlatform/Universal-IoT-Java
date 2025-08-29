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
import cn.universal.core.engine.parsing.VarIndex;
import cn.universal.core.engine.parsing.ast.Expression;
import cn.universal.core.engine.parsing.ast.VariableSetter;

public class VariableAccess extends Expression implements VariableSetter {

  private final VarIndex varIndex;

  public VariableAccess(Span name, VarIndex varIndex) {
    super(name);
    this.varIndex = varIndex;
  }

  public VarIndex getVarIndex() {
    return varIndex;
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    compiler.load(varIndex);
  }
}

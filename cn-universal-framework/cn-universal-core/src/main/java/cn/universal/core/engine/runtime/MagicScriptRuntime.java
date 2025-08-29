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

package cn.universal.core.engine.runtime;

import cn.universal.core.engine.MagicScriptContext;
import cn.universal.core.engine.parsing.Span;
import cn.universal.core.engine.runtime.function.MagicScriptLambdaFunction;
import java.util.List;

public abstract class MagicScriptRuntime {

  protected MagicScriptContext context;

  private String[] varNames;

  private List<Span> spans;

  public abstract Object execute(MagicScriptContext context);

  public String[] getVarNames() {
    return varNames;
  }

  public void setVarNames(String[] varNames) {
    this.varNames = varNames;
  }

  public void setSpans(List<Span> spans) {
    this.spans = spans;
  }

  public Span getSpan(int index) {
    return spans.get(index);
  }

  public List<Span> getSpans() {
    return spans;
  }

  protected MagicScriptLambdaFunction createLambda(
      MagicScriptLambdaFunction function, Variables variables) {
    return (var, arguments) -> function.apply(variables, arguments);
  }
}

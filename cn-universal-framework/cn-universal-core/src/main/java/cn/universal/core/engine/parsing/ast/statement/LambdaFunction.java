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
import cn.universal.core.engine.parsing.ast.Node;
import java.util.List;

public class LambdaFunction extends Expression {

  private final List<VarIndex> parameters;
  private final List<Node> childNodes;
  private String methodName;

  private boolean async;

  public LambdaFunction(Span span, List<VarIndex> parameters, List<Node> childNodes) {
    super(span);
    this.parameters = parameters;
    this.childNodes = childNodes;
  }

  @Override
  public void visitMethod(MagicScriptCompiler compiler) {
    this.methodName =
        compiler.visitMethod((async ? "async_" : "") + "lambda", childNodes, parameters);
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public List<VarIndex> getParameters() {
    return parameters;
  }

  /** 访问lambda方法 */
  private void compileMethod(MagicScriptCompiler compiler) {
    compiler.load0().lambda(methodName);
  }

  @Override
  public void compile(MagicScriptCompiler compiler) {
    compileMethod(compiler);
  }
}

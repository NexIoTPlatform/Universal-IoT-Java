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
import java.util.Map;

public class RuntimeContext {

  private final MagicScriptContext context;

  private final Variables variables;

  public RuntimeContext(MagicScriptContext context, Variables variables) {
    this.context = context;
    this.variables = variables;
  }

  public Variables getVariables() {
    return variables;
  }

  public Map<String, Object> getVarMap() {
    return variables.getVariables(context);
  }

  public MagicScriptContext getScriptContext() {
    return context;
  }

  public Object eval(String script) {
    return this.context.eval(this, script);
  }
}

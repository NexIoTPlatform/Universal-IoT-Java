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

package cn.universal.core.engine.runtime.linq;

import cn.universal.core.engine.runtime.function.MagicScriptLambdaFunction;

public class SelectField {

  private final MagicScriptLambdaFunction function;

  private final String aliasName;

  private final int aliasIndex;

  public SelectField(MagicScriptLambdaFunction function, String aliasName, int aliasIndex) {
    this.function = function;
    this.aliasName = aliasName;
    this.aliasIndex = aliasIndex;
  }

  public MagicScriptLambdaFunction getFunction() {
    return function;
  }

  public String getAliasName() {
    return aliasName;
  }

  public boolean isWhole() {
    return aliasIndex == -1 && "*".equals(aliasName);
  }
}

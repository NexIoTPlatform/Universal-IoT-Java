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

public class LinQOrder {

  private final MagicScriptLambdaFunction function;

  private final int order;

  public LinQOrder(MagicScriptLambdaFunction function, int order) {
    this.function = function;
    this.order = order;
  }

  public MagicScriptLambdaFunction getFunction() {
    return function;
  }

  public int getOrder() {
    return order;
  }
}

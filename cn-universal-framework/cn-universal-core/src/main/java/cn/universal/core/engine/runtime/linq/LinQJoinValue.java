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
import java.util.List;

public class LinQJoinValue {

  private final MagicScriptLambdaFunction condition;

  private final List<Object> target;

  private final boolean isLeftJoin;

  private int aliasIndex = -1;

  public LinQJoinValue(
      MagicScriptLambdaFunction condition,
      List<Object> objects,
      boolean isLeftJoin,
      int aliasIndex) {
    this.condition = condition;
    this.target = objects;
    this.isLeftJoin = isLeftJoin;
    this.aliasIndex = aliasIndex;
  }

  public MagicScriptLambdaFunction getCondition() {
    return condition;
  }

  public int size() {
    return target.size();
  }

  public List<Object> getTarget() {
    return target;
  }

  public boolean isLeftJoin() {
    return isLeftJoin;
  }

  public int getAliasIndex() {
    return aliasIndex;
  }
}

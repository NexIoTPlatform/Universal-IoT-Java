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

import cn.universal.core.engine.runtime.Variables;
import java.util.Collections;
import java.util.List;

public class Record {

  private final Object value;

  private final List<JoinValue> joinValues;

  public Record(Object value) {
    this(value, Collections.emptyList());
  }

  public Record(Object value, List<JoinValue> joinValues) {
    this.value = value;
    this.joinValues = joinValues;
  }

  public Object getValue() {
    return value;
  }

  public List<JoinValue> getJoinValues() {
    return joinValues;
  }

  public void setVariableValue(Variables variables) {
    this.joinValues.forEach(
        joinValue -> variables.setValue(joinValue.getIndex(), joinValue.getValue()));
  }

  public void removeVariableValue(Variables variables) {
    this.joinValues.forEach(
        joinValue -> variables.setValue(joinValue.getIndex(), Collections.emptyMap()));
  }
}

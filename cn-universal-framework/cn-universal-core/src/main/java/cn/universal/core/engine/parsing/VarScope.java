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

package cn.universal.core.engine.parsing;

import java.util.ArrayList;

public class VarScope extends ArrayList<VarIndex> {

  private VarScope parent;

  public VarScope(VarScope parent) {
    this.parent = parent;
  }

  public VarScope() {}

  public VarScope push() {
    return new VarScope(this);
  }

  public VarScope getParent() {
    return parent;
  }

  public VarScope pop() {
    return parent;
  }
}

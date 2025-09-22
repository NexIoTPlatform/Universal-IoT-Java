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

public interface VariableSetter {

  default void compile_visit_variable(MagicScriptCompiler compiler) {
    throw new UnsupportedOperationException("暂不支持编译" + this.getClass().getSimpleName());
  }
}

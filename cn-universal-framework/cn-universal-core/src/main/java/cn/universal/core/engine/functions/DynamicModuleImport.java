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

package cn.universal.core.engine.functions;

import cn.universal.core.engine.MagicScriptContext;
import java.util.function.Function;

public class DynamicModuleImport {

  private final Class<?> targetClass;

  private final Function<MagicScriptContext, Object> finder;

  public DynamicModuleImport(Class<?> targetClass, Function<MagicScriptContext, Object> finder) {
    this.targetClass = targetClass;
    this.finder = finder;
  }

  public Object getDynamicModule(MagicScriptContext context) {
    return finder.apply(context);
  }

  public Class<?> getTargetClass() {
    return targetClass;
  }
}

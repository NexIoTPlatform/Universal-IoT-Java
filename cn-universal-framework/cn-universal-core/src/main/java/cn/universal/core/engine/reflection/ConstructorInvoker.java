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

package cn.universal.core.engine.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

class ConstructorInvoker extends JavaInvoker<Constructor> {

  ConstructorInvoker(Constructor constructor) {
    super(constructor);
  }

  ConstructorInvoker(JavaInvoker<Constructor> invoker) {
    super(invoker);
  }

  @Override
  public ConstructorInvoker copy() {
    return new ConstructorInvoker(this);
  }

  @Override
  Object invoke(Object target, Object... args)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {
    return getExecutable().newInstance(args);
  }
}

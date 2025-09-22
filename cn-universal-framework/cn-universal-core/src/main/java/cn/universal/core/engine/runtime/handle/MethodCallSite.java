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

package cn.universal.core.engine.runtime.handle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

public class MethodCallSite extends MutableCallSite {

  MethodHandles.Lookup caller;

  String methodName;

  Class<?> targetClass;

  MethodHandle fallback;

  public MethodCallSite(
      MethodHandles.Lookup caller, String methodName, MethodType type, Class<?> targetClass) {
    super(type);
    this.caller = caller;
    this.methodName = methodName;
    this.targetClass = targetClass;
  }

  public MethodHandle findStatic(MethodType methodType)
      throws NoSuchMethodException, IllegalAccessException {
    return findStatic(this.methodName, methodType);
  }

  public MethodHandle findStatic(String name, MethodType methodType)
      throws NoSuchMethodException, IllegalAccessException {
    return this.caller.findStatic(this.targetClass, name, methodType);
  }
}

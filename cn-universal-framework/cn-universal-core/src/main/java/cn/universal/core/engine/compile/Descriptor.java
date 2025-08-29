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

package cn.universal.core.engine.compile;

import cn.universal.core.engine.asm.Type;

public class Descriptor {

  public static String make_descriptor(Class<?> target, String methodName, Class<?>... args) {
    try {
      return Type.getMethodDescriptor(target.getMethod(methodName, args));
    } catch (NoSuchMethodException e) {
      throw new MagicScriptCompileException(e);
    }
  }

  public static String make_descriptor(Class<?> type, Class<?>... args) {
    int len = args.length;
    Type[] types = new Type[len];
    for (int i = 0; i < len; i++) {
      types[i] = Type.getType(args[i]);
    }
    return Type.getMethodDescriptor(Type.getType(type), types);
  }
}

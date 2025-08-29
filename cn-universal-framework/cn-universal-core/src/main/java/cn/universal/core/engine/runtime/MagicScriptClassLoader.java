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

package cn.universal.core.engine.runtime;

public class MagicScriptClassLoader extends ClassLoader {

  public MagicScriptClassLoader(ClassLoader parent) {
    super(parent);
  }

  public synchronized Class<MagicScriptRuntime> load(String className, byte[] bytecode)
      throws ClassNotFoundException {
    defineClass(className, bytecode, 0, bytecode.length);
    return (Class<MagicScriptRuntime>) loadClass(className);
  }
}

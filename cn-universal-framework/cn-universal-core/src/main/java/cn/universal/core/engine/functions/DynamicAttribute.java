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

import cn.universal.core.engine.exception.MagicScriptRuntimeException;
import java.beans.Transient;

public interface DynamicAttribute<T, R> {

  @Transient
  T getDynamicAttribute(String key);

  @Transient
  default R setDynamicAttribute(String key, T value) {
    throw new MagicScriptRuntimeException("不支持此赋值操作");
  }
}

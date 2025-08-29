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

package cn.universal.core.engine.runtime.lang;

import java.lang.reflect.Array;
import java.util.Iterator;

public class ArrayValueIterator implements Iterator<Object> {

  private final Object target;

  private final int len;

  protected int index = 0;

  public ArrayValueIterator(Object target) {
    this.target = target;
    this.len = Array.getLength(target);
  }

  @Override
  public boolean hasNext() {
    return index < len;
  }

  @Override
  public Object next() {
    return Array.get(target, index++);
  }
}

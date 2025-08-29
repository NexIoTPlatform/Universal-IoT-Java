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

import java.util.Iterator;

public class KeyValueIterator implements Iterator<Object>, KeyIterator {

  private final Iterator<Object> iterator;

  private int index = 0;

  public KeyValueIterator(Iterator<Object> iterator) {
    this.iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Object next() {
    return iterator.next();
  }

  @Override
  public Object getKey() {
    return index++;
  }
}

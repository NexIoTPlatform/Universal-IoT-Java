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
import java.util.Map;

public class MapKeyValueIterator implements Iterator<Object>, KeyIterator {

  private final Iterator<Map.Entry<Object, Object>> iterator;

  private Map.Entry<Object, Object> current;

  public MapKeyValueIterator(Map<Object, Object> target) {
    this.iterator = target.entrySet().iterator();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Object next() {
    current = iterator.next();
    return current.getValue();
  }

  @Override
  public Object getKey() {
    return current.getKey();
  }
}

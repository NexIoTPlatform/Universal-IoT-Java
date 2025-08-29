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

package cn.universal.core.metadata.types.codec;

import static java.util.Optional.ofNullable;

import cn.universal.core.metadata.ValueType;
import cn.universal.core.metadata.ValueTypeCodec;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractValueTypeCodec<T extends ValueType> implements ValueTypeCodec<T> {

  @Override
  public T decode(T type, Map<String, Object> config) {
    ofNullable(config.get("description")).map(String::valueOf).ifPresent(type::setDescription);

    ofNullable(config.get("expands"))
        .filter(Map.class::isInstance)
        .map(Map.class::cast)
        .ifPresent(type::setExpands);
    return type;
  }

  @Override
  public Map<String, Object> encode(T type) {
    Map<String, Object> encoded = new HashMap<>();
    encoded.put("type", getTypeId());
    encoded.put("description", type.getDescription());
    encoded.put("expands", type.getExpands());
    doEncode(encoded, type);
    return encoded;
  }

  protected void doEncode(Map<String, Object> encoded, T type) {}
}

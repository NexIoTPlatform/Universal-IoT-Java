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

package cn.universal.core.metadata;

import cn.universal.core.metadata.types.codec.ArrayCodec;
import cn.universal.core.metadata.types.codec.BooleanCodec;
import cn.universal.core.metadata.types.codec.DateCodec;
import cn.universal.core.metadata.types.codec.DoubleCodec;
import cn.universal.core.metadata.types.codec.EnumCodec;
import cn.universal.core.metadata.types.codec.FileCodec;
import cn.universal.core.metadata.types.codec.FloatCodec;
import cn.universal.core.metadata.types.codec.GeoPointCodec;
import cn.universal.core.metadata.types.codec.GeoShapeCodec;
import cn.universal.core.metadata.types.codec.IntCodec;
import cn.universal.core.metadata.types.codec.LongCodec;
import cn.universal.core.metadata.types.codec.ObjectCodec;
import cn.universal.core.metadata.types.codec.PasswordCodec;
import cn.universal.core.metadata.types.codec.StringCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ValueTypeCodecs {

  private static final Map<String, ValueTypeCodec<? extends ValueType>> codecMap = new HashMap<>();

  static {
    register(new ArrayCodec());
    register(new BooleanCodec());
    register(new DateCodec());
    register(new DoubleCodec());
    register(new EnumCodec());
    register(new FloatCodec());
    register(new GeoPointCodec());
    register(new IntCodec());
    register(new LongCodec());
    register(new ObjectCodec());
    register(new StringCodec());
    register(new PasswordCodec());
    register(new FileCodec());
    register(new GeoShapeCodec());
  }

  public static void register(ValueTypeCodec<? extends ValueType> codec) {
    codecMap.put(codec.getTypeId(), codec);
  }

  @SuppressWarnings("all")
  public static Optional<ValueTypeCodec<ValueType>> getCodec(String typeId) {

    return Optional.ofNullable((ValueTypeCodec) codecMap.get(typeId));
  }

  public static ValueType decode(ValueType type, Map<String, Object> config) {
    if (type == null) {
      return null;
    }
    return getCodec(type.getId()).map(codec -> codec.decode(type, config)).orElse(type);
  }

  public static Optional<Map<String, Object>> encode(ValueType type) {
    if (type == null) {
      return Optional.empty();
    }
    return getCodec(type.getId()).map(codec -> codec.encode(type));
  }
}

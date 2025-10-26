/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.core.metadata.types;

import cn.universal.core.metadata.ValueType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ValueTypes {

  private static final Map<String, Supplier<ValueType>> supports = new ConcurrentHashMap<>();

  static {
    supports.put(ArrayType.ID, ArrayType::new);
    supports.put(BooleanType.ID, BooleanType::new);
    supports.put(DateTimeType.ID, DateTimeType::new);
    supports.put(DoubleType.ID, DoubleType::new);
    supports.put(EnumType.ID, EnumType::new);
    supports.put(FloatType.ID, FloatType::new);
    supports.put(IntType.ID, IntType::new);
    supports.put(LongType.ID, LongType::new);
    supports.put(ObjectType.ID, ObjectType::new);

    supports.put(StringType.ID, StringType::new);
    supports.put("text", StringType::new);

    supports.put(GeoType.ID, GeoType::new);
    supports.put(FileType.ID, FileType::new);
    supports.put(PasswordType.ID, PasswordType::new);
    supports.put(GeoShapeType.ID, GeoShapeType::new);
  }

  public static void register(String id, Supplier<ValueType> supplier) {
    supports.put(id, supplier);
  }

  public static Supplier<ValueType> lookup(String id) {
    if (id == null) {
      return null;
    }
    Supplier<ValueType> value = supports.get(id);
    if (value != null) {
      return value;
    }
    return supports.get(id);
  }
}

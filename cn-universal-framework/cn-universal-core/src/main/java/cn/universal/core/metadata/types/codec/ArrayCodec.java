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

package cn.universal.core.metadata.types.codec;

import static java.util.Optional.ofNullable;

import cn.hutool.json.JSONObject;
import cn.universal.core.metadata.ValueType;
import cn.universal.core.metadata.ValueTypeCodecs;
import cn.universal.core.metadata.types.ArrayType;
import cn.universal.core.metadata.types.ValueTypes;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArrayCodec extends AbstractValueTypeCodec<ArrayType> {

  @Override
  public String getTypeId() {
    return ArrayType.ID;
  }

  @Override
  public ArrayType decode(ArrayType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);
    ofNullable(jsonObject.get("elementType"))
        .map(
            v -> {
              if (v instanceof JSONObject) {
                return ((JSONObject) v);
              }
              JSONObject eleType = new JSONObject();
              eleType.put("type", v);
              return eleType;
            })
        .map(
            eleType -> {
              ValueType ValueType = ValueTypes.lookup(eleType.getStr("type")).get();

              ValueTypeCodecs.getCodec(ValueType.getId())
                  .ifPresent(codec -> codec.decode(ValueType, eleType));

              return ValueType;
            })
        .ifPresent(type::setElementType);

    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, ArrayType type) {
    super.doEncode(encoded, type);
    ValueTypeCodecs.getCodec(type.getElementType().getId())
        .ifPresent(codec -> encoded.put("elementType", codec.encode(type.getElementType())));
  }
}

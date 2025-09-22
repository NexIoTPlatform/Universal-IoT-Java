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

import cn.hutool.json.JSONObject;
import cn.universal.core.metadata.AbstractPropertyMetadata;
import cn.universal.core.metadata.PropertyMetadata;
import cn.universal.core.metadata.types.ObjectType;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectCodec extends AbstractValueTypeCodec<ObjectType> {

  @Override
  public String getTypeId() {
    return ObjectType.ID;
  }

  @Override
  public ObjectType decode(ObjectType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);

    ofNullable(jsonObject.getJSONArray("properties"))
        .map(
            list ->
                list.stream()
                    .map(JSONObject.class::cast)
                    .<AbstractPropertyMetadata>map(PropertyMetadata::new)
                    .collect(Collectors.toList()))
        .ifPresent(type::setProperties);

    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, ObjectType type) {
    super.doEncode(encoded, type);
    if (type.getProperties() != null) {
      encoded.put(
          "properties",
          type.getProperties().stream()
              .map(PropertyMetadata::new)
              .map(AbstractPropertyMetadata::toJson)
              .collect(Collectors.toList()));
    }
  }
}

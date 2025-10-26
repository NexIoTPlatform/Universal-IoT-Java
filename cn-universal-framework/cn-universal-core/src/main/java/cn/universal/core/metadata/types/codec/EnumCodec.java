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
import cn.universal.core.metadata.types.EnumType;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnumCodec extends AbstractValueTypeCodec<EnumType> {

  @Override
  public String getTypeId() {
    return EnumType.ID;
  }

  @Override
  public EnumType decode(EnumType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);

    ofNullable(jsonObject.getJSONArray("elements"))
        .map(
            list ->
                list.stream()
                    .map(JSONObject.class::cast)
                    .map(
                        e ->
                            EnumType.Element.of(
                                e.getStr("value"), e.getStr("text"), e.getStr("description")))
                    .collect(Collectors.toList()))
        .ifPresent(type::setElements);

    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, EnumType type) {
    super.doEncode(encoded, type);
    if (type.getElements() == null) {
      return;
    }
    encoded.put(
        "elements",
        type.getElements().stream().map(EnumType.Element::toMap).collect(Collectors.toList()));
  }
}

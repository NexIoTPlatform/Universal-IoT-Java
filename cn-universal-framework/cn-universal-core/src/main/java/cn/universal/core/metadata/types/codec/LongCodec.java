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
import cn.universal.core.metadata.types.LongType;
import cn.universal.core.metadata.unit.ValueUnits;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LongCodec extends AbstractValueTypeCodec<LongType> {

  @Override
  public String getTypeId() {
    return LongType.ID;
  }

  @Override
  public LongType decode(LongType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);
    ofNullable(jsonObject.getInt("max")).ifPresent(type::setMax);
    ofNullable(jsonObject.getInt("min")).ifPresent(type::setMin);
    ofNullable(jsonObject.getStr("unit")).flatMap(ValueUnits::lookup).ifPresent(type::setUnit);
    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, LongType type) {
    encoded.put("max", type.getMax());
    encoded.put("min", type.getMin());
    if (type.getUnit() != null) {
      encoded.put("unit", type.getUnit().getId());
    }
  }
}

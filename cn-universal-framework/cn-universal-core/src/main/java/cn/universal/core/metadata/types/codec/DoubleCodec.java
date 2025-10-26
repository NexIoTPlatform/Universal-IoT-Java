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
import cn.universal.core.metadata.types.DoubleType;
import cn.universal.core.metadata.unit.ValueUnits;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoubleCodec extends AbstractValueTypeCodec<DoubleType> {

  @Override
  public String getTypeId() {
    return DoubleType.ID;
  }

  @Override
  public DoubleType decode(DoubleType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);
    ofNullable(jsonObject.getDouble("max")).ifPresent(type::setMax);
    ofNullable(jsonObject.getDouble("min")).ifPresent(type::setMin);
    ofNullable(jsonObject.getInt("scale")).ifPresent(type::setScale);
    ofNullable(jsonObject.getStr("unit")).flatMap(ValueUnits::lookup).ifPresent(type::setUnit);
    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, DoubleType type) {
    encoded.put("max", type.getMax());
    encoded.put("min", type.getMin());

    encoded.put("scale", type.getScale());
    if (type.getUnit() != null) {
      encoded.put("unit", type.getUnit().getId());
    }
  }
}

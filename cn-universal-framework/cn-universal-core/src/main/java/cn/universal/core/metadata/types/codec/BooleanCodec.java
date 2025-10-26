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
import cn.universal.core.metadata.types.BooleanType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BooleanCodec extends AbstractValueTypeCodec<BooleanType> {

  @Override
  public String getTypeId() {
    return BooleanType.ID;
  }

  @Override
  public BooleanType decode(BooleanType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);

    ofNullable(jsonObject.getStr("trueText")).ifPresent(type::setTrueText);
    ofNullable(jsonObject.getStr("falseText")).ifPresent(type::setFalseText);
    ofNullable(jsonObject.getStr("trueValue")).ifPresent(type::setTrueValue);
    ofNullable(jsonObject.getStr("falseValue")).ifPresent(type::setFalseValue);
    ofNullable(jsonObject.getStr("description")).ifPresent(type::setDescription);

    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, BooleanType type) {
    super.doEncode(encoded, type);
    encoded.put("trueText", type.getTrueText());
    encoded.put("falseText", type.getFalseText());
    encoded.put("trueValue", type.getTrueValue());
    encoded.put("falseValue", type.getFalseValue());
  }
}

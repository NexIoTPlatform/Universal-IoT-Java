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
import cn.universal.core.metadata.types.DateTimeType;
import java.time.ZoneId;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateCodec extends AbstractValueTypeCodec<DateTimeType> {

  @Override
  public String getTypeId() {
    return DateTimeType.ID;
  }

  @Override
  public DateTimeType decode(DateTimeType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);
    ofNullable(jsonObject.getStr("format")).ifPresent(type::setFormat);
    ofNullable(jsonObject.getStr("tz")).map(ZoneId::of).ifPresent(type::setZoneId);

    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, DateTimeType type) {
    super.doEncode(encoded, type);
    encoded.put("format", type.getFormat());
    encoded.put("tz", type.getZoneId().toString());
  }
}

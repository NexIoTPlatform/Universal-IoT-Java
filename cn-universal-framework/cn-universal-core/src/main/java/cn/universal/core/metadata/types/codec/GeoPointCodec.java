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
import cn.universal.core.metadata.types.GeoType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoPointCodec extends AbstractValueTypeCodec<GeoType> {

  @Override
  public String getTypeId() {
    return GeoType.ID;
  }

  @Override
  public GeoType decode(GeoType type, Map<String, Object> config) {
    super.decode(type, config);
    JSONObject jsonObject = new JSONObject(config);
    ofNullable(jsonObject.getStr("latProperty")).ifPresent(type::latProperty);
    ofNullable(jsonObject.getStr("lonProperty")).ifPresent(type::lonProperty);
    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, GeoType type) {
    encoded.put("latProperty", type.getLatProperty());
    encoded.put("lonProperty", type.getLonProperty());
  }
}

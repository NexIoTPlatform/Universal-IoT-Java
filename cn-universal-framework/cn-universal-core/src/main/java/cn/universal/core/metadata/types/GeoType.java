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

import cn.universal.core.metadata.Converter;
import cn.universal.core.metadata.FormatSupport;
import cn.universal.core.metadata.ValidateResult;
import cn.universal.core.metadata.ValueType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoType extends AbstractType<GeoType>
    implements ValueType, FormatSupport, Converter<GeoPoint> {

  public static final String ID = "geoPoint";

  public static final GeoType GLOBAL = new GeoType();

  // 经度字段
  private String latProperty = "lat";

  // 纬度字段
  private String lonProperty = "lon";

  public GeoType latProperty(String property) {
    this.latProperty = property;
    return this;
  }

  public GeoType lonProperty(String property) {
    this.lonProperty = property;
    return this;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "地理位置";
  }

  public Map<String, Object> convertToMap(Object value) {
    Map<String, Object> mapGeoPoint = new HashMap<>();

    GeoPoint point = convert(value);
    if (point != null) {
      mapGeoPoint.put("lat", point.getLat());
      mapGeoPoint.put("lon", point.getLon());
    }
    return mapGeoPoint;
  }

  public GeoPoint convert(Object value) {
    return GeoPoint.of(value);
  }

  @Override
  public ValidateResult validate(Object value) {

    GeoPoint geoPoint = convert(value);

    return geoPoint == null
        ? ValidateResult.fail("不支持的Geo格式:" + value)
        : ValidateResult.success(geoPoint);
  }

  @Override
  public String format(Object value) {
    GeoPoint geoPoint = convert(value);

    return String.valueOf(geoPoint);
  }
}

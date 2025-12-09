/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.dm.device.entity;

import static java.util.Optional.ofNullable;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.metadata.AbstractPropertyMetadata;
import cn.universal.core.metadata.ValueType;
import cn.universal.core.metadata.types.ArrayType;
import cn.universal.core.metadata.types.BooleanType;
import cn.universal.core.metadata.types.DateTimeType;
import cn.universal.core.metadata.types.EnumType;
import cn.universal.core.metadata.types.GeoPoint;
import cn.universal.core.metadata.types.GeoType;
import cn.universal.core.metadata.types.NumberType;
import cn.universal.core.metadata.types.ObjectType;
import cn.universal.core.metadata.types.StringType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class IoTDevicePropertiesBO {

  //  private String id;

  private String iotId;

  private String deviceId;

  private String property;

  private String propertyName;

  private String stringValue;
  private Object desireValue;

  private String formatValue;
  private String desireFormatValue;

  private BigDecimal numberValue;

  private GeoPoint geoValue;

  /** 最后更新时间 */
  private long timestamp;

  private Object objectValue;

  private String value;

  private Date timeValue;

  private String type;

  private String symbol;

  private String customized;
  // 是否设置存储策略
  private boolean storagePolicy;

  public Map<String, Object> toMap() {
    return new JSONObject(this);
  }

  public IoTDevicePropertiesBO withValue(AbstractPropertyMetadata metadata, Object value) {
    if (metadata == null) {
      setValue(String.valueOf(value));
      if (value instanceof Number) {
        numberValue = new BigDecimal(value.toString());
      } else if (value instanceof Date) {
        timeValue = ((Date) value);
      }
      return this;
    }
    setProperty(metadata.getId());
    setPropertyName(metadata.getName());
    return withValue(metadata.getValueType(), value);
  }

  public IoTDevicePropertiesBO withValue(ValueType type, Object value) {
    if (value == null) {
      return this;
    }
    setType(type.getType());
    String convertedValue;

    if (type instanceof NumberType) {
      NumberType<?> numberType = (NumberType<?>) type;
      Number number = numberType.convertNumber(value);
      if (number == null) {
        log.error("无法将" + value + "转为" + type.getId());
        //        throw new UnsupportedOperationException("无法将" + value + "转为" + type.getId());
        return this;
      }
      setSymbol(numberType.getUnit() != null ? numberType.getUnit().getSymbol() : "");
      convertedValue = number.toString();
      BigDecimal numberVal;
      if (number instanceof BigDecimal) {
        numberVal = ((BigDecimal) number);
      } else if (number instanceof Integer) {
        numberVal = BigDecimal.valueOf(number.intValue());
      } else if (number instanceof Long) {
        numberVal = BigDecimal.valueOf(number.longValue());
      } else {
        numberVal = BigDecimal.valueOf(number.doubleValue());
      }
      setNumberValue(numberVal);
    } else if (type instanceof DateTimeType) {
      DateTimeType dateTimeType = (DateTimeType) type;
      convertedValue = String.valueOf(value);
      setTimeValue(dateTimeType.convert(value));
    } else if (type instanceof ObjectType) {
      ObjectType objectType = (ObjectType) type;
      Object val = objectType.convert(value);
      convertedValue = JSONUtil.toJsonStr(val);
      setObjectValue(val);
    } else if (type instanceof ArrayType) {
      ArrayType objectType = (ArrayType) type;
      Object val = objectType.convert(value);
      convertedValue = JSONUtil.toJsonStr(val);
      setObjectValue(val);
    } else if (type instanceof GeoType) {
      GeoType geoType = (GeoType) type;
      GeoPoint val = geoType.convert(value);
      convertedValue = String.valueOf(val);
      setGeoValue(val);
    } else if (type instanceof StringType) {
      StringType stringType = ((StringType) type);
      setStringValue(convertedValue = String.valueOf(value));
      setSymbol(stringType.getUnit() != null ? stringType.getUnit().getSymbol() : "");
    } else if (type instanceof EnumType) {
      EnumType enumType = (EnumType) type;
      setFormatValue(enumType.format(value));
      setStringValue(convertedValue = String.valueOf(value));
    } else if (type instanceof BooleanType) {
      BooleanType booleanType = (BooleanType) type;
      setFormatValue(booleanType.format(value));
      setStringValue(convertedValue = String.valueOf(value));
    } else {
      setStringValue(convertedValue = String.valueOf(value));
    }
    setValue(convertedValue);
    ofNullable(type.format(value)).map(String::valueOf).ifPresent(this::setFormatValue);

    return this;
  }

  public IoTDevicePropertiesBO withDesire(ValueType type, Object value) {
    if (value == null) {
      return this;
    }
    ofNullable(type.format(value)).map(String::valueOf).ifPresent(this::setDesireFormatValue);
    return this;
  }
}

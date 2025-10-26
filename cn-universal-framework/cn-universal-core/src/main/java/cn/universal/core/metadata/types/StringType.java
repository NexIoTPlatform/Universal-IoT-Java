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
import cn.universal.core.metadata.UnitSupported;
import cn.universal.core.metadata.ValidateResult;
import cn.universal.core.metadata.ValueType;
import cn.universal.core.metadata.unit.ValueUnit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StringType extends AbstractType<StringType>
    implements ValueType, Converter<String>, UnitSupported {

  public static final String ID = "string";
  public static final StringType GLOBAL = new StringType();

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "字符串";
  }

  // 支持单位
  private ValueUnit unit;

  @Override
  public ValidateResult validate(Object value) {
    return ValidateResult.success(String.valueOf(value));
  }

  @Override
  public String format(Object value) {
    ValueUnit unit = getUnit();
    if (unit == null) {
      return String.valueOf(value);
    }
    return (String) unit.format(value);
  }

  @Override
  public String convert(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}

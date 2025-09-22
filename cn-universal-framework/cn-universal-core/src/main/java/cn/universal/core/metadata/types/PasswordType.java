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

package cn.universal.core.metadata.types;

import cn.universal.core.metadata.Converter;
import cn.universal.core.metadata.ValidateResult;
import cn.universal.core.metadata.ValueType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordType extends AbstractType<PasswordType>
    implements ValueType, Converter<String> {

  public static final String ID = "password";
  public static final PasswordType GLOBAL = new PasswordType();

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "密码";
  }

  @Override
  public ValidateResult validate(Object value) {
    return ValidateResult.success(String.valueOf(value));
  }

  @Override
  public String format(Object value) {
    return String.valueOf(value);
  }

  @Override
  public String convert(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}

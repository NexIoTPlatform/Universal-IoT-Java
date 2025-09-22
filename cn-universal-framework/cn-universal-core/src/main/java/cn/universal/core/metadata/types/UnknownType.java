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

import cn.universal.core.metadata.ValidateResult;
import cn.universal.core.metadata.ValueType;

/**
 * @since 1.0.0
 */
public class UnknownType implements ValueType {

  @Override
  public ValidateResult validate(Object value) {
    return ValidateResult.success();
  }

  @Override
  public String getId() {
    return "unknown";
  }

  @Override
  public String getName() {
    return "未知类型";
  }

  @Override
  public String getDescription() {
    return "未知类型";
  }

  @Override
  public String format(Object value) {
    return String.valueOf(value);
  }
}

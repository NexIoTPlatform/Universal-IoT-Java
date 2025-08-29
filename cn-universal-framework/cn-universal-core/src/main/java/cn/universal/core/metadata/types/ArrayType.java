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

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.universal.core.metadata.Converter;
import cn.universal.core.metadata.ValidateResult;
import cn.universal.core.metadata.ValueType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArrayType extends AbstractType<ArrayType>
    implements ValueType, Converter<List<Object>> {

  public static final String ID = "array";

  private ValueType elementType;

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "数组";
  }

  public ArrayType elementType(ValueType elementType) {
    this.elementType = elementType;
    return this;
  }

  @Override
  public ValidateResult validate(Object value) {

    List<Object> listValue = convert(value);
    if (elementType != null && value instanceof Collection) {
      for (Object data : listValue) {
        ValidateResult result = elementType.validate(data);
        if (!result.isSuccess()) {
          return result;
        }
      }
    }
    return ValidateResult.success(listValue);
  }

  @Override
  public Object format(Object value) {

    if (elementType != null && value instanceof Collection) {
      Collection<?> collection = ((Collection<?>) value);
      return new JSONArray(
          collection.stream().map(data -> elementType.format(data)).collect(Collectors.toList()));
    }

    return JSONUtil.toJsonStr(value);
  }

  @Override
  public List<Object> convert(Object value) {
    if (value instanceof Collection) {
      return ((Collection<?>) value)
          .stream()
              .map(
                  val -> {
                    if (elementType instanceof Converter) {
                      return ((Converter<?>) elementType).convert(val);
                    }
                    return val;
                  })
              .collect(Collectors.toList());
    }
    if (value instanceof String) {
      return JSONUtil.parseArray(String.valueOf(value));
    }
    return Collections.singletonList(value);
  }
}

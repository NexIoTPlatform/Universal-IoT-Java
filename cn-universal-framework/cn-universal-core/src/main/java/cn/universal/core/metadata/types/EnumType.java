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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class EnumType extends AbstractType<EnumType> implements ValueType {

  public static final String ID = "enum";

  private volatile List<Element> elements;

  private boolean multi;

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "枚举";
  }

  public EnumType multi(boolean multi) {
    this.multi = multi;
    return this;
  }

  @Override
  public ValidateResult validate(Object value) {
    if (elements == null) {
      return ValidateResult.fail("值[" + value + "]不在枚举中");
    }
    return elements.stream()
        .filter(ele -> match(value, ele))
        .findFirst()
        .map(e -> ValidateResult.success(e.value))
        .orElseGet(() -> ValidateResult.fail("值[" + value + "]不在枚举中"));
  }

  private boolean match(Object value, Element ele) {
    if (value instanceof Map) {
      // 适配map情况下的枚举信息
      @SuppressWarnings("all")
      Map<Object, Object> mapVal = ((Map<Object, Object>) value);
      return match(mapVal.getOrDefault("value", mapVal.get("id")), ele);
    }
    return ele.value.equals(String.valueOf(value)) || ele.text.equals(String.valueOf(value));
  }

  @Override
  public String format(Object value) {
    String stringVal = String.valueOf(value);
    if (elements == null) {
      return stringVal;
    }
    return elements.stream()
        .filter(ele -> ele.value.equals(String.valueOf(value)))
        .findFirst()
        .map(Element::getText)
        .orElse(stringVal);
  }

  public EnumType addElement(Element element) {
    if (elements == null) {
      synchronized (this) {
        if (elements == null) {
          elements = new ArrayList<>();
        }
      }
    }
    elements.add(element);
    return this;
  }

  @Getter
  @Setter
  @AllArgsConstructor(staticName = "of")
  @NoArgsConstructor
  public static class Element {

    private String value;

    private String text;

    private String description;

    public static Element of(String value, String text) {
      return of(value, text, null);
    }

    public static Element of(Map<String, String> map) {
      return Element.of(map.get("value"), map.get("text"), map.get("description"));
    }

    public Map<String, Object> toMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("value", value);
      map.put("text", text);
      map.put("description", description);

      return map;
    }
  }
}

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

package cn.universal.core.metadata.unit;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JsonValueUnit implements ValueUnit {

  private final String symbol;

  private final String name;

  public static JsonValueUnit of(String jsonStr) {

    JSONObject json = JSONUtil.parseObj(jsonStr);

    String symbol = json.getStr("symbol");
    if (null == symbol) {
      return null;
    }

    return new JsonValueUnit(symbol, (String) json.getOrDefault("name", symbol));
  }

  @Override
  public String getSymbol() {
    return symbol;
  }

  @Override
  public Object format(Object value) {
    if (value == null) {
      return null;
    }
    return value + "" + symbol;
  }

  @Override
  public String getId() {
    return "custom_" + symbol;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return symbol;
  }
}

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

package cn.universal.core.metadata.unit;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class SymbolValueUnit implements ValueUnit {

  private final String symbol;

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
    return symbol;
  }

  @Override
  public String getName() {
    return symbol;
  }

  @Override
  public String getDescription() {
    return symbol;
  }
}

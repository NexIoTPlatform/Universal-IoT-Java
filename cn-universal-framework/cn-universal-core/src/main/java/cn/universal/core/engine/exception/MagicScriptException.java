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

package cn.universal.core.engine.exception;

import cn.universal.core.engine.parsing.Span;

public class MagicScriptException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  private final String simpleMessage;
  private final Span location;

  public MagicScriptException(String errorMessage, String simpleMessage, Span location) {
    super(errorMessage);
    this.simpleMessage = simpleMessage;
    this.location = location;
  }

  public MagicScriptException(String errorMessage, Span location) {
    this(errorMessage, errorMessage, location);
  }

  public MagicScriptException(String errorMessage) {
    this(errorMessage, errorMessage, null);
  }

  public MagicScriptException(
      String message, String simpleMessage, Throwable cause, Span location) {
    super(message, cause);
    this.simpleMessage = simpleMessage;
    this.location = location;
  }

  public String getSimpleMessage() {
    return simpleMessage;
  }

  public Span getLocation() {
    return location;
  }

  public Span.Line getLine() {
    return location == null ? null : location.getLine();
  }
}

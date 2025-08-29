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

package cn.universal.core.engine.parsing;

public class Token {

  private final TokenType type;

  private final Span span;
  private final Object value;
  private TokenStream tokenStream;

  public Token(TokenType type, Span span) {
    this.type = type;
    this.span = span;
    this.value = null;
  }

  public Token(TokenType type, Span span, Object value) {
    this.type = type;
    this.span = span;
    this.value = value;
  }

  public Token(TokenType type, Span span, TokenStream tokenStream) {
    this.type = type;
    this.span = span;
    this.tokenStream = tokenStream;
    this.value = null;
  }

  public TokenType getType() {
    return type;
  }

  public Span getSpan() {
    return span;
  }

  public String getText() {
    return span.getText();
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "Token [type=" + type + ", span=" + span + "]";
  }

  public TokenStream getTokenStream() {
    return tokenStream;
  }
}

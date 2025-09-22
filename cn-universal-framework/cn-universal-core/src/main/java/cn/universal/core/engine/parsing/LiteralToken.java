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

public class LiteralToken extends Token {

  public LiteralToken(TokenType type, Span span) {
    super(type, span);
  }

  public LiteralToken(TokenType type, Span span, TokenStream tokenStream) {
    super(type, span, tokenStream);
  }

  public LiteralToken(TokenType type, Span span, Object value) {
    super(type, span, value);
  }
}

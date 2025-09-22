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

package cn.universal.core.engine.compile;

public class MagicScriptCompileException extends RuntimeException {

  public MagicScriptCompileException(Throwable cause) {
    super(cause);
  }

  public MagicScriptCompileException(String message, Throwable cause) {
    super(message, cause);
  }

  public MagicScriptCompileException(String message) {
    super(message);
  }
}

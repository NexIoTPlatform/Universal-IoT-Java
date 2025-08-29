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

package cn.universal.ossm.oss.exception;

/** OSS异常类 @Author Lion Li */
public class OssException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public OssException(String msg) {
    super(msg);
  }
}

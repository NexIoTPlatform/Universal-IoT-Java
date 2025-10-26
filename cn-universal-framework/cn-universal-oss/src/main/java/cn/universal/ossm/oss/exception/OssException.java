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

package cn.universal.ossm.oss.exception;

/** OSS异常类 @Author Lion Li */
public class OssException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public OssException(String msg) {
    super(msg);
  }
}

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

package cn.universal.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/** 捕获报错日志处理工具类 */
public class ErrorUtil {

  /** Exception出错的栈信息转成字符串 用于打印到日志中 */
  public static String errorInfoToString(Throwable e) {
    // try-with-resource语法糖 处理机制
    try (StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw)) {
      e.printStackTrace(pw);
      pw.flush();
      sw.flush();
      return sw.toString();
    } catch (Exception ignored) {
      throw new RuntimeException(ignored.getMessage(), ignored);
    }
  }
}

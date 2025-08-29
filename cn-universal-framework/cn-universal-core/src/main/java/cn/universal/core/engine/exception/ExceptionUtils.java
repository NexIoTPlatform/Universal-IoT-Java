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

public class ExceptionUtils {

  public static int indexOfThrowable(Throwable root, Class<? extends Throwable> clazz) {
    if (root == null) {
      return -1;
    }
    int index = 0;
    do {
      if (clazz.isAssignableFrom(root.getClass())) {
        return index;
      }
      index++;
    } while ((root = root.getCause()) != null);
    return -1;
  }
}

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

import cn.hutool.core.util.StrUtil;
import java.util.regex.Pattern;

/**
 * 字符串扩展工具类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/23 22:10
 */
public class IotStrUtil {

  /**
   * 检验是否被Base64
   *
   * @param str
   * @return
   */
  public static boolean isBase64(String str) {
    if (StrUtil.isBlank(str)) {
      return false;
    }
    String base64Pattern =
        "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    boolean matches = Pattern.matches(base64Pattern, str);
    if (matches) {
      return true;
    }
    return false;
  }
}

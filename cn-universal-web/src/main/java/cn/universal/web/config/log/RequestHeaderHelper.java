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

package cn.universal.web.config.log;

import java.util.HashSet;
import java.util.Set;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/04/19
 */
public class RequestHeaderHelper {

  public static final String AUTHORIZATION = "Authorization";

  public static final Set<String> headers = new HashSet<>();

  static {
    headers.add(AUTHORIZATION);
    headers.add(AUTHORIZATION.toLowerCase());
  }

  public static boolean matchHeader(String key) {
    return headers.contains(key.toLowerCase());
  }
}

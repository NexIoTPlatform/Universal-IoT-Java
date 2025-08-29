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

package cn.universal.core.engine.constant;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author Aleo
 *
 * @version 1.0
 * @since 2023/5/19
 */

/**
 * @version 1.0 @Author Aleo
 * @since 2023/5/19
 */
public class SafeRule {

  public static Set<String> packages =
      Stream.of(
              "cn.hutool.json.JSONUtil",
              "cn.hutool.service.util.HexUtil",
              "cn.hutool.service.util.RandomUtil",
              "cn.hutool.service.util.StrUtil")
          .collect(Collectors.toSet());

  public static boolean filter(String packageName) {
    return packageName != null
        && !"".equalsIgnoreCase(packageName)
        && !packages.contains(packageName);
  }
}

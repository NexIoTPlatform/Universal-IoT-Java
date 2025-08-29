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

package cn.universal.core.engine.functions;

import cn.universal.core.engine.annotation.Comment;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** Date扩展 */
public class DateExtension {

  private static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();

  @Comment("日期格式化")
  public static String format(
      Date source, @Comment(name = "pattern", value = "格式，如yyyy-MM-dd") String pattern) {
    return Instant.ofEpochMilli(source.getTime())
        .atZone(SYSTEM_ZONE_ID)
        .format(DateTimeFormatter.ofPattern(pattern));
  }
}

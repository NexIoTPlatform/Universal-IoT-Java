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
import java.util.regex.Pattern;

/** Pattern 扩展 */
public class PatternExtension {

  @Comment("校验文本是否符合正则")
  public boolean test(Pattern pattern, @Comment(name = "source", value = "目标字符串") String source) {
    return source != null && pattern.matcher(source).find();
  }
}

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

package cn.universal.rule.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.util.stream.Stream;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * json解析工具类 @Author Aleo
 *
 * @since 2025/11/30 10:43
 */
public class JsonParserUtil {

  private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

  /**
   * 获取指定json节点数据
   *
   * @param json json数据
   * @param key 获取数据key
   * @param clazz 获取数据类型
   * @return T 数据
   */
  public static <T> T getValue(JSONObject json, String key, Class<T> clazz) {
    String str =
        Stream.of(key.split("\\."))
            .map(a -> String.format("['%s']", a))
            .reduce("", (a, b) -> a + b);
    Expression expression = EXPRESSION_PARSER.parseExpression(str);
    return expression.getValue(json, clazz);
  }

  /**
   * 获取指定json节点数据
   *
   * @param json json数据
   * @param key 获取数据key
   * @param clazz 获取数据类型
   * @return T 数据
   */
  public static <T> T getValue(String json, String key, Class<T> clazz) {
    return getValue(JSONUtil.parseObj(json), key, clazz);
  }

  /**
   * 获取指定json节点数据
   *
   * @param json json数据
   * @param key 获取数据key
   * @return 数据
   */
  public static Object getValue(JSONObject json, String key) {
    try {
      return getValue(
          JSONUtil.parseObj(json), key.replaceAll("\"", "").replace("'", ""), Object.class);
    } catch (Exception e) {
      return null;
    }
  }

  public static void main(String[] args) {
    JSONObject set =
        new JSONObject().set("a", new JSONObject().set("b", new JSONObject().set("c", "2")));
    Integer value = getValue(set, "a.b.c", Integer.class);
    System.out.println(value);
  }
}

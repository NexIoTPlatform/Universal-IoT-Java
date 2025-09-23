package cn.universal.manager.notice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtil {

  // 支持 #xxx 和 #{xxx} 两种格式（全部用普通字符串，不用字符串模板）
  private static final Pattern PARAM_PATTERN =
      Pattern.compile("#([a-zA-Z0-9_]+)|#\\{([a-zA-Z0-9_]+)\\}");
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** 替换模板中的参数，兼容 #xxx 和 #{xxx} 两种格式 */
  public static String replaceParams(String template, Map<String, Object> params) {
    if (template == null || params == null) {
      return template;
    }
    Matcher matcher = PARAM_PATTERN.matcher(template);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String paramName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
      Object value = params.get(paramName);
      String replacement = value != null ? value.toString() : "";
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /** 解析JSON字符串为Map */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> parseJson(String json) {
    try {
      if (json == null || json.trim().isEmpty()) {
        return Map.of();
      }
      return objectMapper.readValue(json, Map.class);
    } catch (Exception e) {
      return Map.of();
    }
  }

  /** 将对象转换为JSON字符串 */
  public static String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      return "{}";
    }
  }
}

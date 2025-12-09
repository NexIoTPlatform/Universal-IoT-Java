package cn.universal.databridge.engine;

import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数化模板引擎：支持 #{path} 值占位
 * 所有值均以 '?' 参数占位绑定，避免引号与转义问题
 */
public class ParamTemplateEngine {

  private static final Pattern VAR_PATTERN = Pattern.compile("#\\{([a-zA-Z0-9_.]+)\\}");

  public ParamSql process(String template, Map<String, Object> variables, SqlDialectAdapter adapter) {
    if (StrUtil.isBlank(template)) {
      return new ParamSql("", List.of());
    }
    String sql = template;
    List<Object> params = new ArrayList<>();

    // 处理 #{var.path} 占位符
    Matcher m = VAR_PATTERN.matcher(sql);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String path = m.group(1);
      Object value = getNestedValue(variables, path);
      
      // 将值转换为适合数据库的格式
      Object paramValue = convertValueForDatabase(value, path);
      params.add(paramValue);
      
      // properties 和 data 字段如果是 Map 序列化的 JSON 字符串，需要特殊处理
      String placeholder = "?";
      if (("properties".equals(path) || "data".equals(path)) && paramValue instanceof String) {
        String strValue = (String) paramValue;
        // 检查是否是 JSON 对象格式（以 { 开头且以 } 结尾）
        if (strValue.trim().startsWith("{") && strValue.trim().endsWith("}")) {
          placeholder = adapter.wrapJsonParameter();
        }
      }
      m.appendReplacement(sb, Matcher.quoteReplacement(placeholder));
    }
    m.appendTail(sb);
    sql = sb.toString();

    return new ParamSql(sql, params);
  }

  @SuppressWarnings("unchecked")
  private Object getNestedValue(Map<String, Object> variables, String path) {
    if (path == null || path.isEmpty()) return null;
    String[] keys = path.split("\\.");
    Object curr = variables;
    for (String k : keys) {
      if (curr == null) return null;
      if (curr instanceof Map) {
        curr = ((Map<String, Object>) curr).get(k);
      } else {
        return null;
      }
    }
    // 若直接访问 properties 或 data，返回 JSON 字符串，避免各库的 JSON 绑定差异
    if (curr instanceof Map && ("properties".equals(path) || "data".equals(path))) {
      return cn.hutool.json.JSONUtil.toJsonStr(curr);
    }
    return curr;
  }

  /**
   * 将值转换为适合数据库存储的格式
   * @param value 原始值
   * @param path 变量路径
   * @return 转换后的值
   */
  private Object convertValueForDatabase(Object value, String path) {
    if (value == null) {
      return null;
    }
    
    // 如果已经是字符串，直接返回
    if (value instanceof String) {
      return value;
    }
    
    // 其他类型转为字符串，避免类型不匹配问题
    if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    
    if (value instanceof java.util.Date) {
      return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    }
    
    // Map 类型序列化为 JSON
    if (value instanceof Map) {
      return cn.hutool.json.JSONUtil.toJsonStr(value);
    }
    
    // 其他类型默认转为字符串
    return value.toString();
  }
}

package cn.universal.manager.notice.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.util.Iterator;
import java.util.Map;

/** JSON脱敏工具类 用于检测和脱敏包含Secret关键字的字段 */
public class JsonDesensitizationUtil {

  private static final String SECRET_KEYWORD = "secret";
  private static final String MASK_VALUE = "******";

  /**
   * 对JSON对象进行脱敏处理
   *
   * @param jsonString JSON字符串
   * @return 脱敏后的JSON对象
   */
  public static JSONObject desensitize(String jsonString) {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      return null;
    }

    try {
      JSONObject jsonObject = JSONUtil.parseObj(jsonString);
      return desensitizeJsonObject(jsonObject);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 对JSONObject进行脱敏处理
   *
   * @param jsonObject JSON对象
   * @return 脱敏后的JSON对象
   */
  public static JSONObject desensitizeJsonObject(JSONObject jsonObject) {
    if (jsonObject == null) {
      return null;
    }

    JSONObject result = new JSONObject();
    Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      Object value = entry.getValue();

      // 检查key是否包含Secret关键字（忽略大小写）
      if (containsSecretKeyword(key)) {
        // 脱敏处理
        result.put(key, MASK_VALUE);
      } else if (value instanceof JSONObject) {
        // 递归处理嵌套的JSONObject
        result.put(key, desensitizeJsonObject((JSONObject) value));
      } else {
        // 直接复制其他值
        result.put(key, value);
      }
    }

    return result;
  }

  /**
   * 检查字符串是否包含Secret关键字（忽略大小写）
   *
   * @param text 要检查的字符串
   * @return 是否包含Secret关键字
   */
  private static boolean containsSecretKeyword(String text) {
    if (text == null) {
      return false;
    }
    return text.toLowerCase().contains(SECRET_KEYWORD);
  }

  /**
   * 将Object转换为JSONObject并进行脱敏处理
   *
   * @param obj 要处理的对象
   * @return 脱敏后的JSONObject
   */
  public static JSONObject desensitizeObject(Object obj) {
    if (obj == null) {
      return null;
    }

    try {
      if (obj instanceof JSONObject) {
        return desensitizeJsonObject((JSONObject) obj);
      } else {
        // 先转换为JSON字符串，再解析为JSONObject
        String jsonString = JSONUtil.toJsonStr(obj);
        return desensitize(jsonString);
      }
    } catch (Exception e) {
      return null;
    }
  }
}

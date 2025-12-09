package cn.universal.databridge.plugin.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DefaultJdbcOutPlugin 智能引号处理测试
 * 测试兼容MySQL和PostgreSQL的引号处理功能
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/15
 */
@DisplayName("JDBC插件智能引号处理测试")
public class DefaultJdbcOutPluginQuoteTest {

  private DefaultJdbcOutPlugin plugin;
  private Map<String, Object> variables;

  @BeforeEach
  void setUp() {
    plugin = new DefaultJdbcOutPlugin();
    
    // 准备测试数据
    Map<String, Object> properties = new HashMap<>();
    properties.put("temperature", 14.3);
    properties.put("illuminationDesc", "bright");
    properties.put("csq", 25);
    
    variables = new HashMap<>();
    variables.put("deviceId", "nex123321");
    variables.put("productKey", "Ru871cfJjhoM");
    variables.put("deviceName", "期望值验证");
    variables.put("iotId", "Ru871cfJjhoMnex123321");
    variables.put("deviceNode", "DEVICE");
    variables.put("messageType", "PROPERTIES");
    variables.put("properties", properties);
  }

  /**
   * 使用反射调用受保护的processTemplate方法
   */
  private String processTemplate(String template, Map<String, Object> variables) {
    try {
      java.lang.reflect.Method method = DefaultJdbcOutPlugin.class.getDeclaredMethod(
          "processTemplate", String.class, Map.class);
      method.setAccessible(true);
      return (String) method.invoke(plugin, template, variables);
    } catch (Exception e) {
      throw new RuntimeException("调用processTemplate方法失败", e);
    }
  }

  @Test
  @DisplayName("测试占位符在单引号内的情况（MySQL/PostgreSQL标准）")
  void testPlaceholderInSingleQuotes() {
    // 测试场景：'#{deviceId}' 应该替换为 'nex123321'，不产生双重引号
    String template = "INSERT INTO device_data(device_id) VALUES('#{deviceId}');";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 应该只包含一对单引号，不应该有双重引号
    assertEquals("INSERT INTO device_data(device_id) VALUES('nex123321');", result);
    assertFalse(result.contains("''"), "不应该包含双重单引号");
  }

  @Test
  @DisplayName("测试PostgreSQL json_build_object场景")
  void testPostgreSQLJsonBuildObject() {
    // 用户提供的实际场景
    String template = 
        "INSERT INTO device_data(device_id, product_key, device_name, iot_id, " +
        "device_node, message_type, properties, raw_data) VALUES(" +
        "'#{deviceId}', '#{productKey}', '#{deviceName}', '#{iotId}', " +
        "'#{deviceNode}', '#{messageType}', '#{properties}', " +
        "json_build_object(" +
        "'csq', #{properties.temperature}, " +
        "'tips', #{properties.illuminationDesc}" +
        ")::TEXT);";
    
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 验证单引号内的占位符被正确替换，不产生双重引号
    assertTrue(result.contains("'nex123321'"), "deviceId应该被正确替换");
    assertTrue(result.contains("'Ru871cfJjhoM'"), "productKey应该被正确替换");
    assertTrue(result.contains("'期望值验证'"), "deviceName应该被正确替换");
    
    // 验证json_build_object中的占位符（不在引号内）被正确替换
    assertTrue(result.contains("14.3"), "temperature应该被替换为数字");
    assertTrue(result.contains("bright"), "illuminationDesc应该被替换");
    
    // 不应该有双重引号
    assertFalse(result.contains("''nex123321''"), "不应该有双重单引号");
  }

  @Test
  @DisplayName("测试占位符在双引号内的情况（PostgreSQL JSON）")
  void testPlaceholderInDoubleQuotes() {
    String template = "SELECT \"#{deviceId}\" as id;";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 应该只包含一对双引号，不应该有双重引号
    assertEquals("SELECT \"nex123321\" as id;", result);
    assertFalse(result.contains("\"\""), "不应该包含双重双引号");
  }

  @Test
  @DisplayName("测试占位符不在引号内的情况")
  void testPlaceholderNotInQuotes() {
    String template = "SELECT #{deviceId} as id;";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 不在引号内时，应该添加单引号
    assertEquals("SELECT 'nex123321' as id;", result);
  }

  @Test
  @DisplayName("测试字符串值中包含单引号的情况")
  void testStringWithSingleQuote() {
    variables.put("deviceName", "O'Brien");
    String template = "INSERT INTO device_data(device_name) VALUES('#{deviceName}');";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 单引号应该被转义为两个单引号
    assertTrue(result.contains("O''Brien"), "单引号应该被转义");
    assertEquals("INSERT INTO device_data(device_name) VALUES('O''Brien');", result);
  }

  @Test
  @DisplayName("测试字符串值中包含双引号的情况")
  void testStringWithDoubleQuote() {
    variables.put("description", "He said \"Hello\"");
    String template = "SELECT \"#{description}\" as desc;";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 双引号应该被转义
    assertTrue(result.contains("\\\""), "双引号应该被转义");
  }

  @Test
  @DisplayName("测试NULL值处理")
  void testNullValue() {
    variables.put("optionalField", null);
    String template = "INSERT INTO device_data(optional_field) VALUES('#{optionalField}');";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // NULL值应该被替换为NULL关键字
    assertEquals("INSERT INTO device_data(optional_field) VALUES('NULL');", result);
  }

  @Test
  @DisplayName("测试数字值处理")
  void testNumberValue() {
    String template = "INSERT INTO device_data(temperature) VALUES(#{properties.temperature});";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 数字值不在引号内时，应该直接使用，不添加引号
    assertTrue(result.contains("14.3"), "数字值应该被正确替换");
    assertFalse(result.contains("'14.3'"), "数字值不应该被引号包围");
  }

  @Test
  @DisplayName("测试数字值在引号内的情况")
  void testNumberInQuotes() {
    String template = "INSERT INTO device_data(temperature) VALUES('#{properties.temperature}');";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 数字值在引号内时，应该直接替换，不添加引号
    assertEquals("INSERT INTO device_data(temperature) VALUES('14.3');", result);
  }

  @Test
  @DisplayName("测试嵌套属性访问")
  void testNestedPropertyAccess() {
    String template = 
        "INSERT INTO device_data(csq, temp) VALUES(" +
        "'#{properties.csq}', #{properties.temperature});";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    assertTrue(result.contains("'25'"), "嵌套属性csq应该被正确替换");
    assertTrue(result.contains("14.3"), "嵌套属性temperature应该被正确替换");
  }

  @Test
  @DisplayName("测试混合场景：部分在引号内，部分不在")
  void testMixedQuotes() {
    String template = 
        "INSERT INTO device_data(device_id, product_key, temperature) VALUES(" +
        "'#{deviceId}', '#{productKey}', #{properties.temperature});";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 在引号内的应该只替换值
    assertTrue(result.contains("'nex123321'"), "引号内的deviceId应该被正确替换");
    assertTrue(result.contains("'Ru871cfJjhoM'"), "引号内的productKey应该被正确替换");
    
    // 不在引号内的应该添加引号
    assertTrue(result.contains("'14.3'"), "不在引号内的temperature应该被添加引号");
  }

  @Test
  @DisplayName("测试复杂的PostgreSQL JSON场景")
  void testComplexPostgreSQLJson() {
    String template = 
        "INSERT INTO device_data(raw_data) VALUES(" +
        "json_build_object(" +
        "'deviceId', '#{deviceId}', " +
        "'temperature', #{properties.temperature}, " +
        "'description', '#{properties.illuminationDesc}'" +
        ")::TEXT);";
    
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 验证JSON字符串中的占位符被正确替换
    assertTrue(result.contains("'nex123321'"), "JSON中的deviceId应该被正确替换");
    assertTrue(result.contains("14.3"), "JSON中的temperature应该被正确替换");
    assertTrue(result.contains("'bright'"), "JSON中的description应该被正确替换");
    
    // 不应该有双重引号
    assertFalse(result.contains("''nex123321''"), "不应该有双重单引号");
  }

  @Test
  @DisplayName("测试布尔值处理")
  void testBooleanValue() {
    variables.put("isActive", true);
    String template = "INSERT INTO device_data(is_active) VALUES(#{isActive});";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 布尔值不在引号内时，应该直接使用
    assertTrue(result.contains("true"), "布尔值应该被正确替换");
  }

  @Test
  @DisplayName("测试空字符串处理")
  void testEmptyString() {
    variables.put("emptyField", "");
    String template = "INSERT INTO device_data(empty_field) VALUES('#{emptyField}');";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 空字符串应该被正确处理
    assertEquals("INSERT INTO device_data(empty_field) VALUES('');", result);
  }

  @Test
  @DisplayName("测试多个占位符在同一引号内")
  void testMultiplePlaceholdersInQuotes() {
    // 注意：这种情况在实际SQL中不常见，但测试边界情况
    String template = "SELECT '#{deviceId}_#{productKey}' as combined;";
    String result = processTemplate(template, variables);
    
    System.out.println("模板: " + template);
    System.out.println("结果: " + result);
    
    // 两个占位符都在同一个引号内，应该都被正确替换
    assertTrue(result.contains("nex123321"), "第一个占位符应该被替换");
    assertTrue(result.contains("Ru871cfJjhoM"), "第二个占位符应该被替换");
  }
}


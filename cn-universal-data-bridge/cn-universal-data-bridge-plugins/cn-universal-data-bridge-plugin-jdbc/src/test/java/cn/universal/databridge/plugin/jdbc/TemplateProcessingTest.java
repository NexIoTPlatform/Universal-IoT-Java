package cn.universal.databridge.plugin.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import cn.universal.common.constant.IoTConstant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 模板处理测试类 测试嵌套属性访问功能 */
public class TemplateProcessingTest {
  @Test
  public void testNestedPropertyJSON() {
    // 创建测试数据
    Map<String, Object> properties = new HashMap<>();
    properties.put("csq", "25");
    properties.put("code", "200");
    properties.put("temperature", "23.5");

    Map<String, Object> variables = new HashMap<>();
    variables.put("deviceId", "device001");
    variables.put("productKey", "product001");
    variables.put("deviceName", "测试设备");
    variables.put("iotId", "iot001");
    variables.put("deviceNode", IoTConstant.DeviceNode.DEVICE.toString());
    variables.put("messageType", "PROPERTIES");
    variables.put("properties", properties);
    variables.put("data", "raw_data_content");

    // 测试模板
    String template =
        """
          {
           "deviceId": "#{deviceId}",
           "location": {
             "city": "#{properties.code}",
             "deviceNode": "#{deviceNode}",
             "country": "#{properties.temperature}"
           }
         }
         """;

    // 创建插件实例进行测试
    DefaultJdbcOutPlugin plugin = new DefaultJdbcOutPlugin();

    // 使用反射调用受保护方法进行测试
    try {
      java.lang.reflect.Method method =
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      method.setAccessible(true);
      String result = (String) method.invoke(plugin, template, variables);

      System.out.println("原始模板:");
      System.out.println(template);
      System.out.println("\n处理结果:");
      System.out.println(result);

      // 验证结果
      assertTrue(result.contains("'device001'"));
      assertTrue(result.contains("'product001'"));
      assertTrue(result.contains("'测试设备'"));
      assertTrue(result.contains("'iot001'"));
      assertTrue(result.contains("'DEVICE'"));
      assertTrue(result.contains("'PROPERTIES'"));
      assertTrue(result.contains("'25'")); // properties.csq
      assertTrue(result.contains("'200'")); // properties.code
      assertTrue(result.contains("'23.5'")); // properties.temperature

      System.out.println("\n✅ 嵌套属性访问测试通过！");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testNestedPropertyAccess() {
    // 创建测试数据
    Map<String, Object> properties = new HashMap<>();
    properties.put("csq", "25");
    properties.put("code", "200");
    properties.put("temperature", "23.5");

    Map<String, Object> variables = new HashMap<>();
    variables.put("deviceId", "device001");
    variables.put("productKey", "product001");
    variables.put("deviceName", "测试设备");
    variables.put("iotId", "iot001");
    variables.put("deviceNode", "DEVICE");
    variables.put("messageType", "PROPERTIES");
    variables.put("properties", properties);
    variables.put("data", "raw_data_content");

    // 测试模板
    String template =
        "INSERT INTO device_data(device_id,product_key,device_name,iot_id,device_node,message_type,csq,code,temperature,properties,raw_data) "
            + "VALUES(#{deviceId},#{productKey},#{deviceName},#{iotId},#{deviceNode},#{messageType},#{properties.csq},#{properties.code},#{properties.temperature},#{properties},#{data});";

    // 创建插件实例进行测试
    DefaultJdbcOutPlugin plugin = new DefaultJdbcOutPlugin();

    // 使用反射调用受保护方法进行测试
    try {
      java.lang.reflect.Method method =
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      method.setAccessible(true);
      String result = (String) method.invoke(plugin, template, variables);

      System.out.println("原始模板:");
      System.out.println(template);
      System.out.println("\n处理结果:");
      System.out.println(result);

      // 验证结果
      assertTrue(result.contains("'device001'"));
      assertTrue(result.contains("'product001'"));
      assertTrue(result.contains("'测试设备'"));
      assertTrue(result.contains("'iot001'"));
      assertTrue(result.contains("'DEVICE'"));
      assertTrue(result.contains("'PROPERTIES'"));
      assertTrue(result.contains("'25'")); // properties.csq
      assertTrue(result.contains("'200'")); // properties.code
      assertTrue(result.contains("'23.5'")); // properties.temperature

      System.out.println("\n✅ 嵌套属性访问测试通过！");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testNestedPropertyWithNull() {
    // 测试嵌套属性为null的情况
    Map<String, Object> properties = new HashMap<>();
    properties.put("csq", "25");
    properties.put("code", null); // 故意设置为null
    properties.put("temperature", "23.5");

    Map<String, Object> variables = new HashMap<>();
    variables.put("deviceId", "device001");
    variables.put("properties", properties);

    String template =
        "INSERT INTO test_table(csq,code,temperature) VALUES(#{properties.csq},#{properties.code},#{properties.temperature});";

    DefaultJdbcOutPlugin plugin = new DefaultJdbcOutPlugin();

    try {
      java.lang.reflect.Method method =
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      method.setAccessible(true);
      String result = (String) method.invoke(plugin, template, variables);

      System.out.println("测试null值处理:");
      System.out.println("模板: " + template);
      System.out.println("结果: " + result);

      // 验证null值被转换为SQL的NULL
      assertTrue(result.contains("'25'"));
      assertTrue(result.contains("NULL")); // properties.code为null
      assertTrue(result.contains("'23.5'"));

      System.out.println("✅ null值处理测试通过！");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testNonExistentNestedProperty() {
    // 测试不存在的嵌套属性
    Map<String, Object> properties = new HashMap<>();
    properties.put("csq", "25");

    Map<String, Object> variables = new HashMap<>();
    variables.put("deviceId", "device001");
    variables.put("properties", properties);

    String template =
        "INSERT INTO test_table(csq,non_existent) VALUES(#{properties.csq},#{properties.nonExistent});";

    DefaultJdbcOutPlugin plugin = new DefaultJdbcOutPlugin();

    try {
      java.lang.reflect.Method method =
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      method.setAccessible(true);
      String result = (String) method.invoke(plugin, template, variables);

      System.out.println("测试不存在的嵌套属性:");
      System.out.println("模板: " + template);
      System.out.println("结果: " + result);

      // 验证不存在的属性被转换为SQL的NULL
      assertTrue(result.contains("'25'"));
      assertTrue(result.contains("NULL")); // 不存在的属性

      System.out.println("✅ 不存在属性处理测试通过！");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }
}

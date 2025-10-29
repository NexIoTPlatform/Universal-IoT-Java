package cn.universal.databridge.plugin.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 简单的模板语法测试 */
public class SimpleTemplateTest {

  @Test
  public void testSQLTemplate() {
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

    // 测试SQL模板
    String template =
        "INSERT INTO device_data(device_id,product_key,device_name,iot_id,device_node,message_type,csq,code,temperature,properties,raw_data) "
            + "VALUES(#{deviceId},#{productKey},#{deviceName},#{iotId},#{deviceNode},#{messageType},#{properties.csq},#{properties.code},#{properties.temperature},#{properties},#{data});";

    DefaultJdbcOutPlugin plugin = new DefaultJdbcOutPlugin();

    try {
      java.lang.reflect.Method method =
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      method.setAccessible(true);
      String result = (String) method.invoke(plugin, template, variables);

      System.out.println("SQL模板测试:");
      System.out.println("原始模板: " + template);
      System.out.println("处理结果: " + result);

      // 验证SQL结果
      assertTrue(result.contains("'device001'"));
      assertTrue(result.contains("'product001'"));
      assertTrue(result.contains("'测试设备'"));
      assertTrue(result.contains("'iot001'"));
      assertTrue(result.contains("'DEVICE'"));
      assertTrue(result.contains("'PROPERTIES'"));
      assertTrue(result.contains("'25'")); // properties.csq
      assertTrue(result.contains("'200'")); // properties.code
      assertTrue(result.contains("'23.5'")); // properties.temperature

      System.out.println("✅ SQL模板处理测试通过！");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }
}

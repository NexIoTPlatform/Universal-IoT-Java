package cn.universal.databridge.plugin.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import cn.universal.databridge.entity.DataBridgeConfig;
import cn.universal.databridge.entity.PluginInfo;
import cn.universal.databridge.entity.ResourceConnection;
import cn.universal.databridge.plugin.AbstractDataBridgePlugin;
import cn.universal.databridge.plugin.SourceScope;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 快速模板测试 - 验证通用模板处理功能 */
public class QuickTemplateTest {

  @Test
  public void testGenericVsJdbcProcessing() {
    // 创建测试数据
    Map<String, Object> properties = new HashMap<>();
    properties.put("csq", "25");
    properties.put("code", "200");
    properties.put("temperature", "23.5");

    Map<String, Object> variables = new HashMap<>();
    variables.put("deviceId", "device001");
    variables.put("properties", properties);

    String template = "Device: #{deviceId}, CSQ: #{properties.csq}, Code: #{properties.code}";

    System.out.println("=== 通用 vs JDBC 模板处理对比 ===");
    System.out.println("原始模板: " + template);

    try {
      // 测试通用实现
      AbstractDataBridgePlugin genericPlugin =
          new AbstractDataBridgePlugin() {
            @Override
            public PluginInfo getPluginInfo() {
              return null;
            }

            @Override
            public Boolean testConnection(ResourceConnection connection) {
              return null;
            }

            @Override
            public Boolean validateConfig(DataBridgeConfig config) {
              return null;
            }

            @Override
            public List<SourceScope> getSupportedSourceScopes() {
              return List.of();
            }
          };
      java.lang.reflect.Method genericMethod =
          AbstractDataBridgePlugin.class.getDeclaredMethod(
              "processTemplate", String.class, Map.class);
      genericMethod.setAccessible(true);
      String genericResult = (String) genericMethod.invoke(genericPlugin, template, variables);

      System.out.println("通用处理结果: " + genericResult);

      // 测试JDBC实现
      DefaultJdbcOutPlugin jdbcPlugin = new DefaultJdbcOutPlugin();
      java.lang.reflect.Method jdbcMethod =
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      jdbcMethod.setAccessible(true);
      String jdbcResult = (String) jdbcMethod.invoke(jdbcPlugin, template, variables);

      System.out.println("JDBC处理结果: " + jdbcResult);

      // 验证通用实现 - 直接字符串转换，无单引号
      assertTrue(genericResult.contains("device001"));
      assertTrue(genericResult.contains("25"));
      assertTrue(genericResult.contains("200"));
      assertFalse(genericResult.contains("'device001'")); // 不应该有单引号

      // 验证JDBC实现 - SQL安全转换，有单引号
      assertTrue(jdbcResult.contains("'device001'")); // 应该有单引号
      assertTrue(jdbcResult.contains("'25'")); // 应该有单引号
      assertTrue(jdbcResult.contains("'200'")); // 应该有单引号

      System.out.println("✅ 通用 vs JDBC 处理对比测试通过！");
      System.out.println("通用实现: 直接字符串转换，适合JSON等非SQL场景");
      System.out.println("JDBC实现: SQL安全转换，适合SQL场景");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }

  @Test
  public void testNestedPropertyAccess() {
    Map<String, Object> level2 = new HashMap<>();
    level2.put("value", "nested_value");

    Map<String, Object> level1 = new HashMap<>();
    level1.put("level2", level2);

    Map<String, Object> variables = new HashMap<>();
    variables.put("root", level1);

    String template = "Nested: #{root.level2.value}";

    AbstractDataBridgePlugin plugin =
        new AbstractDataBridgePlugin() {
          @Override
          public PluginInfo getPluginInfo() {
            return null;
          }

          @Override
          public Boolean testConnection(ResourceConnection connection) {
            return null;
          }

          @Override
          public Boolean validateConfig(DataBridgeConfig config) {
            return null;
          }

          @Override
          public List<SourceScope> getSupportedSourceScopes() {
            return List.of();
          }
        };

    try {
      java.lang.reflect.Method method =
          AbstractDataBridgePlugin.class.getDeclaredMethod(
              "processTemplate", String.class, Map.class);
      method.setAccessible(true);
      String result = (String) method.invoke(plugin, template, variables);

      System.out.println("=== 嵌套属性访问测试 ===");
      System.out.println("模板: " + template);
      System.out.println("结果: " + result);

      assertEquals("Nested: nested_value", result);
      System.out.println("✅ 嵌套属性访问测试通过！");

    } catch (Exception e) {
      fail("测试失败: " + e.getMessage());
    }
  }
}

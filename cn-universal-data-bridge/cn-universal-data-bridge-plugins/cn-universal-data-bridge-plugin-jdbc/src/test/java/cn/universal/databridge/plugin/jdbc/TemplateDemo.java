package cn.universal.databridge.plugin.jdbc;

import cn.universal.databridge.entity.DataBridgeConfig;
import cn.universal.databridge.entity.PluginInfo;
import cn.universal.databridge.entity.ResourceConnection;
import cn.universal.databridge.plugin.AbstractDataBridgePlugin;
import cn.universal.databridge.plugin.SourceScope;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 模板处理演示程序 */
public class TemplateDemo {

  public static void main(String[] args) {
    System.out.println("=== 模板处理功能演示 ===\n");

    // 创建测试数据
    Map<String, Object> properties = new HashMap<>();
    properties.put("csq", "25");
    properties.put("code", "200");
    properties.put("temperature", "23.5");
    properties.put("location", Map.of("city", "Beijing", "country", "China"));

    Map<String, Object> variables = new HashMap<>();
    variables.put("deviceId", "device001");
    variables.put("productKey", "product001");
    variables.put("deviceName", "测试设备");
    variables.put("properties", properties);

    // 测试模板
    String template = 
        "Device: #{deviceId}, Product: #{productKey}, Name: #{deviceName}, " +
        "CSQ: #{properties.csq}, Code: #{properties.code}, " +
        "City: #{properties.location.city}, Country: #{properties.location.country}";

    System.out.println("原始模板:");
    System.out.println(template);
    System.out.println();

    try {
      // 1. 通用模板处理
      System.out.println("1. 通用模板处理 (AbstractDataBridgePlugin):");
      AbstractDataBridgePlugin genericPlugin = new AbstractDataBridgePlugin() {
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
          AbstractDataBridgePlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      genericMethod.setAccessible(true);
      String genericResult = (String) genericMethod.invoke(genericPlugin, template, variables);
      System.out.println("结果: " + genericResult);
      System.out.println("特点: 直接字符串转换，无单引号，适合JSON等非SQL场景");
      System.out.println();

      // 2. JDBC专用模板处理
      System.out.println("2. JDBC专用模板处理 (DefaultJdbcOutPlugin):");
      DefaultJdbcOutPlugin jdbcPlugin = new DefaultJdbcOutPlugin();
      java.lang.reflect.Method jdbcMethod = 
          DefaultJdbcOutPlugin.class.getDeclaredMethod("processTemplate", String.class, Map.class);
      jdbcMethod.setAccessible(true);
      String jdbcResult = (String) jdbcMethod.invoke(jdbcPlugin, template, variables);
      System.out.println("结果: " + jdbcResult);
      System.out.println("特点: SQL安全转换，有单引号，防止SQL注入");
      System.out.println();

      // 3. 对比分析
      System.out.println("3. 对比分析:");
      System.out.println("通用实现 vs JDBC实现:");
      System.out.println("- 通用: device001 → device001 (无引号)");
      System.out.println("- JDBC: device001 → 'device001' (有引号)");
      System.out.println();
      System.out.println("- 通用: 25 → 25 (数字无引号)");
      System.out.println("- JDBC: 25 → '25' (数字有引号)");
      System.out.println();
      System.out.println("- 通用: Beijing → Beijing (字符串无引号)");
      System.out.println("- JDBC: Beijing → 'Beijing' (字符串有引号)");
      System.out.println();

      // 4. 使用场景建议
      System.out.println("4. 使用场景建议:");
      System.out.println("通用实现适用于:");
      System.out.println("- JSON模板: {\"deviceId\": \"#{deviceId}\"}");
      System.out.println("- XML模板: <device id=\"#{deviceId}\">");
      System.out.println("- 文本模板: Device #{deviceId} is online");
      System.out.println();
      System.out.println("JDBC实现适用于:");
      System.out.println("- SQL模板: INSERT INTO devices(id, name) VALUES(#{deviceId}, #{deviceName})");
      System.out.println("- 需要SQL注入防护的场景");
      System.out.println();

      System.out.println("✅ 演示完成！");

    } catch (Exception e) {
      System.err.println("演示失败: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

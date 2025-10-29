package cn.universal.databridge.plugin.jdbc;

import cn.universal.databridge.entity.DataBridgeConfig;
import cn.universal.databridge.entity.PluginInfo;
import cn.universal.databridge.entity.ResourceConnection;
import cn.universal.databridge.plugin.AbstractDataOutputPlugin;
import cn.universal.databridge.plugin.SourceScope;
import cn.universal.databridge.util.DataBridgeConnectionManager;
import cn.universal.databridge.util.SqlValueConverter;
import cn.universal.persistence.base.BaseUPRequest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 默认JDBC数据桥接插件 - 输出方向（优化版） */
@Component("defaultJdbcOutPlugin")
@ConditionalOnMissingBean(name = "jdbcOutPlugin")
@Slf4j
public class DefaultJdbcOutPlugin extends AbstractDataOutputPlugin {

  // 数据桥接连接池管理器 - 独立管理，避免与框架冲突
  private final DataBridgeConnectionManager connectionManager = new DataBridgeConnectionManager();

  @Override
  public PluginInfo getPluginInfo() {
    return PluginInfo.builder()
        .name("默认JDBC数据桥接插件")
        .version("2.1.0") // 升级版本号
        .description("默认的JDBC数据桥接实现，支持安全的模板变量替换")
        .author("gitee.com/NexIoT")
        .pluginType("JDBC")
        .supportedResourceTypes(List.of("MYSQL", "POSTGRESQL", "H2", "ORACLE", "SQLSERVER"))
        .dataDirection(PluginInfo.DataDirection.OUTPUT)
        .category("数据库")
        .icon("database")
        .build();
  }

  @Override
  public Boolean testConnection(ResourceConnection connection) {
    try {
      DataSource dataSource = connectionManager.getOrCreateDataSource(connection);
      try (Connection conn = dataSource.getConnection()) {
        return conn.isValid(5);
      }
    } catch (Exception e) {
      log.error("JDBC连接测试失败: {}", e.getMessage(), e);
      return false;
    }
  }

  @Override
  public Boolean validateConfig(DataBridgeConfig config) {
    if (config == null) {
      return false;
    }
    // 验证模板中的占位符格式（可选，增强健壮性）
    if (config.getTemplate() != null
        && config.getTemplate().contains(LEFT_PLACEHOLDER_PREFIX)
        && !config.getTemplate().contains(RIGHT_PLACEHOLDER_SUFFIX)) {
      log.error("模板中存在未闭合的占位符: {}", config.getTemplate());
      return false;
    }
    return true;
  }

  @Override
  public List<SourceScope> getSupportedSourceScopes() {
    return List.of(
        SourceScope.ALL_PRODUCTS, SourceScope.SPECIFIC_PRODUCTS, SourceScope.APPLICATION);
  }

  @Override
  protected void processProcessedData(
      Object processedData,
      BaseUPRequest request,
      DataBridgeConfig config,
      ResourceConnection connection) {
    try {
      DataSource dataSource = connectionManager.getOrCreateDataSource(connection);
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

      List<String> sqlList = generateExecutableSqlList(processedData, config);

      if (sqlList.size() > 1) {
        jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]));
      } else if (sqlList.size() == 1) {
        jdbcTemplate.update(sqlList.get(0));
      }

    } catch (Exception e) {
      log.error("处理Magic脚本返回数据失败: {}", e.getMessage());
      throw new RuntimeException("处理Magic脚本返回数据失败: " + e.getMessage(), e);
    }
  }

  @Override
  protected void processTemplateResult(
      String templateResult,
      BaseUPRequest request,
      DataBridgeConfig config,
      ResourceConnection connection) {
    
    try {
      // 获取数据源
      DataSource dataSource = connectionManager.getOrCreateDataSource(connection);
      
      // 使用Spring的JdbcTemplate执行SQL
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      jdbcTemplate.update(templateResult);
      
      
    } catch (Exception e) {
      log.error("执行SQL失败: {} - {}", templateResult, e.getMessage());
      
      // 清除可能损坏的连接池
      connectionManager.removeDataSource(connection);
      
      throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
    }
  }

  private List<String> generateExecutableSqlList(Object processedData, DataBridgeConfig config) {
    List<String> sqlList = new ArrayList<>();

    try {
      if (processedData instanceof String) {
        sqlList.add((String) processedData);
      } else if (processedData instanceof List) {
        @SuppressWarnings("unchecked")
        List<Object> dataList = (List<Object>) processedData;
        for (Object item : dataList) {
          if (item instanceof String) {
            sqlList.add((String) item);
          } else if (item instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) item;
            sqlList.add(processTemplate(config.getTemplate(), dataMap));
          }
        }
      } else if (processedData instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) processedData;
        sqlList.add(processTemplate(config.getTemplate(), dataMap));
      } else {
        log.warn(
            "Magic脚本返回的数据类型不支持: {}",
            processedData != null ? processedData.getClass().getSimpleName() : "null");
      }

    } catch (Exception e) {
      log.error("处理Magic脚本返回数据失败: {}", e.getMessage());
      throw new RuntimeException("处理Magic脚本返回数据失败: " + e.getMessage(), e);
    }

    return sqlList;
  }

  // 使用正则表达式匹配所有 #{...} 占位符，支持嵌套属性
  protected String processTemplate(String template, Map<String, Object> variables) {
    if (template == null || template.trim().isEmpty()) {
      log.warn("模板为空，无法处理");
      return "";
    }
    String result = template;
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#\\{([a-zA-Z0-9_.]+)\\}");
    java.util.regex.Matcher matcher = pattern.matcher(template);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String paramPath = matcher.group(1); // 获取变量路径，如 "properties.csq"
      Object value = getNestedValue(variables, paramPath);
      String sqlValue = SqlValueConverter.convertToSqlJsonValue(value);
      matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(sqlValue));
    }
    matcher.appendTail(sb);
    result = sb.toString();

    return result;
  }


}

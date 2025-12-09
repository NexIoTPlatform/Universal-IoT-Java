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
      javax.sql.DataSource dataSource = connectionManager.getOrCreateDataSource(connection);
      org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(dataSource);

      // 构建变量并进行参数化模板解析（避免引号问题，兼容多方言）
      java.util.Map<String, Object> variables = buildTemplateVariables(request, parseConfig(config));
      cn.universal.databridge.engine.ParamTemplateEngine engine = new cn.universal.databridge.engine.ParamTemplateEngine();
      cn.universal.databridge.engine.SqlDialectAdapter adapter = getAdapter(connection.getType());
      cn.universal.databridge.engine.ParamSql ps = engine.process(config.getTemplate(), variables, adapter);

      jdbcTemplate.update(ps.getSql(), ps.getParams().toArray());
    } catch (Exception e) {
      log.error("执行SQL失败: {} - {}", templateResult, e.getMessage());
      connectionManager.removeDataSource(connection);
      throw new RuntimeException("SQL执行失败: " + e.getMessage(), e);
    }
  }

  private cn.universal.databridge.engine.SqlDialectAdapter getAdapter(cn.universal.databridge.entity.ResourceConnection.ResourceType type) {
    switch (type) {
      case MYSQL:
      case H2:
        return new cn.universal.databridge.engine.dialect.MySqlDialectAdapter();
      case POSTGRESQL:
        return new cn.universal.databridge.engine.dialect.PostgresDialectAdapter();
      case ORACLE:
        return new cn.universal.databridge.engine.dialect.OracleDialectAdapter();
      case SQLSERVER:
        return new cn.universal.databridge.engine.dialect.SqlServerDialectAdapter();
      default:
        return new cn.universal.databridge.engine.dialect.MySqlDialectAdapter();
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
  // 智能处理引号，兼容MySQL和PostgreSQL
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
      
      // 检测占位符在模板中的上下文，判断是否已经在引号内
      int start = matcher.start();
      int end = matcher.end();
      boolean inSingleQuotes = isInQuotes(template, start, end, '\'');
      boolean inDoubleQuotes = isInQuotes(template, start, end, '"');
      
      String sqlValue;
      if (inSingleQuotes) {
        // 如果已经在单引号内，只替换值，不添加引号，但需要转义单引号
        sqlValue = convertValueForQuotedContext(value, true);
      } else if (inDoubleQuotes) {
        // 如果已经在双引号内，只替换值，不添加引号，但需要转义双引号
        sqlValue = convertValueForQuotedContext(value, false);
      } else {
        // 如果不在引号内，使用标准的SQL值转换（添加引号）
        sqlValue = SqlValueConverter.convertToSqlValue(value);
      }
      
      matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(sqlValue));
    }
    matcher.appendTail(sb);
    result = sb.toString();

    return result;
  }

  /**
   * 检测占位符是否在引号内
   * @param template 模板字符串
   * @param start 占位符开始位置
   * @param end 占位符结束位置
   * @param quoteChar 引号字符（' 或 "）
   * @return 是否在引号内
   */
  private boolean isInQuotes(String template, int start, int end, char quoteChar) {
    // 向前查找最近的未转义引号
    int quoteBefore = -1;
    for (int i = start - 1; i >= 0; i--) {
      if (template.charAt(i) == quoteChar) {
        // 检查是否被转义（考虑连续的反斜杠）
        int backslashCount = 0;
        for (int j = i - 1; j >= 0 && template.charAt(j) == '\\'; j--) {
          backslashCount++;
        }
        // 如果反斜杠数量是偶数，说明引号未被转义
        if (backslashCount % 2 == 0) {
          quoteBefore = i;
          break;
        }
      }
    }
    
    if (quoteBefore == -1) {
      return false;
    }
    
    // 向后查找匹配的未转义引号
    int quoteAfter = -1;
    for (int i = end; i < template.length(); i++) {
      if (template.charAt(i) == quoteChar) {
        // 检查是否被转义
        int backslashCount = 0;
        for (int j = i - 1; j >= 0 && template.charAt(j) == '\\'; j--) {
          backslashCount++;
        }
        // 如果反斜杠数量是偶数，说明引号未被转义
        if (backslashCount % 2 == 0) {
          quoteAfter = i;
          break;
        }
      }
    }
    
    // 如果找到了匹配的引号对，说明在引号内
    return quoteAfter != -1 && quoteBefore < start && quoteAfter >= end;
  }

  /**
   * 为已在引号内的上下文转换值
   * @param value 要转换的值
   * @param isSingleQuote 是否是单引号上下文
   * @return 转换后的值（不包含引号，但已转义）
   */
  private String convertValueForQuotedContext(Object value, boolean isSingleQuote) {
    if (value == null) {
      return "NULL";
    }
    
    String quoteChar = isSingleQuote ? "'" : "\"";
    String escapeChar = isSingleQuote ? "''" : "\\\"";
    
    if (value instanceof String) {
      String strValue = (String) value;
      // 转义引号
      return strValue.replace(quoteChar, escapeChar);
    } else if (value instanceof Number || value instanceof Boolean) {
      // 数字和布尔类型直接转换
      return value.toString();
    } else if (value instanceof java.util.Date) {
      // 日期类型转换为SQL标准格式
      return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
    } else {
      // 其他类型：转为JSON字符串并转义引号
      String jsonValue = cn.hutool.json.JSONUtil.toJsonStr(value);
      return jsonValue.replace(quoteChar, escapeChar);
    }
  }


}

package cn.universal.plugins.protocolapi.datasource.web;

import cn.universal.plugins.protocolapi.core.config.MagicConfiguration;
import cn.universal.plugins.protocolapi.core.model.JsonBean;
import cn.universal.plugins.protocolapi.core.web.MagicController;
import cn.universal.plugins.protocolapi.core.web.MagicExceptionHandler;
import cn.universal.plugins.protocolapi.datasource.model.DataSourceInfo;
import cn.universal.plugins.protocolapi.utils.JdbcUtils;
import java.sql.Connection;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class MagicDataSourceController extends MagicController implements MagicExceptionHandler {

  public MagicDataSourceController(MagicConfiguration configuration) {
    super(configuration);
  }

  @RequestMapping("/datasource/jdbc/test")
  @ResponseBody
  public JsonBean<String> test(@RequestBody DataSourceInfo properties) {
    try {
      Connection connection =
          JdbcUtils.getConnection(
              properties.getDriverClassName(),
              properties.getUrl(),
              properties.getUsername(),
              properties.getPassword());
      JdbcUtils.close(connection);
    } catch (Exception e) {
      return new JsonBean<>(e.getMessage());
    }
    return new JsonBean<>("ok");
  }
}

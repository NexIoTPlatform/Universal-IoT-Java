package cn.universal.plugins.protocolapi.modules.db.inteceptor;

import cn.universal.plugins.protocolapi.modules.db.model.SqlMode;
import cn.universal.plugins.protocolapi.modules.db.table.NamedTable;

/**
 * 单表模块拦截器
 *
 * @since 1.5.3
 */
public interface NamedTableInterceptor {

  /** 执行之前 */
  void preHandle(SqlMode sqlMode, NamedTable namedTable);
}

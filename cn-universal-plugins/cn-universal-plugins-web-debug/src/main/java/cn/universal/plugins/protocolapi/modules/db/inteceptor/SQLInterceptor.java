package cn.universal.plugins.protocolapi.modules.db.inteceptor;

import cn.universal.plugins.protocolapi.core.context.RequestEntity;
import cn.universal.plugins.protocolapi.modules.db.BoundSql;

/**
 * SQL 拦截器
 *
 * @author mxd
 */
public interface SQLInterceptor {

  /**
   * 1.1.1 新增
   *
   * @param boundSql SQL信息
   * @param requestEntity 请求信息
   * @since 1.1.1
   */
  default void preHandle(BoundSql boundSql, RequestEntity requestEntity) {}

  /**
   * @param boundSql SQL信息
   * @param result 执行结果
   * @param requestEntity 请求信息
   * @since 1.7.2
   */
  default Object postHandle(BoundSql boundSql, Object result, RequestEntity requestEntity) {
    return result;
  }

  /**
   * @param boundSql SQL信息
   * @param throwable 异常信息
   * @param requestEntity 请求信息
   * @since 2.1.0
   */
  default void handleException(
      BoundSql boundSql, Throwable throwable, RequestEntity requestEntity) {}
}

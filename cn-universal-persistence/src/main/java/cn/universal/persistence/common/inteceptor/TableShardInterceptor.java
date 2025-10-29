/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.common.inteceptor;

import cn.hutool.extra.spring.SpringUtil;
import cn.universal.persistence.consistent.ITableShardingStrategy;
import cn.universal.persistence.consistent.TableShard;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

@Component
@Intercepts({
  @Signature(
      type = StatementHandler.class,
      method = "prepare",
      args = {Connection.class, Integer.class})
})
public class TableShardInterceptor implements Interceptor {

  private static final ReflectorFactory defaultReflectorFactory = new DefaultReflectorFactory();

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    MetaObject metaObject = getMetaObject(invocation);
    BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
    MappedStatement mappedStatement =
        (MappedStatement) metaObject.getValue("delegate.mappedStatement");

    // 获取分表注解
    TableShard tableShard = getTableShard(mappedStatement);

    // 如果method与class都没有TableShard注解或执行方法不存在，执行下一个插件逻辑
    if (tableShard == null) {
      return invocation.proceed();
    }

    // 获取值
    String value = tableShard.value();
    // value是否字段名，如果是，需要解析请求参数字段名的值
    boolean fieldFlag = tableShard.fieldFlag();

    // 如果value为空，说明不需要分表，直接执行
    if (value == null || value.trim().isEmpty()) {
      return invocation.proceed();
    }

    if (fieldFlag) {
      // 获取请求参数
      Object parameterObject = boundSql.getParameterObject();

      if (parameterObject instanceof MapperMethod.ParamMap) { // ParamMap类型逻辑处理

        MapperMethod.ParamMap parameterMap = (MapperMethod.ParamMap) parameterObject;
        // 根据字段名获取参数值
        Object valueObject = parameterMap.get(value);
        if (valueObject == null) {
          // 如果找不到参数，直接执行，不进行分表
          return invocation.proceed();
        }
        // 替换sql
        replaceSql(tableShard, valueObject, metaObject, boundSql);

      } else { // 单参数逻辑

        // 如果是基础类型抛出异常
        if (isBaseType(parameterObject)) {
          throw new RuntimeException("单参数非法，请使用@Param注解");
        }

        if (parameterObject instanceof Map) {
          Map<String, Object> parameterMap = (Map<String, Object>) parameterObject;
          Object valueObject = parameterMap.get(value);
          if (valueObject == null) {
            // 如果找不到参数，直接执行，不进行分表
            return invocation.proceed();
          }
          // 替换sql
          replaceSql(tableShard, valueObject, metaObject, boundSql);
        } else {
          // 非基础类型对象
          try {
            Class<?> parameterObjectClass = parameterObject.getClass();
            Field declaredField = parameterObjectClass.getDeclaredField(value);
            declaredField.setAccessible(true);
            Object valueObject = declaredField.get(parameterObject);
            // 替换sql
            replaceSql(tableShard, valueObject, metaObject, boundSql);
          } catch (NoSuchFieldException e) {
            // 如果找不到字段，直接执行，不进行分表
            return invocation.proceed();
          }
        }
      }

    } else { // 无需处理parameterField
      // 替换sql
      replaceSql(tableShard, value, metaObject, boundSql);
    }
    // 执行下一个插件逻辑
    return invocation.proceed();
  }

  @Override
  public Object plugin(Object o) {
    // 当目标类是StatementHandler类型时，才包装目标类，否者直接返回目标本身, 减少目标被代理的次数
    if (o instanceof StatementHandler) {
      return Plugin.wrap(o, this);
    } else {
      return o;
    }
  }

  @Override
  public void setProperties(Properties properties) {}

  /** 基本数据类型验证，true是，false否 */
  private boolean isBaseType(Object object) {
    if (object.getClass().isPrimitive()
        || object instanceof String
        || object instanceof Integer
        || object instanceof Double
        || object instanceof Float
        || object instanceof Long
        || object instanceof Boolean
        || object instanceof Byte
        || object instanceof Short) {
      return true;
    } else {
      return false;
    }
  }

  /** 替换sql */
  private void replaceSql(
      TableShard tableShard, Object value, MetaObject metaObject, BoundSql boundSql) {
    String tableNamePrefix = tableShard.tableNamePrefix();
    // 获取策略class
    Class<? extends ITableShardingStrategy> strategyClazz = tableShard.shardStrategy();
    // 从spring ioc容器获取策略类

    ITableShardingStrategy tableShardStrategy = SpringUtil.getBean(strategyClazz);
    // 生成分表名
    String shardTableName = tableShardStrategy.generateTableName(tableNamePrefix, value);
    // 获取sql
    String sql = boundSql.getSql();
    // 完成表名替换
    metaObject.setValue("delegate.boundSql.sql", sql.replaceAll(tableNamePrefix, shardTableName));
  }

  /** 获取MetaObject对象-mybatis里面提供的一个工具类，类似反射的效果 */
  private MetaObject getMetaObject(Invocation invocation) {
    StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
    // MetaObject是mybatis里面提供的一个工具类，类似反射的效果
    MetaObject metaObject =
        MetaObject.forObject(
            statementHandler,
            SystemMetaObject.DEFAULT_OBJECT_FACTORY,
            SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
            defaultReflectorFactory);

    return metaObject;
  }

  /** 获取分表注解 - 修复版本 */
  private TableShard getTableShard(MappedStatement mappedStatement) throws ClassNotFoundException {
    String id = mappedStatement.getId();
    // 获取Class
    final String className = id.substring(0, id.lastIndexOf("."));
    // 获取方法名
    final String methodName = id.substring(id.lastIndexOf(".") + 1);

    // 获取Mapper接口类
    Class<?> mapperClass = Class.forName(className);

    // 分表注解
    TableShard tableShard = null;

    // 先查找方法上的注解
    try {
      Method[] methods = mapperClass.getMethods();
      for (Method method : methods) {
        if (method.getName().equals(methodName)) {
          tableShard = method.getAnnotation(TableShard.class);
          if (tableShard != null) {
            return tableShard; // 找到方法上的注解，直接返回
          }
        }
      }
    } catch (Exception e) {
      // 忽略异常，继续查找类上的注解
    }

    // 如果方法没有设置注解，从Mapper接口上面获取TableShard注解
    tableShard = mapperClass.getAnnotation(TableShard.class);

    return tableShard;
  }
}

/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.cache.aspect;

import cn.universal.cache.annotation.MultiLevelCacheable;
import cn.universal.cache.config.MultiLevelCacheProperties;
import cn.universal.cache.impl.MultiLevelCache;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 多级缓存切面
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j
@Aspect
@Component
public class MultiLevelCacheAspect {

  @Autowired private CacheManager cacheManager;

  @Autowired private MultiLevelCacheProperties properties;

  private final ExpressionParser parser = new SpelExpressionParser();
  private final KeyGenerator keyGenerator = new SimpleKeyGenerator();

  @Around("@annotation(multiLevelCacheable)")
  public Object around(ProceedingJoinPoint joinPoint, MultiLevelCacheable multiLevelCacheable)
      throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    // 解析缓存名称
    String[] cacheNames = multiLevelCacheable.cacheNames();
    if (cacheNames.length == 0) {
      log.warn("缓存名称未配置: {}", method.getName());
      return joinPoint.proceed();
    }

    // 解析缓存键
    String key = resolveKey(multiLevelCacheable, method, joinPoint.getArgs());
    if (key == null) {
      log.warn("缓存键解析失败: {}", method.getName());
      return joinPoint.proceed();
    }

    // 检查条件
    if (!evaluateCondition(multiLevelCacheable.condition(), method, joinPoint.getArgs())) {
      log.debug("缓存条件不满足，跳过缓存: {}", method.getName());
      return joinPoint.proceed();
    }

    // 获取缓存
    Cache cache = cacheManager.getCache(cacheNames[0]);
    if (cache == null) {
      log.warn("缓存未找到: {}", cacheNames[0]);
      return joinPoint.proceed();
    }

    // 尝试从缓存获取
    Cache.ValueWrapper cachedValue = cache.get(key);
    if (cachedValue != null) {
      log.debug("缓存命中: {} -> {}", key, cacheNames[0]);
      return cachedValue.get();
    }

    // 缓存未命中，执行方法
    log.debug("缓存未命中，执行方法: {} -> {}", key, cacheNames[0]);
    Object result = joinPoint.proceed();

    // 检查排除条件
    if (result != null
        && !evaluateUnless(multiLevelCacheable.unless(), method, joinPoint.getArgs(), result)) {
      // 设置缓存过期时间（如果支持）
      setCacheExpiration(cache, key, multiLevelCacheable);

      // 写入缓存
      cache.put(key, result);
      log.debug("缓存写入: {} -> {}", key, cacheNames[0]);
    }

    return result;
  }

  /** 解析缓存键 */
  private String resolveKey(MultiLevelCacheable annotation, Method method, Object[] args) {
    String key = annotation.key();
    String keyGeneratorName = annotation.keyGenerator();

    if (StringUtils.hasText(key)) {
      // 使用SpEL表达式
      return evaluateExpression(key, method, args, String.class);
    } else if (StringUtils.hasText(keyGeneratorName)) {
      // 使用键生成器
      return keyGeneratorName + ":" + keyGenerator.generate(null, method, args);
    } else {
      // 使用默认键生成器
      return keyGenerator.generate(null, method, args).toString();
    }
  }

  /** 评估条件表达式 */
  private boolean evaluateCondition(String condition, Method method, Object[] args) {
    if (!StringUtils.hasText(condition)) {
      return true;
    }

    Boolean result = evaluateExpression(condition, method, args, Boolean.class);
    return result != null && result;
  }

  /** 评估排除条件表达式 */
  private boolean evaluateUnless(String unless, Method method, Object[] args, Object result) {
    if (!StringUtils.hasText(unless)) {
      return false;
    }

    Boolean resultValue = evaluateExpression(unless, method, args, result, Boolean.class);
    return resultValue != null && resultValue;
  }

  /** 评估SpEL表达式 */
  private <T> T evaluateExpression(
      String expression, Method method, Object[] args, Class<T> expectedType) {
    return evaluateExpression(expression, method, args, null, expectedType);
  }

  /** 评估SpEL表达式（带结果） */
  private <T> T evaluateExpression(
      String expression, Method method, Object[] args, Object result, Class<T> expectedType) {
    try {
      Expression exp = parser.parseExpression(expression);
      EvaluationContext context = new StandardEvaluationContext();

      // 设置方法参数
      String[] paramNames = getParameterNames(method);
      for (int i = 0; i < args.length && i < paramNames.length; i++) {
        context.setVariable(paramNames[i], args[i]);
      }

      // 设置结果
      if (result != null) {
        context.setVariable("result", result);
      }

      return exp.getValue(context, expectedType);
    } catch (Exception e) {
      log.error("表达式评估失败: {}", expression, e);
      return null;
    }
  }

  /** 获取方法参数名 */
  private String[] getParameterNames(Method method) {
    String[] names = new String[method.getParameterCount()];
    for (int i = 0; i < names.length; i++) {
      names[i] = "p" + i;
    }
    return names;
  }

  /** 设置缓存过期时间 */
  private void setCacheExpiration(Cache cache, Object key, MultiLevelCacheable annotation) {
    // 这里可以根据具体的缓存实现来设置过期时间
    // 由于Spring Cache接口没有直接提供设置过期时间的方法，
    // 需要在具体的缓存实现中处理
    if (cache instanceof MultiLevelCache) {
      MultiLevelCache multiLevelCache = (MultiLevelCache) cache;

      // 可以通过反射或其他方式设置过期时间
      // 这里暂时跳过，因为需要在缓存实现中支持
      log.debug("缓存过期时间设置: L1={}s, L2={}s", annotation.l1Expire(), annotation.l2Expire());
    }
  }
}

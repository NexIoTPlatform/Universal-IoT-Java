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
package cn.universal.dm.device.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Redisson 配置类 用于配置分布式锁和 Redis 连接
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j
@Configuration
public class RedissonConfig {

  @Value("${spring.data.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.data.redis.port:6379}")
  private int redisPort;

  @Value("${spring.data.redis.password:}")
  private String redisPassword;

  @Value("${spring.data.redis.database:0}")
  private int redisDatabase;

  @Value("${spring.data.redis.timeout:3000}")
  private int redisTimeout;

  @Value("${spring.data.redis.lettuce.pool.max-active:8}")
  private int maxActive;

  @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
  private int maxIdle;

  @Value("${spring.data.redis.lettuce.pool.min-idle:0}")
  private int minIdle;

  @Value("${spring.data.redis.lettuce.pool.max-wait:-1}")
  private long maxWait;

  // Redisson 特定配置
  @Value("${redisson.lock.watchdog-timeout:30000}")
  private int lockWatchdogTimeout;

  @Value("${redisson.lock.wait-time:100}")
  private int lockWaitTime;

  @Value("${redisson.lock.lease-time:30}")
  private int lockLeaseTime;

  @Value("${redisson.connection-pool.size:8}")
  private int redissonPoolSize;

  @Value("${redisson.connection-pool.min-idle:0}")
  private int redissonMinIdle;

  @Value("${redisson.connection.timeout:3000}")
  private int redissonTimeout;

  @Value("${redisson.connection.retry-attempts:3}")
  private int redissonRetryAttempts;

  @Value("${redisson.connection.retry-interval:1500}")
  private int redissonRetryInterval;

  /** 配置 Redisson 客户端 */
  @Bean
  @Primary
  public RedissonClient redissonClient() {
    try {
      Config config = new Config();

      // 单机模式配置
      config
          .useSingleServer()
          .setAddress("redis://" + redisHost + ":" + redisPort)
          .setDatabase(redisDatabase)
          .setConnectionPoolSize(redissonPoolSize)
          .setConnectionMinimumIdleSize(redissonMinIdle)
          .setConnectTimeout(redissonTimeout)
          .setIdleConnectionTimeout(redissonTimeout)
          .setRetryAttempts(redissonRetryAttempts)
          .setRetryInterval(redissonRetryInterval)
          .setKeepAlive(true)
          .setTcpNoDelay(true);

      // 如果设置了密码
      if (redisPassword != null && !redisPassword.trim().isEmpty()) {
        config.useSingleServer().setPassword(redisPassword);
      }

      // 分布式锁配置
      config.setLockWatchdogTimeout(lockWatchdogTimeout); // 看门狗超时时间

      RedissonClient redissonClient = Redisson.create(config);
      log.info("Redisson 客户端初始化成功: {}:{}", redisHost, redisPort);

      return redissonClient;

    } catch (Exception e) {
      log.error("Redisson 客户端初始化失败: {}:{}, error={}", redisHost, redisPort, e.getMessage(), e);
      throw new RuntimeException("Redisson 初始化失败", e);
    }
  }
}

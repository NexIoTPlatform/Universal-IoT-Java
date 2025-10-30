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

package cn.universal.admin.platform.scheduler;

import cn.universal.admin.platform.service.IGatewayPollingService;
import cn.universal.persistence.entity.GatewayPollingConfig;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 网关轮询定时任务调度器
 *
 * <p>使用固定轮询间隔(30s, 60s, 120s, 300s, 600s)，通过分布式锁保证集群环境下只有一个节点执行
 *
 * @author Aleo
 * @date 2025-10-26
 */
@Component
@Slf4j
@ConditionalOnProperty(
    prefix = "gateway.polling",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class GatewayPollingScheduler {

  @Autowired private IGatewayPollingService gatewayPollingService;

  @Autowired private RedissonClient redissonClient;

  /** 30秒轮询任务 */
  @Scheduled(cron = "0/30 * * * * ?")
  public void polling30s() {
    executePollingWithLock(30);
  }

  /** 60秒轮询任务 */
  @Scheduled(cron = "0 0/1 * * * ?")
  public void polling60s() {
    executePollingWithLock(60);
  }

  /** 120秒轮询任务 */
  @Scheduled(cron = "0 0/2 * * * ?")
  public void polling120s() {
    executePollingWithLock(120);
  }

  /** 300秒轮询任务 (5分钟) */
  @Scheduled(cron = "0 0/5 * * * ?")
  public void polling300s() {
    executePollingWithLock(300);
  }

  /** 600秒轮询任务 (10分钟) */
  @Scheduled(cron = "0 0/10 * * * ?")
  public void polling600s() {
    executePollingWithLock(600);
  }

  /**
   * 使用分布式锁执行轮询任务
   *
   * @param intervalSeconds 轮询间隔(秒)
   */
  private void executePollingWithLock(int intervalSeconds) {
    String lockKey = "gateway:polling:lock:" + intervalSeconds;
    RLock lock = redissonClient.getLock(lockKey);

    try {
      // 尝试获取锁，最多等待0秒，锁自动释放时间为任务执行周期的80%
      boolean acquired = lock.tryLock(0, (long) (intervalSeconds * 0.8), TimeUnit.SECONDS);

      if (acquired) {
        log.info("✓ 节点获得 {}s 轮询任务锁，开始执行", intervalSeconds);
        executePollingTask(intervalSeconds);
      } else {
        log.debug("✗ 节点未获得 {}s 轮询任务锁，跳过执行", intervalSeconds);
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("获取轮询任务锁被中断: intervalSeconds={}", intervalSeconds);
    } catch (Exception e) {
      log.error("执行轮询任务异常: intervalSeconds={}", intervalSeconds, e);
    } finally {
      // 释放锁
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  /**
   * 执行轮询任务
   *
   * @param intervalSeconds 轮询间隔(秒)
   */
  private void executePollingTask(int intervalSeconds) {
    long startTime = System.currentTimeMillis();

    // 1. 查询待轮询的网关设备
    List<GatewayPollingConfig> configs =
        gatewayPollingService.getDuePollingDevices(intervalSeconds);

    if (configs == null || configs.isEmpty()) {
      log.debug("本次{}s轮询无待执行设备", intervalSeconds);
      return;
    }

    log.info("本次{}s轮询共 {} 个网关设备", intervalSeconds, configs.size());

    // 2. 并发执行轮询
    int successCount = 0;
    int failCount = 0;

    for (GatewayPollingConfig config : configs) {
      try {
        gatewayPollingService.pollGatewayDevice(config);
        successCount++;
      } catch (Exception e) {
        failCount++;
        log.error("轮询网关设备失败: deviceId={}", config.getDeviceId(), e);
      }
    }

    long costTime = System.currentTimeMillis() - startTime;
    log.info(
        "{}s轮询任务完成: 总数={}, 成功={}, 失败={}, 耗时={}ms",
        intervalSeconds,
        configs.size(),
        successCount,
        failCount,
        costTime);
  }
}

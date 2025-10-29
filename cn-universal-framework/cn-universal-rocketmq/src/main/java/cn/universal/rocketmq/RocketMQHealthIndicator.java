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

package cn.universal.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** RocketMQ 健康检查 @Author gitee.com/NexIoT */
@Slf4j
@Component
public class RocketMQHealthIndicator implements HealthIndicator {
  @Autowired(required = false)
  private RocketMQService rocketMQService;

  @Override
  public Health health() {
    if (rocketMQService == null) {
      return Health.down().withDetail("status", "RocketMQ connection failed").build();
    }
    try {
      boolean isHealthy = rocketMQService.rocketmqNormal();
      if (isHealthy) {
        return Health.up().withDetail("status", "RocketMQ is healthy").build();
      } else {
        return Health.down().withDetail("status", "RocketMQ connection failed").build();
      }
    } catch (Exception e) {
      log.error("RocketMQ health check failed", e);
      return Health.down().withDetail("error", e.getMessage()).build();
    }
  }
}

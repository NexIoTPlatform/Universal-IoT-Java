/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** RocketMQ 监控服务 @Author gitee.com/NexIoT */
@Slf4j
@Service
public class RocketMQMonitorService {

  private final RocketMQService rocketMQService;
  private int consecutiveFailures = 0;
  private static final int FAILURE_THRESHOLD = 5;

  public RocketMQMonitorService(RocketMQService rocketMQService) {
    this.rocketMQService = rocketMQService;
  }

  /** 监控 RocketMQ 连接状态 */
  @Scheduled(fixedRate = 300000) // 每30秒检查一次
  public void monitorConnection() {
    boolean isHealthy = rocketMQService.rocketmqNormal();

    if (!isHealthy) {
      consecutiveFailures++;
      log.warn("RocketMQ connection unhealthy, consecutive failures: {}", consecutiveFailures);

      if (consecutiveFailures >= FAILURE_THRESHOLD) {
        log.error("RocketMQ connection failed {} times, triggering alert", FAILURE_THRESHOLD);
        // 这里可以添加告警逻辑
        sendAlert();
      }
    } else {
      if (consecutiveFailures > 0) {
        log.info("RocketMQ connection recovered after {} failures", consecutiveFailures);
      }
      consecutiveFailures = 0;
    }
  }

  private void sendAlert() {
    // 实现告警逻辑，比如发送钉钉、邮件等
    log.error("RocketMQ connection alert: Server may be down or network issues");
  }
}

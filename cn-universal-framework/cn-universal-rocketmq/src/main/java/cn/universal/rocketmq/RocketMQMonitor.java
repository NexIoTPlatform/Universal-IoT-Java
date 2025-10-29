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

import java.util.HashMap;
import java.util.Set;

/**
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2023/6/29
 */
public interface RocketMQMonitor {

  /**
   * 检查消费组信息
   *
   * @param defaultTopic
   * @return
   */
  HashMap<String, Set<String>> queryDefaultTopicExistConsumer(String defaultTopic);
}

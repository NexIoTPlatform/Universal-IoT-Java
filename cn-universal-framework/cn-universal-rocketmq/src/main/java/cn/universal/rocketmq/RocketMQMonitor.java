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

package cn.universal.rocketmq;

import java.util.HashMap;
import java.util.Set;

/**
 * @version 1.0 @Author Aleo
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

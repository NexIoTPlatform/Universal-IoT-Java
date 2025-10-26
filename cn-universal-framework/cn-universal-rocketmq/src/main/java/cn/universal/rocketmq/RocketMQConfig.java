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
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Slf4j
@Configuration
public class RocketMQConfig {

  @Value("${rocketmq.name-server}")
  private String nameServer;

  @Value("${rocketmq.producer.group}")
  private String producerGroup;

  @Value("${rocketmq.producer.access-key}") // 新增：从配置读取 AccessKey
  private String accessKey;

  @Value("${rocketmq.producer.secret-key}") // 新增：从配置读取 SecretKey
  private String secretKey;

  @Value("${rocketmq.producer.send-message-timeout:10000}")
  private int sendMessageTimeout;

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public RocketMQTemplate rocketMQTemplate() {
    RocketMQTemplate template = new RocketMQTemplate();

    // 关键步骤：创建 RPCHook 注入密钥
    RPCHook rpcHook =
        new AclClientRPCHook(
            new SessionCredentials(accessKey, secretKey) // 使用密钥初始化
            );

    // 使用带 RPCHook 的构造函数
    DefaultMQProducer producer = new DefaultMQProducer(producerGroup, rpcHook); // 修改此行
    producer.setNamesrvAddr(nameServer);
    producer.setSendMsgTimeout(sendMessageTimeout);

    // 优化连接配置（保持原有配置）
    producer.setRetryTimesWhenSendAsyncFailed(2);
    producer.setRetryTimesWhenSendFailed(3);
    producer.setCompressMsgBodyOverHowmuch(4096);
    producer.setMaxMessageSize(4194304); // 4MB
    producer.setClientCallbackExecutorThreads(4);
    producer.setHeartbeatBrokerInterval(30000);
    producer.setPersistConsumerOffsetInterval(5000);

    template.setProducer(producer);
    log.info(
        "RocketMQ producer configured: nameServer={}, group={}, timeout={}ms",
        nameServer,
        producerGroup,
        sendMessageTimeout);
    return template;
  }
}

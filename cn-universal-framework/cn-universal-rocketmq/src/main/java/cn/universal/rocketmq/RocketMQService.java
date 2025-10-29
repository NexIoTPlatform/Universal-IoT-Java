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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.core.base.monitor.NetMonitor;
import cn.universal.core.config.InstanceIdProvider;
import cn.universal.core.constant.IotConstant;
import cn.universal.persistence.base.IotUPRocketMQAdapter;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.remoting.protocol.body.Connection;
import org.apache.rocketmq.remoting.protocol.body.ConsumerConnection;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Role;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * RocketMQ消息服务类
 *
 * <p>提供IoT平台中消息队列的核心功能，包括： - 同步/异步消息发送 - 顺序消息发送（FIFO） - 延迟消息发送 - 事务消息支持 - 消息监控和健康检查 - 消费者连接状态监控
 *
 * <p>实现了IotUPRocketMQAdapter和RocketMQMonitor接口， 为IoT平台提供统一的消息队列服务
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2023/6/28
 */
@Service("rocketMQService")
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class RocketMQService implements IotUPRocketMQAdapter, RocketMQMonitor {

  @Resource private RocketMQTemplate rocketMQTemplate;

  @Autowired private InstanceIdProvider instanceIdProvider;

  @Value("${platform.rocketmq.topic}")
  private String defaultTopic;

  @Value("${platform.rocketmq.heartbeat.topic}")
  private String heartTopic;

  @Value("${platform.rocketmq.logConsumerGroup:log}")
  private String logConsumerGroup;

  @Value("${rocketmq.name-server}")
  private String nameServer;

  @Resource private DefaultMQAdminExt rocketMQAdmin;

  /**
   * 检查RocketMQ连接状态
   *
   * <p>通过监控器检查RocketMQ服务器的连接状态 用于判断消息队列服务是否可用
   *
   * @return true表示连接正常，false表示连接异常
   */
  public boolean rocketmqNormal() {
    Map<String, Boolean> connectionStatus = NetMonitor.getConnectionStatus();
    if (ObjectUtil.isNotNull(connectionStatus.get(nameServer))
        && !connectionStatus.get(nameServer)) {
      log.warn("rocketmq is not connected");
      return false;
    }
    return true;
  }

  /**
   * 同步发送消息
   *
   * <p>同步方式发送消息到指定主题，会等待发送结果 适用于对消息发送结果有严格要求场景
   *
   * @param topic 消息主题，如果为空则使用默认主题
   * @param message 要发送的消息对象
   */
  @Override
  public Object send(String topic, String message) {
    return send(topic, message, instanceIdProvider.getInstanceId());
  }

  @Override
  public Object send(String topic, String message, String tag) {
    String instanceId = instanceIdProvider.getInstanceId();
    String tp = StrUtil.isBlank(topic) ? defaultTopic : topic;
    MessageBuilder<String> builder =
        MessageBuilder.withPayload(message).setHeader(IotConstant.CURRENT_INSTANCE_ID, instanceId);
    if (tag != null) {
      builder.setHeader(RocketMQHeaders.TAGS, tag);
    }
    Message<String> msg = builder.build();
    log.info("发送消息: topic={}, tag={}, sourceId={}, message={}", topic, tag, instanceId, message);
    SendResult result = rocketMQTemplate.syncSend(tp, msg, 8000);
    log.info(
        "同步发送成功: msgId={},status={}",
        result.getMsgId(),
        result == null ? "" : result.getSendStatus().name());
    return result;
  }

  @Override
  public void sendAsync(String topic, String message, String tag) {
    String tp = StrUtil.isBlank(topic) ? defaultTopic : topic;
    String instanceId = instanceIdProvider.getInstanceId();
    MessageBuilder<String> builder =
        MessageBuilder.withPayload(message).setHeader(IotConstant.CURRENT_INSTANCE_ID, instanceId);
    if (tag != null) {
      builder.setHeader(RocketMQHeaders.TAGS, tag);
    }
    Message<String> msg = builder.build();
    log.info("发送消息: topic={}, tag={}, sourceId={}, message={}", topic, tag, instanceId, message);
    try {
      rocketMQTemplate.asyncSend(
          topic,
          msg,
          new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
              log.info("sendAsync onSuccess= {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
              log.error(
                  "rocketmq send sendAsync ={}  message={}",
                  ExceptionUtil.getSimpleMessage(e),
                  JSONUtil.toJsonStr(message));
            }
          });
    } catch (Exception e) {
      log.error(
          "rocketmq send failure ={}  message={}",
          ExceptionUtil.getSimpleMessage(e),
          JSONUtil.toJsonStr(message));
    }
  }

  /**
   * 异步发送消息
   *
   * <p>异步方式发送消息到指定主题，不等待发送结果 通过回调函数处理发送成功或失败的情况
   *
   * @param topic 消息主题，如果为空则使用默认主题
   * @param message 要发送的消息对象
   */
  @Override
  public void sendAsync(String topic, String message) {
    sendAsync(topic, message, null);
  }

  @Override
  public void sendFifo(String topic, String orderKey, String message, String tag) {
    String tp = StrUtil.isBlank(topic) ? defaultTopic : topic;
    try {
      String instanceId = instanceIdProvider.getInstanceId();
      MessageBuilder<String> builder =
          MessageBuilder.withPayload(message)
              .setHeader(IotConstant.CURRENT_INSTANCE_ID, instanceId);
      if (tag != null) {
        builder.setHeader(RocketMQHeaders.TAGS, tag);
      }
      Message<String> msg = builder.build();
      rocketMQTemplate.asyncSendOrderly(
          tp,
          msg,
          orderKey,
          new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
              log.info("sendFifo onSuccess= {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
              log.error(
                  "rocketmq sendFifo sendAsync ={}  message={}",
                  ExceptionUtil.getSimpleMessage(e),
                  JSONUtil.toJsonStr(message));
            }
          });
    } catch (Exception e) {
      log.error(
          "rocketmq send sendAsync ={}  message={}",
          ExceptionUtil.getSimpleMessage(e),
          JSONUtil.toJsonStr(message));
    }
  }

  /**
   * 发送顺序消息（FIFO）
   *
   * <p>根据orderKey确保相同key的消息按顺序发送到同一个队列 适用于需要保证消息顺序的场景
   *
   * @param topic 消息主题，如果为空则使用默认主题
   * @param orderKey 顺序键，相同key的消息会按顺序处理
   * @param message 要发送的消息对象
   */
  @Override
  public void sendFifo(String topic, String orderKey, String message) {
    sendFifo(topic, orderKey, message, null);
  }

  /**
   * 定时检测RocketMQ连接状态
   *
   * <p>通过发送心跳消息检测RocketMQ服务的可用性 需要在RocketMQ控制台创建heartTopic主题 仅在生产环境启用，使用随机分钟数避免同时执行
   */
  @Profile("prod")
  @Scheduled(
      cron = "0 #{T(java.util.concurrent.ThreadLocalRandom).current().nextInt(1, 11)} * * * ?")
  public void heartbeat() {
    String message = UUID.randomUUID().toString();
    try {
      rocketMQTemplate.asyncSend(
          heartTopic,
          message,
          new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
              log.info("rocketmq heartbeat onSuccess= {}", sendResult);
              NetMonitor.freshConnectionStatus(nameServer, Boolean.TRUE);
            }

            @Override
            public void onException(Throwable throwable) {
              log.info("rocketmq heartbeat onException= {}", ExceptionUtil.getRootCause(throwable));
              NetMonitor.freshConnectionStatus(nameServer, Boolean.FALSE);
            }
          });
    } catch (Exception e) {
      log.error("rocketmq heartbeat send failure ={}", ExceptionUtil.getRootCause(e));
      NetMonitor.freshConnectionStatus(nameServer, Boolean.FALSE);
    }
    //    NetMonitor.freshConnectionStatus(IOT_ROCKETMQ_EXIST_CONSUMER,
    // checkIotConsumer(defaultTopic));
  }

  /**
   * 查询主题的消费者信息
   *
   * <p>获取指定主题的所有消费者组和客户端连接信息 用于监控消息消费状态和连接情况
   *
   * @param defaultTopic 要查询的主题
   * @return 消费者组和客户端ID的映射关系
   */
  @Override
  public HashMap<String, Set<String>> queryDefaultTopicExistConsumer(String defaultTopic) {
    HashMap<String, Set<String>> consumerMap = new HashMap<>();
    try {
      GroupList groupList = rocketMQAdmin.queryTopicConsumeByWho(defaultTopic);
      for (String groupName : groupList.getGroupList()) {
        ConsumerConnection consumerConnection =
            rocketMQAdmin.examineConsumerConnectionInfo(groupName);
        Set<String> set = new HashSet<>();
        for (Connection connection : consumerConnection.getConnectionSet()) {
          String clientId = connection.getClientId();
          set.add(clientId);
          log.info("groupName={},其他信息={}", groupName, clientId);
        }

        consumerMap.put(groupName, set);
      }
      log.info("rocketmq ,主题={},消费组信息={}", defaultTopic, consumerMap);
    } catch (Exception err) {
      log.info("op=getClientConnection_error" + err);
    }
    return consumerMap;
  }

  /**
   * 检查消费组是否存在
   *
   * <p>检查指定主题是否有活跃的消费者组 如果没有消费者，则不推送消息以避免消息丢失
   *
   * @param defaultTopic 要检查的主题
   * @return true表示有消费者，false表示没有消费者
   */
  private boolean checkIotConsumer(String defaultTopic) {
    HashMap<String, Set<String>> consumerMap = queryDefaultTopicExistConsumer(defaultTopic);
    return CollectionUtil.isNotEmpty(consumerMap);
  }

  /**
   * 刷新连接状态
   *
   * <p>更新指定服务器的连接状态到监控器
   *
   * @param serverURI 服务器URI
   * @param flag 连接状态标志
   */
  private void freFreshConnectionStatus(String serverURI, Boolean flag) {
    NetMonitor.freshConnectionStatus(serverURI, flag);
  }

  /**
   * 发送延迟消息
   *
   * <p>发送延迟指定秒数的消息 适用于需要延迟处理的业务场景
   *
   * @param topic 消息主题，如果为空则使用默认主题
   * @param message 要发送的消息对象
   * @param secondes 延迟秒数
   */
  @Override
  public void sendDelaySeconds(String topic, Object message, long secondes) {
    String tp = StrUtil.isBlank(topic) ? defaultTopic : topic;
    try {
      String instanceId = instanceIdProvider.getInstanceId();
      MessageBuilder<?> builder =
          MessageBuilder.withPayload(message)
              .setHeader(IotConstant.CURRENT_INSTANCE_ID, instanceId);
      Message<?> msg = builder.build();
      SendResult sendResult = rocketMQTemplate.syncSendDelayTimeSeconds(tp, msg, secondes);
      log.info("rocketmq 发送结果={}", sendResult);
    } catch (Exception e) {
      log.error(
          "rocketmq send failure ={}  message={}",
          ExceptionUtil.getSimpleMessage(e),
          JSONUtil.toJsonStr(message));
    }
  }
}

// package cn.universal.rocketmq.consumer;
//
// import cn.hutool.json.JSONObject;
// import cn.hutool.json.JSONUtil;
// import cn.universal.core.config.InstanceIdProvider;
// import cn.universal.protocol.tcp.util.RocketMQMessageUtil;
// import cn.universal.rocketmq.RocketMQFilterStrategy;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.annotation.SelectorType;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
/// **
// * TCP消息消费者 用于处理来自其他实例的TCP相关消息
// *
// * @version 1.0
// * @Author gitee.com/NexIoT
// * @since 2025/1/27
// */
// @Component
// @Slf4j
// @RocketMQMessageListener(
//    topic = "tcp-message-topic",
//    consumerGroup = "tcp-message-consumer-group",
//    selectorType = SelectorType.SQL92,  // 默认使用SQL92
//    selectorExpression = "sourceId != '${instanceIdProvider.instanceId}'"
// )
// public class TcpMessageConsumer implements RocketMQListener<String> {
//
//  @Autowired
//  private InstanceIdProvider instanceIdProvider;
//
//  @Autowired
//  private RocketMQFilterStrategy filterStrategy;
//
//  @Autowired
//  private RocketMQMessageUtil rocketMQMessageUtil;
//
//  @Override
//  public void onMessage(String message) {
//    try {
//      // 记录当前过滤策略
//      log.debug("当前过滤策略: {}", filterStrategy.getStrategyInfo());
//
//      // 解析消息
//      JSONObject messageJson = JSONUtil.parseObj(message);
//      String sourceId = messageJson.getStr(IotConstant.CURRENT_INSTANCE_ID);
//      String messageType = messageJson.getStr("messageType");
//      String payload = messageJson.getStr("payload");
//
//      // 应用层过滤作为兜底方案（防止SQL92配置错误）
//      if (instanceIdProvider.isOwnMessage(sourceId)) {
//        log.debug("跳过自己发出的消息: sourceId={}, messageType={}, 策略={}",
//            sourceId, messageType, filterStrategy.getCurrentStrategy().name());
//        return;
//      }
//
//      log.info("收到来自其他实例的消息: sourceId={}, messageType={}, instanceId={}, 策略={}",
//          sourceId, messageType, instanceIdProvider.getSimpleInstanceId(),
//          filterStrategy.getCurrentStrategy().name());
//
//      // 根据消息类型处理
//      processMessageByType(messageType, payload, sourceId);
//
//    } catch (Exception e) {
//      log.error("处理消息失败: message={}, error={}", message, e.getMessage(), e);
//    }
//  }
//
//  /**
//   * 根据消息类型处理消息
//   *
//   * @param messageType 消息类型
//   * @param payload     消息内容
//   * @param sourceId    消息源ID
//   */
//  private void processMessageByType(String messageType, String payload, String sourceId) {
//    switch (messageType) {
//      case "DEVICE_CONNECT":
//        handleDeviceConnect(payload, sourceId);
//        break;
//      case "DEVICE_DISCONNECT":
//        handleDeviceDisconnect(payload, sourceId);
//        break;
//      case "DEVICE_MESSAGE":
//        handleDeviceMessage(payload, sourceId);
//        break;
//      case "PRODUCT_CONFIG_UPDATE":
//        handleProductConfigUpdate(payload, sourceId);
//        break;
//      default:
//        log.warn("未知的消息类型: messageType={}", messageType);
//    }
//  }
//
//  /**
//   * 处理设备连接消息
//   */
//  private void handleDeviceConnect(String payload, String sourceId) {
//    log.info("处理设备连接消息: payload={}, 来自实例={}", payload, sourceId);
//    // 你的设备连接处理逻辑
//  }
//
//  /**
//   * 处理设备断开消息
//   */
//  private void handleDeviceDisconnect(String payload, String sourceId) {
//    log.info("处理设备断开消息: payload={}, 来自实例={}", payload, sourceId);
//    // 你的设备断开处理逻辑
//  }
//
//  /**
//   * 处理设备消息
//   */
//  private void handleDeviceMessage(String payload, String sourceId) {
//    log.info("处理设备消息: payload={}, 来自实例={}", payload, sourceId);
//    // 你的设备消息处理逻辑
//  }
//
//  /**
//   * 处理产品配置更新消息
//   */
//  private void handleProductConfigUpdate(String payload, String sourceId) {
//    log.info("处理产品配置更新消息: payload={}, 来自实例={}", payload, sourceId);
//    // 你的产品配置更新处理逻辑
//  }
// }

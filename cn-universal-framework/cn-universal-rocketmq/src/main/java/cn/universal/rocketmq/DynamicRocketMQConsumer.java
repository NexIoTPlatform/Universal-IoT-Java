// package cn.universal.rocketmq;
//
// import cn.universal.core.config.InstanceIdProvider;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.annotation.SelectorType;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
/// **
// * 动态RocketMQ消费者配置 根据SQL92状态自动选择过滤策略
// *
// * @author gitee.com/NexIoT
 * @version 1.0
// * @since 2025/1/27
// */
// @Slf4j
// @Component
// public class DynamicRocketMQConsumer {
//
//  @Autowired private RocketMQFilterStrategy filterStrategy;
//
//  /** 产品刷新配置 - 动态过滤策略 根据SQL92状态自动选择SQL92或Tag过滤 */
//  @Component
//  @RocketMQMessageListener(
//      topic = "${platform.rocketmq.product.flush.topic}",
//      consumerGroup = "${rocketmq.producer.group}-reload-dynamic")
//  public static class DynamicProductFlushConfigure implements RocketMQListener<String> {
//
//    @Autowired private InstanceIdProvider instanceIdProvider;
//
//    @Override
//    public void onMessage(String message) {
//      try {
//        // 应用层过滤：检查是否为自己的消息
//        if (message != null && message.contains("sourceId")) {
//          String sourceId = extractSourceId(message);
//          if (sourceId != null && instanceIdProvider.isOwnMessage(sourceId)) {
//            log.debug("跳过自己发出的产品刷新消息: sourceId={}", sourceId);
//            return;
//          }
//        }
//
//        log.info("接收到产品刷新消息：{}", message);
//        // 处理消息逻辑
//
//      } catch (Exception e) {
//        log.error("处理产品刷新消息失败: message={}, error={}", message, e.getMessage(), e);
//      }
//    }
//
//    private String extractSourceId(String message) {
//      try {
//        int startIndex = message.indexOf("\"sourceId\":\"");
//        if (startIndex != -1) {
//          startIndex += 12;
//          int endIndex = message.indexOf("\"", startIndex);
//          if (endIndex != -1) {
//            return message.substring(startIndex, endIndex);
//          }
//        }
//      } catch (Exception e) {
//        log.debug("提取sourceId失败: {}", e.getMessage());
//      }
//      return null;
//    }
//  }
//
//  /** 智能消费者工厂 根据过滤策略动态创建消费者配置 */
//  public static class SmartConsumerFactory {
//
//    /**
//     * 创建智能消费者配置
//     *
//     * @param topic 主题
//     * @param consumerGroup 消费者组
//     * @param filterStrategy 过滤策略
//     * @return 消费者配置
//     */
//    public static ConsumerConfig createConsumerConfig(
//        String topic, String consumerGroup, RocketMQFilterStrategy filterStrategy) {
//      ConsumerConfig config = new ConsumerConfig();
//      config.setTopic(topic);
//      config.setConsumerGroup(consumerGroup);
//
//      if (filterStrategy.isSql92Enabled()) {
//        config.setSelectorType(SelectorType.SQL92);
//        config.setSelectorExpression(filterStrategy.getSql92Expression());
//        log.info("使用SQL92过滤: topic={}, expression={}", topic, config.getSelectorExpression());
//      } else {
//        config.setSelectorType(SelectorType.TAG);
//        config.setSelectorExpression(filterStrategy.getTagExpression());
//        log.info("使用Tag过滤: topic={}, expression={}", topic, config.getSelectorExpression());
//      }
//
//      return config;
//    }
//  }
//
//  /** 消费者配置类 */
//  public static class ConsumerConfig {
//
//    private String topic;
//    private String consumerGroup;
//    private SelectorType selectorType;
//    private String selectorExpression;
//
//    // getters and setters
//    public String getTopic() {
//      return topic;
//    }
//
//    public void setTopic(String topic) {
//      this.topic = topic;
//    }
//
//    public String getConsumerGroup() {
//      return consumerGroup;
//    }
//
//    public void setConsumerGroup(String consumerGroup) {
//      this.consumerGroup = consumerGroup;
//    }
//
//    public SelectorType getSelectorType() {
//      return selectorType;
//    }
//
//    public void setSelectorType(SelectorType selectorType) {
//      this.selectorType = selectorType;
//    }
//
//    public String getSelectorExpression() {
//      return selectorExpression;
//    }
//
//    public void setSelectorExpression(String selectorExpression) {
//      this.selectorExpression = selectorExpression;
//    }
//  }
// }

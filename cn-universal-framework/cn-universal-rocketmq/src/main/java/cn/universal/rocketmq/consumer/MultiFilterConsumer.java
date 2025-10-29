// package cn.universal.rocketmq.consumer;
//
// import cn.hutool.json.JSONObject;
// import cn.hutool.json.JSONUtil;
// import cn.universal.protocol.tcp.util.RocketMQMessageUtil;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
/// **
// * 多过滤方案消费者示例
// * 展示不同的消息过滤方案
// *
// * @version 1.0
// * @Author gitee.com/NexIoT
// * @since 2025/1/27
// */
// @Component
// @Slf4j
// public class MultiFilterConsumer {
//
//    @Autowired
//    private InstanceIdProvider instanceIdProvider;
//
//    @Autowired
//    private RocketMQMessageUtil rocketMQMessageUtil;
//
//    /**
//     * 方案1: Tag过滤消费者（推荐）
//     * 使用Tag过滤，性能好，配置简单
//     */
//    @Component
//    @RocketMQMessageListener(
//        topic = "tcp-message-topic",
//        consumerGroup = "tcp-message-consumer-tag",
//        selectorExpression = "!instance-*"  // 排除所有instance-开头的Tag
//    )
//    public static class TagFilterConsumer implements RocketMQListener<String> {
//
//        @Autowired
//        private InstanceIdProvider instanceIdProvider;
//
//        @Override
//        public void onMessage(String message) {
//            try {
//                // 应用层过滤作为兜底方案
//                if (message != null && message.contains(IotConstant.CURRENT_INSTANCE_ID)) {
//                    String sourceId = extractSourceId(message);
//                    if (sourceId != null && instanceIdProvider.isOwnMessage(sourceId)) {
//                        log.debug("Tag过滤：跳过自己发出的消息: sourceId={}", sourceId);
//                        return;
//                    }
//                }
//
//                log.info("Tag过滤消费者收到消息: {}", message);
//                // 处理消息
//            } catch (Exception e) {
//                log.error("Tag过滤消费者处理消息失败: message={}, error={}", message, e.getMessage(), e);
//            }
//        }
//
//        private String extractSourceId(String message) {
//            try {
//                int startIndex = message.indexOf("\"sourceId\":\"");
//                if (startIndex != -1) {
//                    startIndex += 12;
//                    int endIndex = message.indexOf("\"", startIndex);
//                    if (endIndex != -1) {
//                        return message.substring(startIndex, endIndex);
//                    }
//                }
//            } catch (Exception e) {
//                log.debug("提取sourceId失败: {}", e.getMessage());
//            }
//            return null;
//        }
//    }
//
//    /**
//     * 方案2: 应用层过滤消费者
//     * 接收所有消息，在应用层过滤
//     */
//    @Component
//    @RocketMQMessageListener(
//        topic = "tcp-message-topic",
//        consumerGroup = "tcp-message-consumer-app"
//    )
//    public static class AppFilterConsumer implements RocketMQListener<String> {
//
//        @Autowired
//        private InstanceIdProvider instanceIdProvider;
//
//        @Override
//        public void onMessage(String message) {
//            try {
//                // 解析消息
//                JSONObject messageJson = JSONUtil.parseObj(message);
//                String sourceId = messageJson.getStr(IotConstant.CURRENT_INSTANCE_ID);
//
//                // 应用层过滤：检查是否为自己的消息
//                if (instanceIdProvider.isOwnMessage(sourceId)) {
//                    log.debug("应用层过滤：跳过自己发出的消息: sourceId={}", sourceId);
//                    return;
//                }
//
//                log.info("应用层过滤消费者收到来自其他实例的消息: sourceId={}", sourceId);
//                // 处理消息
//
//            } catch (Exception e) {
//                log.error("处理消息失败: message={}, error={}", message, e.getMessage(), e);
//            }
//        }
//    }
//
//    /**
//     * 方案3: 混合过滤消费者
//     * 使用Tag预过滤 + 应用层精确过滤
//     */
//    @Component
//    @RocketMQMessageListener(
//        topic = "tcp-message-topic",
//        consumerGroup = "tcp-message-consumer-hybrid",
//        selectorExpression = "business"  // 只接收business标签的消息
//    )
//    public static class HybridFilterConsumer implements RocketMQListener<String> {
//
//        @Autowired
//        private InstanceIdProvider instanceIdProvider;
//
//        @Override
//        public void onMessage(String message) {
//            try {
//                // 解析消息
//                JSONObject messageJson = JSONUtil.parseObj(message);
//                String sourceId = messageJson.getStr(IotConstant.CURRENT_INSTANCE_ID);
//
//                // 应用层精确过滤
//                if (instanceIdProvider.isOwnMessage(sourceId)) {
//                    log.debug("混合过滤：跳过自己发出的消息: sourceId={}", sourceId);
//                    return;
//                }
//
//                log.info("混合过滤消费者收到业务消息: sourceId={}", sourceId);
//                // 处理消息
//
//            } catch (Exception e) {
//                log.error("处理消息失败: message={}, error={}", message, e.getMessage(), e);
//            }
//        }
//    }
//
//    /**
//     * 方案4: 广播消费者
//     * 接收所有消息，包括自己的（适用于某些特殊场景）
//     */
//    @Component
//    @RocketMQMessageListener(
//        topic = "tcp-message-topic",
//        consumerGroup = "tcp-message-consumer-broadcast",
//        messageModel = org.apache.rocketmq.spring.annotation.MessageModel.BROADCASTING
//    )
//    public static class BroadcastConsumer implements RocketMQListener<String> {
//
//        @Autowired
//        private InstanceIdProvider instanceIdProvider;
//
//        @Override
//        public void onMessage(String message) {
//            try {
//                JSONObject messageJson = JSONUtil.parseObj(message);
//                String sourceId = messageJson.getStr(IotConstant.CURRENT_INSTANCE_ID);
//
//                log.info("广播消费者收到消息: sourceId={}, 当前实例={}",
//                    sourceId, instanceIdProvider.getSimpleInstanceId());
//
//                // 广播模式下可能需要处理自己的消息
//                if (instanceIdProvider.isOwnMessage(sourceId)) {
//                    log.info("广播模式：处理自己发出的消息");
//                }
//
//                // 处理消息
//
//            } catch (Exception e) {
//                log.error("处理消息失败: message={}, error={}", message, e.getMessage(), e);
//            }
//        }
//    }
// }

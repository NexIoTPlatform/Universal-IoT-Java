package cn.universal.mqtt.protocol.processor.up.common;

import cn.hutool.core.util.StrUtil;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTPublishMessage;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.processor.MqttMessageProcessor;
import cn.universal.mqtt.protocol.system.SysMQTTManager;
import cn.universal.mqtt.protocol.third.ThirdMQTTServerManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicType;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 公共消息回复处理器基类
 *
 * <p>统一处理所有需要自动回复的MQTT消息（如物模型/透传下行等）
 */
@Slf4j(topic = "mqtt")
public abstract class BaseReplyProcessor extends AbstratIoTService implements MqttMessageProcessor {

  @Autowired protected ThirdMQTTServerManager thirdMqttServerManager;
  @Autowired protected SysMQTTManager sysMQTTManager;

  @Autowired protected MQTTTopicManager mqttTopicManager;
  @Resource private IoTProductDeviceService ioTProductDeviceService;

  @Override
  public String getName() {
    return "消息回复处理器-" + getTopicType();
  }

  @Override
  public String getDescription() {
    return "处理" + getTopicType() + "主题的自动消息回复";
  }

  @Override
  public int getOrder() {
    return 800; // 回复处理一般在业务处理后
  }

  @Override
  public ProcessorResult process(MQTTUPRequest request) {
    try {
      String replyPayload = request.getReplyPayload();
      if (StrUtil.isBlank(replyPayload)) {
        log.debug("[{}] 无需回复，跳过", getName());
        return ProcessorResult.CONTINUE;
      }
      String downTopic = request.getDownTopic();
      if (StrUtil.isBlank(downTopic)) {
        downTopic = buildReplyTopic(request);
      }
      MQTTPublishMessage build =
          MQTTPublishMessage.builder()
              .topic(downTopic)
              .qos(request.getQos())
              .payload(StrUtil.bytes(replyPayload))
              .build();
      boolean success = publishWithSystemOrThirdMqtt(request, build);
      if (success) {
        log.info("[{}] 回复消息发布成功 - 主题: {} 内容：{}", getName(), downTopic, replyPayload);
        request.setContextValue("replySuccess", true);
      } else {
        log.warn("[{}] 回复消息发布失败 - 主题: {}", getName(), downTopic);
        request.setContextValue("replySuccess", false);
      }
      // 主题类型特定的后续处理
      if (!processTopicSpecificReply(request, build)) {
        log.warn("[{}] 主题特定回复处理失败", getName());
        return ProcessorResult.ERROR;
      }

      return ProcessorResult.CONTINUE;

    } catch (Exception e) {
      log.error("[{}] 回复处理异常: ", getName(), e);
      return ProcessorResult.ERROR;
    }
  }

  @Override
  public boolean supports(MQTTUPRequest request) {
    return request.getReplyPayload() != null;
  }

  /** 构建回复topic */
  protected String buildReplyTopic(MQTTUPRequest request) {
    String productKey = request.getProductKey();
    String deviceId = request.getDeviceId();
    String networkUnionId = request.getNetworkUnionId();
    if (StrUtil.isBlank(networkUnionId)) {
      return MQTTTopicType.THING_DOWN.buildTopic(productKey, deviceId);
    }
    // 1. 优先查第三方MQTT自定义下发topic
    String thirdPartyDownPattern =
        mqttTopicManager.getThirdPartyDownTopicPattern(networkUnionId, MqttConstant.TYPE_DOWN);
    if (thirdPartyDownPattern != null) {
      return fillTopicPattern(thirdPartyDownPattern, productKey, deviceId);
    }
    return MQTTTopicType.THING_DOWN.buildTopic(productKey, deviceId);
  }

  /** 将topic pattern中的+替换为实际productKey和deviceId */
  private String fillTopicPattern(String pattern, String productKey, String deviceId) {
    // 只替换前两个+，防止误替换
    int firstPlus = pattern.indexOf("+");
    int secondPlus = pattern.indexOf("+", firstPlus + 1);
    if (firstPlus >= 0 && secondPlus > firstPlus) {
      return pattern.substring(0, firstPlus)
          + productKey
          + pattern.substring(firstPlus + 1, secondPlus)
          + deviceId
          + pattern.substring(secondPlus + 1);
    }
    // 兜底：顺序替换
    return pattern.replaceFirst("\\+", productKey).replaceFirst("\\+", deviceId);
  }

  /** 根据产品类型决定推送到内置MQTT还是第三方MQTT */
  protected boolean publishWithSystemOrThirdMqtt(MQTTUPRequest request, MQTTPublishMessage reply) {
    String productKey = request.getProductKey();
    String topic = reply.getTopic();
    if (sysMQTTManager.isEnabled() && sysMQTTManager.isProductCovered(productKey)) {
      return sysMQTTManager.publishMessage(
          topic, reply.getPayloadAsBytes(), reply.getQos(), reply.isRetained());
    } else {
      // 取networkUnionId，通常等于productKey，或根据实际业务获取
      return thirdMqttServerManager.publishMessage(request.getNetworkUnionId(), reply);
    }
  }

  /** 获取主题类型名称 */
  protected abstract String getTopicType();

  /** 主题类型特定的回复处理（可选扩展） */
  protected boolean processTopicSpecificReply(MQTTUPRequest request, MQTTPublishMessage reply) {
    return true;
  }
}

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

package cn.universal.mqtt.protocol.processor.up.thingmodel;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.processor.up.common.BaseDeviceInfoProcessor;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 物模型主题设备信息处理器
 *
 * <p>处理物模型主题的设备信息提取和回填： - $thing/up/property/${productKey}/${deviceId} (属性上报) -
 * $thing/up/event/${productKey}/${deviceId} (事件上报) - $thing/down/${productKey}/${deviceId} (下行控制)
 *
 * <p>物模型特点： - 消息格式标准化，直接可用不需要编解码 - 支持属性和事件两种消息类型 - 消息体已经是JSON格式的物模型数据
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "mqtt")
@Component
public class ThingModelDeviceInfoProcessor extends BaseDeviceInfoProcessor {

  @Autowired private MQTTTopicManager topicManager;

  @Override
  protected String getTopicType() {
    return "物模型";
  }

  @Override
  public boolean supports(MQTTUPRequest request) {
    if (request.getUpTopic() == null || request.getPayload() == null) {
      return false;
    }
    return MqttConstant.TopicCategory.THING_MODEL.equals(
        MQTTTopicManager.matchCategory(request.getUpTopic()));
  }

  @Override
  protected boolean parseTopicForThisType(MQTTUPRequest request, String topic) {
    try {
      // 使用TopicManager解析物模型主题
      MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);

      if (!topicInfo.isValid()) {
        log.warn("[{}] 物模型主题格式无效: {}", getName(), topic);
        return false;
      }

      if (topicInfo.getCategory() != MqttConstant.TopicCategory.THING_MODEL) {
        log.warn("[{}] 不是物模型主题: {}", getName(), topic);
        return false;
      }

      // 设置设备信息
      request.setProductKey(topicInfo.getProductKey());
      request.setDeviceId(topicInfo.getDeviceId());

      // 设置物模型特定上下文
      request.setContextValue("topicInfo", topicInfo);
      request.setContextValue("topicType", topicInfo.getTopicType());

      log.debug(
          "[{}] 物模型主题解析成功 - 类型: {}, 产品: {}, 设备: {}",
          getName(),
          topicInfo.getTopicType(),
          topicInfo.getProductKey(),
          topicInfo.getDeviceId());

      return true;

    } catch (Exception e) {
      log.error("[{}] 物模型主题解析异常: ", getName(), e);
      return false;
    }
  }

  @Override
  protected boolean processTopicSpecificInfo(MQTTUPRequest request) {
    try {
      // 1. 解析消息内容
      if (!parseThingModelMessage(request)) {
        log.error("[{}] 物模型消息解析失败", getName());
        return false;
      }

      // 2. 验证物模型格式
      if (!validateThingModelFormat(request)) {
        log.error("[{}] 物模型格式验证失败", getName());
        return false;
      }

      // 3. 提取物模型信息
      extractThingModelInfo(request);

      log.debug("[{}] 物模型特定信息处理完成", getName());
      return true;

    } catch (Exception e) {
      log.error("[{}] 物模型特定信息处理异常: ", getName(), e);
      return false;
    }
  }

  /** 解析物模型消息内容 */
  private boolean parseThingModelMessage(MQTTUPRequest request) {
    try {
      String payload = request.getPayload();
      if (StrUtil.isBlank(payload)) {
        log.warn("[{}] 物模型消息内容为空", getName());
        return false;
      }

      // 解析JSON消息
      JSONObject messageJson;
      try {
        messageJson = JSONUtil.parseObj(payload);
      } catch (Exception e) {
        log.warn("[{}] 物模型消息不是有效的JSON格式: {}", getName(), payload);
        return false;
      }
      request.setContextValue("messageJson", messageJson);
      request.setContextValue("messageSize", payload.length());

      log.debug("[{}] 物模型消息解析成功，大小: {}字节", getName(), payload.length());
      return true;

    } catch (Exception e) {
      log.error("[{}] 物模型消息解析异常: ", getName(), e);
      return false;
    }
  }

  /** 验证物模型格式 */
  private boolean validateThingModelFormat(MQTTUPRequest request) {
    try {
      JSONObject messageJson = (JSONObject) request.getContextValue("messageJson");
      if (messageJson == null) {
        return false;
      }

      MQTTTopicManager.TopicInfo topicInfo =
          (MQTTTopicManager.TopicInfo) request.getContextValue("topicInfo");

      // 根据主题类型验证格式
      switch (topicInfo.getTopicType()) {
        case THING_PROPERTY_UP:
          return validatePropertyMessage(messageJson);
        case THING_EVENT_UP:
          return validateEventMessage(messageJson);
        case THING_DOWN:
          return validateDownstreamMessage(messageJson);
        default:
          log.warn("[{}] 不支持的物模型类型: {}", getName(), topicInfo.getTopicType());
          return false;
      }

    } catch (Exception e) {
      log.error("[{}] 物模型格式验证异常: ", getName(), e);
      return false;
    }
  }

  /** 验证属性消息格式 */
  private boolean validatePropertyMessage(JSONObject messageJson) {
    // 属性消息格式：{"messageType": "PROPERTIES", "properties": {...}}
    return true;
  }

  /** 验证事件消息格式 */
  private boolean validateEventMessage(JSONObject messageJson) {
    // 事件消息格式：{"messageType": "EVENT", "event": "online"}
    if (!messageJson.containsKey("event")) {
      log.warn("[{}] 事件消息缺少event字段", getName());
      return false;
    }

    return true;
  }

  /** 验证下行消息格式 */
  private boolean validateDownstreamMessage(JSONObject messageJson) {
    // 下行消息格式比较灵活，基本的JSON格式即可
    return messageJson.size() > 0;
  }

  /** 提取物模型信息 */
  private void extractThingModelInfo(MQTTUPRequest request) {
    try {
      JSONObject messageJson = (JSONObject) request.getContextValue("messageJson");
      MQTTTopicManager.TopicInfo topicInfo =
          (MQTTTopicManager.TopicInfo) request.getContextValue("topicInfo");
      // 根据主题类型提取特定信息
      switch (topicInfo.getTopicType()) {
        case THING_PROPERTY_UP:
          extractPropertyInfo(request, messageJson);
          break;
        case THING_EVENT_UP:
          extractEventInfo(request, messageJson);
          break;
        case THING_DOWN:
          extractDownstreamInfo(request, messageJson);
          break;
      }

      // 设置处理标识
      request.setContextValue("isThingModel", true);
      request.setContextValue("needDecode", false); // 物模型不需要编解码

      log.debug("[{}] 物模型信息提取完成 - 消息类型: {}", getName(), topicInfo.getTopicType());

    } catch (Exception e) {
      log.warn("[{}] 物模型信息提取异常: ", getName(), e);
    }
  }

  /** 提取属性信息 */
  private void extractPropertyInfo(MQTTUPRequest request, JSONObject messageJson) {
    request.setProperties(messageJson);
  }

  /** 提取事件信息 */
  private void extractEventInfo(MQTTUPRequest request, JSONObject messageJson) {
    String event = messageJson.getStr("event");
    request.setEvent(event);
  }

  /** 提取下行信息 */
  private void extractDownstreamInfo(MQTTUPRequest request, JSONObject messageJson) {
    // 提取下行命令信息
    log.debug("[{}] 下行信息提取完成 - 命令: {}", getName());
  }

  @Override
  public int getPriority() {
    return 10; // 物模型处理优先级最高
  }
}

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

package cn.universal.mqtt.protocol.processor.up.passthrough;

import cn.hutool.core.util.StrUtil;
import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.processor.up.common.BaseDeviceInfoProcessor;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 透传主题设备信息处理器
 *
 * <p>处理透传主题的设备信息提取和回填： - $thing/up/${productKey}/${deviceId} (透传上行) -
 * $thing/down/${productKey}/${deviceId} (透传下行)
 *
 * <p>透传特点： - 消息格式不固定，需要编解码 - 支持各种自定义协议格式 - 原始数据需要通过编解码器转换为物模型
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "mqtt")
@Component
public class PassthroughDeviceInfoProcessor extends BaseDeviceInfoProcessor {

  @Autowired private MQTTTopicManager topicManager;

  @Override
  protected String getTopicType() {
    return "透传";
  }

  @Override
  public boolean supports(MQTTUPRequest request) {
    if (request.getUpTopic() == null || request.getPayload() == null) {
      return false;
    }
    // 检查是否为透传主题
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(request.getUpTopic());
    return topicInfo.isValid() && topicInfo.getCategory() == MqttConstant.TopicCategory.PASSTHROUGH;
  }

  @Override
  protected boolean parseTopicForThisType(MQTTUPRequest request, String topic) {
    try {
      // 使用TopicManager解析透传主题
      MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);

      if (!topicInfo.isValid()) {
        log.warn("[{}] 透传主题格式无效: {}", getName(), topic);
        return false;
      }

      if (topicInfo.getCategory() != MqttConstant.TopicCategory.PASSTHROUGH) {
        log.warn("[{}] 不是透传主题: {}", getName(), topic);
        return false;
      }
      // 设置设备信息
      request.setProductKey(topicInfo.getProductKey());
      request.setDeviceId(topicInfo.getDeviceId());
      // 设置透传特定上下文
      request.setContextValue("topicInfo", topicInfo);
      request.setContextValue("topicType", topicInfo.getTopicType());

      log.debug(
          "[{}] 透传主题解析成功 - 类型: {}, 产品: {}, 设备: {}",
          getName(),
          topicInfo.getTopicType(),
          topicInfo.getProductKey(),
          topicInfo.getDeviceId());

      return true;

    } catch (Exception e) {
      log.error("[{}] 透传主题解析异常: ", getName(), e);
      return false;
    }
  }

  @Override
  protected boolean processTopicSpecificInfo(MQTTUPRequest request) {
    try {
      // 1. 分析透传数据类型
      if (!analyzePassthroughDataType(request)) {
        log.error("[{}] 透传数据类型分析失败", getName());
        return false;
      }

      // 2. 设置透传标识
      setPassthroughIdentifiers(request);

      log.debug("[{}] 透传特定信息处理完成", getName());
      return true;

    } catch (Exception e) {
      log.error("[{}] 透传特定信息处理异常: ", getName(), e);
      return false;
    }
  }

  /** 分析透传数据类型 */
  private boolean analyzePassthroughDataType(MQTTUPRequest request) {
    try {
      String payload = request.getPayload();
      if (payload == null) {
        log.warn("[{}] 透传原始数据为空", getName());
        return false;
      }
      return true;

    } catch (Exception e) {
      log.error("[{}] 透传数据类型分析异常: ", getName(), e);
      return false;
    }
  }

  /** 确定数据类型 */
  private String determineDataType(byte[] rawData, String textData) {
    // 检查是否为JSON格式
    if (textData != null && textData.trim().startsWith("{") && textData.trim().endsWith("}")) {
      try {
        // 尝试解析JSON
        cn.hutool.json.JSONUtil.parseObj(textData);
        return "JSON";
      } catch (Exception e) {
        // JSON解析失败，可能是其他格式
      }
    }

    // 检查是否为纯文本
    if (isPrintableText(textData)) {
      return "TEXT";
    }

    // 检查是否为十六进制字符串
    if (isHexString(textData)) {
      return "HEX";
    }

    // 检查二进制数据特征
    if (rawData.length >= 2) {
      int header = ((rawData[0] & 0xFF) << 8) | (rawData[1] & 0xFF);

      switch (header & 0xF000) {
        case 0x1000:
          return "SENSOR_DATA";
        case 0x2000:
          return "CONTROL_CMD";
        case 0x3000:
          return "CONFIG_DATA";
        default:
          break;
      }
    }

    return "BINARY";
  }

  /** 确定编码格式 */
  private String determineEncoding(byte[] rawData, String textData) {
    if (textData != null) {
      // 检查UTF-8编码
      try {
        byte[] utf8Bytes = textData.getBytes("UTF-8");
        if (java.util.Arrays.equals(rawData, utf8Bytes)) {
          return "UTF-8";
        }
      } catch (Exception e) {
        // 忽略编码检查异常
      }

      // 检查ASCII编码
      if (isAsciiText(textData)) {
        return "ASCII";
      }
    }

    return "BINARY";
  }

  /** 检查是否为可打印文本 */
  private boolean isPrintableText(String text) {
    if (StrUtil.isBlank(text)) {
      return false;
    }

    for (char c : text.toCharArray()) {
      if (!Character.isLetterOrDigit(c)
          && !Character.isWhitespace(c)
          && !"!@#$%^&*()_+-=[]{}|;':\",./<>?".contains(String.valueOf(c))) {
        return false;
      }
    }
    return true;
  }

  /** 检查是否为十六进制字符串 */
  private boolean isHexString(String text) {
    if (StrUtil.isBlank(text) || text.length() % 2 != 0) {
      return false;
    }

    try {
      for (int i = 0; i < text.length(); i += 2) {
        Integer.parseInt(text.substring(i, i + 2), 16);
      }
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /** 检查是否为ASCII文本 */
  private boolean isAsciiText(String text) {
    if (StrUtil.isBlank(text)) {
      return false;
    }

    for (char c : text.toCharArray()) {
      if (c > 127) {
        return false;
      }
    }
    return true;
  }

  /** 设置透传标识 */
  private void setPassthroughIdentifiers(MQTTUPRequest request) {
    // 设置处理标识
    request.setContextValue("isPassthrough", true);
    request.setContextValue("needDecode", true); // 透传需要编解码

    // 设置协议信息
    request.setContextValue("protocolType", "PASSTHROUGH");
    request.setContextValue("requiresCodec", true);

    // 分析可能的协议版本
    String dataType = (String) request.getContextValue("passthroughDataType");
    if ("JSON".equals(dataType)) {
      request.setContextValue("possibleProtocol", "JSON_BASED");
    } else if ("HEX".equals(dataType)) {
      request.setContextValue("possibleProtocol", "HEX_PROTOCOL");
    } else if ("BINARY".equals(dataType)) {
      request.setContextValue("possibleProtocol", "BINARY_PROTOCOL");
    } else {
      request.setContextValue("possibleProtocol", "UNKNOWN");
    }

    log.debug("[{}] 透传标识设置完成 - 数据类型: {}", getName(), dataType);
  }

  @Override
  public int getPriority() {
    return 5; // 透传处理优先级中等
  }
}

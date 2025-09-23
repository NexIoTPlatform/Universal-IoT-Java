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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.dm.device.service.impl.IoTDeviceShadowService;
import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.entity.ProcessingStage;
import cn.universal.mqtt.protocol.processor.MqttMessageProcessor;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 透传编解码处理器
 *
 * <p>对应步骤THREE：如果是透传设备，则需要调用编解码，确保消息能被物模型正确识别
 *
 * <p>透传编解码特点： - 调用产品配置的编解码器 - 将原始数据转换为标准的物模型格式 - 兼容MQTT报文按照本平台格式的处理 - 支持多种数据格式的解码
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j(topic = "mqtt")
@Component
public class PassthroughCodecProcessor extends AbstratIoTService implements MqttMessageProcessor {

  @Autowired private IoTDeviceShadowService ioTDeviceShadowService;

  @Override
  public String getName() {
    return "透传编解码处理器";
  }

  @Override
  public String getDescription() {
    return "处理透传数据的编解码转换";
  }

  @Override
  public int getOrder() {
    return 300; // 编解码处理是第三步
  }

  @Override
  public boolean supports(MQTTUPRequest request) {
    if (request.getIoTDeviceDTO() == null || request.getIoTProduct() == null) {
      return false;
    }
    return MQTTTopicManager.matchCategory(request.getUpTopic())
        .equals(MqttConstant.TopicCategory.PASSTHROUGH);
  }

  @Override
  public ProcessorResult process(MQTTUPRequest request) {
    try {
      log.debug("[{}] 开始处理透传编解码，设备: {}", getName(), request.getDeviceId());

      // 1. 尝试产品编解码器解码
      List<BaseUPRequest> upRequestList = tryProductCodec(request);

      // 2. 如果编解码器解码失败，尝试平台格式兼容
      if (CollUtil.isEmpty(upRequestList)) {
        upRequestList = tryPlatformFormat(request);
      }

      // 3. 如果都失败，创建透传原始数据请求
      if (CollUtil.isEmpty(upRequestList)) {
        //        upRequestList = createRawDataRequest(request);
      }
      // 4. 设置解码结果
      request.setUpRequestList(upRequestList);
      request.setContextValue("codecProcessedCount", upRequestList.size());
      request.setContextValue("codecProcessed", true);
      log.debug("[{}] 透传编解码处理完成，生成请求数量: {}", getName(), upRequestList.size());
      return ProcessorResult.CONTINUE;
    } catch (Exception e) {
      log.error("[{}] 透传编解码处理异常，设备: {}, 异常: ", getName(), request.getDeviceId(), e);
      return ProcessorResult.ERROR;
    }
  }

  /** 尝试使用产品编解码器解码 */
  private List<BaseUPRequest> tryProductCodec(MQTTUPRequest request) {
    try {
      String productKey = request.getProductKey();
      String payload = request.getPayload();
      IoTProduct ioTProduct = request.getIoTProduct();
      codecWithShadow(request, ioTProduct);
      log.debug("[{}] 尝试产品编解码器解码 - 产品: {}", getName(), productKey);
      // 调用产品编解码器
      List<MQTTUPRequest> decodedList =
          decode(productKey, payload, request.getCodecContext(), MQTTUPRequest.class);

      if (CollUtil.isNotEmpty(decodedList)) {
        log.debug("[{}] 产品编解码器解码成功，解码数量: {}", getName(), decodedList.size());

        List<BaseUPRequest> upRequestList = new ArrayList<>();
        for (MQTTUPRequest codecResult : decodedList) {
          BaseUPRequest upRequest = convertCodecResult(request, codecResult);
          if (upRequest != null) {
            upRequestList.add(upRequest);
          }
        }
        request.setContextValue("codecSuccess", true);
        request.setContextValue("codecType", "PRODUCT_CODEC");
        request.setStage(ProcessingStage.DECODED);
        return upRequestList;
      } else {
        log.debug("[{}] 产品编解码器未返回解码结果", getName());
        request.setContextValue("codecSuccess", false);
        request.setStage(ProcessingStage.DECODED);
        JSONObject jsonObject = new JSONObject();
        if (JSONUtil.isTypeJSON(request.getPayload())) {
          jsonObject = JSONUtil.parseObj(request.getPayload());
        }
        return List.of(buildCodecNullBean(jsonObject, request));
      }

    } catch (Exception e) {
      log.warn("[{}] 产品编解码器解码异常: ", getName(), e);
      request.setContextValue("codecSuccess", false);
      request.setContextValue("codecError", e.getMessage());
      return null;
    }
  }

  /** 编解码是否带影子 */
  private void codecWithShadow(MQTTUPRequest request, IoTProduct ioTProduct) {
    if (StringUtils.isNotEmpty(ioTProduct.getConfiguration())) {
      JSONObject jsonObject = JSONUtil.parseObj(ioTProduct.getConfiguration());
      // 上行报文是否需要附加影子
      Boolean requireUpShadow = jsonObject.getBool("requireUpShadow", false);
      if (requireUpShadow) {
        JSONObject shadowObj =
            ioTDeviceShadowService.getDeviceShadowObj(
                request.getProductKey(), request.getDeviceId());
        if (ObjectUtil.isNotNull(shadowObj)) {
          request.setShadow(shadowObj);
          request.setCodecContextValue("shadow", shadowObj);
          request.setCodecContextValue("productConfig", jsonObject);
        }
      }
      // 是否自定义了downTopic
      String downTopic = jsonObject.getStr("downTopic", null);
      if (StringUtils.isNotEmpty(downTopic)) {
        request.setDownTopic(downTopic);
      }
    }
  }

  /** 尝试平台格式兼容 */
  private List<BaseUPRequest> tryPlatformFormat(MQTTUPRequest request) {
    try {
      String messageContent = request.getPayload();

      log.debug("[{}] 尝试平台格式兼容解析", getName());

      // 解析MQTT报文按照本平台格式
      JSONObject jsonObject = parseJsonPayload(messageContent);
      if (jsonObject == null || jsonObject.isEmpty()) {
        log.debug("[{}] 平台格式解析失败", getName());
        return null;
      }

      // 创建BaseUPRequest
      BaseUPRequest upRequest = buildPlatformFormatRequest(request, jsonObject);
      if (upRequest == null) {
        return null;
      }

      List<BaseUPRequest> upRequestList = new ArrayList<>();
      upRequestList.add(upRequest);

      log.debug("[{}] 平台格式兼容解析成功", getName());
      request.setContextValue("codecSuccess", true);
      request.setContextValue("codecType", "PLATFORM_FORMAT");
      return upRequestList;

    } catch (Exception e) {
      log.warn("[{}] 平台格式兼容解析异常: ", getName(), e);
      return null;
    }
  }

  /** 转换编解码器结果 */
  private BaseUPRequest convertCodecResult(MQTTUPRequest request, MQTTUPRequest codecResult) {
    try {
      if (codecResult == null) {
        return null;
      }
      IoTDeviceDTO deviceDTO = request.getIoTDeviceDTO();
      BaseUPRequest.BaseUPRequestBuilder<?, ?> builder = getBaseUPRequest(deviceDTO);

      // 构建编解码结果
      JSONObject messageJson = parseJsonPayload(request.getPayload());
      buildCodecNotNullBean(messageJson, deviceDTO, codecResult, builder);
      // 设置回复值
      if (StrUtil.isNotBlank(codecResult.getReplyPayload())) {
        request.setReplyPayload(codecResult.getReplyPayload());
      }
      if (StrUtil.isNotBlank(codecResult.getDownTopic())) {
        request.setDownTopic(codecResult.getDownTopic());
      }
      // 默认为1
      if (codecResult.getQos() != 1) {
        request.setQos(codecResult.getQos());
      }

      BaseUPRequest upRequest = builder.build();

      log.debug("[{}] 编解码器结果转换成功", getName());
      return upRequest;
    } catch (Exception e) {
      log.error("[{}] 编解码器结果转换异常: ", getName(), e);
      return null;
    }
  }

  /** 构建平台格式请求 */
  private BaseUPRequest buildPlatformFormatRequest(MQTTUPRequest request, JSONObject jsonObject) {
    try {
      BaseUPRequest upRequest = buildCodecNullBean(jsonObject, request);

      log.debug("[{}] 平台格式请求构建成功", getName());
      return upRequest;

    } catch (Exception e) {
      log.error("[{}] 平台格式请求构建异常: ", getName(), e);
      return null;
    }
  }

  /** 验证编解码结果 */
  private boolean validateCodecResults(List<BaseUPRequest> upRequestList) {
    if (CollUtil.isEmpty(upRequestList)) {
      return false;
    }

    for (BaseUPRequest upRequest : upRequestList) {
      if (upRequest == null) {
        log.warn("[{}] 发现空的BaseUPRequest", getName());
        return false;
      }

      if (upRequest.getProductKey() == null || upRequest.getDeviceId() == null) {
        log.warn("[{}] BaseUPRequest缺少必要字段", getName());
        return false;
      }
    }

    return true;
  }

  /** 统计编解码结果 */
  private void collectCodecStatistics(MQTTUPRequest request) {
    try {
      Boolean codecSuccess = (Boolean) request.getContextValue("codecSuccess");
      String codecType = (String) request.getContextValue("codecType");
      Integer processedCount = (Integer) request.getContextValue("codecProcessedCount");

      request.setContextValue(
          "codecStatistics",
          "成功: " + codecSuccess + ", 类型: " + codecType + ", 数量: " + processedCount);

      log.debug(
          "[{}] 编解码统计 - 成功: {}, 类型: {}, 数量: {}",
          getName(),
          codecSuccess,
          codecType,
          processedCount != null ? processedCount : 0);

    } catch (Exception e) {
      log.warn("[{}] 编解码统计异常: ", getName(), e);
    }
  }

  @Override
  public boolean preCheck(MQTTUPRequest request) {
    // 检查必要的数据
    return request.getIoTDeviceDTO() != null
        && request.getIoTProduct() != null
        && request.getPayload() != null;
  }

  @Override
  public void postProcess(MQTTUPRequest request, ProcessorResult result) {
    if (result == ProcessorResult.CONTINUE) {
      // 收集编解码统计信息
      collectCodecStatistics(request);

      Integer processedCount = (Integer) request.getContextValue("codecProcessedCount");
      Boolean codecSuccess = (Boolean) request.getContextValue("codecSuccess");

      log.debug(
          "[{}] 透传编解码处理成功 - 设备: {}, 成功: {}, 生成请求: {}",
          getName(),
          request.getDeviceId(),
          codecSuccess,
          processedCount != null ? processedCount : 0);
    } else {
      log.warn("[{}] 透传编解码处理失败 - 设备: {}, 结果: {}", getName(), request.getDeviceId(), result);
    }
  }

  @Override
  public void onError(MQTTUPRequest request, Exception e) {
    log.error("[{}] 透传编解码处理异常，设备: {}, 异常: ", getName(), request.getDeviceId(), e);
    request.setError("透传编解码处理失败: " + e.getMessage());
  }

  @Override
  public int getPriority() {
    return 5; // 透传处理优先级中等
  }
}

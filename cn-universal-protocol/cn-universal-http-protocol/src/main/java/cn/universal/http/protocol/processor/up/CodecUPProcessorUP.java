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

package cn.universal.http.protocol.processor.up;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.UPRequest;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.http.protocol.entity.HttpUPRequest.HttpUPRequestBuilder;
import cn.universal.http.protocol.processor.HttpUPMessageProcessor;
import cn.universal.http.protocol.protocol.codec.HTTPCodecAction;
import cn.universal.persistence.dto.IoTDeviceDTO;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP上行消息解码处理器
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/01/20
 */
@Slf4j
@Component
public class CodecUPProcessorUP implements HttpUPMessageProcessor {

  @Autowired private ICodecService codecService;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceActionAfterService ioTDeviceActionAfterService;

  @Resource private HTTPCodecAction httpCodecActionProcessor;

  @Override
  public String getName() {
    return "HttpCodecUPProcessor";
  }

  @Override
  public String getDescription() {
    return "HTTP上行消息解码处理器";
  }

  @Override
  public int getOrder() {
    return 100;
  }

  @Override
  public List<HttpUPRequest> process(
      JSONObject jsonObject, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    List<UPRequest> decode =
        codecService.decode(
            ioTDeviceDTO.getProductKey(), ioTDeviceDTO.getPayload(), UPRequest.class);
    if (CollUtil.isNotEmpty(decode)) {
      for (UPRequest codec : decode) {
        HttpUPRequestBuilder<?, ?> httpUPRequestBuilder = getHttpUPRequestBuilder(ioTDeviceDTO);
        buildCodecNotNullBean(jsonObject, ioTDeviceDTO, codec, httpUPRequestBuilder);
        // 设置原始编码结果对象
        requests.add(httpUPRequestBuilder.build());
      }
      return requests;
    }
    HttpUPRequest noCodec = buildCodecNullBean(jsonObject, ioTDeviceDTO);
    requests.add(noCodec);
    return requests;
  }

  private HttpUPRequest buildCodecNullBean(JSONObject jsonObject, IoTDeviceDTO ioTDeviceDTO) {
    final HttpUPRequestBuilder<?, ?> builder = getHttpUPRequestBuilder(ioTDeviceDTO);
    if (MessageType.PROPERTIES.name().equalsIgnoreCase(jsonObject.getStr("messageType"))) {
      builder.messageType(MessageType.PROPERTIES);
    }
    if (MessageType.EVENT.name().equalsIgnoreCase(jsonObject.getStr("messageType"))) {
      String event = jsonObject.getStr("event");
      builder.event(event);
      builder.messageType(MessageType.EVENT);
      if ("offline".equals(event)) {
        ioTDeviceActionAfterService.offline(
            ioTDeviceDTO.getProductKey(), ioTDeviceDTO.getDeviceId());
      }
    }
    if (ObjectUtil.isNotNull(jsonObject.getJSONObject("properties"))) {
      builder.properties(jsonObject.getJSONObject("properties"));
    }
    if (ObjectUtil.isNotNull(jsonObject.getJSONObject("data"))) {
      builder.data(jsonObject.getJSONObject("data"));
    }
    if (ObjectUtil.isNotNull(jsonObject.getJSONObject("tags"))) {
      builder.tags(jsonObject.getJSONObject("tags"));
    }
    // 设置原始串
    builder.payload(ioTDeviceDTO.getPayload());

    // 设置回复值
    return builder.build();
  }

  private void buildCodecNotNullBean(
      JSONObject jsonObject,
      IoTDeviceDTO ioTDeviceDTO,
      UPRequest codec,
      HttpUPRequestBuilder<?, ?> builder) {
    if (MessageType.PROPERTIES.name().equalsIgnoreCase(jsonObject.getStr("messageType"))) {
      builder.messageType(MessageType.PROPERTIES);
    }
    if (MessageType.EVENT.name().equalsIgnoreCase(jsonObject.getStr("messageType"))) {
      String event = jsonObject.getStr("event");
      builder.event(event);
      builder.messageType(MessageType.EVENT);
      if ("offline".equals(event)) {
        ioTDeviceActionAfterService.offline(
            ioTDeviceDTO.getProductKey(), ioTDeviceDTO.getDeviceId());
      }
    }
    if (CollUtil.isNotEmpty(codec.getProperties())) {
      codec.getProperties().putAll(jsonObject.getJSONObject("properties"));
    } else {
      codec.setProperties(jsonObject.getJSONObject("properties"));
    }
    // 设置属性值
    builder.properties(codec.getProperties());
    if (ObjectUtil.isNotEmpty(codec.getData())) {
      codec.getData().putAll(jsonObject.getJSONObject("data"));
    } else {
      codec.setData(jsonObject.getJSONObject("data"));
    }
    // 设置data
    builder.data(codec.getProperties());
    // 设置原始串
    if (ioTDeviceDTO.getProductConfig().getBool(IoTConstant.REQUIRE_PAYLOAD, Boolean.FALSE)) {
      builder.payload(ioTDeviceDTO.getPayload());
    }
    if (codec.getSubDevice() != null) {
      builder.subDevice(codec.getSubDevice());
    }
    builder.tags(codec.getTags());
    // 设置回复值
    builder.function(codec.getFunction());
  }

  private HttpUPRequestBuilder<?, ?> getHttpUPRequestBuilder(IoTDeviceDTO ioTDeviceDTO) {
    return HttpUPRequest.builder()
        .deviceNode(DeviceNode.DEVICE)
        .ioTDeviceDTO(ioTDeviceDTO)
        .iotId(ioTDeviceDTO.getIotId())
        .deviceName(ioTDeviceDTO.getDeviceName())
        .messageType(MessageType.PROPERTIES)
        .deviceId(ioTDeviceDTO.getDeviceId())
        .time(System.currentTimeMillis())
        .productKey(ioTDeviceDTO.getProductKey())
        .userUnionId(ioTDeviceDTO.getUserUnionId());
  }

  @Override
  public boolean supports(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    // 检查是否有解码配置
    //    return ioTDeviceDTO != null && ioTDeviceDTO.getIoTProductDTO() != null
    //        && ioTDeviceDTO.getIoTProductDTO().getCodecJar() != null;
    return true;
  }

  @Override
  public boolean preCheck(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.debug("[HttpCodecUPProcessor] 请求列表为空，跳过解码");
      return false;
    }
    return true;
  }

  @Override
  public void onError(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests, Exception e) {
    log.error("[HttpCodecUPProcessor] 处理异常，设备ID: {}, 异常: ", ioTDeviceDTO.getDeviceId(), e);
  }
}

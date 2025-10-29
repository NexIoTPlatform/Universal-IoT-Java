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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.http.protocol.processor.HttpUPMessageProcessor;
import cn.universal.persistence.dto.IoTDeviceDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 元数据其他处理器
 *
 * <p>处理设备元数据相关的其他逻辑 负责补充和完善设备上报数据中的元数据信息
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Slf4j
@Component
public class MetadataElseUPProcessorUP extends AbstratIoTService implements HttpUPMessageProcessor {

  @Override
  public String getName() {
    return "HttpMetadataElseUPProcessor";
  }

  @Override
  public String getDescription() {
    return "HTTP上行消息元数据其他处理器";
  }

  @Override
  public int getOrder() {
    return 300;
  }

  @Override
  public List<HttpUPRequest> process(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (CollectionUtil.isNotEmpty(requests)) {
      requests.stream()
          .filter(s -> !s.isDebug())
          .forEach(
              s -> {
                iIoTDeviceDataService.saveDeviceLog(
                    s, ioTDeviceDTO, getProduct(ioTDeviceDTO.getProductKey()));
                iotDeviceShadowService.doShadow(s, ioTDeviceDTO);
              });
    }
    return requests;
  }

  @Override
  public boolean supports(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    // 所有请求都支持元数据处理
    return true;
  }

  @Override
  public boolean preCheck(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.debug("[HttpMetadataElseUPProcessor] 请求列表为空，跳过元数据处理");
      return false;
    }
    return true;
  }

  @Override
  public void onError(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests, Exception e) {
    log.error("[HttpMetadataElseUPProcessor] 处理异常，设备ID: {}, 异常: ", ioTDeviceDTO.getDeviceId(), e);
  }

  /** 补充元数据信息 */
  private void enrichMetadata(HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    // 设置设备基础信息
    if (request.getDeviceId() == null) {
      request.setDeviceId(ioTDeviceDTO.getDeviceId());
    }

    if (request.getProductKey() == null) {
      request.setProductKey(ioTDeviceDTO.getProductKey());
    }

    // 设置时间戳
    if (request.getTime() == null || request.getTime() == 0) {
      request.setTime(System.currentTimeMillis());
    }

    // 从原始数据中提取额外信息
    if (source.containsKey("timestamp")) {
      request.setTime(source.getLong("timestamp"));
    }

    if (source.containsKey("messageId")) {
      request.setRequestId(source.getStr("messageId"));
    }

    log.debug("[HttpMetadataElseUPProcessor] 元数据补充完成，设备: {}", ioTDeviceDTO.getDeviceId());
  }

  /** 验证元数据完整性 */
  private void validateMetadata(HttpUPRequest request) {
    if (request.getDeviceId() == null) {
      log.warn("[HttpMetadataElseUPProcessor] 设备ID缺失，请求ID: {}", request.getRequestId());
    }

    if (request.getProductKey() == null) {
      log.warn("[HttpMetadataElseUPProcessor] 产品Key缺失，请求ID: {}", request.getRequestId());
    }

    if (request.getTime() == null || request.getTime() == 0) {
      log.warn("[HttpMetadataElseUPProcessor] 时间戳缺失，请求ID: {}", request.getRequestId());
    }
  }
}

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

package cn.universal.http.protocol.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.message.UPRequest;
import cn.universal.core.service.ICodec;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.AbstractUPService;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.http.protocol.config.HttpModuleInfo;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.http.protocol.handle.HttpUPHandle;
import cn.universal.http.protocol.processor.HttpUProcessorChain;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * http平台上行消息处理
 *
 * @version 1.0 @Author Aleo
 * @since 2023/02/24 11:19
 */
@Service("httpUPService")
@Slf4j
public class HttpUPService extends AbstractUPService<HttpUPRequest> implements ICodec {

  @Value("${iot.register.auto.unionId}")
  private String unionId;

  @Value("${iot.register.auto.latitude}")
  private String latitude;

  @Value("${iot.register.auto.longitude}")
  private String longitude;

  @Resource private HttpModuleInfo httpModuleInfo;
  @Resource private HttpUPHandle httpUPHandle;

  @Resource private IoTDeviceActionAfterService ioTDeviceActionAfterService;

  @Resource private HttpUProcessorChain processorChain;

  @Autowired private ICodecService codecService;

  @Override
  protected List<HttpUPRequest> convert(String content) {
    List<HttpUPRequest> requests = new ArrayList<>();
    log.info("[HTTP上行] 原始报文 content={}", content);
    JSONObject jsonObject = JSONUtil.parseObj(content);
    /**
     * http 上行报文接收的时候统一处理一下， 增加一个 key = deviceId；value = 【设备序列号】 的键值对 统一使用 deviceId 键获取设备序列号，查询设备详情
     */
    // .iotId(jsonObject.getStr("iotId"))
    IoTDeviceDTO ioTDeviceDTO =
        lifeCycleDevInstance(
            IoTDeviceQuery.builder()
                .productKey(jsonObject.getStr("productKey"))
                .iotId(jsonObject.getStr("iotId"))
                .deviceId(jsonObject.getStr("deviceId"))
                .extDeviceId(jsonObject.getStr("extDeviceId"))
                .thirdPlatform(name())
                .build());
    // 设置原值，必须
    if (ioTDeviceDTO == null) {
      return null;
    }
    ioTDeviceDTO.setPayload(content);
    processorChain.process(jsonObject, ioTDeviceDTO, requests);
    return requests;
  }

  @Override
  public Object realUPAction(String upMsg) {
    List<HttpUPRequest> ctwingUPRequest = convert(upMsg);
    return httpUPHandle.up(ctwingUPRequest);
  }

  @Override
  @Async
  public void debugAsyncUP(String debugMsg) {
    JSONObject jsonObject = JSONUtil.parseObj(debugMsg);
    List<HttpUPRequest> httpUPRequests = new ArrayList<>();
    IoTDeviceDTO ioTDeviceDTO =
        lifeCycleDevInstance(
            IoTDeviceQuery.builder()
                .productKey(jsonObject.getStr("productKey"))
                .iotId(jsonObject.getStr("iotId"))
                .deviceId(jsonObject.getStr("deviceId"))
                .extDeviceId(jsonObject.getStr("extDeviceId"))
                .thirdPlatform(name())
                .build());
    // 设置原值，必须
    if (ioTDeviceDTO == null) {
      return;
    }
    ioTDeviceDTO.setPayload(debugMsg);
    processorChain.process(jsonObject, ioTDeviceDTO, httpUPRequests);
    log.info("[HTTP上行][模拟调试] httpUPRequests.size={} content={}", httpUPRequests.size(), debugMsg);
    httpUPHandle.up(httpUPRequests);
  }

  @Override
  public String version() {
    return null;
  }

  @Override
  public List<UPRequest> decode(String productKey, String payload) {
    if (StrUtil.isBlank(payload)) {
      return null;
    }
    return codecService.decode(productKey, payload);
  }

  @Override
  public String name() {
    return httpModuleInfo.getCode();
  }

  @Override
  protected String currentComponent() {
    return name();
  }
}

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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.domain.R;
import cn.universal.core.service.ICodec;
import cn.universal.dm.device.service.AbstractDownService;
import cn.universal.http.protocol.config.HttpModuleInfo;
import cn.universal.http.protocol.entity.HttpDownRequest;
import cn.universal.http.protocol.handle.HttpDownHandle;
import cn.universal.http.protocol.processor.HttpDownProcessorChain;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * http下行实现类
 *
 * @version 1.0 @Author Aleo
 * @since 2023/02/24 11:19
 */
@Service("httpDownService")
@Slf4j
public class HttpDownService extends AbstractDownService<HttpDownRequest> implements ICodec {

  @Resource
  private HttpModuleInfo httpModuleInfo;
  @Resource
  private HttpDownHandle httpDownHandle;
  @Resource
  private HttpDownProcessorChain httpDownProcessorChain;

  @Override
  public String code() {
    return httpModuleInfo.getCode();
  }

  @Override
  public String name() {
    return httpModuleInfo.getName();
  }

  @Override
  public R doAction(String msg) {
    HttpDownRequest req = convert(msg);
    log.info(
        "[HTTP下行] deviceId={} productKey={} cmd={} 原始报文={}",
        req.getDeviceId(),
        req.getProductKey(),
        req.getCmd() != null ? req.getCmd().getValue() : null,
        msg);
    httpDownProcessorChain.process(req);
    return httpDownHandle.httpDown(req);
  }

  @Override
  public R doAction(JSONObject msg) {
    HttpDownRequest req = convert(msg);
    log.info(
        "[HTTP下行] deviceId={} productKey={} cmd={} 原始报文={}",
        req.getDeviceId(),
        req.getProductKey(),
        req.getCmd() != null ? req.getCmd().getValue() : null,
        msg);
    httpDownProcessorChain.process(req);
    return httpDownHandle.httpDown(req);
  }

  private HttpDownRequest doConvert(Object request) {
    HttpDownRequest value = null;
    if (request instanceof JSONObject) {
      value = JSONUtil.toBean((JSONObject) request, HttpDownRequest.class);
    } else if (request instanceof String) {
      value = JSONUtil.toBean((String) request, HttpDownRequest.class);
    } else {
      value = JSONUtil.toBean(JSONUtil.toJsonStr(request), HttpDownRequest.class);
    }
    IoTDeviceQuery build =
        IoTDeviceQuery.builder()
            .productKey(value.getProductKey())
            .deviceId(value.getDeviceId())
            .iotId(value.getIotId())
            .build();
    IoTDeviceDTO ioTDeviceDTO = getIoTDeviceDTO(build);
    IoTProduct ioTProduct = getProduct(value.getProductKey());
    value.setIoTProduct(ioTProduct);
    value.setIoTDeviceDTO(ioTDeviceDTO);
    // 获取产品配置
    JSONObject config = JSONUtil.parseObj(ioTProduct.getConfiguration());
    // 获取产品支持的下发类型
    String down = config.getStr("down");
    // 支持第三方增删改或者下发功能且function对象不为空，则编解码，并复制编解码后的结果
    if ((StrUtil.isNotBlank(down)
        && ListUtil.of(down.split(",")).contains(value.getCmd().getValue()))
        || CollectionUtil.isNotEmpty(value.getFunction())) {
      String deResult = spliceDown(value.getProductKey(), JSONUtil.toJsonStr(value));
      //      log.info("电信设备={} 编解码结果={}", value.getDeviceId(), deResult);
      value.setDownResult(deResult);
    }
    return value;
  }

  @Override
  public String spliceDown(String productKey, String payload) {
    return super.spliceDown(productKey, payload);
  }

  @Override
  protected HttpDownRequest convert(String request) {
    return doConvert(request);
  }

  private HttpDownRequest convert(JSONObject request) {
    return doConvert(request);
  }
}

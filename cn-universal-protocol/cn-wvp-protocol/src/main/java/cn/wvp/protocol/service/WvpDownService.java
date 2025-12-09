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

package cn.wvp.protocol.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.domain.R;
import cn.universal.core.service.IDown;
import cn.universal.dm.device.service.AbstractDownService;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import cn.wvp.protocol.config.WvpModuleInfo;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handle.WvpDownHandle;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WVP下行服务
 *
 * @version 2.0
 * @since 2025/1/15
 */
@Service("wvpDownService")
@Slf4j
public class WvpDownService extends AbstractDownService<WvpDownRequest> implements IDown {

  @Resource private WvpModuleInfo wvpModuleInfo;

  @Resource private WvpDownHandle wvpDownHandle;

  @Resource private cn.universal.core.downlink.DownlinkInterceptorChain downlinkInterceptorChain;

  @Override
  public cn.universal.core.downlink.DownlinkInterceptorChain getInterceptorChain() {
    return downlinkInterceptorChain;
  }

  @Override
  public String code() {
    return wvpModuleInfo.getCode();
  }

  @Override
  public String name() {
    return wvpModuleInfo.getName();
  }

  @Override
  @SuppressWarnings("unchecked")
  public R<?> doProcess(cn.universal.core.downlink.DownlinkContext<?> context) {
    try {
      WvpDownRequest downRequest;
      if (context.getDownRequest() != null) {
        downRequest = (WvpDownRequest) context.getDownRequest();
      } else if (context.getRawMessage() != null) {
        downRequest = convert(context.getRawMessage());
      } else if (context.getJsonMessage() != null) {
        downRequest = convert(context.getJsonMessage());
      } else {
        return R.error("消息为空");
      }

      if (downRequest == null) {
        return R.error("转换下行请求失败");
      }

      // 设置上下文信息
      ((cn.universal.core.downlink.DownlinkContext<WvpDownRequest>) context)
          .setDownRequest(downRequest);
      context.setProductKey(downRequest.getProductKey());
      context.setDeviceId(downRequest.getDeviceId());
      context.setIotId(downRequest.getIotId());

      log.info(
          "[WVP下行] deviceId={} productKey={}",
          downRequest.getDeviceId(),
          downRequest.getProductKey());

      // 执行处理
      return wvpDownHandle.down(downRequest);

    } catch (Exception e) {
      log.error("[WVP下行] 处理异常", e);
      return R.error("处理异常: " + e.getMessage());
    }
  }

  @Override
  protected WvpDownRequest convert(String request) {
    return doConvert(request);
  }

  private WvpDownRequest convert(JSONObject request) {
    return doConvert(request);
  }

  /**
   * 执行转换操作
   *
   * @param request 请求对象
   * @return 转换后的下行请求
   */
  private WvpDownRequest doConvert(Object request) {
    try {
      WvpDownRequest value = parseRequest(request);
      if (value == null) {
        return null;
      }
      // 获取设备信息
      IoTDeviceDTO deviceDTO = getDeviceInfo(value);
      value.setIoTDeviceDTO(deviceDTO);
      // 获取产品信息
      IoTProduct product = getProduct(value.getProductKey());
      if (product == null) {
        log.warn("产品不存在: productKey={}", value.getProductKey());
        return null;
      }

      // 设置相关信息
      value.setIoTProduct(product);
      value.setIoTDeviceDTO(deviceDTO);
      value.setWvpRequestData(value.getWvpRequestData());

      return value;
    } catch (Exception e) {
      log.error("转换下行请求失败", e);
      return null;
    }
  }

  /**
   * 解析请求对象
   *
   * @param request 请求对象
   * @return 解析后的下行请求
   */
  private WvpDownRequest parseRequest(Object request) {
    if (request instanceof JSONObject) {
      return JSONUtil.toBean((JSONObject) request, WvpDownRequest.class);
    } else if (request instanceof String) {
      return JSONUtil.toBean((String) request, WvpDownRequest.class);
    } else {
      return JSONUtil.toBean(JSONUtil.toJsonStr(request), WvpDownRequest.class);
    }
  }

  /**
   * 获取设备信息
   *
   * @param value 下行请求
   * @return 设备信息
   */
  private IoTDeviceDTO getDeviceInfo(WvpDownRequest value) {
    IoTDeviceQuery query =
        IoTDeviceQuery.builder()
            .productKey(value.getProductKey())
            .deviceId(value.getDeviceId())
            .iotId(value.getIotId())
            .build();
    return getIoTDeviceDTO(query);
  }
}

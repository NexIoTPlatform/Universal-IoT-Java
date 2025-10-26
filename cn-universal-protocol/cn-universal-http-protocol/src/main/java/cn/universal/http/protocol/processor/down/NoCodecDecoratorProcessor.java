/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.http.protocol.processor.down;

import static cn.universal.http.protocol.config.HttpConstant.THIRD_DOWN_SUPPORT;
import static cn.universal.http.protocol.config.HttpConstant.THIRD_DOWN_URL;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.domain.R;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.http.protocol.entity.HttpDownRequest;
import cn.universal.http.protocol.processor.HttpDownMessageProcessor;
import cn.universal.http.protocol.protocol.codec.HTTPCodecAction;
import cn.universal.persistence.base.CommonRequest;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 编解码装饰器处理器
 *
 * <p>在处理器链中统一处理编解码，避免在每个处理器中重复添加编解码逻辑 通过配置决定是否启用编解码，以及编解码的时机
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Slf4j
@Component
public class NoCodecDecoratorProcessor implements HttpDownMessageProcessor {

  @Resource private IoTDeviceService iotDeviceService;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private HTTPCodecAction httpCodecActionProcessor;

  @Override
  public String getName() {
    return "无编解码执行器";
  }

  @Override
  public int getOrder() {
    return 99; // 在预处理之前执行，确保编解码结果可以被后续处理器使用
  }

  @Override
  public R<?> process(HttpDownRequest downRequest) {
    log.debug("[{}] 开始处理请求: deviceId={}", getName(), downRequest.getDeviceId());
    try {
      String configuration = downRequest.getIoTProduct().getConfiguration();
      JSONObject config = null;
      if (StrUtil.isNotBlank(configuration)) {
        config = JSONUtil.parseObj(downRequest.getIoTProduct().getConfiguration());
      }
      // 第三方支持功能
      if (config != null && config.getBool(THIRD_DOWN_SUPPORT, false)) {
        String downUrl = config.getStr(THIRD_DOWN_URL);
        Map<String, Object> function = downRequest.getFunction();
        if (StrUtil.isNotBlank(downUrl)) {
          String requestId = MDC.get(IoTConstant.TRACE_ID);
          function.put("deviceId", downRequest.getDeviceId());
          function.put("requestId", requestId);
          function.put("productKey", downRequest.getProductKey());
          String rs = CommonRequest.doRequest(downUrl, JSONUtil.toJsonStr(function), true);
          log.info(
              "[HTTP下行][第三方功能] function={} 返回={} (前200字)",
              function != null ? function.get("function") : null,
              rs);
          IoTDeviceDTO ioTDeviceDTO =
              iotDeviceService.selectDevInstanceBO(
                  downRequest.getProductKey(), downRequest.getDeviceId());
          ioTDeviceLifeCycle.command(ioTDeviceDTO, requestId, function);
          return R.ok("访问成功", rs);
        }
      }
      // 保存指令下发日志---开始
      IoTDeviceDTO ioTDeviceDTO =
          iotDeviceService.selectDevInstanceBO(
              downRequest.getProductKey(), downRequest.getDeviceId());
      ioTDeviceLifeCycle.command(ioTDeviceDTO, downRequest.getRequestId(), downRequest);
      // 保存指令下发日志---结束
    } catch (Exception e) {
      log.error("[{}] 处理请求 {} 异常", getName(), downRequest.getDeviceId(), e);
    }

    return R.ok();
  }

  /**
   * 是否支持处理该消息（基于process方法的入参）
   *
   * @param request 请求列表
   * @return true表示支持，false表示不支持
   */
  @Override
  public boolean supports(HttpDownRequest request) {
    if (StrUtil.isBlank(request.getProductKey())) {
      return false;
    }
    if (httpCodecActionProcessor.support(request.getProductKey())) {
      return false;
    }
    String configuration = request.getIoTProduct().getConfiguration();
    JSONObject config = null;
    if (StrUtil.isNotBlank(configuration)) {
      config = JSONUtil.parseObj(request.getIoTProduct().getConfiguration());
    }
    // 第三方支持功能
    return (config != null && config.getBool(THIRD_DOWN_SUPPORT, false));
  }
}

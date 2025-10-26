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

package cn.universal.web.config.intercepter;

import cn.hutool.json.JSONObject;
import cn.universal.common.constant.IoTConstant;
import cn.universal.web.config.log.WebLog;
import cn.universal.web.config.log.WebLogKits;
import cn.universal.web.context.TtlAuthContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Order(100)
public class APILogPrintIntercepter implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(APILogPrintIntercepter.class);

  // 使用ScopedValue替代ThreadLocal，自动管理生命周期
  private static final ThreadLocal<WebLog> WEB_LOG_SCOPE = ThreadLocal.withInitial(() -> null);

  @Value(value = "${logback.enable:true}")
  private boolean logEnable;

  @Value(value = "${logback.weblog.resp:true}")
  private boolean logRespEnable;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    long startTime = System.currentTimeMillis();
    String principal = TtlAuthContextHolder.getInstance().getContext();
    WebLog webLog = WebLog.builder().entryTime(startTime).build();
    JSONObject reqParams = WebLogKits.getParameters(request);
    JSONObject reqJsonData = WebLogKits.getJsonData(request);
    reqParams.putAll(reqJsonData);
    webLog.setReqParams(reqParams.toString());
    JSONObject headersInfo = WebLogKits.getHeadersInfo(request);
    headersInfo.set("principal", principal);
    webLog.setHeaderInfo(headersInfo.toString());

    // 使用ScopedValue设置WebLog
    WEB_LOG_SCOPE.set(webLog);

    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    try {
      WebLog webLog = WEB_LOG_SCOPE.get();
      webLog.setReturnTime(System.currentTimeMillis());
      webLog.setTraceId(MDC.get(IoTConstant.TRACE_ID));
      webLog.log();
    } catch (IllegalStateException e) {
      log.warn("WebLog未设置，跳过日志记录");
    } finally {
      WEB_LOG_SCOPE.remove();
    }
    // ScopedValue会自动清理，无需手动remove
  }
}

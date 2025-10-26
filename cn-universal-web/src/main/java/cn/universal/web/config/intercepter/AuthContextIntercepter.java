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

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.web.context.TtlAuthContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Order(99)
@Slf4j
public class AuthContextIntercepter implements HandlerInterceptor {

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getRequestURI().contains("api") || request.getRequestURI().contains("admin")) {
      String traceId = request.getHeader(IoTConstant.TRACE_ID);
      if (StrUtil.isEmpty(traceId)) {
        traceId = IdUtil.simpleUUID();
      }
      MDC.put(IoTConstant.TRACE_ID, traceId);
      response.addHeader(IoTConstant.TRACE_ID, MDC.get(IoTConstant.TRACE_ID));
    }
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (Objects.isNull(authentication) || Objects.isNull(authentication.getPrincipal())) {
      // 无上下文信息，直接放行
      return true;
    }
    final Object principal = authentication.getPrincipal();
    Jwt jwt = (Jwt) principal;
    // 优先使用 sub 字段
    String subject = jwt.getSubject();
    String currentJti = jwt.getId();
    log.debug("oauth2 subject={}, currentJti={}", subject, currentJti);
    if (StrUtil.isNotBlank(subject)) {
      TtlAuthContextHolder.getInstance().setContext(subject);
    }
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    TtlAuthContextHolder.getInstance().clear();
    MDC.remove(IoTConstant.TRACE_ID);
  }
}

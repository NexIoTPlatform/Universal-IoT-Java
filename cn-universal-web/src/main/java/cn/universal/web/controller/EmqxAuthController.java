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
package cn.universal.web.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.web.dto.EmqxAclRequest;
import cn.universal.web.dto.EmqxAclResponse;
import cn.universal.web.dto.EmqxAuthRequest;
import cn.universal.web.dto.EmqxAuthResponse;
import cn.universal.web.service.EmqxAclService;
import cn.universal.web.service.EmqxAuthService;
import cn.universal.web.service.EmqxHeaderValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EMQX 认证控制器 处理 EMQX 的 HTTP 认证请求
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j
@RestController
@RequestMapping("/emqx")
public class EmqxAuthController {
  @Autowired private EmqxAclService emqxAclService;
  @Autowired private EmqxHeaderValidationService emqxHeaderValidationService;
  @Autowired private EmqxAuthService emqxAuthService;

  /**
   * EMQX 认证接口 路径：/emqx/sys/auth 对应 EMQX 配置中的 HTTP 认证地址
   *
   * @param request 认证请求
   * @return 认证响应
   */
  @PostMapping("/sys/auth")
  public EmqxAuthResponse authenticate(
      @RequestBody EmqxAuthRequest request, HttpServletRequest httpRequest) {
    log.info(
        "收到 EMQX 认证请求: username={}, clientId={}, ip={}",
        request.getUsername(),
        request.getClientid(),
        request.getIp_address());

    // 1. 首先验证请求头
    if (!emqxHeaderValidationService.validateHeader(httpRequest)) {
      log.warn(
          "EMQX 请求头验证失败: username={}, clientId={}, ip={}",
          request.getUsername(),
          request.getClientid(),
          request.getIp_address());
      return new EmqxAuthResponse("deny");
    }

    try {
      EmqxAuthResponse response = emqxAuthService.authenticate(request);
      log.info(
          "EMQX 认证完成: username={}, result={}", request.getUsername(), JSONUtil.toJsonStr(response));
      return response;
    } catch (Exception e) {
      log.error("EMQX 认证异常: username={}, error={}", request.getUsername(), e.getMessage(), e);
      // 发生异常时返回拒绝
      return new EmqxAuthResponse("deny");
    }
  }

  /**
   * EMQX ACL 权限检查接口 路径：/emqx/sys/acl
   *
   * @param request ACL 请求
   * @param httpRequest HTTP 请求
   * @return ACL 响应
   */
  @PostMapping("/sys/acl")
  public EmqxAclResponse checkAcl(
      @RequestBody EmqxAclRequest request, HttpServletRequest httpRequest) {
    log.info(
        "收到 EMQX ACL 权限检查请求: username={}, topic={}, action={}",
        request.getUsername(),
        request.getTopic(),
        request.getAction());

    // 1. 验证请求头
    if (!emqxHeaderValidationService.validateHeader(httpRequest)) {
      log.warn("EMQX ACL 请求头验证失败: IP={}", JakartaServletUtil.getClientIP(httpRequest));
      return new EmqxAclResponse("deny");
    }

    // 2. 检查 ACL 权限
    try {
      EmqxAclResponse response = emqxAclService.checkAcl(request);
      log.info(
          "EMQX ACL 权限检查完成: username={}, topic={}, action={}, result={}",
          request.getUsername(),
          request.getTopic(),
          request.getAction(),
          response.getResult());
      return response;
    } catch (Exception e) {
      log.error(
          "EMQX ACL 权限检查异常: username={}, topic={}, action={}, error={}",
          request.getUsername(),
          request.getTopic(),
          request.getAction(),
          e.getMessage(),
          e);
      return new EmqxAclResponse("deny");
    }
  }

  /**
   * EMQX 认证接口 路径：/emqx/third/auth 对应 EMQX 配置中的 HTTP 认证地址
   *
   * @param request 认证请求
   * @return 认证响应
   */
  @PostMapping("/third/auth")
  public EmqxAuthResponse thirdAuth(
      @RequestBody EmqxAuthRequest request, HttpServletRequest httpRequest) {
    log.info(
        "收到 Third EMQX 认证请求: username={}, clientId={}, ip={}",
        request.getUsername(),
        request.getClientid(),
        request.getIp_address());

    return new EmqxAuthResponse("allow", true);
  }
}

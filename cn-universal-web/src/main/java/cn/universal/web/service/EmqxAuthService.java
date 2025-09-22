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
package cn.universal.web.service;

import cn.universal.web.dto.EmqxAuthRequest;
import cn.universal.web.dto.EmqxAuthResponse;

/**
 * EMQX 认证服务接口 用于处理 EMQX 的 HTTP 认证请求
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public interface EmqxAuthService {

  /**
   * 处理 EMQX 认证请求
   *
   * @param request 认证请求
   * @return 认证响应
   */
  EmqxAuthResponse authenticate(EmqxAuthRequest request);

  /**
   * 验证产品认证
   *
   * @param username 用户名（产品key）
   * @param password 密码（产品密钥）
   * @return 认证结果
   */
  EmqxAuthResponse authenticateProduct(String username, String password);

  /**
   * 验证应用认证
   *
   * @param username 用户名（应用ID）
   * @param password 密码（应用密钥）
   * @return 认证结果
   */
  EmqxAuthResponse authenticateApplication(String username, String password);

  /**
   * 验证用户认证
   *
   * @param username 用户名
   * @param password 密码
   * @return 认证结果
   */
  EmqxAuthResponse authenticateUser(String username, String password);
}

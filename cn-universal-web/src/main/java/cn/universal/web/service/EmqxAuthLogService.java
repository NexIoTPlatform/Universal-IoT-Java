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

/**
 * EMQX 认证日志服务接口 用于记录认证相关的日志信息
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public interface EmqxAuthLogService {

  /**
   * 记录认证结果日志
   *
   * @param username 用户名
   * @param clientId 客户端ID
   * @param ipAddress IP地址
   * @param authType 认证类型
   * @param result 认证结果
   */
  void logAuthResult(
      String username, String clientId, String ipAddress, String authType, String result);
}

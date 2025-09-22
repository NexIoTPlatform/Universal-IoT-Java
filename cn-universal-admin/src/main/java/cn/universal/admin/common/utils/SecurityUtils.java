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

package cn.universal.admin.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.universal.admin.system.service.IIotUserService;
import cn.universal.common.exception.BaseException;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 安全服务工具类 */
@Component
public class SecurityUtils {

  private static ApplicationContext applicationContext;
  private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

  @Autowired
  public void setApplicationContext(ApplicationContext applicationContext) {
    SecurityUtils.applicationContext = applicationContext;
  }

  /** 获取unionId用户 */
  public static String getUnionId() {
    try {
      Authentication authentication = getAuthentication();
      Object principal = authentication.getPrincipal();
      // 如果 principal 是 JWT，从 JWT 中提取用户信息
      if (principal instanceof Jwt) {
        Jwt jwt = (Jwt) principal;
        // 优先使用 sub 字段
        String subject = jwt.getSubject();
        String currentJti = jwt.getId();
        log.debug("oauth2 subject={}, currentJti={}", subject, currentJti);
        if (StringUtils.hasText(subject)) {
          return subject; // 如果 sub 就是 unionId，直接返回
        }
      }
      // 如果无法获取用户信息，抛出异常
      throw new BaseException("无法获取用户信息", HttpStatus.HTTP_UNAUTHORIZED + "");
    } catch (Exception e) {
      throw new IoTException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED);
    }
  }

  /** 获取当前认证用户信息（从 JWT 中获取） */
  public static IoTUser getIoTUnionUser() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
        // 优先使用 sub 字段
        String subject = jwt.getSubject();
        if (StringUtils.hasText(subject)) {
          // 从数据库加载用户信息
          return getIotUserByUnionId(subject);
        }
        // 如果 sub 为空，尝试从 unionId claim 获取
        String unionId = jwt.getClaimAsString("unionId");
        if (StringUtils.hasText(unionId)) {
          return getIotUserByUnionId(unionId);
        }
      }
    } catch (Exception e) {
      log.error("获取当前用户信息失败", e);
    }
    return null;
  }

  /** 根据用户名获取 IoTUser */
  private static IoTUser getIotUserByUsername(String username) {
    if (StrUtil.isBlank(username)) {
      throw new BaseException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED + "");
    }
    try {
      IIotUserService iIotUserService = applicationContext.getBean(IIotUserService.class);
      return iIotUserService.selectUserByUserName(username);
    } catch (Exception e) {
      throw new BaseException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED + "");
    }
  }

  /** 根据 unionId 获取 IoTUser */
  private static IoTUser getIotUserByUnionId(String unionId) {
    if (StrUtil.isBlank(unionId)) {
      throw new BaseException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED + "");
    }
    try {
      IIotUserService iIotUserService = applicationContext.getBean(IIotUserService.class);
      return iIotUserService.selectUserByUnionId(unionId);
    } catch (Exception e) {
      throw new BaseException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED + "");
    }
  }

  /** 获取Authentication */
  public static Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /**
   * 生成BCryptPasswordEncoder密码
   *
   * @param password 密码
   * @return 加密字符串
   */
  public static String encryptPassword(String password) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return passwordEncoder.encode(password);
  }

  /**
   * 判断密码是否相同
   *
   * @param rawPassword 真实密码
   * @param encodedPassword 加密后字符
   * @return 结果
   */
  public static boolean matchesPassword(String rawPassword, String encodedPassword) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}

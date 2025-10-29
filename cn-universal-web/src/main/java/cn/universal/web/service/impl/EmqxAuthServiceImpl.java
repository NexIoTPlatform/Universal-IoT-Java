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
package cn.universal.web.service.impl;

import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.web.dto.EmqxAuthRequest;
import cn.universal.web.dto.EmqxAuthResponse;
import cn.universal.web.service.EmqxAuthLogService;
import cn.universal.web.service.EmqxAuthQueryService;
import cn.universal.web.service.EmqxAuthService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * EMQX 认证服务实现类
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/20
 */
@Slf4j
@Service
public class EmqxAuthServiceImpl implements EmqxAuthService {

  private final EmqxAuthQueryService emqxAuthQueryService;
  private final EmqxAuthLogService emqxAuthLogService;
  private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

  @Value("${mqtt.cfg.client.username:admin}")
  private String adminUsername;

  @Value("${mqtt.cfg.client.password:admin123456}")
  private String adminPassword;

  @Value("${mqtt.cfg.enable:true}")
  private boolean adminEnable;

  @Value("${mqtt.cfg.defined.thing:$thing}")
  private String thingPrefix;

  public EmqxAuthServiceImpl(
      EmqxAuthQueryService emqxAuthQueryService, EmqxAuthLogService emqxAuthLogService) {
    this.emqxAuthQueryService = emqxAuthQueryService;
    this.emqxAuthLogService = emqxAuthLogService;
  }

  @Override
  public EmqxAuthResponse authenticate(EmqxAuthRequest request) {
    if (request == null
        || !StringUtils.hasText(request.getUsername())
        || !StringUtils.hasText(request.getPassword())) {
      log.warn(
          "认证请求参数无效: username={}, password={}",
          request != null ? request.getUsername() : "null",
          request != null ? request.getPassword() : "null");
      return new EmqxAuthResponse("deny");
    }

    String username = request.getUsername();
    String password = request.getPassword();
    String clientId = request.getClientid();
    String ipAddress = request.getIp_address();

    log.info("开始处理 EMQX 认证请求: username={}, clientId={}, ip={}", username, clientId, ipAddress);

    EmqxAuthResponse response = null;
    String authType = null;

    try {
      // 1. 尝试配置账号认证
      response = authenticateAdmin(username, password);
      if ("allow".equals(response.getResult())) {
        authType = "ADMIN";
        log.info("配置账号认证成功: username={}, clientId={}", username, clientId);
      }
    } catch (Exception e) {
      log.warn("配置账号认证异常: username={}, error={}", username, e.getMessage(), e);
    }

    // 2. 如果配置账号认证失败，尝试产品认证
    if (response == null || !"allow".equals(response.getResult())) {
      try {
        response = authenticateProduct(username, password);
        if ("allow".equals(response.getResult())) {
          authType = "PRODUCT";
          log.info("产品认证成功: username={}, clientId={}", username, clientId);
        }
      } catch (Exception e) {
        log.warn("产品认证异常: username={}, error={}", username, e.getMessage(), e);
      }
    }

    // 2. 如果产品认证失败，尝试应用认证
    if (response == null || !"allow".equals(response.getResult())) {
      try {
        response = authenticateApplication(username, password);
        if ("allow".equals(response.getResult())) {
          authType = "APPLICATION";
          log.info("应用认证成功: username={}, clientId={}", username, clientId);
        }
      } catch (Exception e) {
        log.warn("应用认证异常: username={}, error={}", username, e.getMessage(), e);
      }
    }

    // 3. 如果应用认证失败，尝试用户认证
    if (response == null || !"allow".equals(response.getResult())) {
      try {
        response = authenticateUser(username, password);
        if ("allow".equals(response.getResult())) {
          authType = "USER";
          log.info("用户认证成功: username={}, clientId={}", username, clientId);
        }
      } catch (Exception e) {
        log.warn("用户认证异常: username={}, error={}", username, e.getMessage(), e);
      }
    }

    // 5. 如果所有认证都失败，返回拒绝
    if (response == null || !"allow".equals(response.getResult())) {
      response = new EmqxAuthResponse("deny");
      log.warn("所有认证方式都失败: username={}, clientId={}", username, clientId);
    }

    // 6. 记录认证日志
    try {
      emqxAuthLogService.logAuthResult(
          username, clientId, ipAddress, authType, response.getResult());
    } catch (Exception e) {
      log.error("记录认证日志失败: username={}, error={}", username, e.getMessage(), e);
    }

    return response;
  }

  /** 配置账号认证 */
  private EmqxAuthResponse authenticateAdmin(String username, String password) {
    // 检查配置账号是否启用
    if (!adminEnable) {
      log.debug("配置账号认证未启用");
      return new EmqxAuthResponse("deny");
    }

    // 验证配置账号用户名和密码
    if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
      log.debug("配置账号认证失败: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 构建配置账号权限 ACL
    List<EmqxAuthResponse.AclRule> acl = buildAdminAcl();

    log.info("配置账号认证成功: username={}", username);
    return new EmqxAuthResponse("allow", true, acl);
  }

  @Override
  public EmqxAuthResponse authenticateProduct(String username, String password) {
    log.debug("开始产品认证: username={}", username);

    // 查询产品信息
    IoTProduct product = emqxAuthQueryService.queryProductByKey(username);
    if (product == null) {
      log.debug("产品不存在: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 检查产品状态
    if (product.getState() != 0 || product.getIsDeleted() != 0) {
      log.warn(
          "产品状态异常: username={}, state={}, isDeleted={}",
          username,
          product.getState(),
          product.getIsDeleted());
      return new EmqxAuthResponse("deny");
    }

    // 验证产品密钥
    if (!password.equals(product.getProductSecret())) {
      log.warn("产品密钥验证失败: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 构建产品权限
    List<EmqxAuthResponse.AclRule> acl = buildProductAcl(username);

    log.info("产品认证成功: username={}, productName={}", username, product.getName());
    return new EmqxAuthResponse("allow", false, acl);
  }

  @Override
  public EmqxAuthResponse authenticateApplication(String username, String password) {
    log.debug("开始应用认证: username={}", username);

    // 查询应用信息
    IoTUserApplication application = emqxAuthQueryService.queryApplicationById(username);
    if (application == null) {
      log.debug("应用不存在: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 检查应用状态
    if (application.getAppStatus() != 0 || application.getDeleted() != 0) {
      log.warn(
          "应用状态异常: username={}, appStatus={}, deleted={}",
          username,
          application.getAppStatus(),
          application.getDeleted());
      return new EmqxAuthResponse("deny");
    }

    // 检查应用有效期
    if (application.getValidEndDate() != null
        && System.currentTimeMillis() > application.getValidEndDate().getTime()) {
      log.warn("应用已过期: username={}, validEndDate={}", username, application.getValidEndDate());
      return new EmqxAuthResponse("deny");
    }

    // 检查MQTT是否启用
    if (!Boolean.TRUE.equals(application.getMqttEnable())) {
      log.warn("应用MQTT未启用: username={}, mqttEnable={}", username, application.getMqttEnable());
      return new EmqxAuthResponse("deny");
    }

    // 验证应用密钥
    if (!password.equals(application.getAppSecret())) {
      log.warn("应用密钥验证失败: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 构建应用权限
    List<EmqxAuthResponse.AclRule> acl = buildApplicationAcl(username);

    log.info("应用认证成功: username={}, appName={}", username, application.getAppName());
    return new EmqxAuthResponse("allow", false, acl);
  }

  @Override
  public EmqxAuthResponse authenticateUser(String username, String password) {
    log.debug("开始用户认证: username={}", username);

    // 查询用户信息
    IoTUser user = emqxAuthQueryService.queryUserByUsername(username);
    if (user == null) {
      log.debug("用户不存在: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 验证用户密码
    if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
      log.warn("用户密码验证失败: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 查询用户关联的应用
    List<IoTUserApplication> applications =
        emqxAuthQueryService.queryApplicationsByUnionId(username);
    if (applications == null || applications.isEmpty()) {
      log.warn("用户未关联任何应用: username={}", username);
      return new EmqxAuthResponse("deny");
    }

    // 构建用户权限
    List<EmqxAuthResponse.AclRule> acl = buildUserAcl(applications);

    log.info("用户认证成功: username={}, 关联应用数量={}", username, applications.size());
    return new EmqxAuthResponse("allow", false, acl);
  }

  /** 构建配置账号权限 ACL */
  private List<EmqxAuthResponse.AclRule> buildAdminAcl() {
    List<EmqxAuthResponse.AclRule> acl = new ArrayList<>();

    // 订阅权限：$thing/# 和 $ota/#
    acl.add(new EmqxAuthResponse.AclRule(thingPrefix + "/#", "allow", "subscribe"));
    acl.add(new EmqxAuthResponse.AclRule("$ota/#", "allow", "subscribe"));

    // 发布权限：$thing/down/#
    acl.add(new EmqxAuthResponse.AclRule(thingPrefix + "/down/#", "allow", "publish"));

    return acl;
  }

  /** 构建产品权限 ACL */
  private List<EmqxAuthResponse.AclRule> buildProductAcl(String productKey) {
    List<EmqxAuthResponse.AclRule> acl = new ArrayList<>();

    // 产品只能发布自己的产品信息（$thing/up/property/${productKey}/# 物模型格式）
    acl.add(
        new EmqxAuthResponse.AclRule(
            thingPrefix + "/up/property/" + productKey + "/#", "allow", "publish"));

    // 产品只能发布自己的产品信息（$thing/up/${productKey}/# 透传格式）
    acl.add(
        new EmqxAuthResponse.AclRule(thingPrefix + "/up/" + productKey + "/#", "allow", "publish"));

    // 产品只能订阅下行消息（$thing/down/${productKey}/# 格式）
    acl.add(
        new EmqxAuthResponse.AclRule(
            thingPrefix + "/down/" + productKey + "/#", "allow", "subscribe"));

    // 产品只能发布OTA更新消息（$ota/update/${productKey}/# 格式）
    acl.add(new EmqxAuthResponse.AclRule("$ota/update/" + productKey + "/#", "allow", "publish"));

    return acl;
  }

  /** 构建应用权限 ACL */
  private List<EmqxAuthResponse.AclRule> buildApplicationAcl(String appId) {
    List<EmqxAuthResponse.AclRule> acl = new ArrayList<>();

    // 应用只能订阅自己的主题（$thing/${app_id} 格式）
    acl.add(new EmqxAuthResponse.AclRule(thingPrefix + "/" + appId + "/#", "allow", "subscribe"));

    // 明确禁止发布权限（默认拒绝所有发布）
    acl.add(new EmqxAuthResponse.AclRule("#", "deny", "publish"));

    return acl;
  }

  /** 构建用户权限 ACL */
  private List<EmqxAuthResponse.AclRule> buildUserAcl(List<IoTUserApplication> applications) {
    List<EmqxAuthResponse.AclRule> acl = new ArrayList<>();

    // 用户只能订阅关联应用的主题（$thing/${app_id} 格式）
    for (IoTUserApplication app : applications) {
      String appId = app.getAppId();
      if (appId != null) {
        acl.add(
            new EmqxAuthResponse.AclRule(thingPrefix + "/" + appId + "/#", "allow", "subscribe"));
      }
    }

    // 明确禁止发布权限（默认拒绝所有发布）
    acl.add(new EmqxAuthResponse.AclRule("#", "deny", "publish"));

    return acl;
  }
}

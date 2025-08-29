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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.hutool.core.date.DateUtil;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.web.dto.EmqxAuthRequest;
import cn.universal.web.dto.EmqxAuthResponse;
import cn.universal.web.service.impl.EmqxAuthServiceImpl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * EMQX 认证服务测试类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EMQX 认证服务测试")
class EmqxAuthServiceTest {

  @Mock private EmqxAuthQueryService emqxAuthQueryService;

  @Mock private EmqxAuthLogService emqxAuthLogService;
  @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;

  private EmqxAuthServiceImpl emqxAuthService;

  private EmqxAuthRequest validRequest;
  private IoTProduct validProduct;
  private IoTUserApplication validApplication;
  private IoTUser validUser;

  @BeforeEach
  void setUp() {
    // 创建服务实例（构造器注入）
    emqxAuthService = new EmqxAuthServiceImpl(emqxAuthQueryService, emqxAuthLogService);

    // 设置有效的认证请求
    validRequest = new EmqxAuthRequest();
    validRequest.setUsername("test_user");
    validRequest.setPassword("test_password");
    validRequest.setClientid("test_client");
    validRequest.setIp_address("127.0.0.1");

    // 设置有效的产品
    validProduct =
        IoTProduct.builder()
            .productKey("test_product_key")
            .productSecret("test_product_secret")
            .name("测试产品")
            .state((byte) 0)
            .isDeleted(0)
            .build();

    // 设置有效的应用
    validApplication =
        IoTUserApplication.builder()
            .appId("test_app_id")
            .appSecret("test_app_secret")
            .appName("测试应用")
            .appStatus(0)
            .deleted(0)
            .validEndDate(new Date(System.currentTimeMillis() + 86400000)) // 明天过期
            .mqttEnable(true)
            .build();

    // 设置有效的用户
    validUser =
        IoTUser.builder()
            .username("test_user")
            .password("test_password")
            .status("0")
            .deleted(0)
            .build();
  }

  @Test
  @DisplayName("测试配置账号认证成功")
  void testAdminAuthenticationSuccess() {
    // 准备测试数据
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("admin");
    request.setPassword("admin123456");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertFalse(response.getIs_superuser());
    assertNotNull(response.getAcl());
    assertEquals(3, response.getAcl().size());

    // 验证订阅权限
    assertTrue(
        response.getAcl().stream()
            .anyMatch(
                acl ->
                    acl.getTopic().equals("$thing/#")
                        && acl.getPermission().equals("allow")
                        && acl.getAction().equals("subscribe")));

    assertTrue(
        response.getAcl().stream()
            .anyMatch(
                acl ->
                    acl.getTopic().equals("$ota/#")
                        && acl.getPermission().equals("allow")
                        && acl.getAction().equals("subscribe")));

    // 验证发布权限
    assertTrue(
        response.getAcl().stream()
            .anyMatch(
                acl ->
                    acl.getTopic().equals("$thing/down/#")
                        && acl.getPermission().equals("allow")
                        && acl.getAction().equals("publish")));

    // 验证调用
    verify(emqxAuthLogService)
        .logAuthResult(anyString(), anyString(), anyString(), eq("ADMIN"), eq("allow"));
  }

  @Test
  @DisplayName("测试产品认证成功")
  void testProductAuthenticationSuccess() {
    // 准备测试数据
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("test_product_key");
    request.setPassword("test_product_secret");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // Mock 查询服务
    when(emqxAuthQueryService.queryProductByKey("test_product_key")).thenReturn(validProduct);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateProduct("test_product_key", "test_product_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertFalse(response.getIs_superuser());
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());
    assertEquals("$product/test_product_key/#", response.getAcl().get(0).getTopic());
    assertEquals("allow", response.getAcl().get(0).getPermission());
    assertEquals("all", response.getAcl().get(0).getAction());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("test_product_key");
    verify(emqxAuthLogService)
        .logAuthResult(anyString(), anyString(), anyString(), eq("PRODUCT"), eq("allow"));
  }

  @Test
  @DisplayName("测试配置账号认证失败 - 用户名错误")
  void testAdminAuthenticationFailure_WrongUsername() {
    // 准备测试数据
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("wrong_admin");
    request.setPassword("admin123456");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());
  }

  @Test
  @DisplayName("测试配置账号认证失败 - 密码错误")
  void testAdminAuthenticationFailure_WrongPassword() {
    // 准备测试数据
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("admin");
    request.setPassword("wrong_password");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());
  }

  @Test
  @DisplayName("测试产品认证失败 - 产品不存在")
  void testProductAuthenticationFailure_ProductNotFound() {
    // Mock 查询服务返回空
    when(emqxAuthQueryService.queryProductByKey("nonexistent_product")).thenReturn(null);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateProduct("nonexistent_product", "any_password");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("nonexistent_product");
  }

  @Test
  @DisplayName("测试产品认证失败 - 产品已停用")
  void testProductAuthenticationFailure_ProductDisabled() {
    // 设置停用的产品
    IoTProduct disabledProduct =
        IoTProduct.builder()
            .productKey("disabled_product")
            .productSecret("test_secret")
            .state((byte) 1) // 已停用
            .isDeleted(0)
            .build();

    when(emqxAuthQueryService.queryProductByKey("disabled_product")).thenReturn(disabledProduct);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateProduct("disabled_product", "test_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("disabled_product");
  }

  @Test
  @DisplayName("测试产品认证失败 - 产品已删除")
  void testProductAuthenticationFailure_ProductDeleted() {
    // 设置已删除的产品
    IoTProduct deletedProduct =
        IoTProduct.builder()
            .productKey("deleted_product")
            .productSecret("test_secret")
            .state((byte) 0)
            .isDeleted(1) // 已删除
            .build();

    when(emqxAuthQueryService.queryProductByKey("deleted_product")).thenReturn(deletedProduct);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateProduct("deleted_product", "test_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("deleted_product");
  }

  @Test
  @DisplayName("测试产品认证失败 - 密钥错误")
  void testProductAuthenticationFailure_WrongSecret() {
    when(emqxAuthQueryService.queryProductByKey("test_product_key")).thenReturn(validProduct);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateProduct("test_product_key", "wrong_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("test_product_key");
  }

  @Test
  @DisplayName("测试应用认证成功")
  void testApplicationAuthenticationSuccess() {
    // Mock 查询服务
    when(emqxAuthQueryService.queryApplicationById("test_app_id")).thenReturn(validApplication);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("test_app_id", "test_app_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertFalse(response.getIs_superuser());
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());
    assertEquals("$test_app_id", response.getAcl().get(0).getTopic());
    assertEquals("allow", response.getAcl().get(0).getPermission());
    assertEquals("subscribe", response.getAcl().get(0).getAction());

    // 验证调用
    verify(emqxAuthQueryService).queryApplicationById("test_app_id");
    verify(emqxAuthLogService)
        .logAuthResult(anyString(), anyString(), anyString(), eq("APPLICATION"), eq("allow"));
  }

  @Test
  @DisplayName("测试应用认证失败 - 应用不存在")
  void testApplicationAuthenticationFailure_AppNotFound() {
    when(emqxAuthQueryService.queryApplicationById("nonexistent_app")).thenReturn(null);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("nonexistent_app", "any_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryApplicationById("nonexistent_app");
  }

  @Test
  @DisplayName("测试应用认证失败 - 应用已停用")
  void testApplicationAuthenticationFailure_AppDisabled() {
    // 设置停用的应用
    IoTUserApplication disabledApp =
        IoTUserApplication.builder()
            .appId("disabled_app")
            .appSecret("test_secret")
            .appStatus(1) // 已停用
            .deleted(0)
            .mqttEnable(true)
            .build();

    when(emqxAuthQueryService.queryApplicationById("disabled_app")).thenReturn(disabledApp);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("disabled_app", "test_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryApplicationById("disabled_app");
  }

  @Test
  @DisplayName("测试应用认证失败 - 应用已过期")
  void testApplicationAuthenticationFailure_AppExpired() {
    // 设置已过期的应用
    IoTUserApplication expiredApp =
        IoTUserApplication.builder()
            .appId("expired_app")
            .appSecret("test_secret")
            .appStatus(0)
            .deleted(0)
            .validEndDate(new Date(System.currentTimeMillis() - 86400000)) // 昨天过期
            .mqttEnable(true)
            .build();

    when(emqxAuthQueryService.queryApplicationById("expired_app")).thenReturn(expiredApp);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("expired_app", "test_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryApplicationById("expired_app");
  }

  @Test
  @DisplayName("测试应用认证失败 - MQTT未启用")
  void testApplicationAuthenticationFailure_MqttDisabled() {
    // 设置MQTT未启用的应用
    IoTUserApplication mqttDisabledApp =
        IoTUserApplication.builder()
            .appId("mqtt_disabled_app")
            .appSecret("test_secret")
            .appStatus(0)
            .deleted(0)
            .validEndDate(new Date(System.currentTimeMillis() + 86400000))
            .mqttEnable(false) // MQTT未启用
            .build();

    when(emqxAuthQueryService.queryApplicationById("mqtt_disabled_app"))
        .thenReturn(mqttDisabledApp);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("mqtt_disabled_app", "test_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryApplicationById("mqtt_disabled_app");
  }

  @Test
  @DisplayName("测试应用认证失败 - 密钥错误")
  void testApplicationAuthenticationFailure_WrongSecret() {
    when(emqxAuthQueryService.queryApplicationById("test_app_id")).thenReturn(validApplication);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("test_app_id", "wrong_secret");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryApplicationById("test_app_id");
  }

  @Test
  @DisplayName("测试用户认证成功")
  void testUserAuthenticationSuccess() {
    // 设置关联的应用列表
    List<IoTUserApplication> userApps =
        Arrays.asList(
            IoTUserApplication.builder().appId("app1").build(),
            IoTUserApplication.builder().appId("app2").build());

    // Mock 查询服务
    when(emqxAuthQueryService.queryUserByUsername("test_user")).thenReturn(validUser);
    when(emqxAuthQueryService.queryApplicationsByUnionId("test_user")).thenReturn(userApps);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateUser("test_user", "test_password");

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertFalse(response.getIs_superuser());
    assertNotNull(response.getAcl());
    assertEquals(2, response.getAcl().size());

    // 验证ACL规则
    assertTrue(response.getAcl().stream().anyMatch(acl -> acl.getTopic().equals("$app1")));
    assertTrue(response.getAcl().stream().anyMatch(acl -> acl.getTopic().equals("$app2")));

    // 验证调用
    verify(emqxAuthQueryService).queryUserByUsername("test_user");
    verify(emqxAuthQueryService).queryApplicationsByUnionId("test_user");
    verify(emqxAuthLogService)
        .logAuthResult(anyString(), anyString(), anyString(), eq("USER"), eq("allow"));
  }

  @Test
  @DisplayName("测试用户认证失败 - 用户不存在")
  void testUserAuthenticationFailure_UserNotFound() {
    when(emqxAuthQueryService.queryUserByUsername("nonexistent_user")).thenReturn(null);

    // 执行测试
    EmqxAuthResponse response =
        emqxAuthService.authenticateUser("nonexistent_user", "any_password");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryUserByUsername("nonexistent_user");
  }

  @Test
  @DisplayName("测试用户认证失败 - 密码错误")
  void testUserAuthenticationFailure_WrongPassword() {
    when(emqxAuthQueryService.queryUserByUsername("test_user")).thenReturn(validUser);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateUser("test_user", "wrong_password");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryUserByUsername("test_user");
  }

  @Test
  @DisplayName("测试用户认证失败 - 未关联应用")
  void testUserAuthenticationFailure_NoAssociatedApps() {
    when(emqxAuthQueryService.queryUserByUsername("test_user")).thenReturn(validUser);
    when(emqxAuthQueryService.queryApplicationsByUnionId("test_user")).thenReturn(null);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateUser("test_user", "test_password");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryUserByUsername("test_user");
    verify(emqxAuthQueryService).queryApplicationsByUnionId("test_user");
  }

  @Test
  @DisplayName("测试用户认证失败 - 关联应用列表为空")
  void testUserAuthenticationFailure_EmptyAssociatedApps() {
    when(emqxAuthQueryService.queryUserByUsername("test_user")).thenReturn(validUser);
    when(emqxAuthQueryService.queryApplicationsByUnionId("test_user")).thenReturn(Arrays.asList());

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateUser("test_user", "test_password");

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryUserByUsername("test_user");
    verify(emqxAuthQueryService).queryApplicationsByUnionId("test_user");
  }

  @Test
  @DisplayName("测试综合认证流程 - 产品认证优先")
  void testComprehensiveAuthentication_ProductFirst() {
    // 设置产品认证请求
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("test_product_key");
    request.setPassword("test_product_secret");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // Mock 查询服务
    when(emqxAuthQueryService.queryProductByKey("test_product_key")).thenReturn(validProduct);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("test_product_key");
    verify(emqxAuthQueryService, never()).queryApplicationById(anyString());
    verify(emqxAuthQueryService, never()).queryUserByUsername(anyString());
  }

  @Test
  @DisplayName("测试综合认证流程 - 应用认证次之")
  void testComprehensiveAuthentication_ApplicationSecond() {
    // 设置应用认证请求（产品认证失败）
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("test_app_id");
    request.setPassword("test_app_secret");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // Mock 查询服务
    when(emqxAuthQueryService.queryProductByKey("test_app_id")).thenReturn(null); // 产品不存在
    when(emqxAuthQueryService.queryApplicationById("test_app_id")).thenReturn(validApplication);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("test_app_id");
    verify(emqxAuthQueryService).queryApplicationById("test_app_id");
    verify(emqxAuthQueryService, never()).queryUserByUsername(anyString());
  }

  @Test
  @DisplayName("测试综合认证流程 - 用户认证最后")
  void testComprehensiveAuthentication_UserLast() {
    // 设置用户认证请求（产品和应用认证都失败）
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("test_user");
    request.setPassword("test_password");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    List<IoTUserApplication> userApps =
        Arrays.asList(IoTUserApplication.builder().appId("app1").build());

    // Mock 查询服务
    when(emqxAuthQueryService.queryProductByKey("test_user")).thenReturn(null); // 产品不存在
    when(emqxAuthQueryService.queryApplicationById("test_user")).thenReturn(null); // 应用不存在
    when(emqxAuthQueryService.queryUserByUsername("test_user")).thenReturn(validUser);
    when(emqxAuthQueryService.queryApplicationsByUnionId("test_user")).thenReturn(userApps);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("allow", response.getResult());
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("test_user");
    verify(emqxAuthQueryService).queryApplicationById("test_user");
    verify(emqxAuthQueryService).queryUserByUsername("test_user");
    verify(emqxAuthQueryService).queryApplicationsByUnionId("test_user");
  }

  @Test
  @DisplayName("测试综合认证流程 - 所有认证方式都失败")
  void testComprehensiveAuthentication_AllFailed() {
    // 设置无效的认证请求
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("invalid_user");
    request.setPassword("invalid_password");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // Mock 查询服务都返回空
    when(emqxAuthQueryService.queryProductByKey("invalid_user")).thenReturn(null);
    when(emqxAuthQueryService.queryApplicationById("invalid_user")).thenReturn(null);
    when(emqxAuthQueryService.queryUserByUsername("invalid_user")).thenReturn(null);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("invalid_user");
    verify(emqxAuthQueryService).queryApplicationById("invalid_user");
    verify(emqxAuthQueryService).queryUserByUsername("invalid_user");
  }

  @Test
  @DisplayName("测试异常处理 - 查询服务异常")
  void testExceptionHandling_QueryServiceException() {
    // 设置产品认证请求
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("test_product_key");
    request.setPassword("test_product_secret");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // Mock 查询服务抛出异常
    when(emqxAuthQueryService.queryProductByKey("test_product_key"))
        .thenThrow(new RuntimeException("数据库连接失败"));

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证调用
    verify(emqxAuthQueryService).queryProductByKey("test_product_key");
  }

  @Test
  @DisplayName("测试边界情况 - 空用户名")
  void testEdgeCase_EmptyUsername() {
    // 设置空用户名的请求
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("");
    request.setPassword("test_password");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证没有调用查询服务
    verify(emqxAuthQueryService, never()).queryProductByKey(anyString());
    verify(emqxAuthQueryService, never()).queryApplicationById(anyString());
    verify(emqxAuthQueryService, never()).queryUserByUsername(anyString());
  }

  @Test
  @DisplayName("测试边界情况 - 空密码")
  void testEdgeCase_EmptyPassword() {
    // 设置空密码的请求
    EmqxAuthRequest request = new EmqxAuthRequest();
    request.setUsername("test_user");
    request.setPassword("");
    request.setClientid("test_client");
    request.setIp_address("127.0.0.1");

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(request);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证没有调用查询服务
    verify(emqxAuthQueryService, never()).queryProductByKey(anyString());
    verify(emqxAuthQueryService, never()).queryApplicationById(anyString());
    verify(emqxAuthQueryService, never()).queryUserByUsername(anyString());
  }

  @Test
  @DisplayName("测试边界情况 - 空请求对象")
  void testEdgeCase_NullRequest() {
    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticate(null);

    // 验证结果
    assertNotNull(response);
    assertEquals("deny", response.getResult());

    // 验证没有调用查询服务
    verify(emqxAuthQueryService, never()).queryProductByKey(anyString());
    verify(emqxAuthQueryService, never()).queryApplicationById(anyString());
    verify(emqxAuthQueryService, never()).queryUserByUsername(anyString());
  }

  @Test
  @DisplayName("测试ACL规则构建 - 产品权限")
  void testAclRuleBuilding_Product() {
    // 通过反射调用私有方法
    EmqxAuthResponse response =
        emqxAuthService.authenticateProduct("test_product_key", "test_product_secret");

    // 验证ACL规则
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());

    EmqxAuthResponse.AclRule rule = response.getAcl().get(0);
    assertEquals("$product/test_product_key/#", rule.getTopic());
    assertEquals("allow", rule.getPermission());
    assertEquals("all", rule.getAction());
  }

  @Test
  @DisplayName("测试ACL规则构建 - 应用权限")
  void testAclRuleBuilding_Application() {
    // 通过反射调用私有方法
    EmqxAuthResponse response =
        emqxAuthService.authenticateApplication("test_app_id", "test_app_secret");

    // 验证ACL规则
    assertNotNull(response.getAcl());
    assertEquals(1, response.getAcl().size());

    EmqxAuthResponse.AclRule rule = response.getAcl().get(0);
    assertEquals("$test_app_id", rule.getTopic());
    assertEquals("allow", rule.getPermission());
    assertEquals("subscribe", rule.getAction());
  }

  @Test
  @DisplayName("测试ACL规则构建 - 用户权限")
  void testAclRuleBuilding_User() {
    // 设置关联的应用列表
    List<IoTUserApplication> userApps =
        Arrays.asList(
            IoTUserApplication.builder().appId("app1").build(),
            IoTUserApplication.builder().appId("app2").build());

    when(emqxAuthQueryService.queryUserByUsername("test_user")).thenReturn(validUser);
    when(emqxAuthQueryService.queryApplicationsByUnionId("test_user")).thenReturn(userApps);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateUser("test_user", "test_password");

    // 验证ACL规则
    assertNotNull(response.getAcl());
    assertEquals(2, response.getAcl().size());

    // 验证所有应用的权限规则
    assertTrue(
        response.getAcl().stream()
            .allMatch(
                rule ->
                    rule.getPermission().equals("allow") && rule.getAction().equals("subscribe")));

    assertTrue(response.getAcl().stream().anyMatch(rule -> rule.getTopic().equals("$app1")));
    assertTrue(response.getAcl().stream().anyMatch(rule -> rule.getTopic().equals("$app2")));
  }

  @Test
  void testUserAuthenticationSuccess_WithMultipleApplications() {
    // 准备测试数据
    String username = "testuser";
    String password = "password123";

    IoTUser user = new IoTUser();
    user.setUsername(username);
    user.setPassword(bCryptPasswordEncoder.encode(password));

    IoTUserApplication app1 = new IoTUserApplication();
    app1.setAppId("abc");
    app1.setAppName("App ABC");

    IoTUserApplication app2 = new IoTUserApplication();
    app2.setAppId("bdc");
    app2.setAppName("App BDC");

    List<IoTUserApplication> applications = Arrays.asList(app1, app2);

    // Mock 服务调用
    when(emqxAuthQueryService.queryUserByUsername(username)).thenReturn(user);
    when(emqxAuthQueryService.queryApplicationsByUnionId(username)).thenReturn(applications);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateUser(username, password);

    // 验证结果
    assertThat(response.getResult()).isEqualTo("allow");
    assertThat(response.getIs_superuser()).isFalse();
    assertThat(response.getAcl()).hasSize(3); // 2个订阅权限 + 1个发布拒绝权限

    // 验证订阅权限
    List<EmqxAuthResponse.AclRule> subscribeRules =
        response.getAcl().stream()
            .filter(rule -> "subscribe".equals(rule.getAction()))
            .collect(Collectors.toList());

    assertThat(subscribeRules).hasSize(2);
    assertThat(subscribeRules)
        .anyMatch(
            rule -> rule.getTopic().equals("$thing/abc") && "allow".equals(rule.getPermission()));
    assertThat(subscribeRules)
        .anyMatch(
            rule -> rule.getTopic().equals("$thing/bdc") && "allow".equals(rule.getPermission()));

    // 验证发布拒绝权限
    List<EmqxAuthResponse.AclRule> publishDenyRules =
        response.getAcl().stream()
            .filter(
                rule -> "publish".equals(rule.getAction()) && "deny".equals(rule.getPermission()))
            .collect(Collectors.toList());

    assertThat(publishDenyRules).hasSize(1);
    assertThat(publishDenyRules).anyMatch(rule -> rule.getTopic().equals("#"));
  }

  @Test
  void testApplicationAuthenticationSuccess_WithThingPrefix() {
    // 准备测试数据
    String appId = "testapp";
    String appSecret = "secret123";

    IoTUserApplication application = new IoTUserApplication();
    application.setAppId(appId);
    application.setAppSecret(appSecret);
    application.setAppName("Test App");
    application.setAppStatus(0);
    application.setValidEndDate(DateUtil.offsetDay(new Date(), 30));
    application.setMqttEnable(true);

    // Mock 服务调用
    when(emqxAuthQueryService.queryApplicationById(appId)).thenReturn(application);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateApplication(appId, appSecret);

    // 验证结果
    assertThat(response.getResult()).isEqualTo("allow");
    assertThat(response.getIs_superuser()).isFalse();
    assertThat(response.getAcl()).hasSize(2); // 1个订阅权限 + 1个发布拒绝权限

    // 验证订阅权限
    List<EmqxAuthResponse.AclRule> subscribeRules =
        response.getAcl().stream()
            .filter(rule -> "subscribe".equals(rule.getAction()))
            .collect(Collectors.toList());

    assertThat(subscribeRules).hasSize(1);
    assertThat(subscribeRules)
        .anyMatch(
            rule ->
                rule.getTopic().equals("$thing/testapp") && "allow".equals(rule.getPermission()));

    // 验证发布拒绝权限
    List<EmqxAuthResponse.AclRule> publishDenyRules =
        response.getAcl().stream()
            .filter(
                rule -> "publish".equals(rule.getAction()) && "deny".equals(rule.getPermission()))
            .collect(Collectors.toList());

    assertThat(publishDenyRules).hasSize(1);
    assertThat(publishDenyRules).anyMatch(rule -> rule.getTopic().equals("#"));
  }

  @Test
  void testProductAuthenticationSuccess_WithThingPrefix() {
    // 准备测试数据
    String productKey = "testproduct";
    String productSecret = "secret123";

    IoTProduct product = new IoTProduct();
    product.setProductKey(productKey);
    product.setProductSecret(productSecret);
    product.setState((byte)0);

    // Mock 服务调用
    when(emqxAuthQueryService.queryProductByKey(productKey)).thenReturn(product);

    // 执行测试
    EmqxAuthResponse response = emqxAuthService.authenticateProduct(productKey, productSecret);

    // 验证结果
    assertThat(response.getResult()).isEqualTo("allow");
    assertThat(response.getIs_superuser()).isFalse();
    assertThat(response.getAcl()).hasSize(3); // 3个发布权限

    // 验证发布权限
    List<EmqxAuthResponse.AclRule> publishRules =
        response.getAcl().stream()
            .filter(rule -> "publish".equals(rule.getAction()))
            .collect(Collectors.toList());

    assertThat(publishRules).hasSize(3);
    assertThat(publishRules)
        .anyMatch(
            rule ->
                rule.getTopic().equals("$thing/testproduct/#")
                    && "allow".equals(rule.getPermission()));
    assertThat(publishRules)
        .anyMatch(
            rule ->
                rule.getTopic().equals("$thing/down/testproduct/#")
                    && "allow".equals(rule.getPermission()));
    assertThat(publishRules)
        .anyMatch(
            rule ->
                rule.getTopic().equals("$ota/update/testproduct/#")
                    && "allow".equals(rule.getPermission()));
  }
}

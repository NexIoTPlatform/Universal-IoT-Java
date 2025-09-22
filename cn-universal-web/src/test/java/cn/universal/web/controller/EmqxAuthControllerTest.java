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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cn.universal.web.dto.EmqxAuthRequest;
import cn.universal.web.dto.EmqxAuthResponse;
import cn.universal.web.service.EmqxAuthService;
import cn.universal.web.service.EmqxHeaderValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * EMQX 认证控制器集成测试
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@WebMvcTest({EmqxAuthController.class})
@ActiveProfiles("test")
@DisplayName("EMQX 认证控制器集成测试")
class EmqxAuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EmqxAuthService emqxAuthService;

  @MockBean private EmqxHeaderValidationService emqxHeaderValidationService;

  @Autowired private ObjectMapper objectMapper;

  private EmqxAuthRequest validRequest;
  private EmqxAuthResponse successResponse;
  private EmqxAuthResponse denyResponse;

  @BeforeEach
  void setUp() {
    // 设置有效的认证请求
    validRequest = new EmqxAuthRequest();
    validRequest.setUsername("test_user");
    validRequest.setPassword("test_password");
    validRequest.setClientid("test_client");
    validRequest.setIp_address("127.0.0.1");

    // 设置成功响应
    successResponse =
        new EmqxAuthResponse(
            "allow",
            false,
            Arrays.asList(new EmqxAuthResponse.AclRule("$test_topic", "allow", "subscribe")));

    // 设置拒绝响应
    denyResponse = new EmqxAuthResponse("deny");
  }

  @Test
  @DisplayName("测试认证成功 - 产品认证")
  void testAuthenticationSuccess_Product() throws Exception {
    // Mock 请求头验证成功
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(true);

    // Mock 认证服务返回成功
    when(emqxAuthService.authenticate(any(EmqxAuthRequest.class))).thenReturn(successResponse);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("allow"))
        .andExpect(jsonPath("$.is_superuser").value(false))
        .andExpect(jsonPath("$.acl").isArray())
        .andExpect(jsonPath("$.acl[0].topic").value("$test_topic"))
        .andExpect(jsonPath("$.acl[0].permission").value("allow"))
        .andExpect(jsonPath("$.acl[0].action").value("subscribe"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 请求头验证失败")
  void testAuthenticationFailure_HeaderValidationFailed() throws Exception {
    // Mock 请求头验证失败
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(false);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "wrong_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("deny"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 缺少请求头")
  void testAuthenticationFailure_MissingHeader() throws Exception {
    // 执行测试（不包含请求头）
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("deny"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 认证服务异常")
  void testAuthenticationFailure_ServiceException() throws Exception {
    // Mock 请求头验证成功
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(true);

    // Mock 认证服务抛出异常
    when(emqxAuthService.authenticate(any(EmqxAuthRequest.class)))
        .thenThrow(new RuntimeException("认证服务异常"));

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("deny"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 无效的请求体")
  void testAuthenticationFailure_InvalidRequestBody() throws Exception {
    // 执行测试（无效的JSON）
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
        .andExpect(status().isBadRequest());

    // 验证没有调用验证服务
    verify(emqxHeaderValidationService, never()).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 空请求体")
  void testAuthenticationFailure_EmptyRequestBody() throws Exception {
    // 执行测试（空请求体）
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    // 验证没有调用验证服务
    verify(emqxHeaderValidationService, never()).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 错误的Content-Type")
  void testAuthenticationFailure_WrongContentType() throws Exception {
    // 执行测试（错误的Content-Type）
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
        .andExpect(status().isUnsupportedMediaType());

    // 验证没有调用验证服务
    verify(emqxHeaderValidationService, never()).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证成功 - 应用认证")
  void testAuthenticationSuccess_Application() throws Exception {
    // 设置应用认证请求
    EmqxAuthRequest appRequest = new EmqxAuthRequest();
    appRequest.setUsername("test_app_id");
    appRequest.setPassword("test_app_secret");
    appRequest.setClientid("test_client");
    appRequest.setIp_address("127.0.0.1");

    // Mock 请求头验证成功
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(true);

    // Mock 认证服务返回成功
    when(emqxAuthService.authenticate(any(EmqxAuthRequest.class))).thenReturn(successResponse);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(appRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("allow"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证成功 - 用户认证")
  void testAuthenticationSuccess_User() throws Exception {
    // 设置用户认证请求
    EmqxAuthRequest userRequest = new EmqxAuthRequest();
    userRequest.setUsername("test_user");
    userRequest.setPassword("test_password");
    userRequest.setClientid("test_client");
    userRequest.setIp_address("127.0.0.1");

    // Mock 请求头验证成功
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(true);

    // Mock 认证服务返回成功
    when(emqxAuthService.authenticate(any(EmqxAuthRequest.class))).thenReturn(successResponse);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("allow"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试认证失败 - 所有认证方式都失败")
  void testAuthenticationFailure_AllMethodsFailed() throws Exception {
    // Mock 请求头验证成功
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(true);

    // Mock 认证服务返回拒绝
    when(emqxAuthService.authenticate(any(EmqxAuthRequest.class))).thenReturn(denyResponse);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("deny"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试请求头验证 - 正确的请求头值")
  void testHeaderValidation_CorrectValue() throws Exception {
    // Mock 请求头验证成功
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(true);

    // Mock 认证服务返回成功
    when(emqxAuthService.authenticate(any(EmqxAuthRequest.class))).thenReturn(successResponse);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("allow"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
  }

  @Test
  @DisplayName("测试请求头验证 - 错误的请求头值")
  void testHeaderValidation_WrongValue() throws Exception {
    // Mock 请求头验证失败
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(false);

    // 执行测试
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("universal-emqx", "wrong_value")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("deny"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }

  @Test
  @DisplayName("测试请求头验证 - 大小写敏感")
  void testHeaderValidation_CaseSensitive() throws Exception {
    // Mock 请求头验证失败（大小写敏感）
    when(emqxHeaderValidationService.validateHeader(any())).thenReturn(false);

    // 执行测试（大写请求头）
    mockMvc
        .perform(
            post("/emqx/sys/auth")
                .header("UNIVERSAL-EMQX", "d41d8cd98f00b204e9800998ecf8427e")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result").value("deny"));

    // 验证调用
    verify(emqxHeaderValidationService).validateHeader(any());
    verify(emqxAuthService, never()).authenticate(any(EmqxAuthRequest.class));
  }
}

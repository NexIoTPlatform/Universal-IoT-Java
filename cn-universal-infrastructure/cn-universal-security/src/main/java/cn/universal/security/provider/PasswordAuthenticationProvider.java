/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoTXin 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoTXin
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.security.provider;

import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.mapper.IoTUserMapper;
import cn.universal.security.token.PasswordAuthenticationToken;
import java.security.Principal;
import java.util.Collections;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;

public class PasswordAuthenticationProvider implements AuthenticationProvider {

  private final OAuth2TokenGenerator<?> tokenGenerator;
  private final RegisteredClientRepository clientRepository;
  private final IoTUserMapper iotUserMapper;
  private final BCryptPasswordEncoder passwordEncoder;
  private final OAuth2AuthorizationService authorizationService;

  public PasswordAuthenticationProvider(
      OAuth2TokenGenerator<?> tokenGenerator,
      RegisteredClientRepository clientRepository,
      IoTUserMapper iotUserMapper,
      OAuth2AuthorizationService authorizationService) {
    Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
    Assert.notNull(clientRepository, "clientRepository cannot be null");
    Assert.notNull(iotUserMapper, "iotUserMapper cannot be null");
    Assert.notNull(authorizationService, "authorizationService cannot be null");
    this.tokenGenerator = tokenGenerator;
    this.clientRepository = clientRepository;
    this.iotUserMapper = iotUserMapper;
    this.authorizationService = authorizationService;
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    PasswordAuthenticationToken passwordAuthenticationToken =
        (PasswordAuthenticationToken) authentication;

    // 从客户端认证令牌中获取RegisteredClient
    OAuth2ClientAuthenticationToken clientPrincipal =
        (OAuth2ClientAuthenticationToken) passwordAuthenticationToken.getPrincipal();
    RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

    // 验证用户名和密码
    String username = passwordAuthenticationToken.getUsername();
    String password = passwordAuthenticationToken.getPassword();

    if (username == null || password == null) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_request", "用户名或密码不能为空", null));
    }

    // 查询用户 - 使用 selectList 方法
    IoTUser queryUser = new IoTUser();
    queryUser.setUsername(username);
    java.util.List<IoTUser> users = iotUserMapper.selectList(queryUser);

    if (users == null || users.isEmpty()) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_request", "用户名或密码不能为空", null));
    }

    IoTUser iotUser = users.get(0);

    // 验证密码
    if (!passwordEncoder.matches(password, iotUser.getPassword())) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_request", "用户名或密码不能为空", null));
    }

    // 创建用户详情
    UserDetails userDetails =
        User.builder()
            .username(iotUser.getUsername())
            .password(iotUser.getPassword())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("USER")))
            .build();

    // 创建用户认证令牌
    UsernamePasswordAuthenticationToken userAuth =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    // 生成 OAuth2 令牌
    DefaultOAuth2TokenContext.Builder tokenContextBuilder =
        DefaultOAuth2TokenContext.builder()
            .registeredClient(registeredClient)
            .principal(userAuth)
            .authorizationServerContext(AuthorizationServerContextHolder.getContext())
            .authorizationGrantType(
                org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD)
            .authorizationGrant(passwordAuthenticationToken);

    OAuth2TokenContext tokenContext =
        tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
    OAuth2Token generatedAccessToken = tokenGenerator.generate(tokenContext);
    if (generatedAccessToken == null) {
      throw new OAuth2AuthenticationException(new OAuth2Error("server_error", "无法生成访问令牌", null));
    }

    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            generatedAccessToken.getTokenValue(),
            generatedAccessToken.getIssuedAt(),
            generatedAccessToken.getExpiresAt(),
            registeredClient.getScopes());

    OAuth2RefreshToken refreshToken = null;
    if (registeredClient
        .getAuthorizationGrantTypes()
        .contains(org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN)) {
      tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
      OAuth2Token generatedRefreshToken = tokenGenerator.generate(tokenContext);
      if (generatedRefreshToken != null) {
        refreshToken =
            new OAuth2RefreshToken(
                generatedRefreshToken.getTokenValue(),
                generatedRefreshToken.getIssuedAt(),
                generatedRefreshToken.getExpiresAt());
      }
    }

    // 保存授权信息
    OAuth2Authorization.Builder authorizationBuilder =
        OAuth2Authorization.withRegisteredClient(registeredClient)
            .principalName(userDetails.getUsername())
            .attribute(Principal.class.getName(), userAuth)
            .authorizationGrantType(
                org.springframework.security.oauth2.core.AuthorizationGrantType.PASSWORD)
            .authorizedScopes(registeredClient.getScopes());

    if (accessToken != null) {
      authorizationBuilder.accessToken(accessToken);
    }
    if (refreshToken != null) {
      authorizationBuilder.refreshToken(refreshToken);
    }

    OAuth2Authorization authorization = authorizationBuilder.build();
    authorizationService.save(authorization);

    return new OAuth2AccessTokenAuthenticationToken(
        registeredClient, clientPrincipal, accessToken, refreshToken);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}

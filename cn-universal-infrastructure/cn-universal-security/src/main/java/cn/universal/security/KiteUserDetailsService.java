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

package cn.universal.security;

import cn.universal.admin.system.service.AsyncService;
import cn.universal.common.constant.Constants;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.utils.ServletUtils;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.mapper.IoTUserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "kiteUserDetailsService")
public class KiteUserDetailsService implements UserDetailsService {

  @Resource private IoTUserMapper iotUserMapper;

  @Resource private AsyncService asyncService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("usernameis:" + username);

    // 首先尝试通过 username 查找用户
    IoTUser iotUser =
        IoTUser.builder().username(username).status(IoTConstant.NORMAL.toString()).build();
    try {
      iotUser = iotUserMapper.selectOne(iotUser);

      // 如果通过 username 没找到，尝试通过 unionId 查找
      if (iotUser == null) {
        iotUser = IoTUser.builder().unionId(username).status(IoTConstant.NORMAL.toString()).build();
        iotUser = iotUserMapper.selectOne(iotUser);
      }

      if (iotUser == null) {
        HttpServletRequest request = ServletUtils.getRequest();
        asyncService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名或密码错误", request);
        throw new UsernameNotFoundException("the user is not found");
      } else if (IoTConstant.UN_NORMAL.toString().equals(iotUser.getStatus())) {
        HttpServletRequest request = ServletUtils.getRequest();
        asyncService.recordLogininfor(
            iotUser.getUsername(), Constants.LOGIN_FAIL, "用户已冻结,请联系管理员解冻", request);
        throw new UsernameNotFoundException("the user is  disabled");
      }
      //      asyncService.recordLogininfor(iotUser.getUsername(), Constants.LOGIN_SUCCESS, "登录成功");
      // 返回自定义的 KiteUserDetails
      List<SimpleGrantedAuthority> Authorities = new ArrayList<>();
      Authorities.add(new SimpleGrantedAuthority("WEB"));

      // 处理密码编码格式，避免 BCrypt 警告
      String password = iotUser.getPassword();
      // 如果密码不是以 BCrypt 格式开头，添加 {noop} 前缀
      if (password != null
          && !password.startsWith("$2a$")
          && !password.startsWith("$2b$")
          && !password.startsWith("$2y$")
          && !password.startsWith("{")) {
        password = "{noop}" + password;
      }

      // 使用 unionId 作为 User 的 username，这样在 JWT 的 sub 字段中就是 unionId
      User user = new User(iotUser.getUnionId(), password, Authorities);
      // 更新登录时间
      iotUserMapper.updateLoginDate(iotUser.getId());
      return user;
    } catch (Exception e) {
      log.error("加载用户详情失败: " + username, e);
      throw new UsernameNotFoundException("the user is not found");
    }
  }
}

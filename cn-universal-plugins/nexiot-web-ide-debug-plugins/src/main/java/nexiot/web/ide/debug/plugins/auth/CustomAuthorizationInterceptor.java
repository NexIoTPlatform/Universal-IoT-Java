package nexiot.web.ide.debug.plugins.auth;

import cn.hutool.core.util.StrUtil;
import nexiot.web.ide.debug.plugins.core.context.MagicUser;
import nexiot.web.ide.debug.plugins.core.exception.MagicLoginException;
import nexiot.web.ide.debug.plugins.core.interceptor.Authorization;
import nexiot.web.ide.debug.plugins.core.interceptor.AuthorizationInterceptor;
import nexiot.web.ide.debug.plugins.core.model.Group;
import nexiot.web.ide.debug.plugins.core.model.MagicEntity;
import nexiot.web.ide.debug.plugins.core.servlet.MagicHttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** 自定义用户名密码登录 */
@Component // 注入到Spring容器中
@Slf4j
public class CustomAuthorizationInterceptor implements AuthorizationInterceptor {

  private Map<String, Object> user = new HashMap<>();
  @Autowired private UserDetailsService userDetailsService;

  /** 配置是否需要登录 */
  @Override
  public boolean requireLogin() {
    return true;
  }

  /** 根据Token获取User */
  @Override
  public MagicUser getUserByToken(String token) throws MagicLoginException {
    String username = SymmetricEncryptionUtil.desDecryptWithBuiltinKey(token);
    if (user.containsKey(username)) {
      return new MagicUser(
          username, username, SymmetricEncryptionUtil.desEncryptWithBuiltinKey(username));
    }
    throw new MagicLoginException("token无效");
  }

  @Override
  public MagicUser login(String username, String password) throws MagicLoginException {
    // 根据实际情况进行修改，如查询数据库。。
    if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
      throw new MagicLoginException("用户名或密码不能为空");
    }
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    // 注释密码验证，专注解决类型转换问题
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    if (!passwordEncoder.matches(password, userDetails.getPassword())) {
      throw new MagicLoginException("用户名或密码错误");
    }
    user.put(username, SymmetricEncryptionUtil.desEncryptWithBuiltinKey(username));
    return new MagicUser(
        username, username, SymmetricEncryptionUtil.desEncryptWithBuiltinKey(username));
  }

  @Override
  public boolean allowVisit(
      MagicUser magicUser,
      MagicHttpServletRequest request,
      Authorization authorization,
      MagicEntity entity) {
    if (entity.getCreateBy() == null) {
      return true;
    }
    if (magicUser.getUsername().equalsIgnoreCase(entity.getCreateBy())) {
      return true;
    }
    log.info("magicUser:{}, request:{}, entity:{}", magicUser, request, entity);
    return true;
  }

  @Override
  public boolean allowVisit(
      MagicUser magicUser,
      MagicHttpServletRequest request,
      Authorization authorization,
      Group group) {
    log.info("magicUser:{}, request:{}, group:{}", magicUser, request, group);
    if (group.getCreateBy() == null) {
      return true;
    }
    if (magicUser.getUsername().equalsIgnoreCase(group.getCreateBy())) {
      return true;
    }
    return false;
  }
}

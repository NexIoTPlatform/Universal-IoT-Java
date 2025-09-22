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

package cn.universal.admin.system.web;

import cn.hutool.core.util.StrUtil;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.permission.SysPermissionService;
import cn.universal.admin.system.service.IIotUserService;
import cn.universal.admin.system.service.ISysMenuService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.domain.R;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.admin.SysMenu;
import cn.universal.persistence.entity.admin.vo.RouterVo;
import cn.universal.persistence.query.AjaxResult;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录验证 @Author ruoyi */
@RestController
@RequestMapping("admin")
public class SysUserController {

  @Resource private IIotUserService iIotUserService;

  @Resource private ISysMenuService menuService;

  @Resource private SysPermissionService permissionService;
  @Resource private StringRedisTemplate stringRedisTemplate;

  /**
   * 获取用户信息
   *
   * @return 用户信息
   */
  @GetMapping("getInfo")
  public R getInfo(HttpServletRequest request) {
    final String unionId = SecurityUtils.getUnionId();
    IoTUser iotUser = iIotUserService.selectUserByUnionId(unionId);
    // 更新登录时间
    // 角色集合
    Set<String> roles = permissionService.getRolePermission(iotUser);
    // 权限集合
    Set<String> permissions = permissionService.getMenuPermission(iotUser);
    Map<String, Object> map = new HashMap<>();
    map.put("user", iotUser);
    map.put("roles", roles);
    map.put("permissions", permissions);
    return R.ok(map);
  }

  /**
   * 获取路由信息
   *
   * @return 路由信息
   */
  @GetMapping("getRouters")
  public AjaxResult<List<RouterVo>> getRouters() {
    List<SysMenu> menus = menuService.selectMenuTreeByUnionId(SecurityUtils.getUnionId());
    return AjaxResult.success(menuService.buildMenus(menus));
  }

  @GetMapping("logout")
  public AjaxResult<Void> logout(HttpServletRequest request) {
    String token = StringUtil.substringAfter(request.getHeader("Authorization"), ' ');
    String unionId = SecurityUtils.getUnionId();
    String key = IoTConstant.EXCLUSIVE_LOGIN + ":" + unionId;
    String tokenKey = IoTConstant.EXCLUSIVE_LOGIN_TOKEN + ":" + unionId;
    if (StrUtil.isNotBlank(token) && Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenKey))) {
      String origin = stringRedisTemplate.opsForValue().get(tokenKey);
      if (token.equals(origin)) {
        stringRedisTemplate.delete(key);
        stringRedisTemplate.delete(tokenKey);
      }
    }
    return AjaxResult.success();
  }
}

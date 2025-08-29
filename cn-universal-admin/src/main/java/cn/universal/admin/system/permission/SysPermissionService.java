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

package cn.universal.admin.system.permission;

import cn.universal.admin.system.service.ISysMenuService;
import cn.universal.admin.system.service.ISysRoleService;
import cn.universal.persistence.entity.IoTUser;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 用户权限处理服务 */
@Component
public class SysPermissionService {

  @Autowired private ISysRoleService roleService;

  @Autowired private ISysMenuService menuService;

  /** 获取角色数据权限 */
  public Set<String> getRolePermission(IoTUser user) {
    Set<String> roles = new HashSet<String>();
    // 管理员拥有所有权限
    if (user.isAdmin()) {
      roles.add("admin");
    } else {
      roles.addAll(roleService.selectRolePermissionByUnionId(user.getUnionId()));
    }
    return roles;
  }

  /**
   * 获取菜单数据权限
   *
   * @param user 用户信息
   * @return 菜单权限信息
   */
  public Set<String> getMenuPermission(IoTUser user) {
    Set<String> perms = new HashSet<String>();
    if (user.isAdmin()) {
      perms.add("*:*:*");
    } else {
      perms.addAll(menuService.selectMenuPermsByUserId(user.getUnionId()));
    }
    return perms;
  }
}

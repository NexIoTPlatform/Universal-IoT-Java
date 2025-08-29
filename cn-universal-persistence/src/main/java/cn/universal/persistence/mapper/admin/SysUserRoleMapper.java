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

package cn.universal.persistence.mapper.admin;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.admin.SysUserRole;

/** 用户与角色关联表 数据层 @Author ruoyi */
// @Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

  public int deleteUserRoleByUserIds(String[] ids);
}

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

package cn.universal.admin.system.service.impl;

import cn.hutool.core.lang.Validator;
import cn.universal.admin.system.service.ISysRoleService;
import cn.universal.common.annotation.DataScope;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.admin.SysRole;
import cn.universal.persistence.entity.admin.SysRoleMenu;
import cn.universal.persistence.entity.admin.SysUserRole;
import cn.universal.persistence.mapper.admin.SysRoleMapper;
import cn.universal.persistence.mapper.admin.SysRoleMenuMapper;
import cn.universal.persistence.mapper.admin.SysUserRoleMapper;
import cn.universal.security.utils.SecurityUtils;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

/** 角色 业务层处理 @Author ruoyi */
@Service
public class SysRoleServiceImpl implements ISysRoleService {

  @Resource private SysRoleMenuMapper roleMenuMapper;

  @Resource private SysUserRoleMapper userRoleMapper;
  @Resource private SysRoleMapper roleMapper;

  @Override
  //  @DataScope(alias = "ur", created = "r.create_by")
  public List<SysRole> selectPageRoleList(SysRole role) {
    return roleMapper.selectRoleList(role);
  }

  /**
   * 根据条件分页查询角色数据
   *
   * @param role 角色信息
   * @return 角色数据集合信息
   */
  @Override
  @DataScope(alias = "ur", created = "r.create_by")
  public List<SysRole> selectRoleList(SysRole role) {
    return roleMapper.selectRoleList(role);
  }

  /**
   * 根据用户ID查询角色
   *
   * @param unionId 用户unionId
   * @return 角色列表
   */
  @Override
  public List<SysRole> selectRolesByUnionId(String unionId) {
    List<SysRole> userRoles = roleMapper.selectRolePermissionByUnionId(unionId);
    List<SysRole> roles = roleMapper.selectRolePermissionByUnionId(SecurityUtils.getUnionId());
    if (CollectionUtils.isEmpty(roles) || roles.get(0).getRoleId() == 1) {
      roles = roleMapper.selectAll();
    }
    for (SysRole role : roles) {
      for (SysRole userRole : userRoles) {
        if (role.getRoleId().longValue() == userRole.getRoleId().longValue()) {
          role.setFlag(true);
        }
      }
    }
    return roles;
  }

  /**
   * 根据用户ID查询权限
   *
   * @param unionId 用户unionId
   * @return 权限列表
   */
  @Override
  public Set<String> selectRolePermissionByUnionId(String unionId) {
    List<SysRole> perms = roleMapper.selectRolePermissionByUnionId(unionId);
    Set<String> permsSet = new HashSet<>();
    for (SysRole perm : perms) {
      if (Validator.isNotNull(perm)) {
        permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
      }
    }
    return permsSet;
  }

  /**
   * 查询所有角色
   *
   * @return 角色列表
   */
  @Override
  public List<SysRole> selectRoleAll() {
    return roleMapper.selectRoleList(new SysRole());
  }

  /**
   * 根据用户ID获取角色选择框列表
   *
   * @param unionId 用户unionId
   * @return 选中角色ID列表
   */
  @Override
  public List<Long> selectRoleListByUnionId(String unionId) {
    return roleMapper.selectRoleListByUnionId(unionId);
  }

  /**
   * 通过角色ID查询角色
   *
   * @param roleId 角色ID
   * @return 角色对象信息
   */
  @Override
  public SysRole selectRoleById(Long roleId) {
    return roleMapper.selectByPrimaryKey(roleId);
  }

  /**
   * 校验角色名称是否唯一
   *
   * @param role 角色信息
   * @return 结果
   */
  @Override
  public String checkRoleNameUnique(SysRole role) {
    Long roleId = Validator.isNull(role.getRoleId()) ? -1L : role.getRoleId();
    Example ex = new Example(SysRole.class);
    ex.createCriteria().andEqualTo("roleName", role.getRoleName());
    List<SysRole> roles = roleMapper.selectByExample(ex);
    //    List<SysRole> roles = roleMapper
    //        .selectRoleList(SysRole.builder().roleName(role.getRoleName()).build());
    if (!CollectionUtils.isEmpty(roles)
        && roles.get(0).getRoleId().longValue() != roleId.longValue()) {
      return IoTUserConstants.NOT_UNIQUE;
    }
    return IoTUserConstants.UNIQUE;
  }

  /**
   * 校验角色权限是否唯一
   *
   * @param role 角色信息
   * @return 结果
   */
  @Override
  public String checkRoleKeyUnique(SysRole role) {
    Long roleId = Validator.isNull(role.getRoleId()) ? -1L : role.getRoleId();
    Example ex = new Example(SysRole.class);
    ex.createCriteria().andEqualTo("roleKey", role.getRoleKey());
    List<SysRole> roles = roleMapper.selectByExample(ex);
    if (!CollectionUtils.isEmpty(roles)
        && roles.get(0).getRoleId().longValue() != roleId.longValue()) {
      return IoTUserConstants.NOT_UNIQUE;
    }
    return IoTUserConstants.UNIQUE;
  }

  /**
   * 校验角色是否允许操作
   *
   * @param role 角色信息
   */
  @Override
  public void checkRoleAllowed(SysRole role) {
    if (Validator.isNotNull(role.getRoleId()) && role.isAdmin()) {
      throw new IoTException("不允许操作超级管理员角色");
    }
  }

  /**
   * 通过角色ID查询角色使用数量
   *
   * @param roleId 角色ID
   * @return 结果
   */
  @Override
  public int countUserRoleByRoleId(Long roleId) {
    // 当前用户！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
    //    Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
    Example ex = new Example(SysUserRole.class);
    ex.createCriteria().andEqualTo("roleId", roleId);
    return userRoleMapper.selectCountByExample(ex);
    //            .andNotEqualTo("userId", userId));
  }

  /**
   * 新增保存角色信息
   *
   * @param role 角色信息
   * @return 结果
   */
  @Override
  @Transactional
  public int insertRole(SysRole role) {
    // 新增角色信息
    role.setDataScope("4");
    role.setCreateTime(new Date());
    roleMapper.insertUseGeneratedKeys(role);
    return insertRoleMenu(role);
  }

  /**
   * 修改保存角色信息
   *
   * @param role 角色信息
   * @return 结果
   */
  @Override
  @Transactional
  public int updateRole(SysRole role) {
    // 修改角色信息
    role.setUpdateTime(new Date());
    roleMapper.updateByPrimaryKey(role);
    // 删除角色与菜单关联
    Example ex = new Example(SysRoleMenu.class);
    ex.createCriteria().andEqualTo("roleId", role.getRoleId());
    roleMenuMapper.deleteByExample(ex);
    return insertRoleMenu(role);
  }

  /**
   * 修改角色状态
   *
   * @param role 角色信息
   * @return 结果
   */
  @Override
  public int updateRoleStatus(SysRole role) {
    return roleMapper.updateByPrimaryKey(role);
  }

  /**
   * 修改数据权限信息
   *
   * @param role 角色信息
   * @return 结果
   */
  //  @Override
  //  @Transactional
  //  public int authDataScope(SysRole role) {
  //    // 修改角色信息
  //    roleMapper.updateByPrimaryKey(role);
  //    // 删除角色与部门关联
  //    roleDeptMapper
  //        .delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId,
  // role.getRoleId()));
  //    // 新增角色和部门信息（数据权限）
  //    return insertRoleDept(role);
  //  }

  /**
   * 新增角色菜单信息
   *
   * @param role 角色对象
   */
  public int insertRoleMenu(SysRole role) {
    int rows = 1;
    // 新增用户与角色管理
    List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
    for (Long menuId : role.getMenuIds()) {
      SysRoleMenu rm = new SysRoleMenu();
      rm.setRoleId(role.getRoleId());
      rm.setMenuId(menuId);
      list.add(rm);
    }
    if (list.size() > 0) {
      rows = roleMenuMapper.insertList(list);
    }
    return rows;
  }

  /**
   * 新增角色部门信息(数据权限)
   *
   * @param role 角色对象
   */
  //  public int insertRoleDept(SysRole role) {
  //    int rows = 1;
  //    // 新增角色与部门（数据权限）管理
  //    List<SysRoleDept> list = new ArrayList<SysRoleDept>();
  //    for (Long deptId : role.getDeptIds()) {
  //      SysRoleDept rd = new SysRoleDept();
  //      rd.setRoleId(role.getRoleId());
  //      rd.setDeptId(deptId);
  //      list.add(rd);
  //    }
  //    if (list.size() > 0) {
  //      rows = roleDeptMapper.insertAll(list);
  //    }
  //    return rows;
  //  }

  /**
   * 通过角色ID删除角色
   *
   * @param roleId 角色ID
   * @return 结果
   */
  @Override
  @Transactional
  public int deleteRoleById(Long roleId) {
    // 删除角色与菜单关联
    roleMenuMapper.deleteByPrimaryKey(roleId);
    // 删除角色与部门关联
    //    roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId,
    // roleId));
    return roleMapper.deleteByPrimaryKey(roleId);
  }

  /**
   * 批量删除角色信息
   *
   * @param roleIds 需要删除的角色ID
   * @return 结果
   */
  @Override
  @Transactional
  public int deleteRoleByIds(Long[] roleIds) {
    for (Long roleId : roleIds) {
      checkRoleAllowed(new SysRole(roleId));
      SysRole role = selectRoleById(roleId);
      if (countUserRoleByRoleId(roleId) > 0) {
        throw new IoTException(String.format("%1$s已分配,不能删除", role.getRoleName()));
      }
    }
    List<Long> ids = Arrays.asList(roleIds);
    String ids1 = ids.stream().map(Object::toString).collect(Collectors.joining(","));
    // 删除角色与菜单关联
    Example ex = new Example(SysRoleMenu.class);
    ex.createCriteria().andIn("roleId", ids);
    roleMenuMapper.deleteByExample(ex);
    // 删除角色与部门关联
    //    roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId,
    // ids));
    return roleMapper.deleteByIds(ids1);
  }

  /**
   * 取消授权用户角色
   *
   * @param userRole 用户和角色关联信息
   * @return 结果
   */
  @Override
  public int deleteAuthUser(SysUserRole userRole) {
    return userRoleMapper.delete(
        SysUserRole.builder().unionId(userRole.getUnionId()).roleId(userRole.getRoleId()).build());
  }

  /**
   * 批量取消授权用户角色
   *
   * @param roleId 角色ID
   * @param unionIds 需要取消授权的用户数据ID
   * @return 结果
   */
  @Override
  public int deleteAuthUsers(Long roleId, String[] unionIds) {
    Example ex = new Example(SysUserRole.class);
    ex.createCriteria().andEqualTo("roleId", roleId).andIn("unionId", Arrays.asList(unionIds));
    return userRoleMapper.deleteByExample(ex);
  }

  /**
   * 批量选择授权用户角色
   *
   * @param roleId 角色ID
   * @param unionIds 需要删除的用户数据ID
   * @return 结果
   */
  @Override
  public int insertAuthUsers(Long roleId, String[] unionIds) {
    // 新增用户与角色管理
    int rows = 1;
    List<SysUserRole> list = new ArrayList<SysUserRole>();
    for (String unionId : unionIds) {
      SysUserRole ur = new SysUserRole();
      ur.setUnionId(unionId);
      ur.setRoleId(roleId);
      list.add(ur);
    }
    if (list.size() > 0) {
      rows = userRoleMapper.insertList(list);
    }
    return rows;
  }

  @Override
  public String selectUserRoleGroup(String unionId) {
    List<SysRole> list = roleMapper.selectRolePermissionByUnionId(unionId);
    if (CollectionUtils.isEmpty(list)) {
      return "";
    }
    return list.stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
  }
}

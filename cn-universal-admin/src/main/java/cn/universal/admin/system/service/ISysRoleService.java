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

package cn.universal.admin.system.service;

import cn.universal.persistence.entity.admin.SysRole;
import cn.universal.persistence.entity.admin.SysUserRole;
import java.util.List;
import java.util.Set;

/** 角色业务层 @Author ruoyi */
public interface ISysRoleService {

  List<SysRole> selectPageRoleList(SysRole role);

  /**
   * 根据条件分页查询角色数据
   *
   * @param role 角色信息
   * @return 角色数据集合信息
   */
  public List<SysRole> selectRoleList(SysRole role);

  /**
   * 根据用户ID查询角色列表
   *
   * @param unionId 用户unionId
   * @return 角色列表
   */
  public List<SysRole> selectRolesByUnionId(String unionId);

  /**
   * 根据用户ID查询角色权限
   *
   * @param unionId 用户unionId
   * @return 权限列表
   */
  public Set<String> selectRolePermissionByUnionId(String unionId);

  /**
   * 查询所有角色
   *
   * @return 角色列表
   */
  public List<SysRole> selectRoleAll();

  /**
   * 根据用户ID获取角色选择框列表
   *
   * @param unionId 用户unionId
   * @return 选中角色ID列表
   */
  public List<Long> selectRoleListByUnionId(String unionId);

  /**
   * 通过角色ID查询角色
   *
   * @param roleId 角色ID
   * @return 角色对象信息
   */
  public SysRole selectRoleById(Long roleId);

  /**
   * 校验角色名称是否唯一
   *
   * @param role 角色信息
   * @return 结果
   */
  public String checkRoleNameUnique(SysRole role);

  /**
   * 校验角色权限是否唯一
   *
   * @param role 角色信息
   * @return 结果
   */
  public String checkRoleKeyUnique(SysRole role);

  /**
   * 校验角色是否允许操作
   *
   * @param role 角色信息
   */
  public void checkRoleAllowed(SysRole role);

  /**
   * 通过角色ID查询角色使用数量
   *
   * @param roleId 角色ID
   * @return 结果
   */
  public int countUserRoleByRoleId(Long roleId);

  /**
   * 新增保存角色信息
   *
   * @param role 角色信息
   * @return 结果
   */
  public int insertRole(SysRole role);

  /**
   * 修改保存角色信息
   *
   * @param role 角色信息
   * @return 结果
   */
  public int updateRole(SysRole role);

  /**
   * 修改角色状态
   *
   * @param role 角色信息
   * @return 结果
   */
  public int updateRoleStatus(SysRole role);

  /**
   * 修改数据权限信息
   *
   * @param role 角色信息
   * @return 结果
   */
  //  public int authDataScope(SysRole role);

  /**
   * 通过角色ID删除角色
   *
   * @param roleId 角色ID
   * @return 结果
   */
  public int deleteRoleById(Long roleId);

  /**
   * 批量删除角色信息
   *
   * @param roleIds 需要删除的角色ID
   * @return 结果
   */
  public int deleteRoleByIds(Long[] roleIds);

  /**
   * 取消授权用户角色
   *
   * @param userRole 用户和角色关联信息
   * @return 结果
   */
  public int deleteAuthUser(SysUserRole userRole);

  /**
   * 批量取消授权用户角色
   *
   * @param roleId 角色ID
   * @param unionIds 需要取消授权的用户数据ID
   * @return 结果
   */
  public int deleteAuthUsers(Long roleId, String[] unionIds);

  /**
   * 批量选择授权用户角色
   *
   * @param roleId 角色ID
   * @param unionIds 需要删除的用户数据ID
   * @return 结果
   */
  public int insertAuthUsers(Long roleId, String[] unionIds);

  /**
   * 根据用户ID查询用户所属角色组
   *
   * @param unionId 用户名
   * @return 结果
   */
  public String selectUserRoleGroup(String unionId);
}

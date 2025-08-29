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

import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTUserBO;
import java.util.List;

/** 用户业务层接口 */
public interface IIotUserService {

  //  TableDataInfo<IoTUser> selectPageUserList(IoTUserBO user);

  /**
   * 根据条件分页查询用户列表
   *
   * @param user 用户信息
   * @return 用户信息集合信息
   */
  public List<IoTUser> selectUserList(IoTUser user);

  /**
   * 根据条件分页查询已分配用户角色列表
   *
   * @param user 用户信息
   * @return 用户信息集合信息
   */
  public List<IoTUser> selectAllocatedList(IoTUserBO user);

  /**
   * 根据条件分页查询未分配用户角色列表
   *
   * @param user 用户信息
   * @return 用户信息集合信息
   */
  public List<IoTUser> selectUnallocatedList(IoTUserBO user);

  /**
   * 通过用户名查询用户
   *
   * @param userName 用户名
   * @return 用户对象信息
   */
  public IoTUser selectUserByUserName(String userName);

  /**
   * 通过用户ID查询用户
   *
   * @param userId 用户ID
   * @return 用户对象信息
   */
  public IoTUser selectUserById(Long userId);

  /**
   * 通过用户手机号查询用户
   *
   * @param mobile 用户手机号
   * @return 用户对象信息
   */
  public IoTUser selectUserByMobile(String mobile);

  /**
   * 通过用户unionId查询用户
   *
   * @param
   * @return 用户对象信息
   */
  public IoTUser selectUserByUnionId(String unionId);

  /**
   * 查询用户
   *
   * @param iotUser 查询条件
   * @return
   */
  IoTUser queryOne(IoTUser iotUser);

  /**
   * 根据用户ID查询用户所属角色组
   *
   * @param userName 用户名
   * @return 结果
   */
  //  public String selectUserRoleGroup(String userName);

  /**
   * 根据用户ID查询用户所属岗位组
   *
   * @param userName 用户名
   * @return 结果
   */
  //  public String selectUserPostGroup(String userName);

  /**
   * 校验用户名称是否唯一
   *
   * @param user 用户
   * @return 结果
   */
  public String checkUserNameUnique(IoTUser user);

  /**
   * 校验手机号码是否唯一
   *
   * @param user 用户
   * @return 结果
   */
  public String checkPhoneUnique(IoTUser user);

  /**
   * 校验email是否唯一
   *
   * @param user 用户信息
   * @return 结果
   */
  //  public String checkEmailUnique(IoTUser user);

  /**
   * 校验用户是否允许操作
   *
   * @param user 用户信息
   */
  public void checkUserAllowed(IoTUser user);

  /**
   * 新增用户信息
   *
   * @param userbo 用户信息
   * @return 结果
   */
  public int insertUser(IoTUserBO userbo);

  /**
   * 修改用户信息
   *
   * @param user 用户信息
   * @return 结果
   */
  public int updateUser(IoTUserBO user);

  void updateUserById(IoTUser iotUser);

  /**
   * 用户授权角色
   *
   * @param unionId 用户ID
   * @param roleIds 角色组
   */
  public void insertUserAuth(String unionId, Long[] roleIds);

  /**
   * 授权子账户
   *
   * @param id
   * @param unionId
   */
  public void addSubAccount(Long id, String unionId, int isSub);

  /**
   * 修改用户状态
   *
   * @param user 用户信息
   * @return 结果
   */
  //  public int updateUserStatus(IoTUser user);

  /**
   * 修改用户基本信息
   *
   * @param user 用户信息
   * @return 结果
   */
  //  public int updateUserProfile(IoTUser user);

  /**
   * 修改用户头像
   *
   * @param userName 用户名
   * @param avatar 头像地址
   * @return 结果
   */
  //  public boolean updateUserAvatar(String userName, String avatar);

  /**
   * 重置用户密码
   *
   * @param user 用户信息
   * @return 结果
   */
  //  public int resetPwd(IoTUser user);

  /**
   * 重置用户密码
   *
   * @param userName 用户名
   * @param password 密码
   * @return 结果
   */
  //  public int resetUserPwd(String userName, String password);

  /**
   * 通过用户ID删除用户
   *
   * @param userId 用户ID
   * @return 结果
   */
  public int deleteUserById(Long userId);

  /**
   * 批量删除用户信息
   *
   * @param userIds 需要删除的用户ID
   * @return 结果
   */
  public int deleteUserByIds(Long[] userIds);

  /**
   * 导入用户数据
   *
   * @param userList 用户数据列表
   * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
   * @param operName 操作用户
   * @return 结果
   */
  //  public String importUser(List<IoTUserBO> userList, Boolean isUpdateSupport, String operName);

  /*public int addUser(IoTUserBO sysUserBo, IoTUser user);*/
}

/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.mapper;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTUserBO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface IoTUserMapper extends BaseMapper<IoTUser> {

  /**
   * 更新登录时间
   *
   * @param id 用户ID
   */
  int updateLoginDate(Long id);

  List<IoTUser> selectList(IoTUser iotUser);

  /**
   * 根据条件分页查询未已配用户角色列表
   *
   * @param user 用户信息
   * @return 用户信息集合信息
   */
  public List<IoTUser> selectAllocatedList(IoTUserBO user);

  /**
   * 通过用户手机号查询用户
   *
   * @param mobile 用户手机号
   * @return 用户对象信息
   */
  public IoTUser selectUserByMobile(String mobile);

  /**
   * 根据条件分页查询未分配用户角色列表
   *
   * @param user 用户信息
   * @return 用户信息集合信息
   */
  public List<IoTUser> selectUnallocatedList(IoTUserBO user);

  /**
   * 扣除license额度
   *
   * @param unionId 用户唯一标识
   */
  public void licenseBuckle(String unionId);

  /**
   * 增加license额度
   *
   * @param unionId 用户唯一标识
   */
  public void licenseAdd(String unionId);

  /**
   * 增加license额度
   *
   * @param unionId 用户唯一标识
   */
  public void licenseRecharge(
      @Param("unionId") String unionId, @Param("licenseNumber") Integer licenseNumber);

  int doAccountDisable();
}

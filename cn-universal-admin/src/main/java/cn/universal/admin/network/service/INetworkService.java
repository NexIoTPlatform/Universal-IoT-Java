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

package cn.universal.admin.network.service;

import cn.universal.common.domain.R;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.Network;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.bo.NetworkBO;
import cn.universal.persistence.entity.vo.NetworkVO;
import cn.universal.persistence.query.NetworkQuery;
import java.util.List;

/**
 * 网络组件Service接口
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public interface INetworkService {

  boolean start(String productKey);

  boolean stop(String productKey);

  Object get(String productKey);

  boolean del(String productKey);

  List<NetworkVO> selectNetworkList(NetworkBO bo);

  NetworkVO getDetail(Long id);

  List<IoTDevice> queryMileSightList(NetworkBO bo);

  R insertDevInstance(IoTDeviceBO devInstancebo);

  R deleteDevInstanceByIds(String[] ids);

  void reloadTcpClient(String applicationId);

  /**
   * 查询网络组件列表
   *
   * @param query 查询条件
   * @return 分页结果
   */
  List<NetworkBO> selectNetworkList(NetworkQuery query);

  /**
   * 根据ID查询网络组件
   *
   * @param id 网络组件ID
   * @return 网络组件
   */
  NetworkVO selectNetworkById(Integer id);

  /**
   * 新增网络组件
   *
   * @param network 网络组件
   * @return 结果
   */
  int insertNetwork(Network network);

  /**
   * 修改网络组件
   *
   * @param network 网络组件
   * @return 结果
   */
  int updateNetwork(Network network);

  /**
   * 删除网络组件
   *
   * @param id 网络组件ID
   * @return 结果
   */
  int deleteNetworkById(Integer id);

  /**
   * 批量删除网络组件
   *
   * @param ids 网络组件ID数组
   * @return 结果
   */
  int deleteNetworkByIds(Integer[] ids);

  /**
   * 启动网络组件
   *
   * @param id 网络组件ID
   * @return 结果
   */
  int startNetwork(Integer id);

  /**
   * 停止网络组件
   *
   * @param id 网络组件ID
   * @return 结果
   */
  int stopNetwork(Integer id);

  /**
   * 重启网络组件
   *
   * @param id 网络组件ID
   * @return 结果
   */
  int restartNetwork(Integer id);

  /**
   * 获取网络类型列表
   *
   * @return 网络类型列表
   */
  List<String> getNetworkTypes();

  /**
   * 验证网络组件配置
   *
   * @param network 网络组件
   * @return 验证结果
   */
  boolean validateNetworkConfig(Network network);

  /**
   * 根据ID查询设备
   *
   * @param id 设备ID
   * @return 设备
   */
  IoTDevice getDeviceById(String id);
}

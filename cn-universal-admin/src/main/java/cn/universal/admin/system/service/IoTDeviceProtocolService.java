/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.system.service;

import cn.universal.admin.common.service.BaseService;
import cn.universal.persistence.entity.IoTDeviceProtocol;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTDeviceProtocolBO;
import java.util.List;
import java.util.Set;

/**
 * 设备协议Service接口
 *
 * @since 2023-01-06
 */
public interface IoTDeviceProtocolService extends BaseService {

  Set<String> method =
      Set.of(
          "decode",
          "encode",
          "preDecode",
          "codecAdd",
          "codecDelete",
          "codecUpdate",
          "codecQuery",
          "iotToYour",
          "yourToIot");

  int countProtocol(String id);

  /**
   * 查询设备协议
   *
   * @param id 设备协议主键
   * @return 设备协议
   */
  public IoTDeviceProtocol selectDevProtocolById(String id, String unionId);

  /** 包名查重 */
  int countByProvider(String provider);

  /**
   * 查询设备协议列表
   *
   * @param ioTDeviceProtocol 设备协议
   * @return 设备协议集合
   */
  public List<IoTDeviceProtocol> selectDevProtocolList(
      IoTDeviceProtocol ioTDeviceProtocol, IoTUser iotUser);

  /**
   * 新增设备协议
   *
   * @param ioTDeviceProtocol 设备协议
   * @return 结果
   */
  public int insertDevProtocol(IoTDeviceProtocol ioTDeviceProtocol);

  public int insertList(List<IoTDeviceProtocol> ioTDeviceProtocolList);

  /**
   * 修改设备协议
   *
   * @return 结果
   */
  public int updateDevProtocol(IoTDeviceProtocolBO ioTDeviceProtocolBO, IoTUser iotUser);

  /**
   * 批量删除设备协议
   *
   * @param ids 需要删除的设备协议主键集合
   * @return 结果
   */
  public int deleteDevProtocolByIds(String[] ids);

  /**
   * 删除设备协议信息
   *
   * @param id 设备协议主键
   * @return 结果
   */
  public int deleteDevProtocolById(String id);
}

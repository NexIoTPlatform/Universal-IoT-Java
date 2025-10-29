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

package cn.universal.admin.platform.service;

import cn.universal.common.domain.R;
import cn.universal.persistence.dto.GatewayPollingConfigDTO;
import cn.universal.persistence.entity.GatewayPollingConfig;
import java.util.List;

/**
 * 网关轮询服务接口
 * 
 * @author Aleo
 * @date 2025-10-26
 */
public interface IGatewayPollingService {

  /**
   * 保存或更新网关轮询配置
   * 
   * @param dto 配置DTO
   * @return 操作结果
   */
  R savePollingConfig(GatewayPollingConfigDTO dto);

  /**
   * 获取网关轮询配置
   * 
   * @param productKey 产品Key
   * @param deviceId 设备ID
   * @return 配置信息
   */
  R getPollingConfig(String productKey, String deviceId);

  /**
   * 删除网关轮询配置
   * 
   * @param productKey 产品Key
   * @param deviceId 设备ID
   * @return 操作结果
   */
  R deletePollingConfig(String productKey, String deviceId);

  /**
   * 执行单个网关的轮询任务
   * 
   * @param config 轮询配置
   */
  void pollGatewayDevice(GatewayPollingConfig config);

  /**
   * 查询待轮询的网关设备列表
   * 
   * @param intervalSeconds 轮询间隔
   * @return 待轮询设备列表
   */
  List<GatewayPollingConfig> getDuePollingDevices(int intervalSeconds);

  /**
   * 测试轮询 - 立即执行一次轮询
   * 
   * @param productKey 产品Key
   * @param deviceId 设备ID
   * @return 操作结果
   */
  R testPolling(String productKey, String deviceId);
}

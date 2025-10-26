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

package cn.universal.admin.platform.service;

import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionHistoryBO;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionTaskBO;
import cn.universal.persistence.entity.vo.IoTDeviceFunctionHistoryVO;
import cn.universal.persistence.entity.vo.IoTDeviceFunctionTaskVO;
import java.util.List;

/**
 * 设备Service接口
 *
 * @since 2025-12-24
 */
public interface IIoTDeviceFunctionTaskService {

  List<IoTDeviceFunctionTaskVO> selectTaskList(IoTDeviceFunctionTaskBO bo, IoTUser iotUser);

  List<IoTDeviceFunctionHistoryVO> queryFunctionListByTaskId(
      IoTDeviceFunctionHistoryBO bo, IoTUser iotUser);

  Boolean addFunctionTask(IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO, IoTUser iotUser);

  Boolean retryFunctionTask(IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO);
}

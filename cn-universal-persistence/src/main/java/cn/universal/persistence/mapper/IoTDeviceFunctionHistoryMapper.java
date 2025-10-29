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

package cn.universal.persistence.mapper;

import cn.universal.persistence.common.BaseMapper;
import cn.universal.persistence.entity.IoTDeviceFunctionHistory;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionTaskBO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 设备功能下发任务Mapper接口
 *
 * @since 2025-12-30
 */
public interface IoTDeviceFunctionHistoryMapper extends BaseMapper<IoTDeviceFunctionHistory> {

  int retryHistory(@Param("bo") IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO);

  void batchInsert(@Param("list") List<IoTDeviceFunctionHistory> histories);
}

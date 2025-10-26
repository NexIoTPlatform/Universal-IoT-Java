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

package cn.universal.dm.device.service.log;

import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.query.LogQuery;
import cn.universal.persistence.query.PageBean;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/9/23
 */
public interface IIoTDeviceDataService {

  /**
   * 保存日志消息
   *
   * @param upRequest 上行消息
   * @param ioTDeviceDTO 设备信息
   * @param ioTProduct 产品信息
   */
  void saveDeviceLog(BaseUPRequest upRequest, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct);

  default void saveDeviceLog(
      IoTDeviceLog ioTDeviceLog, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {}

  /**
   * 查询设备日志
   *
   * @param logQuery 查询条件
   * @return list
   */
  default PageBean<IoTDeviceLogVO> pageList(LogQuery logQuery) {
    return null;
  }

  /** 日志详情 */
  default IoTDeviceLogVO queryById(LogQuery logQuery) {
    return null;
  }

  /**
   * 事件详情
   *
   * @param productKey 产品标志
   */
  default PageBean<IoTDeviceEvents> queryEventTotal(String productKey, String iotId) {
    return null;
  }

  default PageBean<IoTDeviceLogMetadataVO> queryLogMeta(LogQuery logQuery) {
    return null;
  }
}

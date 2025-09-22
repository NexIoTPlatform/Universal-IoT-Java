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

package cn.universal.admin.platform.service.impl;

import cn.universal.admin.platform.service.IIoTDevLogService;
import cn.universal.admin.platform.service.IIoTProductService;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.bo.IoTDeviceLogBO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.mapper.IoTDeviceLogMapper;
import cn.universal.persistence.query.LogQuery;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 设备日志Service业务层处理
 *
 * @since 2025-12-30
 */
@Service
public class IoTDevLogServiceImpl implements IIoTDevLogService {

  @Autowired private IoTDeviceLogMapper ioTDeviceLogMapper;

  @Autowired private IIoTProductService devProductService;

  /** 分页查询设备日志列表 */
  @Override
  public List<IoTDeviceLogVO> queryPageList(IoTDeviceLogBO bo) {
    return ioTDeviceLogMapper.queryLogPageList(bo);
  }

  /** 根据日志id查询日志详情 */
  @Override
  public IoTDeviceLogVO queryById(LogQuery logQuery) {
    IoTDeviceLogVO ioTDeviceLogVO = ioTDeviceLogMapper.queryLogById(logQuery);
    //    IoTDeviceLogVO ioTDeviceLogVO = BeanUtil.toBean(devLog, IoTDeviceLogVO.class);
    return ioTDeviceLogVO;
  }

  /**
   * 获取设备事件的统计信息
   *
   * @param productId 产品Id
   * @param iotId 设备id
   */
  @Override
  public List<IoTDeviceEvents> queryEventTotal(String productId, String iotId) {
    List<IoTDeviceEvents> list = devProductService.selectDevEvents(productId);
    for (IoTDeviceEvents devEvent : list) {
      List<String> events = ioTDeviceLogMapper.queryEventTotalByEventAndId(devEvent.getId(), iotId);
      int size = events.size();
      if (size > 0) {
        devEvent.setTime(events.get(0));
        devEvent.setQty(size >= 100 ? "99+" : String.valueOf(size));
      }
    }
    return list;
  }
}

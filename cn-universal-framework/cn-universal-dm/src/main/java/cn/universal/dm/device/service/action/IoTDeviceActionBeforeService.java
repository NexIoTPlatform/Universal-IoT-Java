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

package cn.universal.dm.device.service.action;

import cn.universal.core.message.DownRequest;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 生命周期前置处理
 *
 * @version 1.0 @Author Aleo
 * @since 2023/5/30
 */
@Service("ioTDeviceActionBeforeService")
@Slf4j
public class IoTDeviceActionBeforeService implements IoTDeviceLifeCycle {

  @Override
  public void create(String productKey, String deviceId, DownRequest downRequest) {
    //    if (downRequest.getAppUnionId() != null) {
    //      IoTUser iotUser = IoTUser.builder().unionId(downRequest.getAppUnionId()).build();
    //      IoTUser iotUser1 = iotUserMapper.selectOne(iotUser);
    //      if (iotUser1.getLicenseTotalAmount() == null) {
    //        throw new IoTException("设备接入数已用完");
    //      }
    //      if (iotUser1.getLicense() <= 0) {
    //        throw new IoTException("设备接入数已用完");
    //      }
    //    }
  }

  @Override
  public void online(String productKey, String deviceId) {}

  @Override
  public void offline(String productKey, String deviceId) {}

  @Override
  public void update(String iotId) {}

  @Override
  public void enable(String iotId) {}

  @Override
  public void disable(String iotId) {}

  @Override
  public void delete(IoTDeviceDTO ioTDeviceDTO, DownRequest downRequest) {}
}

package cn.universal.dm.device.service;

import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/6/26 20:10
 */
@Slf4j
public abstract class AbstractCodecService {

  @Resource private IoTProductDeviceService iotProductDeviceService;
}

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

package cn.universal.dm.device.service.action;

import cn.universal.dm.device.service.impl.IoTCacheRemoveService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.IoTProductAction;
import cn.universal.persistence.entity.IoTProduct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * 产品生命周期，用于统一处理
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2023/6/8
 */
@Service("ioTProductActionService")
@Slf4j
public class IoTProductActionService implements IoTProductAction {

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list",
        "selectDevProductV4List",
        "iot_product_device"
      },
      allEntries = true)
  public void create(String productKey, String unionId) {
    // TODO 产品创建，清除列表缓存
    log.info("产品创建,productKey={},创建用户={}", productKey, unionId);
    iotCacheRemoveService.removeProductCache();
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list",
        "selectDevProductV4List",
        "iot_product_device"
      },
      allEntries = true)
  public void update(IoTProduct product) {}

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list",
        "selectDevProductV4List",
        "iot_product_device"
      },
      allEntries = true)
  public void delete(String productKey) {}

  @Override
  public void enable(String productKey) {}

  @Override
  public void disable(String productKey) {}

  @Override
  public void publish(String productKey) {}

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list",
        "selectDevProductV4List",
        "iot_product_device"
      },
      allEntries = true)
  public void metadataCreate(String productKey) {}

  @Override
  public void metadataUpdate(String productKey) {}

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list",
        "selectDevProductV4List",
        "iot_product_device"
      },
      allEntries = true)
  public void metadataDelete(String productKey) {}
}

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

package cn.universal.dm.device.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DeviceSubscribe;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTErrorCode;
import cn.universal.persistence.dto.IoTDeviceSubscribeBO;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import cn.universal.persistence.mapper.IoTDeviceSubscribeMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 设备消息订阅
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/12 16:10
 */
@Component
@Slf4j
public class IoTDeviceSubscribeService {

  /** 订阅所有 */
  private final String MSG_TYPE_ALL = "ALL";

  @Resource private IoTDeviceSubscribeMapper ioTDeviceSubscribeMapper;

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  public R doSubscribe(
      String iotId,
      String productKey,
      String creater,
      String instance,
      IoTDeviceSubscribeBO ioTDeviceSubscribeBO) {
    IoTDeviceSubscribe build =
        IoTDeviceSubscribe.builder()
            .subType(DeviceSubscribe.DEVICE.name())
            .iotId(iotId)
            .productKey(productKey)
            .build();
    int i = ioTDeviceSubscribeMapper.selectCount(build);
    if (i > IoTConstant.MAX_DEV_MSG_SUBSCRIBE_NUM) {
      return R.error(
          IoTErrorCode.DEV_SUBSCRIBE_REPEAT_ERROR.getCode(),
          IoTErrorCode.DEV_SUBSCRIBE_REPEAT_ERROR.getName());
    }
    if (ioTDeviceSubscribeBO == null
        || (StrUtil.isBlank(ioTDeviceSubscribeBO.getUrl())
            && StrUtil.isBlank(ioTDeviceSubscribeBO.getTopic()))) {
      return R.error(IoTErrorCode.DATA_CAN_NOT_NULL.getCode(), "订阅url和topic要求至少有一个");
    }
    build.setUrl(ioTDeviceSubscribeBO.getUrl());
    build.setTopic(ioTDeviceSubscribeBO.getTopic());
    build.setCreater(creater);
    build.setInstance(StrUtil.isBlank(instance) ? "0" : instance);
    build.setCreateDate(new Date());
    build.setSubType(DeviceSubscribe.DEVICE.name());
    build.setEnabled(true);
    MessageType messageType = MessageType.find(ioTDeviceSubscribeBO.getMsgType());
    if (messageType == null) {
      build.setMsgType(MessageType.ALL.getValue());
    } else {
      build.setMsgType(messageType.getValue().toUpperCase());
    }
    ioTDeviceSubscribeMapper.insertSelective(build);
    iotCacheRemoveService.removeIotDeviceSubscribeCache();
    return R.ok("订阅成功");
  }

  public R deleteSubscribe(String iotId, String creater, String instance) {
    IoTDeviceSubscribe build =
        IoTDeviceSubscribe.builder()
            .iotId(iotId)
            .creater(creater)
            .instance(StrUtil.isBlank(instance) ? "0" : instance)
            .build();
    ioTDeviceSubscribeMapper.delete(build);
    iotCacheRemoveService.removeIotDeviceSubscribeCache();
    return R.ok("删除成功");
  }

  @Cacheable(
      cacheNames = "iot_dev_subscribe",
      unless = "#result == null",
      keyGenerator = "redisKeyGenerate")
  public List<IoTDeviceSubscribe> selectByProductKeyAndMsgType(
      String productkey, String iotId, MessageType messageType) {
    if (StrUtil.isBlank(productkey)) {
      log.error("productId or iotId can not be null");
      return null;
    }
    IoTDeviceSubscribe query =
        IoTDeviceSubscribe.builder().enabled(Boolean.TRUE).productKey(productkey).build();
    List<IoTDeviceSubscribe> ioTDeviceSubscribeList = ioTDeviceSubscribeMapper.select(query);
    List<IoTDeviceSubscribe> subscribes = new ArrayList<>();
    if (CollectionUtil.isNotEmpty(ioTDeviceSubscribeList)) {
      for (IoTDeviceSubscribe sub : ioTDeviceSubscribeList) {
        // 产品级别
        if (DeviceSubscribe.PRODUCT.name().equalsIgnoreCase(sub.getSubType())) {
          if (checkMsg(messageType, sub)) {
            subscribes.add(sub);
          }
        }
        // 设备级别
        else if (DeviceSubscribe.DEVICE.name().equalsIgnoreCase(sub.getSubType())) {
          // 任一设备iotId为空,直接忽略
          if (StrUtil.isBlank(iotId) || StrUtil.isBlank(sub.getIotId())) {
            log.warn("设备订阅为空，跳过. iotId={} subIotId={}", iotId, sub.getIotId());
            continue;
          }
          // 设备不匹配直接忽略
          if (!sub.getIotId().equalsIgnoreCase(iotId)) {
            continue;
          }
          if (checkMsg(messageType, sub)) {
            subscribes.add(sub);
          }
        }
      }
    }
    return subscribes;
  }

  private boolean checkMsg(MessageType messageType, IoTDeviceSubscribe sub) {
    if (MSG_TYPE_ALL.equalsIgnoreCase(sub.getMsgType())) {
      return true;
    }
    if (messageType.name().equalsIgnoreCase(sub.getMsgType())) {
      return true;
    }
    return false;
  }
}

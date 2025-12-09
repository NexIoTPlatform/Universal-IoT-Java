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

package cn.universal.dm.device.service.wrapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.domain.R;
import cn.universal.core.metadata.AbstractPropertyMetadata;
import cn.universal.core.metadata.DeviceMetadata;
import cn.universal.core.metadata.PropertyMode;
import cn.universal.core.message.DownRequest;
import cn.universal.dm.device.service.impl.IoTCacheRemoveService;
import cn.universal.dm.device.service.impl.IoTDeviceShadowService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.IoTDownWrapper;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.shadow.Shadow;
import cn.universal.persistence.shadow.State;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service("ioTDeviceDownFunctionIntercept")
@Slf4j
public class IoTDeviceDownFunctionIntercept implements IoTDownWrapper {

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  @Autowired private IoTDeviceShadowService ioTDeviceShadowService;

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Autowired private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Autowired private IoTProductDeviceService ioTProductDeviceService;

  // 保存功能期望值
  @Override
  public R beforeFunctionOrConfigDown(
      IoTProduct product, IoTDevice ioTDevice, DownRequest downRequest) {
    if (downRequest.getFunction() == null) {
      return null;
    }
    String function = downRequest.getFunction().get("function").toString();
    JSONObject data = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    // 当方法是set开头且data里不为空 保存data内的数据到影子期望值
    if (IoTConstant.SET_DEVICE_DESIRED_PROPERTIES.equalsIgnoreCase(function)
        && ObjectUtil.isNotEmpty(data)) {
      // 过滤掉只读属性，只保留可读写（rw）的属性
      JSONObject filteredData = filterReadWriteProperties(product, data);
      if (ObjectUtil.isNotEmpty(filteredData)) {
        doShadow(ioTDevice, filteredData);
      }
    }
    return null;
  }

  private void doShadow(IoTDevice ioTDevice, JSONObject data) {
    try {
      JSONObject deviceShadowObj =
          ioTDeviceShadowService.getDeviceShadowObj(
              ioTDevice.getProductKey(), ioTDevice.getDeviceId());
      // 影子不存在时创建
      Boolean flag =
          stringRedisTemplate
              .opsForValue()
              .setIfAbsent("doCreateShadow:" + ioTDevice.getIotId(), "1", 10, TimeUnit.MINUTES);
      if (ObjectUtil.isNull(deviceShadowObj) && flag) {
        IoTDeviceShadow ioTDeviceShadow =
            IoTDeviceShadow.builder()
                .iotId(ioTDevice.getIotId())
                .productKey(ioTDevice.getProductKey())
                .extDeviceId(ioTDevice.getExtDeviceId())
                .deviceId(ioTDevice.getDeviceId())
                .activeTime(new Date())
                .onlineTime(new Date())
                .updateDate(new Date())
                .lastTime(new Date())
                .build();
        State state = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();
        State metadata =
            State.builder().desired(new JSONObject()).reported(new JSONObject()).build();
        Shadow shadow =
            Shadow.builder()
                .state(state)
                .metadata(metadata)
                .timestamp(DateUtil.currentSeconds())
                .version(1L)
                .build();
        ioTDeviceShadow.setMetadata(JSONUtil.toJsonStr(shadow));
        ioTDeviceShadowMapper.insert(ioTDeviceShadow);
        // 代理调用内部方法，清除缓存
        iotCacheRemoveService.removeDevInstanceBOCache();
      }
      Shadow shadow = JSONUtil.toBean(deviceShadowObj, Shadow.class);
      doDesired(shadow, ioTDevice, data);
    } catch (Exception e) {
      log.error("SET_DEVICE_DESIRED_PROPERTIES 出现异常", e);
    }
  }

  private void doDesired(Shadow shadow, IoTDevice ioTDevice, JSONObject data) {
    // 设置期望值
    shadow.getState().getDesired().putAll(data);
    // 设置期望值时间
    JSONObject metaTime = new JSONObject();
    Timestamp timestamp = new Timestamp(DateUtil.currentSeconds());

    for (String key : data.keySet()) {
      metaTime.set(key, timestamp);
    }
    shadow.getMetadata().getDesired().putAll(metaTime);
    // 处理时间和版本
    shadow.setTimestamp(DateUtil.currentSeconds());
    shadow.setVersion(
        (shadow.getVersion() == null || shadow.getVersion() <= 0) ? 1L : (shadow.getVersion() + 1));
    if (ioTDevice != null) {
      ioTDeviceShadowService.writeShadowToCache(
          ioTDevice.getIotId() == null
              ? (ioTDevice.getProductKey() + ioTDevice.getDeviceId())
              : ioTDevice.getIotId(),
          shadow);
    }
  }

  /**
   * 过滤掉只读属性，只保留可读写（rw）的属性
   * 
   * @param product 产品信息
   * @param data 原始数据
   * @return 过滤后的数据，只包含可读写属性
   */
  private JSONObject filterReadWriteProperties(IoTProduct product, JSONObject data) {
    if (product == null || data == null || data.isEmpty()) {
      return data;
    }

    try {
      // 获取产品的物模型元数据
      DeviceMetadata metadata = ioTProductDeviceService.getDeviceMetadata(product.getProductKey());
      if (metadata == null) {
        log.warn("无法获取产品物模型元数据，productKey: {}", product.getProductKey());
        return data;
      }

      // 创建过滤后的数据对象
      JSONObject filteredData = new JSONObject();
      
      // 遍历原始数据，只保留可读写属性
      for (String propertyId : data.keySet()) {
        AbstractPropertyMetadata property = metadata.getPropertyOrNull(propertyId);
        if (property != null) {
          String mode = property.getMode();
          // 只保留 mode 为 rw（读写）的属性
          if (PropertyMode.rw.name().equals(mode)) {
            filteredData.set(propertyId, data.get(propertyId));
          } else {
            log.debug("过滤掉只读属性: {}, mode: {}", propertyId, mode);
          }
        } else {
          // 如果物模型中找不到该属性，默认保留（向后兼容）
          log.warn("物模型中未找到属性: {}, 默认保留", propertyId);
          filteredData.set(propertyId, data.get(propertyId));
        }
      }

      return filteredData;
    } catch (Exception e) {
      log.error("过滤可读写属性时发生异常，productKey: {}", product.getProductKey(), e);
      // 发生异常时返回原始数据，避免影响功能
      return data;
    }
  }

  @AllArgsConstructor
  @Getter
  private class Timestamp {

    private Long timestamp;
  }
}

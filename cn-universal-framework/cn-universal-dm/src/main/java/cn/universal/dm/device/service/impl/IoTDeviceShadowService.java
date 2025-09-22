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
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.UPRequest;
import cn.universal.core.metadata.AbstractPropertyMetadata;
import cn.universal.dm.device.entity.IoTDevicePropertiesBO;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.dto.LogStorePolicyDTO;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.entity.admin.SysDictData;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.mapper.admin.SysDictDataMapper;
import cn.universal.persistence.shadow.Shadow;
import cn.universal.persistence.shadow.State;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/9/17
 */
@Component
@Slf4j
public class IoTDeviceShadowService {

  @Resource private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Resource private SysDictDataMapper dictDataMapper;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  // 注入优化版服务
  @Resource private IoTDeviceShadowServiceOptimized ioTDeviceShadowServiceOptimized;

  public IoTDeviceShadow getDeviceShadow(String iotId) {
    return ioTDeviceShadowMapper.getDeviceShadow(iotId);
  }

  public JSONObject getDeviceShadowObj(String productKey, String deviceId) {
    if (StrUtil.isBlank(productKey) || StrUtil.isBlank(deviceId)) {
      return null;
    }
    String shadowMetadata = ioTDeviceShadowMapper.getShadowMetadata(productKey, deviceId);
    if (StrUtil.isBlank(shadowMetadata)) {
      return null;
    }
    return JSONUtil.parseObj(shadowMetadata);
  }

  //  @Cacheable(cacheNames = "iot_dev_shadow_bo", key = "''+#iotId", unless = "#result==null")
  public List<IoTDevicePropertiesBO> getDevState(String iotId) {
    List<IoTDevicePropertiesBO> result = new ArrayList<>();
    IoTDeviceShadow ioTDeviceShadow =
        ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
    if (ioTDeviceShadow == null) {
      //      throw new IoTException("设备=[" + iotId + "]影子不存在,清检查");
      log.warn("设备=[" + iotId + "]影子不存在,清检查");
      return result;
    }
    Shadow shadow = JSONUtil.toBean(ioTDeviceShadow.getMetadata(), Shadow.class);

    JSONObject properties = shadow.getState().getReported();
    JSONObject desireProperties = shadow.getState().getDesired();
    Map<String, Object> map = new HashMap<>();
    map.put("iotId", iotId);
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(map);
    List<AbstractPropertyMetadata> propertyMetadataList =
        ioTDeviceDTO.getDeviceMetadata().getProperties();
    if (CollectionUtil.isEmpty(propertyMetadataList)) {
      return result;
    }
    LogStorePolicyDTO storePolicyDTO =
        iotProductDeviceService.getProductLogStorePolicy(ioTDeviceDTO.getProductKey());
    // 过滤没有上报过的属性
    propertyMetadataList.stream()
        .filter(s -> properties.get(s.getId()) != null)
        .forEach(
            s -> {
              Object obj = properties.get(s.getId());
              IoTDevicePropertiesBO entity = new IoTDevicePropertiesBO();
              entity.setDesireValue(desireProperties.get(s.getId()));
              entity.withValue(s.getValueType(), obj);
              entity.setPropertyName(s.getName());
              entity.setIotId(iotId);
              entity.setDeviceId(ioTDeviceDTO.getDeviceId());
              entity.setTimestamp(
                  shadow.getMetadata().getReported().getJSONObject(s.getId()).getLong("timestamp"));
              entity.setProperty(s.getId());
              entity.setStoragePolicy(storePolicyDTO.getProperties().containsKey(s.getId()));
              // 期望值
              if (desireProperties.get(s.getId()) != null) {
                entity.setCustomized(IoTConstant.DEVICE_SHADOW_DESIRED_PROPERTY);
              }
              result.add(entity);
            });
    result.addAll(devCommonProperties(ioTDeviceDTO));
    return result;
  }

  /** 数据为空也返回结果 */
  public List<IoTDevicePropertiesBO> getDevStateWithNullResult(String iotId) {
    List<IoTDevicePropertiesBO> result = new ArrayList<>();
    IoTDeviceShadow ioTDeviceShadow =
        ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
    Shadow shadow = null;
    JSONObject properties = null;
    JSONObject desireProperties = null;

    if (ioTDeviceShadow == null) {
      //      throw new IoTException("设备=[" + iotId + "]影子不存在,清检查");
      log.warn("设备=[" + iotId + "]影子不存在,清检查");
      shadow = Shadow.builder().build();
      properties = new JSONObject();
      desireProperties = new JSONObject();
    } else {
      shadow = JSONUtil.toBean(ioTDeviceShadow.getMetadata(), Shadow.class);
      properties = shadow.getState().getReported();
      desireProperties = shadow.getState().getDesired();
    }
    Map<String, Object> map = new HashMap<>();
    map.put("iotId", iotId);
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(map);
    if (ioTDeviceDTO == null) {
      return result;
    }
    List<AbstractPropertyMetadata> propertyMetadataList =
        ioTDeviceDTO.getDeviceMetadata().getProperties();
    if (CollectionUtil.isEmpty(propertyMetadataList)) {
      return result;
    }
    // 过滤没有上报过的属性
    Shadow finalShadow = shadow;
    JSONObject finalProperties = properties;
    JSONObject finalDesireProperties = desireProperties;
    propertyMetadataList.forEach(
        s -> {
          Object obj = finalProperties.get(s.getId());
          IoTDevicePropertiesBO entity = new IoTDevicePropertiesBO();
          long times = 0;
          if (obj != null) {
            entity.withValue(s.getValueType(), obj);
            entity.setTimestamp(
                finalShadow
                    .getMetadata()
                    .getReported()
                    .getJSONObject(s.getId())
                    .getLong("timestamp"));
          } else {
            entity.withValue(s.getValueType(), null);
            entity.setTimestamp(System.currentTimeMillis());
          }
          entity.withValue(s.getValueType(), obj);
          entity.setPropertyName(s.getName());
          entity.setIotId(iotId);
          entity.setDeviceId(ioTDeviceDTO.getDeviceId());
          entity.setProperty(s.getId());
          // 期望值
          if (finalDesireProperties.get(s.getId()) != null) {
            entity.setDesireValue(finalDesireProperties.get(s.getId()));
            entity.setCustomized(IoTConstant.DEVICE_SHADOW_DESIRED_PROPERTY);
          }
          result.add(entity);
        });
    result.addAll(devCommonProperties(ioTDeviceDTO));
    return result;
  }

  private List<SysDictData> queryCommonPropertyDict() {
    String key = "dict:" + IoTConstant.DEVICE_SHADOW_CUSTOMIZED_PROPERTY;
    String jsonStr = stringRedisTemplate.opsForValue().get(key);
    List<SysDictData> sysDictData = null;
    if (StrUtil.isNotBlank(jsonStr)) {
      try {
        sysDictData = JSONUtil.toList(jsonStr, SysDictData.class);
      } catch (Exception e) {
        log.warn("Failed to deserialize Redis data for key: {}, error: {}", key, e.getMessage());
      }
    }
    if (CollectionUtil.isEmpty(sysDictData)) {
      sysDictData =
          dictDataMapper.selectDictDataByType(IoTConstant.DEVICE_SHADOW_CUSTOMIZED_PROPERTY);
      if (CollectionUtil.isNotEmpty(sysDictData)) {
        stringRedisTemplate
            .opsForValue()
            .set(key, JSONUtil.toJsonStr(sysDictData), 2, TimeUnit.HOURS);
      }
    }
    return sysDictData;
  }

  /**
   * 处理部分产品设备配置的表号，imei，deviceId字段
   *
   * <p>查看dict字段表`device_common_property`
   */
  private List<IoTDevicePropertiesBO> devCommonProperties(IoTDeviceDTO ioTDeviceDTO) {
    List<IoTDevicePropertiesBO> result = new ArrayList<>();
    if (ioTDeviceDTO == null) {
      return result;
    }
    if (StrUtil.isNotBlank(ioTDeviceDTO.getDevConfiguration())) {
      JSONObject cfg = JSONUtil.parseObj(ioTDeviceDTO.getDevConfiguration());
      List<SysDictData> property = queryCommonPropertyDict();
      if (CollectionUtil.isNotEmpty(property)) {
        property.stream()
            .forEach(
                s -> {
                  if (cfg.containsKey(s.getDictValue())) {
                    IoTDevicePropertiesBO imeiValue =
                        IoTDevicePropertiesBO.builder()
                            .iotId(ioTDeviceDTO.getIotId())
                            .deviceId(ioTDeviceDTO.getDeviceId())
                            .formatValue(cfg.getStr(s.getDictValue()))
                            .property(s.getDictValue())
                            .propertyName(s.getDictLabel())
                            .value(cfg.getStr(s.getDictValue()))
                            .timestamp(0L)
                            .stringValue(cfg.getStr(s.getDictValue()))
                            .customized(IoTConstant.DEVICE_SHADOW_CUSTOMIZED_PROPERTY)
                            .build();
                    result.add(imeiValue);
                  }
                });
      }
    }
    return result;
  }

  public void doShadowOriginal(UPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    // 设备影子创建或更新
    boolean flag =
        stringRedisTemplate
            .opsForValue()
            .setIfAbsent("doCreateShadow:" + ioTDeviceDTO.getIotId(), "1", 10, TimeUnit.MINUTES);
    if (!ioTDeviceDTO.isShadow() && flag) {
      IoTDeviceShadow ioTDeviceShadow =
          IoTDeviceShadow.builder()
              .iotId(ioTDeviceDTO.getIotId())
              .extDeviceId(ioTDeviceDTO.getExtDeviceId())
              .productKey(ioTDeviceDTO.getProductKey())
              .deviceId(ioTDeviceDTO.getDeviceId())
              .activeTime(new Date())
              .onlineTime(new Date())
              .updateDate(new Date())
              .lastTime(new Date())
              .build();
      State state = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();
      State metadata = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();
      Shadow shadow =
          Shadow.builder()
              .state(state)
              .metadata(metadata)
              .timestamp(DateUtil.currentSeconds())
              .version(1L)
              .build();
      doDesired(shadow, ioTDeviceDTO, upRequest);
      ioTDeviceShadow.setMetadata(JSONUtil.toJsonStr(shadow));
      ioTDeviceShadowMapper.insertSelective(ioTDeviceShadow);
      // 代理调用内部方法，清除缓存
      iotCacheRemoveService.removeDevInstanceBOCache();
    } else {
      // 更新最后通信时间
      IoTDeviceShadow ioTDeviceShadow =
          IoTDeviceShadow.builder().iotId(upRequest.getIotId()).build();
      ioTDeviceShadow = ioTDeviceShadowMapper.selectOne(ioTDeviceShadow);
      Shadow shadow = null;
      if (ioTDeviceShadow == null || StrUtil.isBlank(ioTDeviceShadow.getMetadata())) {
        shadow = Shadow.builder().timestamp(DateUtil.currentSeconds()).version(1L).build();
      } else {
        shadow = JSONUtil.toBean(ioTDeviceShadow.getMetadata(), Shadow.class);
      }
      doDesired(shadow, ioTDeviceDTO, upRequest);
      ioTDeviceShadow.setMetadata(JSONUtil.toJsonStr(shadow));
      ioTDeviceShadow.setLastTime(new Date());
      ioTDeviceShadow.setUpdateDate(new Date());
      ioTDeviceShadowMapper.updateByPrimaryKeySelective(ioTDeviceShadow);
    }
  }

  /**
   * 影子期望值
   *
   * @param shadow 影子
   * @param ioTDeviceDTO 设备信息
   * @param request 消息原文
   */
  private void doDesired(Shadow shadow, IoTDeviceDTO ioTDeviceDTO, UPRequest request) {
    if (request == null) {
      return;
    }
    Map<String, Object> properties = request.getProperties();
    Map<String, Object> data = request.getData();
    // 属性上报
    if (CollectionUtil.isNotEmpty(properties)) {
      // 如果属性中存在iccid，进行存储
      Object iccid = properties.get("iccid");
      if (iccid != null) {
        String iccidLast = null;
        JSONObject configuration = JSONUtil.parseObj(ioTDeviceDTO.getDevConfiguration());
        String icc = configuration.getStr("iccid");
        if (iccid != null && !iccid.equals(icc)) {
          iccidLast = iccid.toString();
        }
        configuration.set("iccid", iccidLast);
        Map<String, String> map = new HashMap<>();
        map.put("deviceId", ioTDeviceDTO.getDeviceId());
        map.put("configuration", configuration.toString());
        ioTDeviceMapper.updateDevConfiguration(map);
      }

      /** 开始处理state */
      State state = shadow.getState() != null ? shadow.getState() : new State();
      // 处理state-reported
      JSONObject stateReported =
          state.getReported() != null ? state.getReported() : new JSONObject();
      properties.forEach(
          (property, value) -> {
            stateReported.set(property, value);
          });
      state.setReported(stateReported);
      // 处理state-reported
      // TODO 设备期望值功能暂未实现，
      JSONObject stateDesired = state.getDesired() != null ? state.getDesired() : new JSONObject();
      state.setDesired(stateDesired);
      /** 开始处理metadata */
      State metadata = shadow.getMetadata() != null ? shadow.getMetadata() : new State();
      // 处理state-reported
      JSONObject metadataReported =
          metadata.getReported() != null ? metadata.getReported() : new JSONObject();

      properties.forEach(
          (property, value) -> {
            metadataReported.set(property, new Timestamp(DateUtil.currentSeconds()));
          });
      // 处理state-reported
      // TODO 设备期望值功能暂未实现，
      JSONObject metadataDesired =
          metadata.getDesired() != null ? metadata.getDesired() : new JSONObject();
      metadata.setDesired(metadataDesired);
      metadata.setReported(metadataReported);

      shadow.setMetadata(metadata);
      shadow.setState(state);
      // 处理时间和版本
      shadow.setTimestamp(DateUtil.currentSeconds());
      shadow.setVersion(
          (shadow.getVersion() == null || shadow.getVersion() <= 0)
              ? 1L
              : (shadow.getVersion() + 1));
    }

    // 处理消息类型为reply且data不为空
    if (MessageType.REPLY.equals(request.getMessageType()) && CollectionUtil.isNotEmpty(data)) {
      /** 开始处理state */
      State state = shadow.getState() != null ? shadow.getState() : new State();
      JSONObject stateReported =
          state.getReported() != null ? state.getReported() : new JSONObject();
      JSONObject stateDesired = state.getDesired() != null ? state.getDesired() : new JSONObject();

      // 处理metadata
      State metadata = shadow.getMetadata() != null ? shadow.getMetadata() : new State();
      JSONObject metadataReported =
          metadata.getReported() != null ? metadata.getReported() : new JSONObject();
      JSONObject metadataDesired =
          metadata.getReported() != null ? metadata.getDesired() : new JSONObject();
      Timestamp timestamp = new Timestamp(DateUtil.currentSeconds());

      data.forEach(
          (key, value) -> {
            if (stateDesired.containsKey(key)) {
              stateReported.set(key, stateDesired.get(key));
              // 删除当前reply回复的期望
              stateDesired.remove(key);
              metadataDesired.remove(key);
              metadataReported.set(key, timestamp);
            }
            // 回复内有数据时以回复为准
            if (!"".equals(value)) {
              stateReported.set(key, value);
              metadataReported.set(key, timestamp);
            }
          });
      state.setReported(stateReported);
      state.setDesired(stateDesired);
      metadata.setDesired(metadataDesired);
      metadata.setReported(metadataReported);
      shadow.setMetadata(metadata);
      shadow.setState(state);
      // 处理时间和版本
      shadow.setTimestamp(DateUtil.currentSeconds());
      shadow.setVersion(
          (shadow.getVersion() == null || shadow.getVersion() <= 0)
              ? 1L
              : (shadow.getVersion() + 1));
    }
  }

  @AllArgsConstructor
  @Getter
  private class Timestamp {

    private Long timestamp;
  }

  // ==================== 新优化方法 ====================

  /** 优化的设备影子处理方法 - 直接调用优化类 */
  public void doShadow(UPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    doShadowOriginal(upRequest, ioTDeviceDTO);
    //    try {
    //      // 直接调用优化类的doShadow方法
    //      if (ioTDeviceShadowServiceOptimized != null) {
    //        ioTDeviceShadowServiceOptimized.doShadow(upRequest, ioTDeviceDTO);
    //      } else {
    //        log.warn("优化版服务未注入，使用原方法");
    //        doShadowOriginal(upRequest, ioTDeviceDTO);
    //      }
    //    } catch (Exception e) {
    //      log.error("优化方法处理失败，降级到原方法: iotId={}, error={}", upRequest.getIotId(), e.getMessage(),
    // e);
    //      // 降级处理：使用原来的同步方式
    //      doShadowOriginal(upRequest, ioTDeviceDTO);
    //    }
  }
}

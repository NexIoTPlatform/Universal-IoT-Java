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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 设备影子处理
 *
 * @version 1.0 @Author gitee.com/NexIoT
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

  @Value("${shadow.cache.enabled:true}")
  private boolean shadowCacheEnabled;

  @Value("${shadow.cache.key-prefix:shadow}")
  private String shadowKeyPrefix;

  @Value("${shadow.cache.ttl-seconds:604800}") // 7 days
  private long shadowCacheTtlSeconds;

  @Value("${shadow.flush.zset-key:shadow:flush}")
  private String shadowFlushZsetKey;

  @Value("${shadow.flush.base-interval-ms:7200000}") // 24h
  private long flushBaseIntervalMs;

  @Value("${shadow.flush.jitter-rate:0.2}") // ±20%
  private double flushJitterRate;

  // 强制刷盘配置 - 使用默认值，不增加环境配置
  private static final long MAX_DELAY_MS = 86400000L; // 24小时最大延迟
  private static final long MIN_INTERVAL_MS = 3600000L; // 最小间隔：1小时
  private static final int VERSION_THRESHOLD = 10; // 版本号阈值：10
  private static final long FORCE_FLUSH_DELAY_MS = 30000L; // 强制刷盘延迟：30秒（确保能被5分钟扫描间隔捕获）

  private String buildShadowKey(String iotId) {
    return shadowKeyPrefix + ":" + iotId;
  }

  private Long calcNextFlushAtMs(String iotId) {
    long now = System.currentTimeMillis();

    // 获取当前已设置的刷盘时间
    Double currentScore = stringRedisTemplate.opsForZSet().score(shadowFlushZsetKey, iotId);

    if (currentScore != null) {
      long currentFlushTime = currentScore.longValue();
      long timeSinceLastFlush = now - (currentFlushTime - flushBaseIntervalMs);

      // 1. 最大延迟时间检查
      if (timeSinceLastFlush > MAX_DELAY_MS) {
        log.debug("[ShadowFlush] 最大延迟强制刷盘: iotId={}, 延迟时间={}ms", iotId, timeSinceLastFlush);
        return now + FORCE_FLUSH_DELAY_MS;
      }

      // 2. 最小间隔检查
      if (timeSinceLastFlush >= MIN_INTERVAL_MS) {
        log.debug("[ShadowFlush] 最小间隔强制刷盘: iotId={}, 间隔时间={}ms", iotId, timeSinceLastFlush);
        return now + FORCE_FLUSH_DELAY_MS;
      }

      // 3. 版本号检查
      if (checkVersionThreshold(iotId)) {
        log.debug("[ShadowFlush] 版本号强制刷盘: iotId={}", iotId);
        return now + FORCE_FLUSH_DELAY_MS;
      }

      // 如果当前刷盘时间还没到，保持原时间
      if (currentFlushTime > now) {
        return currentFlushTime;
      }
    }

    // 正常计算新的刷盘时间
    double jitter = (Math.random() * 2 * flushJitterRate) - flushJitterRate;
    long offset = (long) (flushBaseIntervalMs * (1 + jitter));
    return now + Math.max(0L, offset);
  }

  private Shadow readShadowFromCache(String iotId) {
    if (!shadowCacheEnabled) {
      return null;
    }
    final String json = buildAndGetFromRedis(iotId);
    if (json == null) return null;
    try {
      return JSONUtil.toBean(json, Shadow.class);
    } catch (Exception e) {
      log.warn("[Shadow][Cache] parse error iotId={}, err={}", iotId, e.getMessage());
      return null;
    }
  }

  private String buildAndGetFromRedis(String iotId) {
    String key = buildShadowKey(iotId);
    String json = stringRedisTemplate.opsForValue().get(key);
    if (StrUtil.isBlank(json)) {
      return null;
    }
    return json;
  }

  private void writeShadowToCache(String iotId, Shadow shadow) {
    if (!shadowCacheEnabled || shadow == null) {
      return;
    }
    String key = buildShadowKey(iotId);
    stringRedisTemplate
        .opsForValue()
        .set(key, JSONUtil.toJsonStr(shadow), shadowCacheTtlSeconds, TimeUnit.SECONDS);
  }

  private void markShadowDirtyForFlush(String iotId) {
    if (!shadowCacheEnabled) {
      return;
    }
    try {
      Long nextAt = calcNextFlushAtMs(iotId);
      stringRedisTemplate.opsForZSet().add(shadowFlushZsetKey, iotId, nextAt.doubleValue());
    } catch (Exception e) {
      log.warn("[Shadow][Flush] mark dirty failed iotId={}, err={}", iotId, e.getMessage());
    }
  }

  public JSONObject getDeviceShadowObj(String productKey, String deviceId) {
    if (StrUtil.isBlank(productKey) || StrUtil.isBlank(deviceId)) {
      return null;
    }
    String s = buildAndGetFromRedis(productKey + deviceId);
    // 缓存拿到，直接返回缓存的
    if (StrUtil.isNotBlank(s)) {
      return JSONUtil.parseObj(s);
    }
    String shadowMetadata = ioTDeviceShadowMapper.getShadowMetadata(productKey, deviceId);
    if (StrUtil.isBlank(shadowMetadata)) {
      return null;
    }
    return JSONUtil.parseObj(shadowMetadata);
  }

  /** 格式化的属性 */
  public List<IoTDevicePropertiesBO> getDevState(String iotId) {
    List<IoTDevicePropertiesBO> result = new ArrayList<>();

    // Cache-first
    Shadow cached = readShadowFromCache(iotId);
    Shadow shadow = null;
    IoTDeviceShadow ioTDeviceShadow = null;
    if (cached != null) {
      shadow = cached;
    } else {
      ioTDeviceShadow =
          ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
      if (ioTDeviceShadow == null) {
        log.warn("设备=[{}]影子不存在,清检查", iotId);
        return result;
      }
      shadow = JSONUtil.toBean(ioTDeviceShadow.getMetadata(), Shadow.class);
      // backfill cache
      writeShadowToCache(iotId, shadow);
    }
    JSONObject properties =
        shadow.getState() != null ? shadow.getState().getReported() : new JSONObject();
    JSONObject desireProperties =
        shadow.getState() != null ? shadow.getState().getDesired() : new JSONObject();
    Shadow finalShadow = shadow;
    JSONObject finalProperties = properties;
    JSONObject finalDesireProperties = desireProperties;
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
    propertyMetadataList.stream()
        .filter(s -> finalProperties.get(s.getId()) != null)
        .forEach(
            s -> {
              Object obj = finalProperties.get(s.getId());
              IoTDevicePropertiesBO entity = new IoTDevicePropertiesBO();
              Long ts =
                  finalShadow.getMetadata() != null
                      && finalShadow.getMetadata().getReported() != null
                      && finalShadow.getMetadata().getReported().getJSONObject(s.getId())
                      != null
                      ? finalShadow
                      .getMetadata()
                      .getReported()
                      .getJSONObject(s.getId())
                      .getLong("timestamp")
                      : DateUtil.currentSeconds();
              entity.setDesireValue(finalDesireProperties.get(s.getId()));
              entity.withValue(s.getValueType(), obj);
              entity.setPropertyName(s.getName());
              entity.setIotId(iotId);
              entity.setDeviceId(ioTDeviceDTO.getDeviceId());
              entity.setTimestamp(ts);
              entity.setProperty(s.getId());
              entity.setStoragePolicy(storePolicyDTO.getProperties().containsKey(s.getId()));
              if (finalDesireProperties.get(s.getId()) != null) {
                entity.setCustomized(IoTConstant.DEVICE_SHADOW_DESIRED_PROPERTY);
              }
              result.add(entity);
            });
    result.addAll(devCommonProperties(ioTDeviceDTO));
    return result;
  }

  /** 数据为空也返回结果 - cache first */
  public List<IoTDevicePropertiesBO> getDevStateWithNullResult(String iotId) {
    List<IoTDevicePropertiesBO> result = new ArrayList<>();
    Shadow shadow = readShadowFromCache(iotId);
    if (shadow == null) {
      IoTDeviceShadow ioTDeviceShadow =
          ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
      if (ioTDeviceShadow != null && StrUtil.isNotBlank(ioTDeviceShadow.getMetadata())) {
        shadow = JSONUtil.toBean(ioTDeviceShadow.getMetadata(), Shadow.class);
        writeShadowToCache(iotId, shadow);
      } else {
        shadow = Shadow.builder().build();
      }
    }

    JSONObject properties =
        shadow.getState() != null ? shadow.getState().getReported() : new JSONObject();
    JSONObject desireProperties =
        shadow.getState() != null ? shadow.getState().getDesired() : new JSONObject();

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
    Shadow finalShadow = shadow;
    JSONObject finalProperties = properties;
    JSONObject finalDesireProperties = desireProperties;
    propertyMetadataList.forEach(
        s -> {
          Object obj = finalProperties.get(s.getId());
          IoTDevicePropertiesBO entity = new IoTDevicePropertiesBO();
          if (obj != null) {
            entity.withValue(s.getValueType(), obj);
            entity.setTimestamp(
                finalShadow.getMetadata() != null
                    && finalShadow.getMetadata().getReported() != null
                    && finalShadow.getMetadata().getReported().getJSONObject(s.getId()) != null
                    ? finalShadow
                    .getMetadata()
                    .getReported()
                    .getJSONObject(s.getId())
                    .getLong("timestamp")
                    : DateUtil.currentSeconds());
          } else {
            entity.withValue(s.getValueType(), null);
            entity.setTimestamp(DateUtil.currentSeconds());
          }
          entity.setPropertyName(s.getName());
          entity.setIotId(iotId);
          entity.setDeviceId(ioTDeviceDTO.getDeviceId());
          entity.setProperty(s.getId());
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
      //      iotCacheRemoveService.removeDevInstanceBOCache();
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

      data.forEach(
          (key, value) -> {
            if (stateDesired.containsKey(key)) {
              stateReported.set(key, stateDesired.get(key));
              // 删除当前reply回复的期望
              stateDesired.remove(key);
              metadataDesired.remove(key);
              metadataReported.set(key, new Timestamp(DateUtil.currentSeconds()));
            }
            // 回复内有数据时以回复为准
            if (!"".equals(value)) {
              stateReported.set(key, value);
              metadataReported.set(key, new Timestamp(DateUtil.currentSeconds()));
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
    if (shadowCacheEnabled) {
      doShadowCache(upRequest, ioTDeviceDTO);
      return;
    }
    doShadowOriginal(upRequest, ioTDeviceDTO);
  }

  /** 新增：仅缓存与标记刷盘的影子处理，不改动原有 doShadowOriginal */
  public void doShadowCache(UPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    if (!shadowCacheEnabled) {
      // 回退到原始逻辑
      doShadowOriginal(upRequest, ioTDeviceDTO);
      return;
    }
    String iotId = ioTDeviceDTO.getIotId();
    Shadow shadow = readShadowFromCache(iotId);
    if (shadow == null) {
      // fallback DB
      IoTDeviceShadow fromDb =
          ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
      if (fromDb != null && StrUtil.isNotBlank(fromDb.getMetadata())) {
        shadow = JSONUtil.toBean(fromDb.getMetadata(), Shadow.class);
      } else {
        shadow = Shadow.builder().timestamp(DateUtil.currentSeconds()).version(1L).build();
      }
    }
    // 合并期望/上报并自增版本
    doDesired(shadow, ioTDeviceDTO, upRequest);
    // 写缓存
    writeShadowToCache(iotId, shadow);
    // 标记刷盘
    markShadowDirtyForFlush(iotId);
    // 清除相关业务缓存
    //    iotCacheRemoveService.removeDevInstanceBOCache();
  }

  /** 检查版本号是否超过阈值，需要强制刷盘 */
  private boolean checkVersionThreshold(String iotId) {
    try {
      String cacheJson = stringRedisTemplate.opsForValue().get(buildShadowKey(iotId));
      if (StrUtil.isBlank(cacheJson)) {
        return false;
      }

      JSONObject shadow = JSONUtil.parseObj(cacheJson);
      Long currentVersion = shadow.getLong("version", 1L);

      IoTDeviceShadow dbShadow =
          ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
      if (dbShadow != null && StrUtil.isNotBlank(dbShadow.getMetadata())) {
        // 数据库记录存在：正常比较
        JSONObject dbShadowObj = JSONUtil.parseObj(dbShadow.getMetadata());
        Long dbVersion = dbShadowObj.getLong("version", 1L);

        return currentVersion != null
            && dbVersion != null
            && (currentVersion - dbVersion) >= VERSION_THRESHOLD;
      } else {
        // 数据库记录不存在：立即触发刷盘（创建记录）
        log.debug("[ShadowFlush] 数据库记录不存在，立即刷盘: iotId={}, currentVersion={}", iotId, currentVersion);
        return true;
      }
    } catch (Exception e) {
      log.warn("[ShadowFlush] 版本号检查失败: iotId={}, error={}", iotId, e.getMessage());
    }
    return false;
  }
}

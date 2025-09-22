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

package cn.universal.dm.device.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.entity.bo.UPPushBO;
import cn.universal.persistence.mapper.IoTUserApplicationMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 上行推送配置服务
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/9
 */
@Slf4j
@Service
public class UPPushConfigService {

  @Resource private IoTUserApplicationMapper ioTUserApplicationMapper;

  // 本地缓存：应用ID -> 推送配置
  private final Cache<String, UPPushBO> pushConfigCache =
      Caffeine.newBuilder()
          .maximumSize(5000) // 最大缓存1000个应用
          .expireAfterWrite(10, TimeUnit.MINUTES) // 10分钟过期
          .build();

  /**
   * 获取推送配置（带缓存）
   *
   * @param applicationId 应用ID
   * @return 推送配置
   */
  @Cacheable(cacheNames = "getPushConfig", key = "#applicationId", unless = "#result == null")
  public UPPushBO getPushConfig(String applicationId) {
    if (StrUtil.isBlank(applicationId)) {
      log.warn("[推送配置] 应用ID为空，返回默认配置");
      return getDefaultPushConfig();
    }

    // 先从缓存获取
    UPPushBO cachedConfig = pushConfigCache.getIfPresent(applicationId);
    if (cachedConfig != null) {
      log.debug("[推送配置] 缓存命中，应用ID: {}", applicationId);
      return cachedConfig;
    }

    // 缓存未命中，从数据库查询
    log.debug("[推送配置] 缓存未命中，从数据库查询，应用ID: {}", applicationId);
    UPPushBO config = loadPushConfigFromDB(applicationId);

    // 缓存配置
    if (config != null) {
      pushConfigCache.put(applicationId, config);
      log.debug("[推送配置] 配置已缓存，应用ID: {}", applicationId);
    }

    return config != null ? config : getDefaultPushConfig();
  }

  /**
   * 批量获取推送配置
   *
   * @param applicationIds 应用ID列表
   * @return 推送配置映射
   */
  public Map<String, UPPushBO> getPushConfigs(List<String> applicationIds) {
    return applicationIds.stream()
        .collect(java.util.stream.Collectors.toMap(appId -> appId, this::getPushConfig));
  }

  /**
   * 刷新缓存
   *
   * @param applicationId 应用ID
   */
  public void refreshCache(String applicationId) {
    if (StrUtil.isNotBlank(applicationId)) {
      pushConfigCache.invalidate(applicationId);
      log.info("[推送配置] 缓存已刷新，应用ID: {}", applicationId);
    }
  }

  /** 清空所有缓存 */
  public void clearCache() {
    pushConfigCache.invalidateAll();
    log.info("[推送配置] 所有缓存已清空");
  }

  /**
   * 从数据库加载推送配置
   *
   * @param applicationId 应用ID
   * @return 推送配置
   */
  private UPPushBO loadPushConfigFromDB(String applicationId) {
    try {
      IoTUserApplication application =
          ioTUserApplicationMapper.selectIotUserApplicationById(applicationId);
      if (application == null) {
        log.warn("[推送配置] 应用不存在，应用ID: {}", applicationId);
        return null;
      }

      // 解析推送配置JSON
      String pushConfigJson = application.getCfg();
      if (StrUtil.isBlank(pushConfigJson)) {
        log.warn("[推送配置] 推送配置为空，应用ID: {}", applicationId);
        return getDefaultPushConfig();
      }

      return parsePushConfig(pushConfigJson);
    } catch (Exception e) {
      log.error("[推送配置] 加载配置异常，应用ID: {}", applicationId, e);
      return getDefaultPushConfig();
    }
  }

  /**
   * 解析推送配置JSON
   *
   * @param pushConfigJson 推送配置JSON字符串
   * @return 推送配置对象
   */
  private UPPushBO parsePushConfig(String pushConfigJson) {
    try {
      JSONObject config = JSONUtil.parseObj(pushConfigJson);

      return UPPushBO.builder()
          .http(parseHttpConfig(config.getJSONObject("http")))
          .mqtt(parseMqttConfig(config.getJSONObject("mqtt")))
          .kafka(parseKafkaConfig(config.getJSONObject("kafka")))
          .rocketMQ(parseRocketMQConfig(config.getJSONObject("rocketMQ")))
          .build();
    } catch (Exception e) {
      log.error("[推送配置] 解析配置JSON异常: {}", pushConfigJson, e);
      return getDefaultPushConfig();
    }
  }

  /** 解析HTTP配置 */
  private UPPushBO.HttpPushConfig parseHttpConfig(JSONObject httpConfig) {
    if (httpConfig == null) {
      return UPPushBO.HttpPushConfig.builder().enable(false).support(false).build();
    }

    return UPPushBO.HttpPushConfig.builder()
        .url(httpConfig.getStr("url"))
        .enable(httpConfig.getBool("enable", false))
        .header(httpConfig.getStr("header"))
        .secret(httpConfig.getStr("secret"))
        .support(httpConfig.getBool("support", false))
        .build();
  }

  /** 解析MQTT配置 */
  private UPPushBO.MqttPushConfig parseMqttConfig(JSONObject mqttConfig) {
    if (mqttConfig == null) {
      return UPPushBO.MqttPushConfig.builder().enable(false).support(false).build();
    }

    return UPPushBO.MqttPushConfig.builder()
        .url(mqttConfig.getStr("url"))
        .topic(mqttConfig.getStr("topic"))
        .enable(mqttConfig.getBool("enable", false))
        .support(mqttConfig.getBool("support", false))
        .password(mqttConfig.getStr("password"))
        .username(mqttConfig.getStr("username"))
        .build();
  }

  /** 解析Kafka配置 */
  private UPPushBO.KafkaPushConfig parseKafkaConfig(JSONObject kafkaConfig) {
    if (kafkaConfig == null) {
      return UPPushBO.KafkaPushConfig.builder().enable(false).support(false).build();
    }

    return UPPushBO.KafkaPushConfig.builder()
        .enable(kafkaConfig.getBool("enable", false))
        .support(kafkaConfig.getBool("support", false))
        .build();
  }

  /** 解析RocketMQ配置 */
  private UPPushBO.RocketMQPushConfig parseRocketMQConfig(JSONObject rocketMQConfig) {
    if (rocketMQConfig == null) {
      return UPPushBO.RocketMQPushConfig.builder().enable(false).support(false).build();
    }

    return UPPushBO.RocketMQPushConfig.builder()
        .enable(rocketMQConfig.getBool("enable", false))
        .support(rocketMQConfig.getBool("support", false))
        .build();
  }

  /**
   * 获取默认推送配置
   *
   * @return 默认配置
   */
  private UPPushBO getDefaultPushConfig() {
    return UPPushBO.builder()
        .http(UPPushBO.HttpPushConfig.builder().enable(false).support(false).build())
        .mqtt(UPPushBO.MqttPushConfig.builder().enable(false).support(false).build())
        .kafka(UPPushBO.KafkaPushConfig.builder().enable(false).support(false).build())
        .rocketMQ(UPPushBO.RocketMQPushConfig.builder().enable(false).support(false).build())
        .build();
  }
}

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

package cn.universal.dm.device.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.dm.device.service.push.UPProcessorManager;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.base.IoTDeviceExtendService;
import cn.universal.persistence.base.IoTUPWrapper;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.bo.UPPushBO;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/8/6 11:56
 */
@Slf4j
public abstract class IoTUPPushAdapter<T extends BaseUPRequest> {

  @Resource private IoTDeviceExtendService deviceExtTemplate;

  @Resource private UPProcessorManager upProcessorManager;

  @Resource private UPPushConfigService upPushConfigService;

  @Resource private UPPushExecutor upPushExecutor;

  protected void sendMqtt(String topic, String msg) {
    //    mqttUPWrapper.mqttPush(topic, msg);
  }

  /**
   * 推送前钩子方法，子类可重写进行消息转换、规则引擎等处理
   *
   * @param upRequests 上行请求列表
   */
  protected void beforePush(List<T> upRequests) {
    // 调用现有的beforePush逻辑
    callExistingBeforePush(upRequests);

    // 执行插件化处理器
    if (upProcessorManager != null) {
      List<T> ts = upProcessorManager.executeBeforePush(upRequests);
      if (CollectionUtil.isNotEmpty(ts)) {
        upRequests.clear();
        upRequests.addAll(ts);
      }
    }

    // 子类可重写此方法进行扩展
    onBeforePush(upRequests);
  }

  /**
   * 推送后钩子方法，子类可重写进行结果处理、日志记录等
   *
   * @param upRequests 上行请求列表
   * @param pushResults 推送结果列表
   */
  protected void afterPush(List<T> upRequests, List<IoTPushResult> pushResults) {
    // 执行插件化处理器（带推送结果）
    if (upProcessorManager != null) {
      upProcessorManager.executeAfterPush(upRequests, pushResults);
    }

    // 子类可重写此方法进行扩展
    onAfterPush(upRequests, pushResults);
  }

  /**
   * 推送前扩展点，子类可重写
   *
   * @param upRequests 上行请求列表
   */
  protected void onBeforePush(List<T> upRequests) {
    // 默认空实现，子类可重写
  }

  /**
   * 推送后扩展点，子类可重写
   *
   * @param upRequests 上行请求列表
   * @param pushResults 推送结果列表
   */
  protected void onAfterPush(List<T> upRequests, List<IoTPushResult> pushResults) {
    // 默认空实现，子类可重写
  }

  /**
   * 实际的推送逻辑
   *
   * @param upRequests 上行请求列表
   * @return 推送结果列表
   */
  protected List<IoTPushResult> executePush(List<T> upRequests) {
    upRequests.forEach(req -> deviceExtTemplate.upExt(req));

    // 按应用分组处理推送
    Map<String, List<T>> appGroups = groupByApplication(upRequests);
    List<IoTPushResult> allResults = new java.util.ArrayList<>();

    for (Map.Entry<String, List<T>> entry : appGroups.entrySet()) {
      String appId = entry.getKey();
      List<T> requests = entry.getValue();

      try {
        // 获取推送配置
        UPPushBO pushConfig = upPushConfigService.getPushConfig(appId);
        List<IoTPushResult> results =
            upPushExecutor.executeBatchPush((List<BaseUPRequest>) requests, pushConfig);
        log.debug(
            "[推送执行] 应用 {} 推送完成，消息数量: {}, 结果数量: {}",
            appId,
            requests.size(),
            results != null ? results.size() : 0);

        // 收集推送结果
        if (results != null) {
          allResults.addAll(results);
        }
      } catch (Exception e) {
        log.error("[推送执行] 应用 {} 推送异常", appId, e);
      }
    }

    return allResults;
  }

  /**
   * 按应用ID分组，过滤掉无效的请求
   *
   * @param upRequests 上行请求列表
   * @return 分组后的请求映射
   */
  private Map<String, List<T>> groupByApplication(List<T> upRequests) {
    return upRequests.stream()
        .filter(
            request -> {
              IoTDeviceDTO deviceDTO = request.getIoTDeviceDTO();
              if (deviceDTO == null) {
                log.warn("[分组] 设备DTO为空，丢弃消息: {}", request);
                return false;
              }

              String applicationId = deviceDTO.getApplicationId();
              if (applicationId == null || applicationId.trim().isEmpty()) {
                log.warn("[分组] 应用ID为空，设备ID: {}, 丢弃消息", deviceDTO.getDeviceId());
                return false;
              }
              return true;
            })
        .collect(
            Collectors.groupingBy(
                request -> {
                  IoTDeviceDTO deviceDTO = request.getIoTDeviceDTO();
                  return deviceDTO.getApplicationId();
                }));
  }

  /**
   * 模板方法：统一的上行消息处理流程
   *
   * @param upRequests 上行请求列表
   * @return 处理结果
   */
  protected String doUp(List<T> upRequests) {
    if (CollectionUtil.isEmpty(upRequests)) {
      return null;
    }
    // 推送前处理
    beforePush(upRequests);

    // 执行推送
    List<IoTPushResult> pushResults = executePush(upRequests);

    // 推送后处理
    afterPush(upRequests, pushResults);

    return null;
  }

  /**
   * 调用现有的beforePush逻辑
   *
   * @param upRequests 上行请求列表
   */
  private void callExistingBeforePush(List<T> upRequests) {
    Map<String, IoTUPWrapper> downWrapperMap = SpringUtil.getBeansOfType(IoTUPWrapper.class);
    if (MapUtil.isNotEmpty(downWrapperMap)) {
      for (Map.Entry<String, IoTUPWrapper> entry : downWrapperMap.entrySet()) {
        String serviceName = entry.getKey();
        IoTUPWrapper iotDownWrapper = entry.getValue();
        iotDownWrapper.beforePush(upRequests);
        log.debug("调用推送前置处理,实现名称={}.返回结果={}", serviceName);
      }
    }
  }
}

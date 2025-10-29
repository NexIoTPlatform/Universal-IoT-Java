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

package cn.universal.dm.device.service.push;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.persistence.base.BaseUPRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 上行消息处理器管理器
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Slf4j
@Component
public class UPProcessorManager {

  /**
   * 获取所有处理器并按优先级排序
   *
   * @param <T> 请求类型
   * @return 处理器列表
   */
  @SuppressWarnings("unchecked")
  public <T extends BaseUPRequest> List<UPProcessor<T>> getProcessors() {
    Map<String, UPProcessor> processorMap = SpringUtil.getBeansOfType(UPProcessor.class);
    if (processorMap.isEmpty()) {
      return List.of();
    }

    return processorMap.values().stream()
        .map(processor -> (UPProcessor<T>) processor)
        .sorted(Comparator.comparingInt(UPProcessor::getOrder))
        .collect(Collectors.toList());
  }

  /**
   * 执行推送前处理
   *
   * @param upRequests 上行请求列表
   * @param <T> 请求类型
   * @return 处理后的请求列表
   */
  public <T extends BaseUPRequest> List<T> executeBeforePush(List<T> upRequests) {
    if (CollectionUtil.isEmpty(upRequests)) {
      return upRequests;
    }

    List<UPProcessor<T>> processors = getProcessors();
    if (processors.isEmpty()) {
      return upRequests;
    }

    List<T> processedRequests = upRequests;
    for (UPProcessor<T> processor : processors) {
      try {
        // 过滤出支持的请求
        List<T> supportedRequests =
            processedRequests.stream().filter(processor::supports).collect(Collectors.toList());

        if (!supportedRequests.isEmpty()) {
          log.debug(
              "[UP处理器][推送前] 执行处理器: {}, 处理 {} 条消息", processor.getName(), supportedRequests.size());

          List<T> result = processor.beforePush(supportedRequests);
          if (result != null) {
            processedRequests = result;
          }
        }
      } catch (Exception e) {
        log.error("[UP处理器][推送前] 处理器 {} 执行异常", processor.getName(), e);
      }
    }

    return processedRequests;
  }

  /**
   * 执行推送后处理（带推送结果）
   *
   * @param upRequests 上行请求列表
   * @param pushResults 推送结果列表
   * @param <T> 请求类型
   */
  public <T extends BaseUPRequest> void executeAfterPush(
      List<T> upRequests, List<IoTPushResult> pushResults) {
    if (CollectionUtil.isEmpty(upRequests)) {
      return;
    }

    List<UPProcessor<T>> processors = getProcessors();
    if (processors.isEmpty()) {
      return;
    }

    for (UPProcessor<T> processor : processors) {
      try {
        // 过滤出支持的请求
        List<T> supportedRequests =
            upRequests.stream().filter(processor::supports).collect(Collectors.toList());

        if (!supportedRequests.isEmpty()) {
          log.debug(
              "[UP处理器][推送后] 执行处理器: {}, 处理 {} 条消息, 结果数量: {}",
              processor.getName(),
              supportedRequests.size(),
              pushResults != null ? pushResults.size() : 0);

          processor.afterPush(supportedRequests, pushResults);
        }
      } catch (Exception e) {
        log.error("[UP处理器][推送后] 处理器 {} 执行异常", processor.getName(), e);
      }
    }
  }
}

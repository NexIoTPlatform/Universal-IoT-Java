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

package cn.universal.http.protocol.processor;

import cn.universal.dm.device.service.plugin.ProcessorExecutor;
import cn.universal.http.protocol.entity.HttpDownRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP下行消息处理器链
 *
 * <p>使用通用的ProcessorExecutor执行处理器逻辑 专注于HTTP下行业务逻辑，通用逻辑由ProcessorExecutor处理
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Slf4j
@Component
public class HttpDownProcessorChain {

  @Autowired private ProcessorExecutor processorExecutor;

  @Autowired private List<HttpDownMessageProcessor> processors;

  /**
   * 处理HTTP下行消息
   *
   * @param request HTTP下行请求
   * @return 处理结果
   */
  public boolean process(HttpDownRequest request) {
    if (request == null) {
      log.warn("[HTTP_DOWN] 请求为空，跳过处理");
      return false;
    }

    log.info("[HTTP_DOWN] 开始处理下行消息，请求ID: {}", request.getRequestId());

    // 使用ProcessorExecutor执行处理器链
    boolean success =
        processorExecutor.executeChain(
            processors,
            "HTTP_DOWN",
            processor -> {
              // 执行前置检查
              if (!processor.preCheck(request)) {
                log.debug("[HTTP_DOWN] 处理器 {} 预检查失败", processor.getName());
                return null; // 返回null表示跳过
              }

              // 执行处理器
              var result = processor.process(request);

              // 执行后置处理
              processor.postProcess(request, result);

              return result;
            },
            result -> result != null, // 成功检查：结果不为null
            processor -> processor.supports(request) // 支持性检查
            );

    log.info("[HTTP_DOWN] 下行消息处理完成，成功: {}", success);
    return success;
  }

  /** 获取处理器数量 */
  public int getProcessorCount() {
    return processors.size();
  }

  /** 获取处理器名称列表（用于调试） */
  public List<String> getProcessorNames() {
    return processorExecutor.getProcessorNames(processors);
  }

  /** 检查是否有指定名称的处理器 */
  public boolean hasProcessor(String name) {
    return processors.stream().anyMatch(p -> p.getName().equals(name));
  }
}

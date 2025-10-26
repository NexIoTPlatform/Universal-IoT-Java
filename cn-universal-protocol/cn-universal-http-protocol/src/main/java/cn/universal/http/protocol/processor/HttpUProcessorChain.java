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

package cn.universal.http.protocol.processor;

import cn.hutool.json.JSONObject;
import cn.universal.dm.device.service.plugin.ProcessorExecutor;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * HTTP上行消息处理器链
 *
 * <p>使用通用的ProcessorExecutor执行处理器逻辑 专注于HTTP业务逻辑，通用逻辑由ProcessorExecutor处理
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Slf4j
@Component
public class HttpUProcessorChain {

  @Autowired private ProcessorExecutor processorExecutor;

  @Autowired private List<HttpUPMessageProcessor> processors;

  /**
   * 处理HTTP消息
   *
   * @param source 原始JSON数据
   * @param ioTDeviceDTO 设备实例信息
   * @param requests 请求列表
   * @return 处理后的请求列表
   */
  public List<HttpUPRequest> process(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (source == null || requests == null) {
      log.warn("[HTTP_UP] 消息payload或requests为空，跳过处理");
      return requests;
    }

    log.info("[HTTP_UP] 开始处理消息，请求数量: {}", requests.size());

    // 使用ProcessorExecutor执行处理器链
    processorExecutor.executeChain(
        processors,
        "HTTP_UP",
        processor -> {
          // 执行前置检查
          if (!processor.preCheck(source, ioTDeviceDTO, requests)) {
            log.debug("[HTTP_UP] 处理器 {} 预检查失败", processor.getName());
            return requests; // 返回原requests表示跳过
          }

          // 执行处理器
          List<HttpUPRequest> result = processor.process(source, ioTDeviceDTO, requests);

          // 执行后置处理
          processor.postProcess(source, ioTDeviceDTO, requests, result);

          return result;
        },
        result -> result != null, // 成功检查：结果不为null
        processor -> processor.supports(source, ioTDeviceDTO, requests) // 支持性检查
        );

    log.info("[HTTP_UP] 消息处理完成，最终请求数量: {}", requests.size());
    return requests;
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

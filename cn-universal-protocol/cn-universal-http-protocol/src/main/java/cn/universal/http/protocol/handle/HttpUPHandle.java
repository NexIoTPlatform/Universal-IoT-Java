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

package cn.universal.http.protocol.handle;

import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.dm.device.service.IoTUPPushAdapter;
import cn.universal.http.protocol.entity.HttpUPRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * http上行消息实际处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2023/02/24 11:19
 */
@Slf4j
@Component
public class HttpUPHandle extends IoTUPPushAdapter<HttpUPRequest> {

  public String up(List<HttpUPRequest> upRequests) {
    try {
      return doUp(upRequests);
    } catch (Exception e) {
      log.error("[HTTP上行][处理异常]", e);
    }
    return null;
  }

  /**
   * 推送前扩展：消息转换、规则引擎等处理
   *
   * @param upRequests 上行请求列表
   */
  @Override
  protected void onBeforePush(List<HttpUPRequest> upRequests) {
    log.info("[HTTP上行][推送前处理] 开始处理 {} 条消息", upRequests.size());

    // 示例1：消息转换 - 数据增强
    upRequests.forEach(
        request -> {
          // 可以在这里进行数据增强
          // 比如添加来源标识、数据校验等
          if (request.getIoTDeviceDTO() != null) {
            // 添加HTTP协议标识
            request.getIoTDeviceDTO().setThirdPlatform("http");
          }
          log.debug("[HTTP上行][消息转换] 处理消息: {}", request.getIotId());
        });

    // 示例2：规则引擎 - 消息验证
    upRequests.removeIf(
        request -> {
          // 验证消息完整性
          boolean shouldFilter =
              request.getIotId() == null
                  || request.getProductKey() == null
                  || request.getIoTDeviceDTO() == null;
          if (shouldFilter) {
            log.warn("[HTTP上行][规则验证] 消息不完整，过滤: {}", request);
          }
          return shouldFilter;
        });

    log.info("[HTTP上行][推送前处理] 处理完成，剩余 {} 条消息", upRequests.size());
  }

  /**
   * 推送后扩展：结果处理、日志记录等
   *
   * @param upRequests 上行请求列表
   * @param pushResults 推送结果列表
   */
  @Override
  protected void onAfterPush(List<HttpUPRequest> upRequests, List<IoTPushResult> pushResults) {
    log.info(
        "[HTTP上行][推送后处理] 推送完成，消息数量: {}, 结果数量: {}",
        upRequests.size(),
        pushResults != null ? pushResults.size() : 0);

    // 示例1：推送结果记录
    if (pushResults != null) {
      pushResults.forEach(
          result -> {
            log.debug(
                "[HTTP上行][推送记录] 设备 {} 渠道 {} 推送结果: {}",
                result.getDeviceId(),
                result.getChannel(),
                result.isOk() ? "成功" : "失败");
          });
    }

    // 示例2：性能监控 - 可以在这里添加推送性能统计
    // 比如推送耗时、成功率等指标
    if (pushResults != null) {
      long successCount = pushResults.stream().filter(IoTPushResult::isOk).count();
      long totalCount = pushResults.size();
      double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
      log.info("[HTTP上行][性能统计] 推送成功率: {}/{} ({:.2f}%)", successCount, totalCount, successRate);
    }
  }
}

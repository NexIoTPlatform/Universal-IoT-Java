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

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HTTP订阅推送策略实现
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Slf4j
@Component
public class SubscribePushStrategy implements PushStrategy {

  /** HTTP请求超时时间（毫秒） */
  private static final Integer HTTP_TIME_OUT = 1200;

  /** URL黑名单缓存 */
  private static final Cache<String, Integer> blockUrl =
      Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).maximumSize(256).build();

  /** 超过失败次数则入黑名单 */
  private static final Integer BLOCK_COUNT = 30;

  private static final String CACHE_NOTICE = "DNotice:";

  @Override
  public IoTPushResult execute(BaseUPRequest request, String messageJson) {

    // 订阅的推送
    try {
      Object devSubObj = request.getDevSubscribe();
      if (devSubObj != null) {
        List<IoTDeviceSubscribe> subscribes = (List<IoTDeviceSubscribe>) devSubObj;
        for (IoTDeviceSubscribe subscribe : subscribes) {
          if (StrUtil.isBlank(subscribe.getUrl())) {
            log.warn("[HTTP订阅推送] URL为空，跳过推送");
            return IoTPushResult.failed(
                request.getIoTDeviceDTO().getThirdPlatform(),
                request.getProductKey(),
                request.getIotId(),
                "Subscribe",
                messageJson,
                "URL为空",
                "URL_EMPTY");
          }

          // 检查URL是否被冻结
          if (isUrlBlocked(subscribe.getUrl())) {
            log.warn("[HTTP订阅推送] URL {} 因多次推送失败已被冻结", subscribe.getUrl());
            return IoTPushResult.failed(
                request.getIoTDeviceDTO().getThirdPlatform(),
                request.getProductKey(),
                request.getIotId(),
                "Subscribe",
                messageJson,
                "URL已被冻结",
                "URL_BLOCKED");
          }

          // 创建HTTP请求
          HttpRequest httpRequest = HttpUtil.createPost(subscribe.getUrl());
          httpRequest.timeout(HTTP_TIME_OUT);

          // 添加时间戳和签名
          String timestamp = String.valueOf(System.currentTimeMillis());
          //      String signature = String.valueOf((timestamp + httpConfig.getUrl()).hashCode());
          httpRequest.header("X-Timestamp", timestamp);
          //      httpRequest.header("X-Signature", signature);
          httpRequest.header(Header.CONTENT_TYPE, "application/json");

          // 设置请求体
          httpRequest.body(messageJson);

          // 执行请求
          HttpResponse response = httpRequest.execute();
          String result = response.body();

          // 检查响应状态
          if (response == null || response.getStatus() != 200) {
            log.warn(
                "[HTTP订阅推送] 推送失败, url={}, status={}, response={}",
                subscribe.getUrl(),
                response != null ? response.getStatus() : "null",
                result);
            countFail(subscribe.getUrl());
            return IoTPushResult.failed(
                request.getIoTDeviceDTO().getThirdPlatform(),
                request.getProductKey(),
                request.getIotId(),
                "Subscribe",
                messageJson,
                "HTTP状态码错误: " + (response != null ? response.getStatus() : "null"),
                "HTTP_ERROR");
          }

          log.info("[HTTP订阅推送] 推送成功, url={}, deviceId={}", subscribe.getUrl(), request.getIotId());
          removeSuccess(subscribe.getUrl());
        }
      }

      // 如果没有订阅或所有订阅都成功，返回成功
      return IoTPushResult.success(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "Subscribe",
          messageJson,
          System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[HTTP订阅推送] 推送失败: {}", request.getIotId(), e);
      return IoTPushResult.failed(
          request.getIoTDeviceDTO().getThirdPlatform(),
          request.getProductKey(),
          request.getIotId(),
          "Subscribe",
          messageJson,
          "推送异常: " + e.getMessage(),
          "PUSH_EXCEPTION");
    }
  }

  @Override
  public boolean isSupported() {
    return true;
  }

  /** 检查URL是否被冻结 */
  private boolean isUrlBlocked(String url) {
    return blockUrl.getIfPresent(url) != null && blockUrl.getIfPresent(url) >= BLOCK_COUNT;
  }

  /** 记录失败次数 */
  private void countFail(String url) {
    int failure = blockUrl.getIfPresent(url) == null ? 0 : (blockUrl.getIfPresent(url) + 1);
    if (failure > BLOCK_COUNT) {
      // 5分钟只推送一次
      if (blockUrl.getIfPresent(CACHE_NOTICE + url) != null) {
        log.warn("地址：{} 推送失败超过阈值", url);
        blockUrl.put(CACHE_NOTICE + url, 0);
      }
    } else {
      blockUrl.put(url, failure);
    }
  }

  /** 移除成功记录 */
  private void removeSuccess(String url) {
    if (blockUrl.getIfPresent(url) != null) {
      blockUrl.invalidate(url);
      blockUrl.invalidate(CACHE_NOTICE + url);
    }
  }
}

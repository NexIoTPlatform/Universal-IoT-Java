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

package cn.universal.http.protocol.processor.down;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.http.protocol.entity.HttpDownRequest;
import cn.universal.http.protocol.processor.HttpDownMessageProcessor;
import cn.universal.http.protocol.protocol.codec.HTTPCodecAction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 编解码装饰器处理器
 *
 * <p>在处理器链中统一处理编解码，避免在每个处理器中重复添加编解码逻辑 通过配置决定是否启用编解码，以及编解码的时机
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/01/20
 */
@Slf4j
@Component
public class CodecDecoratorProcessor implements HttpDownMessageProcessor {

  @Resource private HTTPCodecAction httpCodecActionProcessor;

  @Override
  public String getName() {
    return "编解码装饰器处理器";
  }

  @Override
  public int getOrder() {
    return 1; // 在预处理之前执行，确保编解码结果可以被后续处理器使用
  }

  @Override
  public R<?> process(HttpDownRequest httpDownRequest) {
    log.debug("[编解码装饰器] 开始处理请求: deviceId={}", httpDownRequest.getDeviceId());

    try {
      // 检查是否需要编解码
      if (shouldApplyCodec(httpDownRequest)) {
        applyCodec(httpDownRequest);
      }
    } catch (Exception e) {
      log.error("[编解码装饰器] 处理请求 {} 异常", httpDownRequest.getDeviceId(), e);
    }

    return R.ok();
  }

  /**
   * 是否支持处理该消息（基于process方法的入参）
   *
   * @param request 请求列表
   * @return true表示支持，false表示不支持
   */
  @Override
  public boolean supports(HttpDownRequest request) {
    if (StrUtil.isBlank(request.getProductKey())) {
      return false;
    }
    return httpCodecActionProcessor.support(request.getProductKey());
  }

  /**
   * 判断是否需要对请求应用编解码
   *
   * @param request 下行请求
   * @return 是否需要编解码
   */
  private boolean shouldApplyCodec(HttpDownRequest request) {
    // 1. 检查是否已经有编解码结果
    if (StrUtil.isNotBlank(request.getDownResult())) {
      log.debug("[编解码装饰器] 请求 {} 已有编解码结果，跳过", request.getDeviceId());
      return false;
    }

    // 2. 检查是否支持编解码的命令类型
    if (!isCodecSupported(request.getCmd())) {
      log.debug("[编解码装饰器] 请求 {} 的命令类型 {} 不支持编解码", request.getDeviceId(), request.getCmd());
      return false;
    }

    // 3. 检查是否有编解码所需的参数
    if (!hasCodecParameters(request)) {
      log.debug("[编解码装饰器] 请求 {} 缺少编解码参数", request.getDeviceId());
      return false;
    }

    // 4. 检查产品配置是否启用编解码
    if (!isCodecEnabled(request)) {
      log.debug("[编解码装饰器] 请求 {} 的产品未启用编解码", request.getDeviceId());
      return false;
    }
    return true;
  }

  /**
   * 应用编解码处理
   *
   * @param request 下行请求
   */
  private void applyCodec(HttpDownRequest request) {
    try {
      // 获取编解码参数
      String payload = getCodecPayload(request);
      String productKey = request.getProductKey();

      log.info(
          "[编解码装饰器] 开始编解码: deviceId={}, cmd={}, productKey={}",
          request.getDeviceId(),
          request.getCmd(),
          productKey);

      // 执行编解码
      R<?> codecResult = executeCodec(request.getCmd(), payload, productKey);

      if (codecResult != null) {
        // 将编解码结果设置到请求中
        request.setDownResult(JSONUtil.toJsonStr(codecResult));

        log.info(
            "[编解码装饰器] 编解码完成: deviceId={}, success={}",
            request.getDeviceId(),
            R.SUCCESS.equals(codecResult.getCode()));
      }

    } catch (Exception e) {
      log.error("[编解码装饰器] 编解码处理异常: deviceId={}", request.getDeviceId(), e);
    }
  }

  private String getCodecPayload(HttpDownRequest request) {
    return JSONUtil.toJsonStr(request);
  }

  /**
   * 执行编解码
   *
   * @param cmd 命令类型
   * @param payload 数据
   * @param productKey 产品标识
   * @return 编解码结果
   */
  private R<?> executeCodec(DownCmd cmd, String payload, String productKey) {
    String result =
        switch (cmd) {
          case DEV_ADD -> httpCodecActionProcessor.executeAddCodec(productKey, payload);
          case DEV_DEL -> httpCodecActionProcessor.executeDeleteCodec(productKey, payload);
          case DEV_UPDATE -> httpCodecActionProcessor.executeUpdateCodec(productKey, payload);
          //      case DEV_SELECT -> httpCodecActionProcessor.executeSelectCodec(productKey,
          // payload);
          case DEV_FUNCTION -> httpCodecActionProcessor.executeFunctionCodec(productKey, payload);
          default -> httpCodecActionProcessor.executeOtherCodec(productKey, payload);
        };

    // 处理编解码结果
    if (result == null || result.trim().isEmpty()) {
      return R.error("编解码结果为空");
    }

    // 如果结果是JSON格式，尝试解析为R对象
    if (JSONUtil.isTypeJSON(result)) {
      try {
        return JSONUtil.toBean(result, R.class);
      } catch (Exception e) {
        log.warn("[编解码装饰器] JSON解析失败，返回原始结果: {}", result);
        return R.error(result);
      }
    }

    // 如果结果包含错误信息，返回错误
    if (result.startsWith("编解码失败:")) {
      return R.error(result);
    }

    // 返回成功结果
    return R.ok(result);
  }

  /**
   * 检查命令类型是否支持编解码
   *
   * @param cmd 命令类型
   * @return 是否支持
   */
  private boolean isCodecSupported(DownCmd cmd) {
    return cmd != null
        && (cmd == DownCmd.DEV_ADD
            || cmd == DownCmd.DEV_DEL
            || cmd == DownCmd.DEV_UPDATE
            || cmd == DownCmd.DEV_FUNCTION);
  }

  /**
   * 检查是否有编解码所需的参数
   *
   * @param request 下行请求
   * @return 是否有参数
   */
  private boolean hasCodecParameters(HttpDownRequest request) {
    // 检查基本参数
    if (StrUtil.isBlank(request.getDeviceId()) || StrUtil.isBlank(request.getProductKey())) {
      return false;
    }
    // 检查数据参数
    if (request.getData() == null) {
      return false;
    }
    return true;
  }

  /**
   * 检查产品是否启用编解码
   *
   * @param request 下行请求
   * @return 是否启用
   */
  private boolean isCodecEnabled(HttpDownRequest request) {
    // 默认启用编解码
    return true;
  }
}

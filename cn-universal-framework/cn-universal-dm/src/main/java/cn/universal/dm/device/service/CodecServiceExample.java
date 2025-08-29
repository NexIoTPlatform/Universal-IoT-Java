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

import cn.universal.core.message.UPRequest;
import cn.universal.core.protocol.support.ProtocolCodecSupport.CodecMethod;
import cn.universal.core.service.ICodecService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 编解码服务使用示例
 *
 * <p>展示如何使用统一的编解码服务进行各种编解码操作
 *
 * @version 1.0 @Author Aleo
 * @since 2025/01/20
 */
@Slf4j
@Service
public class CodecServiceExample {

  @Autowired private ICodecService codecService;

  /**
   * 示例：解码设备上报数据
   *
   * @param productKey 产品Key
   * @param payload 原始数据
   * @return 解码后的UPRequest列表
   */
  public List<UPRequest> decodeDeviceData(String productKey, String payload) {
    log.info("开始解码设备数据: productKey={}, payload={}", productKey, payload);

    // 使用统一的解码服务
    List<UPRequest> result = codecService.decode(productKey, payload);

    log.info("解码完成: 结果数量={}", result.size());
    return result;
  }

  /**
   * 示例：编码下行指令
   *
   * @param productKey 产品Key
   * @param payload 原始指令数据
   * @return 编码后的指令
   */
  public String encodeDownCommand(String productKey, String payload) {
    log.info("开始编码下行指令: productKey={}, payload={}", productKey, payload);

    // 使用统一的编码服务
    String result = codecService.encode(productKey, payload);

    log.info("编码完成: result={}", result);
    return result;
  }

  /**
   * 示例：预解码TCP数据
   *
   * @param productKey 产品Key
   * @param payload 原始TCP数据
   * @return 预解码后的UPRequest
   */
  public UPRequest preDecodeTcpData(String productKey, String payload) {
    log.info("开始预解码TCP数据: productKey={}, payload={}", productKey, payload);

    // 使用统一的预解码服务
    UPRequest result = codecService.preDecode(productKey, payload);

    log.info("预解码完成: result={}", result);
    return result;
  }

  /**
   * 示例：通用编解码方法
   *
   * @param productKey 产品Key
   * @param payload 原始数据
   * @param codecMethod 编解码方法类型
   * @return 编解码结果
   */
  public String generalCodec(String productKey, String payload, CodecMethod codecMethod) {
    log.info("开始通用编解码: productKey={}, payload={}, method={}", productKey, payload, codecMethod);

    // 使用统一的通用编解码服务
    String result = codecService.codec(productKey, payload, codecMethod);

    log.info("通用编解码完成: result={}", result);
    return result;
  }

  /**
   * 示例：检查是否支持特定编解码方法
   *
   * @param productKey 产品Key
   * @param codecMethod 编解码方法类型
   * @return 是否支持
   */
  public boolean checkSupport(String productKey, CodecMethod codecMethod) {
    boolean supported = codecService.isSupported(productKey, codecMethod);
    log.info("检查编解码支持: productKey={}, method={}, supported={}", productKey, codecMethod, supported);
    return supported;
  }

  /**
   * 示例：泛型解码
   *
   * @param productKey 产品Key
   * @param payload 原始数据
   * @param elementType 目标类型
   * @param <T> 泛型类型
   * @return 解码后的对象列表
   */
  public <T> List<T> decodeWithType(String productKey, String payload, Class<T> elementType) {
    log.info("开始泛型解码: productKey={}, payload={}, elementType={}", productKey, payload, elementType);

    // 使用统一的泛型解码服务
    List<T> result = codecService.decode(productKey, payload, elementType);

    log.info("泛型解码完成: 结果数量={}", result.size());
    return result;
  }

  /**
   * 示例：IoT到第三方数据转换
   *
   * @param productKey 产品Key
   * @param payload 原始数据
   * @return 转换后的数据
   */
  public String iotToYour(String productKey, String payload) {
    log.info("开始IoT到第三方数据转换: productKey={}, payload={}", productKey, payload);

    // 使用统一的IoT到第三方转换服务
    String result = codecService.iotToYour(productKey, payload);

    log.info("IoT到第三方数据转换完成: result={}", result);
    return result;
  }

  /**
   * 示例：第三方到IoT数据转换
   *
   * @param productKey 产品Key
   * @param payload 原始数据
   * @return 转换后的数据
   */
  public String yourToIot(String productKey, String payload) {
    log.info("开始第三方到IoT数据转换: productKey={}, payload={}", productKey, payload);

    // 使用统一的第三方到IoT转换服务
    String result = codecService.yourToIot(productKey, payload);

    log.info("第三方到IoT数据转换完成: result={}", result);
    return result;
  }
}

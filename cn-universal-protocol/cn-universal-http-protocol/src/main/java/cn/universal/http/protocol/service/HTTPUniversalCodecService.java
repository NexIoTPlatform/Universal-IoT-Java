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

package cn.universal.http.protocol.service;

import cn.universal.common.exception.CodecException;
import cn.universal.core.message.UPRequest;
import cn.universal.core.protocol.support.ProtocolCodecSupport.CodecMethod;
import cn.universal.core.service.ICodecService;
import cn.universal.http.protocol.enums.HTTPCodecMethod;
import cn.universal.http.protocol.protocol.HTTPProtocolCodecLoader;
import cn.universal.http.protocol.protocol.HTTPProtocolSupportDefinition;
import cn.universal.http.protocol.protocol.HTTPProtocolUniversalCodec;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * HTTP统一编解码服务
 *
 * <p>专门处理HTTP协议的编解码，聚合HTTPProtocolCodecLoader功能，支持统一的CodecMethod
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/01/20
 */
@Slf4j
@Service
public class HTTPUniversalCodecService implements HTTPProtocolCodecLoader, IHTTPCodecService {

  @Autowired private ICodecService codecService;

  private final HTTPProtocolUniversalCodec httpProtocolUniversalCodec =
      HTTPProtocolUniversalCodec.getInstance();

  /**
   * HTTP编解码 - 支持统一的CodecMethod
   *
   * @param productKey 产品Key
   * @param payload 原始数据
   * @param codecMethod 编解码方法
   * @return 编解码结果
   */
  @Override
  public String httpCodec(String productKey, String payload, CodecMethod codecMethod) {
    String result = "";

    if (!httpProtocolUniversalCodec.isLoaded(productKey)) {
      try {
        load(productKey, CodecMethod.decode);
      } catch (CodecException e) {
        log.error("加载HTTP编解码器失败: productKey={}", productKey, e);
      }
    }

    long t1 = System.currentTimeMillis();
    try {
      result =
          switch (codecMethod) {
            case decode -> httpProtocolUniversalCodec.decode(productKey, payload);
            case encode -> httpProtocolUniversalCodec.encode(productKey, payload);
            case preDecode ->
                httpProtocolUniversalCodec.decode(productKey, payload); // HTTP没有preDecode，使用decode
            case codecAdd -> httpProtocolUniversalCodec.add(productKey, payload);
            case codecDelete -> httpProtocolUniversalCodec.delete(productKey, payload);
            case codecUpdate -> httpProtocolUniversalCodec.update(productKey, payload);
            case codecQuery -> httpProtocolUniversalCodec.query(productKey, payload);
            case iotToYour -> httpProtocolUniversalCodec.iotToYour(productKey, payload);
            case yourToIot -> httpProtocolUniversalCodec.yourToIot(productKey, payload);
            case codecFunction ->
                httpProtocolUniversalCodec.iotToYour(productKey, payload); // 映射到iotToYour
            case codecOther ->
                httpProtocolUniversalCodec.yourToIot(productKey, payload); // 映射到yourToIot
            default -> {
              log.warn("HTTP不支持的编解码方法: {}", codecMethod);
              yield payload;
            }
          };
    } catch (Exception e) {
      log.error("productKey={} 原始报文={} HTTP编解码报错", productKey, payload, e);
      result = "编解码失败: " + e.getMessage();
    }

    long t2 = System.currentTimeMillis();
    log.info(
        "HTTP编解码 productKey={} 原始报文={} {}={} 耗时={}ms",
        productKey,
        payload,
        codecMethod,
        result,
        (t2 - t1));
    return result;
  }

  /** 使用统一编解码服务的decode方法 */
  public <R> List<R> decode(
      String productKey, String payload, Object context, Class<R> elementType) {
    return codecService.decode(productKey, payload, context, elementType);
  }

  /** 使用统一编解码服务的encode方法 */
  public String encode(String productKey, String payload) {
    return codecService.encode(productKey, payload);
  }

  /** 使用统一编解码服务的preDecode方法 */
  public UPRequest preDecode(String productKey, String payload) {
    return codecService.preDecode(productKey, payload);
  }

  /** 使用统一编解码服务的通用codec方法 */
  public String codec(String productKey, String payload, CodecMethod codecMethod) {
    return codecService.codec(productKey, payload, codecMethod);
  }

  @Override
  public void load(String productKey, CodecMethod codecMethod) throws CodecException {
    log.debug("加载HTTP编解码器: productKey={}, method={}", productKey, codecMethod);
    // 这里可以添加HTTP编解码器的加载逻辑
  }

  /**
   * 检查是否支持HTTP编解码
   *
   * @param productKey 产品Key
   * @return 是否支持
   */
  @Override
  public boolean support(String productKey) {
    return httpProtocolUniversalCodec.isLoaded(productKey);
  }

  /** 实现HTTPProtocolCodecLoader接口的方法 */
  @Override
  public void load(HTTPProtocolSupportDefinition definition, HTTPCodecMethod codecMethod)
      throws CodecException {
    // 将HTTPCodecMethod转换为CodecMethod
    CodecMethod unifiedMethod = convertToUnifiedMethod(codecMethod);
    load(definition.getProductKey(), unifiedMethod);
  }

  @Override
  public void remove(String protocol) {
    log.debug("移除HTTP编解码器: protocol={}", protocol);
  }

  @Override
  public String codecAdd(String productKey, String payload) throws CodecException {
    return httpCodec(productKey, payload, CodecMethod.codecAdd);
  }

  @Override
  public String codecDelete(String productKey, String payload) throws CodecException {
    return httpCodec(productKey, payload, CodecMethod.codecDelete);
  }

  @Override
  public String codecUpdate(String productKey, String payload) throws CodecException {
    return httpCodec(productKey, payload, CodecMethod.codecUpdate);
  }

  @Override
  public String codecQuery(String productKey, String payload) throws CodecException {
    return httpCodec(productKey, payload, CodecMethod.codecQuery);
  }

  @Override
  public boolean isLoaded(String productKey) {
    return httpProtocolUniversalCodec.isLoaded(productKey);
  }

  @Override
  public String execute(String productKey, String payload, CodecMethod codecMethod)
      throws CodecException {
    return httpCodec(productKey, payload, codecMethod);
  }

  /** 将HTTPCodecMethod转换为统一的CodecMethod */
  private CodecMethod convertToUnifiedMethod(HTTPCodecMethod httpMethod) {
    return switch (httpMethod) {
      case decode -> CodecMethod.decode;
      case encode -> CodecMethod.encode;
      case preDecode -> CodecMethod.preDecode;
      case codecAdd -> CodecMethod.codecAdd;
      case codecDelete -> CodecMethod.codecDelete;
      case codecUpdate -> CodecMethod.codecUpdate;
      case codecQuery -> CodecMethod.codecQuery;
      case iotToYour -> CodecMethod.iotToYour;
      case yourToIot -> CodecMethod.yourToIot;
      case codecFunction -> CodecMethod.codecFunction;
      case codecOther -> CodecMethod.codecOther;
    };
  }
}

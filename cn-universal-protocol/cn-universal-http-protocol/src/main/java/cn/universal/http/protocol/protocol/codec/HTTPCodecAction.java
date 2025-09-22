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

package cn.universal.http.protocol.protocol.codec;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.UPRequest;
import cn.universal.http.protocol.enums.HTTPCodecMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * HTTP编解码处理器
 *
 * <p>提供HTTP协议的编解码功能，支持add、update、delete、select、function、other等操作
 *
 * @version 1.0 @Author Aleo
 * @since 2025/01/20
 */
@Slf4j
@Component
public class HTTPCodecAction extends HTTPAbstractCodec {

  /**
   * 执行编解码操作
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @param codecMethod 编解码方法
   * @return 编解码结果
   */
  public String executeCodec(String productKey, String payload, HTTPCodecMethod codecMethod) {
    if (payload == null || productKey == null) {
      log.warn("[HTTP编解码] 参数不完整: payload={}, productKey={}", payload, productKey);
      return "编解码参数不完整";
    }

    return codec(productKey, payload, codecMethod);
  }

  /**
   * 执行添加设备编解码
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeAddCodec(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecAdd);
  }

  /**
   * 执行删除设备编解码
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeDeleteCodec(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecDelete);
  }

  /**
   * 执行更新设备编解码
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeUpdateCodec(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecUpdate);
  }

  /**
   * 执行查询设备编解码
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeSelectCodec(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecQuery);
  }

  /**
   * 执行功能调用编解码
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeFunctionCodec(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecFunction);
  }

  /**
   * 执行其他操作编解码
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeOtherCodec(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecOther);
  }

  /**
   * 执行编解码功能方法
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeCodecFunction(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecFunction);
  }

  /**
   * 执行编解码其他方法
   *
   * @param productKey 产品标识
   * @param payload 原始数据
   * @return 编解码结果
   */
  public String executeCodecOther(String productKey, String payload) {
    return executeCodec(productKey, payload, HTTPCodecMethod.codecOther);
  }

  public <R> List<R> decode(String productKey, String payload, Class<R> elementType) {
    // 开始解码操作
    List<R> rs = null;
    long t1 = System.currentTimeMillis();
    if (support(productKey)) {
      // 根据类型选择解码实现类
      try {
        String result = executeCodec(productKey, payload, HTTPCodecMethod.decode);
        if (StrUtil.isNotBlank(result)) {
          if (JSONUtil.isTypeJSONObject(result)) {
            rs = Stream.of(JSONUtil.toBean(result, elementType)).collect(Collectors.toList());
          } else if (JSONUtil.isTypeJSONArray(result)) {
            rs = JSONUtil.toList(result, elementType);
          } else {
            rs = emptyProtocol(result, elementType);
          }
        }
      } catch (Exception e) {
        log.error("产品编号={} 原始报文={} , 解码报错", productKey, payload, e);
      }
    } else {
      rs = emptyProtocol(payload, elementType);
    }
    long t2 = System.currentTimeMillis();
    log.info(
        "产品编号={} 原始报文={} , 解码={} 耗时={}ms", productKey, payload, JSONUtil.toJsonStr(rs), (t2 - t1));
    return rs;
  }

  /** 当编解码为空，拼装payload */
  private <R> List<R> emptyProtocol(String content, Class<R> elementType) {
    // 如果消息内容为空，则当一个"空报文"属性上报
    List<R> list = new ArrayList<>();
    // 如果消息是纯字符串
    try {
      R upRequest = JSONUtil.toBean(content, elementType);
      // 检查是否是UPRequest的子类，如果是则设置默认值
      if (upRequest instanceof UPRequest request) {
        request.setEmptyProtocol(true);
        request.setMessageType(MessageType.PROPERTIES);
        request.setPayload(content);
      }
      list.add(upRequest);
    } catch (Exception e) {
      log.warn("emptyProtocol: ", e);
    }
    return list;
  }
}

/*
 *
 * Copyright (c) 2025, cn-universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */
package cn.universal.core.service;

import cn.universal.core.message.UPRequest;
import java.util.List;

public interface Codec {

  /**
   * 消息解码->物模型
   *
   * @param productKey 产品ProductKey
   * @param payload 实际入参字符串
   * @param elementType 格式化类型
   * @param <R> 泛型
   * @return 返回解码后的结构化消息
   */
  <R> List<R> decode(String productKey, String payload, Class<R> elementType);

  /**
   * 消息解码->物模型
   *
   * @param productKey 产品ProductKey
   * @param payload 实际入参字符串
   * @return 返回解码后的结构化消息
   */
  default List<UPRequest> decode(String productKey, String payload) {
    return null;
  }

  /**
   * 结构化消息->设备识别（二进制、16进制、其他）
   *
   * @param productKey 产品key
   * @param payload 消息的原串
   */
  String encode(String productKey, String payload);
}

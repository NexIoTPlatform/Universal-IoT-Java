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

package cn.universal.core.service;

import cn.universal.core.message.UPRequest;

/** 消息编解码 */
public interface ICodec {

  default String version() {
    return "1.0.0";
  }

  /** 上行消息消息，预编码 */
  default UPRequest preDecode(String productKey, String message) {
    return null;
  }

  /** 进编码前特殊处理 附加影子 */
  default String beforeEncode(String productKey, String deviceId, String config, String function) {
    return function;
  }

  /** 下行消息编码 */
  default String spliceDown(String productKey, String payload) {
    return null;
  }
}

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

package cn.universal.core.service;

import cn.hutool.json.JSONObject;
import cn.universal.common.domain.R;

/** （下行）平台到设备 */
public interface IDown {
  /** 执行服务名称 */
  String name();

  /** 执行服务code */
  String code();

  /** 下行处理 */
  R doAction(String msg);

  /** 产品级下行处理 */
  default R downToThirdPlatform(String msg) {
    return null;
  }

  default R doAction(JSONObject msg) {
    return null;
  }

  /**
   * 保存设备云端指令
   *
   * @param productKey 产品key
   * @param deviceId 设备序列号
   */
  default void storeCommand(String productKey, String deviceId, Object data) {}
}

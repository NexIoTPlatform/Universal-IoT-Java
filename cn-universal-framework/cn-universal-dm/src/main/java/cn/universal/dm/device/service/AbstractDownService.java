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

import cn.hutool.json.JSONObject;
import cn.universal.core.service.ICodecService;
import cn.universal.core.service.IDown;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 下发抽象工具类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 10:50
 */
@Slf4j
public abstract class AbstractDownService<T> extends AbstratIoTService implements IDown {

  @Autowired protected ICodecService iCodecService;

  /**
   * 消息转换和编解码
   *
   * @param request
   * @return
   */
  protected abstract T convert(String request);

  protected String encodeWithShadow(String productKey, String deviceId, String payload) {
    JSONObject jsonObject = getProductConfiguration(productKey);
    JSONObject context = null;
    if (jsonObject != null) {
      // 上行报文是否需要附加影子
      Boolean requireDownShadow = jsonObject.getBool("requireDownShadow", false);
      if (requireDownShadow) {
        context = iotDeviceShadowService.getDeviceShadowObj(productKey, deviceId);
      }
    }
    // 使用新的统一编解码服务
    return iCodecService.encode(productKey, payload, context);
  }
}

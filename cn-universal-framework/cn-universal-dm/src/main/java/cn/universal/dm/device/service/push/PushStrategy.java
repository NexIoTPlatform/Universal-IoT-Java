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

package cn.universal.dm.device.service.push;

import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.persistence.base.BaseUPRequest;

/**
 * 推送策略接口
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/9
 */
public interface PushStrategy {

  /**
   * 执行推送
   *
   * @param request 上行请求
   * @param messageJson 消息JSON字符串
   */
  IoTPushResult execute(BaseUPRequest request, String messageJson);

  /**
   * 是否支持该推送类型
   *
   * @return 是否支持
   */
  boolean isSupported();
}

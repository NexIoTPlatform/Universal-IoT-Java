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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 编解码服务工厂
 *
 * <p>用于获取不同协议的编解码服务
 *
 * @version 1.0 @Author Aleo
 * @since 2025/01/20
 */
@Slf4j
@Component
public class CodecServiceFactory {

  @Autowired private ICodecService defaultCodecService;

  /**
   * 获取默认编解码服务
   *
   * @return 默认编解码服务
   */
  public ICodecService getDefaultCodecService() {
    return defaultCodecService;
  }

  /**
   * 根据协议类型获取编解码服务
   *
   * @param protocolType 协议类型
   * @return 对应的编解码服务
   */
  public ICodecService getCodecService(String protocolType) {
    return defaultCodecService;
  }

  /**
   * 检查是否支持指定协议
   *
   * @param protocolType 协议类型
   * @return 是否支持
   */
  public boolean isSupported(String protocolType) {
    return true; // 默认服务支持所有协议
  }
}

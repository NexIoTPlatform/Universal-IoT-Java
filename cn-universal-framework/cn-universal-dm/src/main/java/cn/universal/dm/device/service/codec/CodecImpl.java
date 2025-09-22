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

package cn.universal.dm.device.service.codec;

import cn.universal.core.message.UPRequest;
import cn.universal.core.service.ICodec;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.AbstratIoTService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/6/20
 */
@Service("codecImpl")
@Slf4j
public class CodecImpl extends AbstratIoTService implements ICodec {

  @Autowired private ICodecService codecService;

  @Override
  public UPRequest preDecode(String productKey, String message) {
    // 使用新的统一编解码服务
    return codecService.preDecode(productKey, message);
  }
}

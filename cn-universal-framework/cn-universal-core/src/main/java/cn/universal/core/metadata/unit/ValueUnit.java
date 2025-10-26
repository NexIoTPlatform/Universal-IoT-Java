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

package cn.universal.core.metadata.unit;

import cn.universal.core.metadata.FormatSupport;
import cn.universal.core.metadata.Metadata;
import java.io.Serializable;
import java.util.Map;

/**
 * 值单位
 *
 * @version 1.0
 */
public interface ValueUnit extends Metadata, FormatSupport, Serializable {

  String getSymbol();

  @Override
  default Map<String, Object> getExpands() {
    return null;
  }
}

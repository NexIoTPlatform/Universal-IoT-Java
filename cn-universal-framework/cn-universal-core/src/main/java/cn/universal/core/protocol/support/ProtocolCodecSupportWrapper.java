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

package cn.universal.core.protocol.support;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;

/**
 * 编解码支持类
 *
 * @version 1.0 @Author Aleo
 * @since 2023/5/20 19:08
 */
public abstract class ProtocolCodecSupportWrapper {

  protected String str(Object obj) {
    if (null == obj || "null".equalsIgnoreCase(obj + "")) {
      return CharSequenceUtil.EMPTY;
    } else if (obj instanceof String) {
      return obj.toString();
    } else {
      return JSONUtil.toJsonStr(obj);
    }
  }
}

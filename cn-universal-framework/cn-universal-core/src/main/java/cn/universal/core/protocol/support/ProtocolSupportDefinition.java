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

import cn.universal.core.protocol.support.ProtocolCodecSupport.CodecMethod;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 19:11
 */
@Data
public class ProtocolSupportDefinition {

  private String id;
  private String name;
  private String description;
  private String provider;
  private String type;
  private byte state;
  private Map<String, Object> configuration;
  private Set<String> supportMethods;

  public boolean supportMethod(CodecMethod method) {
    if (supportMethods == null) {
      return false;
    }
    if (supportMethods.contains(method.name())) {
      return true;
    }
    return false;
  }
}

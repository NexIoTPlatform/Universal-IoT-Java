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

package cn.universal.core.protocol.request;

import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 21:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProtocolEncodeRequest {

  private ProtocolSupportDefinition definition;
  private String payload;
  private Object context;

  public ProtocolEncodeRequest(ProtocolSupportDefinition definition, String payload) {
    this.definition = definition;
    this.payload = payload;
  }
}

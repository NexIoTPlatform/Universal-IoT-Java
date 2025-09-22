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

package cn.universal.http.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP 协议配置属性
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/2
 */
@ConfigurationProperties(prefix = "http.protocol")
@Data
public class HttpProperties {

  /** 是否启用HTTP协议模块 */
  private boolean enabled = true;
}

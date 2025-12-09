/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.wvp.protocol.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WVP 协议配置属性
 *
 * @version 2.0
 * @Author gitee.com/NexIoT
 * @since 2025/1/8
 */
@ConfigurationProperties(prefix = "wvp.protocol")
@Data
public class WvpProperties {

  /** 是否启用WVP协议模块 */
  private boolean enabled = true;
}

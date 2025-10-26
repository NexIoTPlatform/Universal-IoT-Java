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

package cn.universal.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 协议更新事件
 *
 * <p>当协议配置更新时触发，用于通知相关组件清理缓存
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Getter
public class ProtocolUpdatedEvent extends ApplicationEvent {

  private final String productKey;
  private final String protocolType;

  /**
   * 构造函数
   *
   * @param source 事件源
   * @param productKey 产品Key
   * @param protocolType 协议类型
   */
  public ProtocolUpdatedEvent(Object source, String productKey, String protocolType) {
    super(source);
    this.productKey = productKey;
    this.protocolType = protocolType;
  }
}

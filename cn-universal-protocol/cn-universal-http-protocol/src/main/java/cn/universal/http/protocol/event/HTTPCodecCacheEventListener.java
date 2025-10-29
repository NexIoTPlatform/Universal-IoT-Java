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

package cn.universal.http.protocol.event;

import cn.universal.common.event.ProtocolUpdatedEvent;
import cn.universal.core.protocol.jar.ProtocolCodecJar;
import cn.universal.core.protocol.jscrtipt.ProtocolCodecJscript;
import cn.universal.core.protocol.magic.ProtocolCodecMagic;
import cn.universal.http.protocol.protocol.codec.HTTPAbstractCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * HTTP编解码缓存事件监听器
 *
 * <p>监听协议更新事件，自动清理相关的编解码缓存
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/01/20
 */
@Slf4j
@Component
public class HTTPCodecCacheEventListener extends HTTPAbstractCodec {

  /**
   * 监听协议更新事件
   *
   * <p>当协议配置更新时，自动清理相关的编解码缓存
   *
   * @param event 协议更新事件
   */
  @EventListener
  public void handleProtocolUpdatedEvent(ProtocolUpdatedEvent event) {
    try {
      String productKey = event.getProductKey();
      String protocolType = event.getProtocolType();

      removeCodec(protocolType, productKey);

      log.info("[HTTP编解码缓存监听器] 收到协议更新事件: productKey={}, protocolType={}", productKey, protocolType);

      // 只处理HTTP相关的协议类型
      if (isHttpProtocol(protocolType)) {
        // 清理HTTP编解码缓存
        removeCodec(productKey);
        log.info("[HTTP编解码缓存监听器] 已清理HTTP编解码缓存: productKey={}", productKey);
      } else {
        log.debug(
            "[HTTP编解码缓存监听器] 跳过非HTTP协议: productKey={}, protocolType={}", productKey, protocolType);
      }

    } catch (Exception e) {
      log.error("[HTTP编解码缓存监听器] 处理协议更新事件异常: productKey={}", event.getProductKey(), e);
    }
  }

  private void removeCodec(String protocolType, String productKey) {
    if ("jscript".equals(protocolType)) {
      ProtocolCodecJscript.getInstance().remove(productKey);
    } else if ("magic".equals(protocolType)) {
      ProtocolCodecMagic.getInstance().remove(productKey);
    } else {
      ProtocolCodecJar.getInstance().remove(productKey);
    }
  }

  /**
   * 判断是否为HTTP协议类型
   *
   * @param protocolType 协议类型
   * @return 是否为HTTP协议
   */
  private boolean isHttpProtocol(String protocolType) {
    // 根据实际业务逻辑判断是否为HTTP协议
    // 这里可以根据协议类型、配置等来判断
    return "magic".equals(protocolType) || "jscript".equals(protocolType);
  }
}

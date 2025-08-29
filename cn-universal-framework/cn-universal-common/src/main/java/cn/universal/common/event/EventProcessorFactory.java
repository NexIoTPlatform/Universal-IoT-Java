package cn.universal.common.event;

import cn.universal.common.event.processer.FenceEventProcessor;
import cn.universal.common.event.processer.ProductConfigProcessor;
import cn.universal.common.event.processer.TcpDownProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 事件处理器工厂类 统一管理所有Redis事件处理器
 *
 * @author Aleo
 * @version 1.0
 * @since 2025/1/9
 */
@Slf4j
@Component
public class EventProcessorFactory {

  // 电子围栏事件处理器
  @Autowired(required = false)
  private FenceEventProcessor fenceEventProcessor;

  // TCP下行指令处理器
  @Autowired(required = false)
  private TcpDownProcessor tcpDownProcessor;

  // 产品配置处理器
  @Autowired(required = false)
  private ProductConfigProcessor productConfigProcessor;

  /** 处理电子围栏事件 */
  public void handleFenceEvent(EventMessage message) {
    if (fenceEventProcessor != null) {
      try {
        fenceEventProcessor.handleFenceEvent(message);
      } catch (Exception e) {
        log.error("[事件处理器] 电子围栏事件处理失败", e);
      }
    } else {
      log.warn("[事件处理器] FenceEventProcessor未找到，跳过电子围栏事件处理");
    }
  }

  /** 处理TCP下行指令事件 */
  public void handleTcpDown(EventMessage message) {
    if (tcpDownProcessor != null) {
      try {
        tcpDownProcessor.handleTcpDownEvent(message);
      } catch (Exception e) {
        log.error("[事件处理器] TCP下行指令处理失败", e);
      }
    } else {
      log.warn("[事件处理器] TcpDownProcessor未找到，跳过TCP下行指令处理");
    }
  }

  /** 处理产品配置更新事件 */
  public void handleProductConfigUpdated(EventMessage message) {
    if (productConfigProcessor != null) {
      try {
        productConfigProcessor.handleProductConfigUpdated(message);
      } catch (Exception e) {
        log.error("[事件处理器] 产品配置更新处理失败", e);
      }
    } else {
      log.warn("[事件处理器] ProductConfigProcessor未找到，跳过产品配置更新处理");
    }
  }

  /** 处理产品配置更新事件 */
  public void handleProtocolUpdated(EventMessage message) {
    if (productConfigProcessor != null) {
      try {
        productConfigProcessor.handleProtocolUpdated(message);
      } catch (Exception e) {
        log.error("[事件处理器] 产品协议更新处理失败", e);
      }
    } else {
      log.warn("[事件处理器] ProductConfigProcessor未找到，产品协议更新处理失败");
    }
  }
}

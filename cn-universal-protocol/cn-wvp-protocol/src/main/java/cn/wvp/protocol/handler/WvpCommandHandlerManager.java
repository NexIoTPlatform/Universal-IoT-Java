package cn.wvp.protocol.handler;

import cn.hutool.core.map.MapUtil;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * WVP命令处理器管理器
 */
@Slf4j
@Component
public class WvpCommandHandlerManager implements ApplicationContextAware {

  private Map<String, WvpCommandHandler> handlerMap = new HashMap<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    Map<String, WvpCommandHandler> current =
        applicationContext.getBeansOfType(WvpCommandHandler.class);
    if (current != null && MapUtil.isNotEmpty(current)) {
      current.forEach(
          (key, handler) -> {
            String command = handler.getSupportedCommand();
            if (command != null && !command.isEmpty()) {
              handlerMap.put(command.toLowerCase(), handler);
              log.info("注册WVP命令处理器: {} -> {}", command, handler.getClass().getSimpleName());
            }
          });
    }
  }

  /**
   * 处理命令
   *
   * @param request 下行请求
   * @return 处理结果
   */
  public R<?> handleCommand(WvpDownRequest request) {
    String commandKey = getCommandKey(request);
    if (commandKey == null) {
      log.warn("WVP设备处理功能下行无法确定命令类型,不处理={}", request);
      return R.error("无法确定命令类型");
    }

    WvpCommandHandler handler = handlerMap.get(commandKey);
    if (handler == null) {
      log.info("WVP设备处理功能下行未匹配到处理器: {}", commandKey);
      return R.error("未找到对应的处理器: " + commandKey);
    }

    try {
      return handler.handle(request);
    } catch (Exception e) {
      log.error("处理命令时发生异常: {}", commandKey, e);
      return R.error("处理命令时发生异常: " + e.getMessage());
    }
  }

  /**
   * 获取命令键值
   * 兼容两种命令格式：
   * 1. DEV_FUNCTION 类型：从 function.function 字段或 data.function 字段获取
   * 2. DEV_ADD/DEV_DEL/DEV_UPDATE 类型：从 cmd 字段获取
   *
   * @param request 下行请求
   * @return 命令键值
   */
  private String getCommandKey(WvpDownRequest request) {
    // 首先尝试从 function.function 字段获取（DEV_FUNCTION 类型，新方式）
    if (request.getFunction() != null) {
      String function = (String) request.getFunction().get("function");
      if (function != null && !function.isEmpty()) {
        return function.toLowerCase();
      }
    }
    
    // 其次尝试从 data.function 字段获取（DEV_FUNCTION 类型，兼容旧方式）
    if (request.getData() != null) {
      String function = request.getData().getStr("function");
      if (function != null && !function.isEmpty()) {
        return function.toLowerCase();
      }
    }

    // 如果 function 为空，则从 cmd 字段获取（DEV_ADD/DEV_DEL/DEV_UPDATE 类型）
    if (request.getCmd() != null) {
      return request.getCmd().name().toLowerCase();
    }

    return null;
  }

  /**
   * 获取所有支持的命令
   *
   * @return 支持的命令列表
   */
  public String[] getSupportedCommands() {
    return handlerMap.keySet().toArray(new String[0]);
  }
}

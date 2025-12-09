package cn.wvp.protocol.handle;

import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.persistence.base.IoTDownAdapter;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandlerManager;
import jakarta.annotation.Resource;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * WVP 下行实际处理入口（统一命令分发模式）
 */
@Component
public class WvpDownHandle extends IoTDownAdapter<WvpDownRequest> {

  private static final Logger log = LoggerFactory.getLogger(WvpDownHandle.class);

  @Resource private WvpCommandHandlerManager commandHandlerManager;

  public R<?> down(WvpDownRequest downRequest) {
    if (downRequest == null || downRequest.getCmd() == null) {
      log.warn("WVP设备处理下行对象为空,不处理={}", downRequest);
      return R.error("WVP设备处理下行对象为空");
    }

    R<?> preResult =
        beforeDownAction(downRequest.getIoTProduct(), downRequest.getData(), downRequest);
    if (Objects.nonNull(preResult)) {
      return preResult;
    }

    return dispatchCommand(downRequest);
  }

  private R<?> dispatchCommand(WvpDownRequest downRequest) {
    DownCmd cmd = downRequest.getCmd();
    switch (cmd) {
      case DEV_ADD:
      case DEV_DEL:
      case DEV_UPDATE:
        return commandHandlerManager.handleCommand(downRequest);
      case DEV_FUNCTION:
        return commandHandlerManager.handleCommand(downRequest);
      default:
        log.info("WVP设备处理下行未匹配到方法: {}", cmd);
        return R.error("未支持的命令类型: " + cmd);
    }
  }
}

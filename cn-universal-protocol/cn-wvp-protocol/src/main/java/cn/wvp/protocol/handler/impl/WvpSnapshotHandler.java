package cn.wvp.protocol.handler.impl;

import cn.universal.common.constant.CameraCommand;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import cn.wvp.protocol.service.WvpDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class WvpSnapshotHandler implements WvpCommandHandler {
  @Resource private WvpDeviceService wvpDeviceService;

  public R<?> handle(WvpDownRequest request) {
    return wvpDeviceService.snapshot(request);
  }

  public String getSupportedCommand() {
    return CameraCommand.CAMERA_SNAPSHOT.getFunctionName();
  }
}

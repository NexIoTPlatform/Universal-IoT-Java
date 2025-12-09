package cn.wvp.protocol.handler.impl;

import cn.universal.common.constant.CameraCommand;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import cn.wvp.protocol.service.WvpDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class WvpLiveStreamHandler implements WvpCommandHandler {
  @Resource private WvpDeviceService wvpDeviceService;

  public R<?> handle(WvpDownRequest request) {
    String function = request.getData() == null ? null : request.getData().getStr("function");
    return wvpDeviceService.startPreview(request);
  }
  public String getSupportedCommand() { return CameraCommand.CAMERA_LIVE_STREAM.getFunctionName(); }
}

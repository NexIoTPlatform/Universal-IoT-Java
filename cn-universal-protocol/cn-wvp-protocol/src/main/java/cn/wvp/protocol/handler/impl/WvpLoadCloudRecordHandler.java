package cn.wvp.protocol.handler.impl;

import cn.universal.common.constant.CameraCommand;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import cn.wvp.protocol.service.WvpDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * WVP加载云端录像处理器
 *
 * @version 1.0
 * @since 2025/11/09
 */
@Component
public class WvpLoadCloudRecordHandler implements WvpCommandHandler {
  @Resource private WvpDeviceService wvpDeviceService;

  public R<?> handle(WvpDownRequest request) {
    return wvpDeviceService.loadCloudRecord(request);
  }

  public String getSupportedCommand() {
    return CameraCommand.LOAD_CLOUD_RECORD.getFunctionName();
  }
}

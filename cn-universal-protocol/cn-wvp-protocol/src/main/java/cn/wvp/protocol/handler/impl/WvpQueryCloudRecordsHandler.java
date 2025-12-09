package cn.wvp.protocol.handler.impl;

import cn.universal.common.constant.CameraCommand;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import cn.wvp.protocol.service.WvpDeviceService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * WVP查询云端录像处理器
 *
 * @version 1.0
 * @since 2025/11/09
 */
@Component
public class WvpQueryCloudRecordsHandler implements WvpCommandHandler {
  @Resource private WvpDeviceService wvpDeviceService;

  public R<?> handle(WvpDownRequest request) {
    return wvpDeviceService.queryCloudRecords(request);
  }

  public String getSupportedCommand() {
    return CameraCommand.QUERY_CLOUD_RECORDS.getFunctionName();
  }
}

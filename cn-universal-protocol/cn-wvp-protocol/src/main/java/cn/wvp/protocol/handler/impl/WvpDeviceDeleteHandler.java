package cn.wvp.protocol.handler.impl;

import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WvpDeviceDeleteHandler implements WvpCommandHandler {

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;
  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Resource private IoTDeviceService iotDeviceService;

  public R<?> handle(WvpDownRequest downRequest) {
    // 幂等检查
    Map<String,Object> param = new HashMap<>();
    param.put("iotId", downRequest.getProductKey() + downRequest.getDeviceId());
    IoTDeviceDTO dto = ioTDeviceMapper.selectIoTDeviceBO(param);
    if (dto == null) {
      return R.error("设备不存在");
    }
    // TODO: 接入WVP解绑逻辑，若未绑定则直接删除本地
    ioTDeviceLifeCycle.delete(dto, downRequest);
    iotDeviceService.delDevInstance(dto.getIotId());
    return R.ok();
  }

  public String getSupportedCommand() {
    return DownCmd.DEV_DEL.name().toLowerCase();
  }
}

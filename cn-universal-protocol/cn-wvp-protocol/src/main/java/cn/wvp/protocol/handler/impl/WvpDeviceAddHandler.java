package cn.wvp.protocol.handler.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DeviceStatus;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class WvpDeviceAddHandler implements WvpCommandHandler {

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  public R<?> handle(WvpDownRequest downRequest) {
    // 幂等检查：productKey + deviceId
    Map<String,Object> query = new HashMap<>();
    query.put("iotId", downRequest.getProductKey() + downRequest.getDeviceId());
    IoTDeviceDTO exist = ioTDeviceMapper.selectIoTDeviceBO(query);
    if (exist != null) {
      return R.error("设备已存在");
    }
    // TODO: 接入WVP级联/GB注册绑定逻辑
    IoTDevice ioTDevice = IoTDevice.builder()
        .deviceId(downRequest.getDeviceId())
        .createTime(System.currentTimeMillis() / 1000)
        .deviceName(StrUtil.emptyToDefault(downRequest.getDetail(), downRequest.getDeviceId()))
        .state(DeviceStatus.offline.getCode())
        .iotId(downRequest.getProductKey() + downRequest.getDeviceId())
        .creatorId(downRequest.getAppUnionId())
        .productName(downRequest.getIoTProduct().getName())
        .application(downRequest.getApplicationId())
        .detail(downRequest.getDetail())
        .productKey(downRequest.getProductKey())
        .build();
    Map<String, Object> config = new HashMap<>();
    config.put("discardValue", 0);
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
    ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
    ioTDeviceLifeCycle.create(downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);
    Map<String, Object> result = new HashMap<>();
    result.put("iotId", ioTDevice.getIotId());
    if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
      result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
    }
    result.put("productKey", downRequest.getProductKey());
    result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
    return R.ok(result);
  }

  public String getSupportedCommand() {
    return DownCmd.DEV_ADD.name().toLowerCase();
  }
}

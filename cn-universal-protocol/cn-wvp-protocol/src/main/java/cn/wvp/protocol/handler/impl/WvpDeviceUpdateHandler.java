package cn.wvp.protocol.handler.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.handler.WvpCommandHandler;
import jakarta.annotation.Resource;
import java.util.Map;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

@Component
public class WvpDeviceUpdateHandler implements WvpCommandHandler {
  @Resource private IoTDeviceMapper ioTDeviceMapper;

  public R<?> handle(WvpDownRequest request) {
    Map<String, Object> data = request.getWvpRequestData();
    if (data == null || MapUtil.isEmpty(data)) return R.error("缺少参数: data");
    String newName = (String) data.get("deviceName");
    if (newName == null || newName.isEmpty()) return R.error("缺少参数: deviceName");
    String iotId = request.getProductKey() + request.getDeviceId();
    String latitude = (String) data.get("latitude");
    String longitude = (String) data.get("longitude");
    // 根据iotId更新设备名称
    IoTDevice dev = new IoTDevice();
    dev.setDeviceName(newName);
    if (StrUtil.isNotBlank(latitude) && StrUtil.isNotBlank(longitude)) {
      dev.setCoordinate(longitude + "," + latitude);
    }

    // 使用Example构建更新条件，根据iotId更新
    Example example = new Example(IoTDevice.class);
    example.createCriteria().andEqualTo("iotId", iotId);
    int count = ioTDeviceMapper.updateByExampleSelective(dev, example);
    if (count > 0) {
      return R.ok("更新成功");
    }
    return R.ok("无变化");
  }

  public String getSupportedCommand() {
    return DownCmd.DEV_UPDATE.name().toLowerCase();
  }
}

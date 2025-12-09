package cn.wvp.protocol.entity;

import cn.universal.common.constant.IoTConstant.DownCmd;
import java.util.Map;

/**
 * WVP 下行请求封装
 */
public class WvpDownRequest extends cn.universal.persistence.base.BaseDownRequest {
  private DownCmd cmd;
  private String productKey;
  private String deviceId;
  private Map<String, Object> wvpRequestData;

  public DownCmd getCmd() {
    return cmd;
  }

  public void setCmd(DownCmd cmd) {
    this.cmd = cmd;
  }

  public String getProductKey() {
    return productKey;
  }

  public void setProductKey(String productKey) {
    this.productKey = productKey;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public Map<String, Object> getWvpRequestData() {
    return wvpRequestData;
  }

  public void setWvpRequestData(Map<String, Object> wvpRequestData) {
    this.wvpRequestData = wvpRequestData;
  }
}

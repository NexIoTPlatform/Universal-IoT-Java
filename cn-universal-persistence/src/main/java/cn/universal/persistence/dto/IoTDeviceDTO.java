/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.dto;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.core.metadata.DeviceMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceDTO {

  /** 归属应用ID */
  private String applicationId;

  private String appId;
  private String userId;

  /** 用户唯一标志 */
  private String userUnionId;

  private String upTopic;
  private String downTopic;
  private String scope;
  private String iotId;
  private String deviceId;
  private String extDeviceId;
  private String deviceName;
  private DeviceNode deviceNode;
  private String productKey;
  private String productName;
  private String thirdPlatform;
  private String gwProductKey;
  private String metadata;
  private String configuration;
  // 在线和离线
  private Boolean state;

  /** 应用状态,true表示禁用 */
  private boolean appDisable;

  /** 首次注册时间 */
  private Integer registryTime;

  private Integer onlineTime;
  private List<String> devGroupId;
  private JSONObject productConfig;

  /** 是否有影子 */
  private boolean shadow;

  /** 设备配置字段 */
  private String devConfiguration;

  private String areasId;
  private String coordinate;
  private String detail;
  private String storePolicyCfg;
  private String storePolicy;

  /**
   * 原串
   *
   * @return
   */
  private String payload;

  @JsonIgnore
  public DeviceMetadata getDeviceMetadata() {
    if (StrUtil.isNotBlank(metadata)) {
      return new DeviceMetadata(new JSONObject(metadata));
    }
    return new DeviceMetadata(new JSONObject());
  }

  public JSONObject getProductConfig() {
    if (StrUtil.isNotBlank(configuration)) {
      productConfig = JSONUtil.parseObj(configuration);
    } else {
      productConfig = new JSONObject();
    }
    return productConfig;
  }

  public void clearMetadata() {
    setMetadata(null);
  }
}

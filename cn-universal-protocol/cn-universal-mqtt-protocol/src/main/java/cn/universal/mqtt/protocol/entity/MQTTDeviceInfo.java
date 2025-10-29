/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.mqtt.protocol.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MQTT设备信息实体
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/1/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MQTTDeviceInfo {

  /** 产品Key */
  private String productKey;

  /** 设备ID */
  private String deviceId;

  /** 设备唯一标识 */
  private String deviceUniqueId;

  /** 设备名称 */
  private String deviceName;

  /** 设备状态 */
  @Builder.Default private DeviceStatus status = DeviceStatus.OFFLINE;

  /** 最后活跃时间 */
  private LocalDateTime lastActiveTime;

  /** 注册时间 */
  @Builder.Default private LocalDateTime registerTime = LocalDateTime.now();

  /** 关联的MQTT客户端ID */
  private String mqttClientId;

  /** 网关产品Key（如果是子设备） */
  private String gatewayProductKey;

  /** 网关设备ID（如果是子设备） */
  private String gatewayDeviceId;

  /** 是否为网关设备 */
  @Builder.Default private boolean isGateway = false;

  /** 设备属性 */
  private String properties;

  /** 设备标签 */
  private String tags;

  /** 设备状态枚举 */
  public enum DeviceStatus {
    ONLINE, // 在线
    OFFLINE, // 离线
    INACTIVE, // 不活跃
    UNKNOWN // 未知
  }

  /** 获取设备唯一标识 */
  public String getDeviceUniqueId() {
    if (deviceUniqueId == null && productKey != null && deviceId != null) {
      deviceUniqueId = productKey + ":" + deviceId;
    }
    return deviceUniqueId;
  }

  /** 检查是否为子设备 */
  public boolean isSubDevice() {
    return gatewayProductKey != null && gatewayDeviceId != null;
  }

  /** 更新最后活跃时间 */
  public void updateLastActiveTime() {
    this.lastActiveTime = LocalDateTime.now();
    if (this.status == DeviceStatus.OFFLINE) {
      this.status = DeviceStatus.ONLINE;
    }
  }
}

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

package cn.universal.persistence.entity.bo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceBO implements Serializable {

  private static final long serialVersionUID = 1L;
  private Long id;

  /** 对外设备唯一标识符 */
  private String iotId;

  /** Lora密钥 */
  private String secretKey;

  /** 归属应用 */
  private String application;

  /** 激活时间 */
  private Integer registryTime;

  /** 最后上线时间 */
  private Long onlineTime;

  /** 设备自身序号 */
  private String deviceId;

  /** 第三方设备ID唯一标识符 */
  private String extDeviceId;

  /** 别名 */
  private String nickName;

  /** 设备名称 */
  private String productName;

  private Long features;

  /** 网关产品ProductKey */
  private String gwProductKey;

  private String gwDeviceId;

  /** 设备密钥 */
  private String deviceSecret;

  /** 产品key */
  private String productKey;

  /** 设备实例名称 */
  private String deviceName;

  private String creatorId;
  private String creatorName;

  /** 0-离线，1-在线 */
  private Boolean state;

  /** 说明 */
  private String detail;

  private Long createTime;

  /** 派生元数据,有的设备的属性，功能，事件可能会动态的添加 */
  private String deriveMetadata;

  /** 其他配置 */
  private String configuration;

  /** 设备坐标 */
  private String coordinate;

  /** 区域ID */
  private String areasId;

  /** 纬度 */
  private String latitude;

  /** 经度 */
  private String longitude;

  private String fenceId;
  private String online;
  private Long devGroupId;

  private String transportProtocol;

  /** 请求参数 */
  @Builder.Default private Map<String, Object> params = new HashMap<>();

  private Boolean hasProtocol;

  /** 接收额外参数 */
  @Builder.Default private Map<String, Object> otherParams = new HashMap<>();

  private List<String> devGroupName;

  /** 所属父设备信息 */
  private String gwDeviceInfo;

  /** 子设备地址 */
  private String slaveAddress;
}

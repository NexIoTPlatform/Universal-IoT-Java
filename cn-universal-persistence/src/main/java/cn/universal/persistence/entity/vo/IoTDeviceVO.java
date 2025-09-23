/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceVO {

  private static final long serialVersionUID = 1L;

  /** 对外设备唯一标识符 */
  private String iotId;

  private String gwProductKey;

  /** 设备自身序号 */
  private String deviceId;

  /** 派生元数据,有的设备的属性，功能，事件可能会动态的添加 */
  private String metadata;

  /** 产品key */
  private String productKey;

  /** 设备名称 */
  private String productName;

  /** 设备实例名称 */
  private String deviceName;

  /** 别名 */
  private String nickName;

  private String latlng;

  private String deviceNode;

  private String topic;
  private String subMsgType;
  private String subType;
  private String subUrl;
  private String state;
  private String deviceTypeName;
  private String deviceModel;
  private String classifiedName;
  private String name;
}

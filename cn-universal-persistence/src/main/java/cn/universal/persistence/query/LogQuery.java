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

package cn.universal.persistence.query;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/11/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class LogQuery extends BasePage {

  /** 日志ID，非自增 */
  private String id;

  /** 设备编码 */
  private String iotId;

  /** 设备序列号 */
  private String deviceId;

  /** 第三方设备ID唯一标识符 */
  private String extDeviceId;

  /** 产品ID */
  private String productKey;

  /** 设备名称 */
  private String deviceName;

  /** 消息类型 */
  private String messageType;

  /** 事件名称 */
  private String event;

  /** 请求参数 */
  private Map<String, Object> params;

  private Long endCreateTime;

  private Long beginCreateTime;

  /** 属性 */
  private String property;
}

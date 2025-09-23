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

package cn.universal.persistence.entity;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IoTDeviceEvents implements Serializable {

  /** 事件标识 */
  private String id;

  /** 事件名称 */
  private String name;

  /** 事件级别 */
  private String level;

  /** 描述 */
  private String description;

  /** 事件总数 */
  private String qty;

  /** 最新事件上报时间 */
  private String time;

  // 是否设置存储策略
  private boolean storagePolicy;
}

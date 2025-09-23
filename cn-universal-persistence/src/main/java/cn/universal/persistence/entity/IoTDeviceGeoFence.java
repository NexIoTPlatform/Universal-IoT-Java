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

import cn.universal.persistence.common.inteceptor.SQenGenId;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

/**
 * 设备围栏 @Author Aleo
 *
 * @since 2023/8/5 8:47
 */
@Table(name = "iot_device_geo_fence")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceGeoFence implements Serializable {

  @Id
  @KeySql(genId = SQenGenId.class)
  private Long id;

  /** 围栏名称 */
  private String name;

  /** 围栏状态 0.启用 1.停用 */
  private Integer status;

  /** 触发模式 in.进入 out.离开 all.进入&离开 */
  private String touchWay;

  /** 范围 */
  private String fence;

  /** 类型 circle.圆 polygon.多边形 */
  private String type;

  /** 圆形中心点 */
  private String point;

  /** 半径 */
  private BigDecimal radius;

  /** 创建人 */
  private String creatorId;

  /** 周触发(天) */
  private String weekTime;

  /** 天触发开始时间(时) */
  private String beginTime;

  /** 天触发结束时间(时) */
  private String endTime;

  /** 归属第三方应用 */
  private String creatorUser;

  @Transient private List<String> queryUserList;

  /** 创建时间 */
  private Date createDate;

  /** 更新时间 */
  private Date updateDate;

  /** 不触发时间 */
  private String noTriggerTime;

  /** 延迟时间 分钟 */
  private Integer delayTime;
}

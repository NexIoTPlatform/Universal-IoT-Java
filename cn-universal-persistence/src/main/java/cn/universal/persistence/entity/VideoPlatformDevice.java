/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频平台设备主表实体
 * @Author: gitee.com/NexIoT
 *
 */
package cn.universal.persistence.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频平台设备主表
 * 
 * 存储各视频平台设备的公共字段信息
 * 
 * @author gitee.com/NexIoT
 * @version 2.0
 * @since 2025/11/08
 */
@Table(name = "video_platform_device")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoPlatformDevice implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id
  private Long id;

  /** 平台实例唯一标识 */
  @Column(name = "instance_key")
  private String instanceKey;

  /** 设备ID（平台侧唯一标识） */
  @Column(name = "device_id")
  private String deviceId;

  /** 设备名称 */
  @Column(name = "device_name")
  private String deviceName;

  /** 设备状态: online/offline/1/0等 */
  @Column(name = "device_status")
  private String deviceStatus;

  /** 设备型号 */
  @Column(name = "device_model")
  private String deviceModel;

  /** 设备IP地址 */
  @Column(name = "device_ip")
  private String deviceIp;

  /** 设备端口 */
  @Column(name = "device_port")
  private Integer devicePort;

  /** 设备厂商 */
  @Column(name = "manufacturer")
  private String manufacturer;

  /** 所属组织ID */
  @Column(name = "org_id")
  private String orgId;

  /** 所属组织名称 */
  @Column(name = "org_name")
  private String orgName;

  /** 经度（longitude） */
  @Column(name = "gps_x")
  private String gpsX;

  /** 纬度（latitude） */
  @Column(name = "gps_y")
  private String gpsY;

  /** Z轴高度（altitude） */
  @Column(name = "gps_z")
  private String gpsZ;

  /** 设备配置（JSON）包含channelList、能力集等公共扩展信息 */
  @Column(name = "configuration")
  private String configuration;

  /** 备注说明 */
  @Column(name = "remark")
  private String remark;

  /** 是否启用：0-禁用，1-启用 */
  @Column(name = "enabled")
  private Integer enabled;

  /** 创建者ID */
  @Column(name = "create_id")
  private String createId;

  /** 更新者ID */
  @Column(name = "update_id")
  private String updateId;

  /** 创建时间 */
  @Column(name = "create_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  /** 更新时间 */
  @Column(name = "update_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;
}

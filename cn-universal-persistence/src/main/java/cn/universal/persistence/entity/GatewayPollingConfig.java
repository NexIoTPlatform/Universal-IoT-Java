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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关轮询配置实体
 * 
 * @author Aleo
 * @date 2025-10-26
 */
@Data
@Table(name = "iot_gateway_polling_config")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayPollingConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id
  private Long id;

  /** 网关设备ID */
  @Column(name = "device_id")
  private String deviceId;

  /** 产品KEY */
  @Column(name = "product_key")
  private String productKey;

  /** IoT ID */
  @Column(name = "iot_id")
  private String iotId;

  /** 是否启用轮询 */
  @Column(name = "enabled")
  private Boolean enabled;

  /** 轮询间隔(秒) */
  @Column(name = "interval_seconds")
  private Integer intervalSeconds;

  /** 超时时间(秒) */
  @Column(name = "timeout_seconds")
  private Integer timeoutSeconds;

  /** 失败重试次数 */
  @Column(name = "retry_times")
  private Integer retryTimes;

  /** 指令间隔(毫秒) */
  @Column(name = "command_interval_ms")
  private Integer commandIntervalMs;

  /** 下次轮询时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "next_poll_time")
  private Date nextPollTime;

  /** 最后轮询时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "last_poll_time")
  private Date lastPollTime;

  /** 最后成功时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "last_success_time")
  private Date lastSuccessTime;

  /** 连续失败次数 */
  @Column(name = "continuous_fail_count")
  private Integer continuousFailCount;

  /** 轮询状态: NORMAL-正常, PAUSED-暂停, FAILED-失败 */
  @Column(name = "polling_status")
  private String pollingStatus;

  /** 总轮询次数 */
  @Column(name = "total_poll_count")
  private Long totalPollCount;

  /** 成功次数 */
  @Column(name = "success_count")
  private Long successCount;

  /** 失败次数 */
  @Column(name = "fail_count")
  private Long failCount;

  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "create_time")
  private Date createTime;

  /** 更新时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "update_time")
  private Date updateTime;

  /** 创建人ID */
  @Column(name = "creator_id")
  private String creatorId;
}

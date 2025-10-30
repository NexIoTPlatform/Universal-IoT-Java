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

package cn.universal.databridge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据输入任务实体
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "iot_data_input_task")
public class DataInputTask implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 配置ID */
  @Column(name = "config_id")
  private Long configId;

  /** 任务名称 */
  @Column(name = "task_name")
  private String taskName;

  /** 任务类型 */
  @Column(name = "task_type")
  @Enumerated(EnumType.STRING)
  private TaskType taskType;

  /** Cron表达式 */
  @Column(name = "cron_expression")
  private String cronExpression;

  /** 最后执行时间 */
  @Column(name = "last_execution_time")
  private LocalDateTime lastExecutionTime;

  /** 下次执行时间 */
  @Column(name = "next_execution_time")
  private LocalDateTime nextExecutionTime;

  /** 状态 */
  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private Status status;

  /** 错误信息 */
  @Column(name = "error_message")
  private String errorMessage;

  /** 创建时间 */
  @Column(name = "create_time")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Column(name = "update_time")
  private LocalDateTime updateTime;

  /** 创建者 */
  @Column(name = "create_by")
  private String createBy;

  /** 更新者 */
  @Column(name = "update_by")
  private String updateBy;

  /** 任务类型枚举 */
  public enum TaskType {
    SCHEDULED, // 定时任务
    REALTIME // 实时任务
  }

  /** 状态枚举 */
  public enum Status {
    RUNNING, // 运行中
    STOPPED, // 已停止
    ERROR // 错误状态
  }
}

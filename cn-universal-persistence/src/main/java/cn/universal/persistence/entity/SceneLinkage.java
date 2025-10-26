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

package cn.universal.persistence.entity;

import cn.universal.common.annotation.Excel;
import cn.universal.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * scene_linkage表 SceneLinkage @Author gitee.com/NexIoT
 *
 * @since 2023-03-01
 */
@Table(name = "scene_linkage")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SceneLinkage extends BaseEntity {

  private static final long serialVersionUID = 1L;

  @Id private Long id;

  /** 场景名称 */
  @Excel(name = "场景名称")
  @Column(name = "scene_name")
  private String sceneName;

  /** 触发条件 all.全部 one.任意一个 */
  @Excel(name = "触发条件 all.全部 one.任意一个")
  @Column(name = "touch")
  private String touch;

  /** 触发条件 */
  @Excel(name = "触发条件")
  @Column(name = "trigger_condition")
  private String triggerCondition;

  /** 执行动作 */
  @Excel(name = "执行动作")
  @Column(name = "exec_action")
  private String execAction;

  /** 沉默周期 */
  @Excel(name = "沉默周期")
  @Column(name = "sleep_cycle")
  private Integer sleepCycle;

  /** 0启用 1停用 */
  @Excel(name = "0启用 1停用")
  @Column(name = "status")
  private Integer status;

  /** 设备id */
  @Excel(name = "设备id")
  @Column(name = "dev_id")
  private String devId;
}

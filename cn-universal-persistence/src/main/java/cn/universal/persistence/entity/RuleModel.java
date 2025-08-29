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
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

/**
 * 规则模型 @Author Aleo
 *
 * @since 2023/1/13 14:28
 */
@Table(name = "rule_model")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleModel implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id
  @KeySql(genId = SQenGenId.class)
  @Schema(description = "主键ID")
  private Long id;

  /** 规则名称 */
  @Schema(description = "规则名称")
  @Column(name = "rule_name")
  private String ruleName;

  /** 数据级别 */
  @Schema(description = "数据级别")
  @Column(name = "data_level")
  private String dataLevel;

  /** 描述 */
  @Schema(description = "描述")
  @Column(name = "description")
  private String description;

  /** 状态 */
  @Schema(description = "状态")
  @Column(name = "status")
  private String status;

  ;

  /** 产品KEY */
  @Schema(description = "产品KEY")
  @Column(name = "product_key")
  private String productKey;

  /** 规则配置 */
  @Schema(description = "规则配置")
  @Column(name = "config")
  private String config;

  /** 创建人 */
  @Schema(description = "创建人")
  @Column(name = "creator_id")
  private String creatorId;

  /** 创建时间 */
  @Schema(description = "创建时间")
  @Column(name = "create_time")
  private Date createTime;

  /** 创建时间 */
  @Schema(description = "创建时间")
  @Column(name = "update_time")
  private Date updateTime;
}

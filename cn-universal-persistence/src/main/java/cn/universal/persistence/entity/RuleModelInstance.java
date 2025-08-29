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
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

/**
 * 规则实例 @Author Aleo
 *
 * @since 2023/1/13 14:28
 */
@Table(name = "rule_model_instance")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuleModelInstance implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id
  @KeySql(genId = SQenGenId.class)
  private Long id;

  /** 模型id */
  @Column(name = "model_id")
  private Long modelId;

  /** 关联类型 */
  @Column(name = "relation_type")
  private String relationType;

  /** 关联id */
  @Column(name = "relation_id")
  private String relationId;
}

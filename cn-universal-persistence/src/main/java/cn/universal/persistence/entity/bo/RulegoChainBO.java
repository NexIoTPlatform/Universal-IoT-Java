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

package cn.universal.persistence.entity.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rulego规则链BO
 *
 * @author Aleo
 * @since 2025/01/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema
public class RulegoChainBO implements Serializable {

  private static final long serialVersionUID = 1L;

  @Schema(description = "主键ID")
  private Long id;

  @Schema(description = "rulego规则链ID")
  private String rulegoId;

  @Schema(description = "规则链名称")
  private String chainName;

  @Schema(description = "规则链描述")
  private String description;

  @Schema(description = "创建人unionId")
  private String creatorId;

  @Schema(description = "创建人姓名")
  private String creatorName;

  @Schema(description = "状态")
  private String status;

  @Schema(description = "规则链DSL内容")
  private String dslContent;

  @Schema(description = "最后同步时间")
  private String lastSyncTime;

  @Schema(description = "创建时间")
  private String createTime;

  @Schema(description = "更新时间")
  private String updateTime;

  @Schema(description = "是否删除")
  private Integer deleted;

  @Schema(description = "页码")
  private Integer pageNum;

  @Schema(description = "页大小")
  private Integer pageSize;
}

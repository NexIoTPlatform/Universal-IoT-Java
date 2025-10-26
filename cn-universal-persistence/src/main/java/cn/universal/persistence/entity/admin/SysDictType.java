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

package cn.universal.persistence.entity.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 字典类型表 sys_dict_type @Author ruoyi */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Table(name = "sys_dict_type")
public class SysDictType implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 字典主键 */
  //  @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
  //  @TableId(value = "dict_id", type = IdType.AUTO)
  @Id private Long dictId;

  /** 字典名称 */
  //  @Excel(name = "字典名称")
  @NotBlank(message = "字典名称不能为空")
  @Size(min = 0, max = 100, message = "字典类型名称长度不能超过100个字符")
  @Column(name = "dict_name")
  private String dictName;

  /** 字典类型 */
  //  @Excel(name = "字典类型")
  @NotBlank(message = "字典类型不能为空")
  @Size(min = 0, max = 100, message = "字典类型类型长度不能超过100个字符")
  @Column(name = "dict_type")
  private String dictType;

  /** 状态（0正常 1停用） */
  //  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;

  /** 创建者 */
  //  @TableField(fill = FieldFill.INSERT)
  @Column(name = "create_by")
  private String createBy;

  /** 创建时间 */
  //  @TableField(fill = FieldFill.INSERT)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "create_time")
  private Date createTime;

  /** 更新者 */
  //  @TableField(fill = FieldFill.INSERT_UPDATE)
  @Column(name = "update_by")
  private String updateBy;

  /** 更新时间 */
  //  @TableField(fill = FieldFill.INSERT_UPDATE)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "update_time")
  private Date updateTime;

  /** 备注 */
  private String remark;

  /** 请求参数 */
  //  @TableField(exist = false)
  private Map<String, Object> params = new HashMap<>();
}

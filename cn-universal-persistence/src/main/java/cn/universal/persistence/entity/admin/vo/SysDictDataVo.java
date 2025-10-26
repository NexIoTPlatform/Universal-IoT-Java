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

package cn.universal.persistence.entity.admin.vo;

import cn.universal.common.constant.IoTUserConstants;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 字典数据表 sys_dict_data @Author ruoyi */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysDictDataVo implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 字典编码 */
  private Long dictCode;

  /** 字典排序 */
  private Long dictSort;

  /** 字典标签 */
  private String dictLabel;

  /** 字典键值 */
  private String dictValue;

  /** 字典类型 */
  private String dictType;

  /** 样式属性（其他样式扩展） */
  private String cssClass;

  /** 表格字典样式 */
  private String listClass;

  /** 是否默认（Y是 N否） */
  private String isDefault;

  /** 状态（0正常 1停用） */
  private String status;

  /** 创建者 */
  private String createBy;

  /** 创建时间 */
  private Date createTime;

  /** 更新者 */
  private String updateBy;

  /** 更新时间 */
  private Date updateTime;

  /** 开始时间 */
  private String startTime;

  /** 结束时间 */
  private String endTime;

  /** 备注 */
  private String remark;

  /** 请求参数 */
  //  @TableField(exist = false)
  private Map<String, Object> params = new HashMap<>();

  public boolean getDefault() {
    return IoTUserConstants.YES.equals(this.isDefault);
  }
}

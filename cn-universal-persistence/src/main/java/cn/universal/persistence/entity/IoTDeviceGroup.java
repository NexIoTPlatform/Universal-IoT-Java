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

@Table(name = "iot_device_group")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceGroup implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 分组ID，非自增 */
  @Id
  @KeySql(genId = SQenGenId.class)
  private Long id;

  /** 分组名称 */
  @Column(name = "group_name")
  private String groupName;

  /** 分组标识 */
  @Column(name = "group_code")
  private String groupCode;

  /** 群组描述 */
  @Column(name = "group_describe")
  private String groupDescribe;

  /** 父id */
  @Column(name = "parent_id")
  private Long parentId;

  /** 是否有子分组 */
  @Column(name = "has_child")
  private Integer hasChild;

  /** 分组级别 */
  @Column(name = "group_level")
  private Integer groupLevel;

  /** 激活设备数 */
  @Column(name = "relat_dev_count")
  private Integer relatDevCount;

  /** 关联设备树 */
  @Column(name = "active_dev_count")
  private Integer activeDevCount;

  /** 创建人 */
  @Column(name = "creator_id")
  private String creatorId;

  /** 实例编号 */
  @Column(name = "instance")
  private String instance;

  /** 标签 */
  @Column(name = "tag")
  private String tag;
}

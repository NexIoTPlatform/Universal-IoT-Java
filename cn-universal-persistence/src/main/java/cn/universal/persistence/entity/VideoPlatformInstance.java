/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频平台实例实体
 * @Author: gitee.com/NexIoT
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

@Table(name = "video_platform_instance")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoPlatformInstance implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id private Long id;

  /** 平台类型：wvp/ics/icc */
  @Column(name = "platform_type")
  private String platformType;

  /** 实例唯一标识 */
  @Column(name = "instance_key")
  private String instanceKey;

  /** 实例名称 */
  @Column(name = "name")
  private String name;

  /** 平台API地址或域 */
  @Column(name = "endpoint")
  private String endpoint;

  /** 鉴权配置（JSON） */
  @Column(name = "auth")
  private String auth;

  /** 平台版本 */
  @Column(name = "version")
  private String version;

  /** 其他选项配置（JSON） */
  @Column(name = "options")
  private String options;

  /** WVP是否自动创建GB/级联产品 */
  @Column(name = "auto_create_products")
  private Integer autoCreateProducts;

  /** 是否启用 */
  @Column(name = "enabled")
  private Integer enabled;

  /** 创建者ID */
  @Column(name = "creator_id")
  private String creatorId;

  /** 创建时间 */
  @Column(name = "create_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  /** 更新时间 */
  @Column(name = "update_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;
}

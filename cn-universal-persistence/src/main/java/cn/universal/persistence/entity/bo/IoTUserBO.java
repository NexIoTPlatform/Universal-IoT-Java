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

package cn.universal.persistence.entity.bo;

import cn.universal.common.annotation.Excel;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTUserBO implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id private Long id;

  /** 归属组织机构 */
  @Column(name = "org_id")
  private Long orgId;

  /** 用户名 */
  private String username;

  /** 密码 */
  private String password;

  /** 别名 */
  private String alias;

  /** 用户唯一标识 */
  @Column(name = "union_id")
  private String unionId;

  /** 邮箱 */
  private String email;

  /** 密码加盐 */
  private String salt;

  /** 手机号 */
  private String mobile;

  /** 账号状态（0正常，1停用） */
  private Integer status;

  /** 上级用户唯一id */
  @Excel(name = "上级用户唯一id")
  private String parentUnionId;

  /** 账号身份 0.超级管理员 1.普通用户 2.子用户 */
  @Excel(name = "账号身份 0.超级管理员 1.普通用户 2.子用户")
  private Integer identity;

  /** 头像 */
  private String avatar;

  /** 登录IP */
  @Column(name = "login_ip")
  private String loginIp;

  /** 登录时间 */
  @Column(name = "login_date")
  private Date loginDate;

  /** 创建者 */
  @Column(name = "create_by")
  private String createBy;

  /** 创建时间 */
  @Column(name = "create_date")
  private Date createDate;

  @Column(name = "update_by")
  private String updateBy;

  @Column(name = "update_date")
  private Date updateDate;

  /** 备注 */
  private String remark;

  /** 0-正常，1-删除 */
  private Integer deleted;

  /** 设备接入额度剩余 */
  private Integer license;

  /** 设备接入总数额度 */
  private Integer licenseTotalAmount;

  private Set<String> roles;

  /** 角色组 */
  private Long[] roleIds;

  /** 角色ID */
  private Long roleId;

  private String cfg;

  @Column(name = "register_from")
  private String registerFrom;
}

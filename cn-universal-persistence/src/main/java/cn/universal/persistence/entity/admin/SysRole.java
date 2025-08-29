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

package cn.universal.persistence.entity.admin;

import cn.universal.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 角色表 sys_role @Author ruoyi */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
@Builder
@Table(name = "sys_role")
public class SysRole implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 角色ID */
  @Id private Long roleId;

  /** 角色名称 */
  @Excel(name = "角色名称")
  @Column(name = "role_name")
  private String roleName;

  /** 角色权限 */
  @Column(name = "role_key")
  private String roleKey;

  /** 角色排序 */
  @Column(name = "role_sort")
  private String roleSort;

  /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
  @Column(name = "data_scope")
  private String dataScope;

  /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
  @Column(name = "menu_check_strictly")
  private Boolean menuCheckStrictly;

  /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
  @Column(name = "dept_check_strictly")
  private Boolean deptCheckStrictly;

  /** 角色状态（0正常 1停用） */
  private String status;

  /** 创建者 */
  @Column(name = "create_by")
  private String createBy;

  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "create_time")
  private Date createTime;

  /** 更新者 */
  @Column(name = "update_by")
  private String updateBy;

  /** 更新时间 */
  @Column(name = "update_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;

  /** 备注 */
  private String remark;

  /** 请求参数 */
  @Builder.Default private Map<String, Object> params = new HashMap<>();

  /** 用户是否存在此角色标识 默认不存在 */
  private boolean flag = false;

  /** 菜单组 */
  private Long[] menuIds;

  /** 部门组（数据权限） */

  //  private Long[] deptIds;
  public SysRole(Long roleId) {
    this.roleId = roleId;
  }

  public boolean isAdmin() {
    return isAdmin(this.roleId);
  }

  public static boolean isAdmin(Long roleId) {
    return roleId != null && 1L == roleId;
  }
}

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

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 角色和菜单关联 sys_role_menu @Author ruoyi
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@AllArgsConstructor
@Builder
@Table(name = "sys_role_menu")
public class SysRoleMenu implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  private Long uuid;

  /**
   * 角色ID
   */
  private Long roleId;

  /**
   * 菜单ID
   */
  private Long menuId;
}

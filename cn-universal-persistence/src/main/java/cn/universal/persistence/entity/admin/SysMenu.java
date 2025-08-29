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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 菜单权限表 sys_menu @Author ruoyi */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@Table(name = "sys_menu")
public class SysMenu implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 菜单ID */
  @Id private Long menuId;

  /** 菜单名称 */
  @Column(name = "menu_name")
  private String menuName;

  /** 父菜单名称 */
  //  @Column(name = "parent_name")
  //  private String parentName;

  /** 父菜单ID */
  @Column(name = "parent_id")
  private Long parentId;

  /** 显示顺序 */
  @Column(name = "order_num")
  private String orderNum;

  /** 路由地址 */
  private String path;

  /** 组件路径 */
  private String component;

  /** 是否为外链（0是 1否） */
  @Column(name = "is_frame")
  private String isFrame;

  /** 是否缓存（0缓存 1不缓存） */
  @Column(name = "is_cache")
  private String isCache;

  /** 类型（M目录 C菜单 F按钮） */
  @Column(name = "menu_type")
  private String menuType;

  /** 显示状态（0显示 1隐藏） */
  private String visible;

  /** 菜单状态（0显示 1隐藏） */
  private String status;

  /** 权限字符串 */
  private String perms;

  /** 菜单图标 */
  private String icon;

  /** 创建者 */
  @Column(name = "create_by")
  private String createBy;

  /** 创建时间 */
  @Column(name = "create_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

  /** 子菜单 */
  @Builder.Default private List<SysMenu> children = new ArrayList<SysMenu>();
}

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

package cn.universal.persistence.entity.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** Treeselect树结构实体类 @Author ruoyi */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TreeSelect implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 节点ID */
  private Long id;

  /** 节点名称 */
  private String label;

  /** 子节点 */
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TreeSelect> children;

  public TreeSelect(SysMenu menu) {
    this.id = menu.getMenuId();
    this.label = menu.getMenuName();
    this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
  }
}

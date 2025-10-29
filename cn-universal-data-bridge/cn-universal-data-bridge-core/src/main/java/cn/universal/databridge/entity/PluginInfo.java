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

package cn.universal.databridge.entity;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 插件信息
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 插件名称 */
  private String name;

  /** 插件版本 */
  private String version;

  /** 插件描述 */
  private String description;

  /** 插件作者 */
  private String author;

  /** 插件类型 */
  private String pluginType;

  /** 支持的资源类型 */
  private List<String> supportedResourceTypes;

  /** 数据流向 - INPUT: 仅输入, OUTPUT: 仅输出, BIDIRECTIONAL: 双向 */
  private DataDirection dataDirection;

  /** 插件分类 - 用于前端分组显示 */
  private String category;

  /** 图标名称 - 用于前端显示 */
  private String icon;

  /** 是否启用 */
  @Builder.Default private Boolean enabled = true;

  /** 数据流向枚举 */
  public enum DataDirection {
    /** 仅数据输入（从外部系统拉取数据） */
    INPUT("INPUT", "数据输入"),

    /** 仅数据输出（向外部系统推送数据） */
    OUTPUT("OUTPUT", "数据输出"),

    /** 双向流转（既可输入也可输出） */
    BIDIRECTIONAL("BIDIRECTIONAL", "双向流转");

    private final String code;
    private final String description;

    DataDirection(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }
}

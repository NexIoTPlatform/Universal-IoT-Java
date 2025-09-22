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

package cn.universal.persistence.entity.bo;

import cn.universal.persistence.entity.Network;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网络组件业务对象
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NetworkBO extends Network {

  /**
   * 网络类型名称
   */
  private String typeName;

  /**
   * 是否正在运行
   */
  private boolean running;

  /**
   * 状态名称
   */
  private String stateName;

  /**
   * 创建时间格式化
   */
  private String createDateStr;

  /**
   * 网络类型列表（多个类型）
   */
  private List<String> types;

  /**
   * 启用/停用状态名称
   */
  private String enableName;
}

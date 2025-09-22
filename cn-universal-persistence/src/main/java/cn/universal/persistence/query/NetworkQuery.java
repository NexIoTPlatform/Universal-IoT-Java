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

package cn.universal.persistence.query;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网络组件查询对象
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NetworkQuery extends BasePage {

  /** 网络类型（单个类型） */
  private String type;

  /** 网络类型列表（多个类型） */
  private List<String> types;

  /** 网络组件名称 */
  private String name;

  /** 产品Key */
  private String productKey;

  /** 状态 */
  private Boolean state;

  /** 唯一标识 */
  private String unionId;

  /** 创建用户 */
  private String createUser;
}

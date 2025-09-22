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

package cn.universal.rule.rulego.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rulego保存请求
 *
 * @author Aleo
 * @since 2025/09/01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RulegoSaveRequest {

  /** 规则链ID */
  private String id;

  /** 规则链名称 */
  private String name;

  private boolean root = true;

  /** 规则链描述 */
  private String description;
}

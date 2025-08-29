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

package cn.universal.rule.model.bo;

import cn.universal.rule.model.RuleTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * todo @Author Aleo
 *
 * @since 2023/1/18 15:36
 */
@Data
@Schema
public class RuleTargetTestBO {

  public RuleTarget ruleTarget;

  public String param;
}

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

import lombok.Data;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/3/21
 */
@Data
public class PasswordBO {

  private String oldPassword;
  private String newPassword;
}

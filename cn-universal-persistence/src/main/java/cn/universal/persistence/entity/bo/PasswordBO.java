/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.entity.bo;

import lombok.Data;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/3/21
 */
@Data
public class PasswordBO {

  private String oldPassword;
  private String newPassword;
}

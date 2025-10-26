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

package cn.universal.core.metadata.types.codec;

import cn.universal.core.metadata.types.PasswordType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordCodec extends AbstractValueTypeCodec<PasswordType> {

  @Override
  public String getTypeId() {
    return PasswordType.ID;
  }
}

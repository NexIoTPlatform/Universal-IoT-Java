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

package cn.universal.core.metadata;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数合法性校验
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/8/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidateResult {

  private boolean success;

  private Object value;

  private String errorMsg;

  public static ValidateResult success(Object value) {
    ValidateResult result = new ValidateResult();
    result.setSuccess(true);
    result.setValue(value);
    return result;
  }

  public static ValidateResult success() {
    ValidateResult result = new ValidateResult();
    result.setSuccess(true);
    return result;
  }

  public static ValidateResult fail(String message) {
    ValidateResult result = new ValidateResult();
    result.setSuccess(false);
    result.setErrorMsg(message);
    return result;
  }

  public Object assertSuccess() {
    if (!success) {
      throw new IllegalArgumentException(errorMsg);
    }
    return value;
  }

  public void ifFail(Consumer<ValidateResult> resultConsumer) {
    if (!success) {
      resultConsumer.accept(this);
    }
  }
}

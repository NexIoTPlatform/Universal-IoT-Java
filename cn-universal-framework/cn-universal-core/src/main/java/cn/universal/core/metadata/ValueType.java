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

import java.util.Map;

/**
 * 物模型值数据
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/8/25
 */
public interface ValueType extends Metadata, FormatSupport {

  /**
   * 验证是否合法
   *
   * @param value 值
   * @return ValidateResult
   */
  ValidateResult validate(Object value);

  /**
   * @return 类型标识
   */
  default String getType() {
    return getId();
  }

  /**
   * @return 拓展属性
   */
  @Override
  default Map<String, Object> getExpands() {
    return null;
  }
}

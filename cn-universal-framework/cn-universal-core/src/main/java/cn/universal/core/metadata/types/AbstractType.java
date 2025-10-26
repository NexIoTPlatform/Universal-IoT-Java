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

package cn.universal.core.metadata.types;

import cn.universal.core.metadata.ValueType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

@Getter
@Setter
@SuppressWarnings("all")
public abstract class AbstractType<R> implements ValueType {

  private Map<String, Object> expands;

  private String description;

  public R expands(Map<String, Object> expands) {
    if (CollectionUtils.isEmpty(expands)) {
      return (R) this;
    }
    if (this.expands == null) {
      this.expands = new HashMap<>();
    }
    this.expands.putAll(expands);
    return (R) this;
  }

  public R expand(String configKey, Object value) {

    if (value == null) {
      return (R) this;
    }
    if (expands == null) {
      expands = new HashMap<>();
    }
    expands.put(configKey, value);
    return (R) this;
  }

  public R description(String description) {
    this.description = description;
    return (R) this;
  }
}

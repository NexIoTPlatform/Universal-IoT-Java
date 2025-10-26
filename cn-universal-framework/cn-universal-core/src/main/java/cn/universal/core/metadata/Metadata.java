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
import java.util.Optional;

/**
 * 元数据
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/8/25
 */
public interface Metadata {

  String getId();

  String getName();

  String getDescription();

  Map<String, Object> getExpands();

  default Optional<Object> getExpand(String name) {
    return Optional.ofNullable(getExpands()).map(map -> map.get(name));
  }

  default void setExpands(Map<String, Object> expands) {}

  default void setName(String name) {}

  default void setDescription(String description) {}
}

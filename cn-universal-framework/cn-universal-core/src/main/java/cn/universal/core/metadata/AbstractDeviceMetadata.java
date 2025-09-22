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

package cn.universal.core.metadata;

import java.util.List;
import java.util.Optional;

/**
 * 基础设备物模型定义
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/25
 */
public interface AbstractDeviceMetadata extends Metadata, Jsonable {

  /**
   * @return 所有属性定义
   */
  List<AbstractPropertyMetadata> getProperties();

  /**
   * @return 所有功能定义
   */
  List<AbstractFunctionMetadata> getFunctions();

  /**
   * @return 事件定义
   */
  List<AbstractEventMetadata> getEvents();

  /**
   * @return 标签定义
   */
  List<AbstractPropertyMetadata> getTags();

  default Optional<AbstractEventMetadata> getEvent(String id) {
    return Optional.ofNullable(getEventOrNull(id));
  }

  AbstractEventMetadata getEventOrNull(String id);

  default Optional<AbstractPropertyMetadata> getProperty(String id) {
    return Optional.ofNullable(getPropertyOrNull(id));
  }

  AbstractPropertyMetadata getPropertyOrNull(String id);

  default Optional<AbstractFunctionMetadata> getFunction(String id) {
    return Optional.ofNullable(getFunctionOrNull(id));
  }

  AbstractFunctionMetadata getFunctionOrNull(String id);

  default Optional<AbstractPropertyMetadata> getTag(String id) {
    return Optional.ofNullable(getTagOrNull(id));
  }

  AbstractPropertyMetadata getTagOrNull(String id);
}

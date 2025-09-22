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

/**
 * 功能元数据
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/25
 */
public interface AbstractFunctionMetadata extends Metadata, Jsonable {

  /**
   * @return 输入参数定义
   */
  List<AbstractPropertyMetadata> getInputs();

  /**
   * @return 输出类型，为null表示无输出
   */
  ValueType getOutput();

  /**
   * @return 是否异步
   */
  boolean isAsync();
}

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

package cn.universal.dm.device.service.push;

import cn.universal.dm.device.entity.IoTPushResult;
import cn.universal.persistence.base.BaseUPRequest;
import java.util.List;

/**
 * 上行消息处理器接口，用于插件化扩展
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/9
 */
public interface UPProcessor<T extends BaseUPRequest> {

  /**
   * 处理器名称
   *
   * @return 处理器名称
   */
  String getName();

  /**
   * 处理器描述
   *
   * @return 处理器描述
   */
  String getDescription();

  /**
   * 处理器优先级，数字越小优先级越高
   *
   * @return 优先级
   */
  int getOrder();

  /**
   * 推送前处理
   *
   * @param upRequests 上行请求列表
   * @return 处理后的请求列表
   */
  default List<T> beforePush(List<T> upRequests) {
    return upRequests;
  }

  /**
   * 推送后处理（带推送结果）
   *
   * @param upRequests 上行请求列表
   * @param pushResults 推送结果列表
   */
  default void afterPush(List<T> upRequests, List<IoTPushResult> pushResults) {
    // 默认空实现
  }

  /**
   * 是否支持处理指定类型的请求
   *
   * @param request 请求对象
   * @return 是否支持
   */
  default boolean supports(T request) {
    return true;
  }
}

/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.http.protocol.processor;

import cn.hutool.json.JSONObject;
import cn.universal.dm.device.service.plugin.BaseMessageProcessor;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import java.util.List;

/**
 * HTTP消息处理器接口
 *
 * <p>继承通用的BaseMessageProcessor，定义HTTP模块特有的处理方法 各HTTP处理器实现此接口，提供具体的处理逻辑
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/1/20
 */
public interface HttpUPMessageProcessor extends BaseMessageProcessor {

  /**
   * 处理HTTP消息
   *
   * @param source 原始JSON数据
   * @param ioTDeviceDTO 设备实例信息
   * @param requests 请求列表
   * @return 处理后的请求列表
   */
  List<HttpUPRequest> process(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests);

  /**
   * 是否支持处理该消息
   *
   * @param source 原始JSON数据
   * @param ioTDeviceDTO 设备实例信息
   * @param requests 请求列表
   * @return true表示支持，false表示不支持
   */
  boolean supports(JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests);

  /** 处理前的预检查（可选） */
  default boolean preCheck(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    return true;
  }

  /** 处理后的后置操作（可选） */
  default void postProcess(
      JSONObject source,
      IoTDeviceDTO ioTDeviceDTO,
      List<HttpUPRequest> requests,
      List<HttpUPRequest> result) {
    // 默认不做任何操作
  }

  /** 异常处理（可选） */
  default void onError(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests, Exception e) {
    // 默认不做任何操作
  }
}

/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频平台适配器接口
 * @Author: gitee.com/NexIoT
 *
 */
package cn.universal.dm.video;

import cn.universal.persistence.entity.VideoPlatformInstance;
import java.util.List;
import java.util.Map;

/**
 * 视频平台适配器统一接口
 * 用于屏蔽不同平台（WVP/HIK/ICC）的API差异
 */
public interface VideoPlatformAdapter {

  /**
   * 获取支持的平台类型
   */
  String getSupportedPlatformType();

  /**
   * 测试平台连接
   * @param instance 平台实例配置
   * @return 测试结果 {reachable: boolean, latencyMs: int, message: string}
   */
  Map<String, Object> testConnection(VideoPlatformInstance instance);

  /**
   * 实时拉取设备目录（不落库）
   * @param instance 平台实例
   * @param filters 过滤条件 {deviceType, keyword, page, pageSize}
   * @return 设备列表，每个设备包含：extDeviceId, name, manufacturer, model, status, channels等
   */
  List<Map<String, Object>> listDevices(VideoPlatformInstance instance, Map<String, Object> filters);

  /**
   * 获取设备实时流地址
   * @param instance 平台实例
   * @param extDeviceId 三方设备ID
   * @param channelId 通道ID（可选）
   * @param streamType 码流类型：main主码流/sub子码流
   * @return {rtsp, rtmp, flv, hls, webrtc}
   */
  Map<String, String> getStreamUrl(
      VideoPlatformInstance instance, String extDeviceId, String channelId, String streamType);

  /**
   * 获取设备回放流地址
   * @param instance 平台实例
   * @param extDeviceId 三方设备ID
   * @param channelId 通道ID
   * @param startTime 开始时间（时间戳，毫秒）
   * @param endTime 结束时间（时间戳，毫秒）
   * @return {flv, ws_flv, wss_flv, hls, rtmp, rtsp, app, stream, mediaServerId, key, ...}
   */
  Map<String, Object> getPlaybackUrl(
      VideoPlatformInstance instance, String extDeviceId, String channelId, long startTime, long endTime);

  /**
   * PTZ控制
   * @param instance 平台实例
   * @param extDeviceId 三方设备ID
   * @param channelId 通道ID
   * @param command PTZ指令：up/down/left/right/zoomIn/zoomOut/stop
   * @param speed 速度：1-255
   */
  void controlPTZ(
      VideoPlatformInstance instance,
      String extDeviceId,
      String channelId,
      String command,
      int speed);

  /**
   * 查询录像记录
   * @param instance 平台实例
   * @param extDeviceId 三方设备ID
   * @param channelId 通道ID
   * @param startTime 开始时间戳（秒）
   * @param endTime 结束时间戳（秒）
   * @return 录像片段列表 [{startTime, endTime, fileSize, fileUrl}]
   */
  List<Map<String, Object>> queryRecords(
      VideoPlatformInstance instance,
      String extDeviceId,
      String channelId,
      long startTime,
      long endTime);

  /**
   * 获取组织树（按需实现）
   * @param instance 平台实例
   * @param filters 过滤条件 {keyword}
   * @return 组织节点列表 [{id, name, parentId, path}]
   */
  List<Map<String, Object>> listOrganizations(VideoPlatformInstance instance, Map<String, Object> filters);

  /**
   * 订阅平台事件
   * @param instance 平台实例
   * @param topics 订阅事件类型集合
   * @param callback 回调地址或处理器标识
   */
  void subscribe(VideoPlatformInstance instance, java.util.List<String> topics, String callback);

  /**
   * 取消订阅
   * @param instance 平台实例
   * @param topics 取消的事件类型集合（为空则取消全部）
   */
  void unsubscribe(VideoPlatformInstance instance, java.util.List<String> topics);
}

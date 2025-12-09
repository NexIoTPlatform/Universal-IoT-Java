/*
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 */
package cn.wvp.protocol.constant;

/**
 * WVP平台API路径常量
 * 基于wvp-GB28181-pro标准接口
 */
public interface WvpApiConstants {

  /** 设备查询接口 */
  String API_DEVICE_QUERY = "/api/device/query/devices";

  /** 通道查询接口 */
  String API_DEVICE_CHANNELS = "/api/device/query/devices/%s/channels";

  /** 云台控制接口 (方向+缩放) */
  String API_PTZ_CONTROL = "/api/front-end/ptz/%s/%s";
  
  /** 光圈控制接口 */
  String API_IRIS_CONTROL = "/api/front-end/fi/iris/%s/%s";
  
  /** 聚焦控制接口 */
  String API_FOCUS_CONTROL = "/api/front-end/fi/focus/%s/%s";

  /** 预置位查询接口 */
  String API_PRESET_QUERY = "/api/front-end/preset/query/%s/%s";

  /** 抓拍接口 */
  String API_SNAPSHOT = "/api/play/snap";

  // ==================== 国标录像相关接口 ====================
  
  /** 国标录像查询接口 */
  String API_GB_RECORD_QUERY = "/api/gb_record/query/%s/%s";

  /** 国标录像下载开始 */
  String API_GB_RECORD_DOWNLOAD_START = "/api/gb_record/download/start/%s/%s";

  /** 国标录像下载停止 */
  String API_GB_RECORD_DOWNLOAD_STOP = "/api/gb_record/download/stop/%s/%s/%s";

  /** 国标录像下载进度 */
  String API_GB_RECORD_DOWNLOAD_PROGRESS = "/api/gb_record/download/progress/%s/%s/%s";

  // ==================== 云端录像相关接口 ====================
  
  /** 云端录像日期列表 */
  String API_CLOUD_RECORD_DATE_LIST = "/api/cloud/record/date/list";

  /** 云端录像列表查询 */
  String API_CLOUD_RECORD_LIST = "/api/cloud/record/list";

  /** 加载云端录像文件 */
  String API_CLOUD_RECORD_LOAD = "/api/cloud/record/loadRecord";

  /** 云端录像定位 */
  String API_CLOUD_RECORD_SEEK = "/api/cloud/record/seek";

  /** 云端录像倍速 */
  String API_CLOUD_RECORD_SPEED = "/api/cloud/record/speed";

  /** 云端录像删除 */
  String API_CLOUD_RECORD_DELETE = "/api/cloud/record/delete";

  /** 云端录像收藏添加 */
  String API_CLOUD_RECORD_COLLECT_ADD = "/api/cloud/record/collect/add";

  /** 云端录像收藏删除 */
  String API_CLOUD_RECORD_COLLECT_DELETE = "/api/cloud/record/collect/delete";

  /** 直播开始接口 */
  String API_PLAY_START = "/api/play/start/%s/%s";

  /** 直播停止接口 */
  String API_PLAY_STOP = "/api/play/stop/%s/%s";

  /** 回放开始接口 */
  String API_PLAYBACK_START = "/api/playback/start/%s/%s";

  /** 回放停止接口 */
  String API_PLAYBACK_STOP = "/api/playback/stop/%s/%s";

  /** 云端录像下载地址获取 */
  String API_CLOUD_RECORD_PLAY_PATH = "/api/cloud/record/play/path";

  /** Token请求头名称 */
  String HEADER_ACCESS_TOKEN = "Access-Token";
}

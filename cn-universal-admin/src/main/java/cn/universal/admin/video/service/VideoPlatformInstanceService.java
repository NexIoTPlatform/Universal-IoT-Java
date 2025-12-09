/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频平台实例服务接口
 * @Author: gitee.com/NexIoT
 *
 */
package cn.universal.admin.video.service;

import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.entity.VideoPlatformDevice;
import java.util.List;
import java.util.Map;

public interface VideoPlatformInstanceService {

  /**
   * 查询所有平台实例
   */
  List<VideoPlatformInstance> listAll();

  /**
   * 根据平台类型查询
   */
  List<VideoPlatformInstance> listByType(String platformType);

  /**
   * 根据instanceKey查询
   */
  VideoPlatformInstance getByInstanceKey(String instanceKey);

  /** 根据ID查询 */
  VideoPlatformInstance getById(Long id);

  /**
   * 创建平台实例
   */
  VideoPlatformInstance create(VideoPlatformInstance instance);

  /**
   * 更新平台实例
   */
  VideoPlatformInstance update(VideoPlatformInstance instance);

  /**
   * 删除平台实例
   */
  void delete(Long id);
  void delete(Long id, boolean force);

  /**
   * 测试连接（调用适配器真实测试）
   */
  Map<String, Object> testConnection(String instanceKey);

  /**
   * 实时拉取设备目录（调用适配器）
   */
  List<Map<String, Object>> listDevices(String instanceKey, Map<String, Object> filters);

  /**
   * 获取组织树（调用适配器，部分平台支持）
   */
  List<Map<String, Object>> listOrganizations(String instanceKey, Map<String, Object> filters);

  /**
   * 应用订阅（根据实例配置）
   */
  void applySubscription(String instanceKey);

  /**
   * 取消订阅（根据实例配置）
   */
  void cancelSubscription(String instanceKey);

  /**
   * 获取设备通道列表（从中间表）
   */
  List<Map<String, Object>> listDeviceChannels(String instanceKey, String deviceId);

  /**
   * 查询通道列表（从通道表）
   * @param instanceKey 平台实例Key
   * @param deviceId 设备ID（可选）
   * @param filters 筛选条件: keyword(通道名称), status(通道状态), ptzType(PTZ类型)
   */
  List<Map<String, Object>> listChannels(String instanceKey, String deviceId, Map<String, Object> filters);

  /**
   * 获取通道的流地址（预览流）
   */
  Map<String, String> getStreamUrl(String instanceKey, String deviceId, String channelId, String streamType);

  /**
   * 获取通道的回放流地址
   */
  Map<String, Object> getPlaybackUrl(String instanceKey, String deviceId, String channelId, long startTime, long endTime);

  /**
   * PTZ控制
   */
  void controlPTZ(String instanceKey, String deviceId, String channelId, String command, int speed);

  /**
   * 查询录像记录
   */
  List<Map<String, Object>> queryRecords(String instanceKey, String deviceId, String channelId, long startTime, long endTime);

  // ==================== 国标录像下载 ====================

  /**
   * 开始国标录像下载
   */
  cn.universal.common.domain.R<Map<String, Object>> startGBRecordDownload(String instanceKey, String deviceId, String channelId, Map<String, Object> data);

  /**
   * 停止国标录像下载
   */
  cn.universal.common.domain.R<Void> stopGBRecordDownload(String instanceKey, String deviceId, String channelId, Map<String, Object> data);

  /**
   * 获取国标录像下载进度
   */
  cn.universal.common.domain.R<Map<String, Object>> getGBRecordDownloadProgress(String instanceKey, String deviceId, String channelId, String stream);

  // ==================== 云端录像 ====================

  /**
   * 查询云端录像日期列表
   */
  cn.universal.common.domain.R<List<String>> queryCloudRecordDates(String instanceKey, String deviceId, String channelId, Map<String, String> params);

  /**
   * 查询云端录像列表
   */
  cn.universal.common.domain.R<Map<String, Object>> queryCloudRecords(String instanceKey, String deviceId, String channelId, Map<String, String> params);

  /**
   * 加载云端录像文件
   */
  cn.universal.common.domain.R<Map<String, Object>> loadCloudRecord(String instanceKey, String deviceId, String channelId, Map<String, Object> data);

  /**
   * 云端录像定位
   */
  cn.universal.common.domain.R<Void> seekCloudRecord(String instanceKey, String deviceId, String channelId, Map<String, Object> data);

  /**
   * 设置云端录像倍速
   */
  cn.universal.common.domain.R<Void> setCloudRecordSpeed(String instanceKey, String deviceId, String channelId, Map<String, Object> data);

  /**
   * 获取云端录像下载地址
   */
  cn.universal.common.domain.R<Map<String, Object>> getCloudRecordPlayPath(String instanceKey, Integer recordId);

  /**
   * 获取设备完整信息（跨主表和扩展表查询）
   * @param instanceKey 平台实例Key
   * @param deviceId 设备ID
   * @return 设备完整信息（主表+扩展表）
   */
  Map<String, Object> getDeviceDetail(String instanceKey, String deviceId);

  /**
   * 更新设备基本信息（remark/gps/org/enabled等）
   */
  Map<String, Object> updateDevice(String instanceKey, String deviceId, Map<String, Object> updates);

  /** 删除设备（级联删除扩展与通道） */
  void deleteDevice(String instanceKey, String deviceId);
}

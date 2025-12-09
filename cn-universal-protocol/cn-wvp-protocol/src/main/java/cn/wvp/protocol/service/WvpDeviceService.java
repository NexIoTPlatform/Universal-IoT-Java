package cn.wvp.protocol.service;

import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import java.util.Map;

/** WVP 设备服务接口 */
public interface WvpDeviceService {
  // 设备/组织/通道拉取（供缓存同步使用）
  R<?> listDevices(String instanceKey, Map<String, Object> filters);

  R<?> listOrgs(String instanceKey, Map<String, Object> filters);

  R<?> listChannels(String serial, Map<String, Object> filters);

  // 能力调用（供命令处理器使用）
  R<?> ptzControl(WvpDownRequest request);

  R<?> startPreview(WvpDownRequest request);

  R<?> stopPreview(WvpDownRequest request);

  R<?> startPlayback(WvpDownRequest request);

  R<?> stopPlayback(WvpDownRequest request);

  R<?> snapshot(WvpDownRequest request);

  R<?> queryPresets(WvpDownRequest request);

  // ==================== 国标录像相关 ====================

  /** 查询国标录像 */
  R<?> queryGBRecords(WvpDownRequest request);

  /** 开始下载国标录像 */
  R<?> startGBRecordDownload(WvpDownRequest request);

  /** 停止下载国标录像 */
  R<?> stopGBRecordDownload(WvpDownRequest request);

  /** 获取下载进度 */
  R<?> getGBRecordDownloadProgress(WvpDownRequest request);

  // ==================== 云端录像相关 ====================

  /** 查询云端录像日期列表 */
  R<?> queryCloudRecordDates(WvpDownRequest request);

  /** 查询云端录像列表 */
  R<?> queryCloudRecords(WvpDownRequest request);

  /** 加载云端录像文件 */
  R<?> loadCloudRecord(WvpDownRequest request);

  /** 云端录像定位 */
  R<?> seekCloudRecord(WvpDownRequest request);

  /** 设置云端录像倍速 */
  R<?> setCloudRecordSpeed(WvpDownRequest request);

  /** 获取云端录像下载地址 */
  R<?> getCloudRecordPlayPath(WvpDownRequest request);
}

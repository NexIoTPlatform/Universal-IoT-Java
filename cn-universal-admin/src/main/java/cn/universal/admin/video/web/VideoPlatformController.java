package cn.universal.admin.video.web;

import cn.universal.admin.video.service.VideoDeviceImportService;
import cn.universal.admin.video.service.VideoPlatformInstanceService;
import cn.universal.common.annotation.Log;
import cn.universal.common.domain.R;
import cn.universal.common.enums.BusinessType;
import cn.universal.common.exception.BaseException;
import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.entity.VideoPlatformDevice;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 视频平台实例与设备接口（先行联调，逐步接入真实适配器） */
@RestController
@RequestMapping("/api/video/platforms")
@RequiredArgsConstructor
public class VideoPlatformController extends BaseController {

  private final VideoPlatformInstanceService instanceService;
  private final VideoDeviceImportService deviceImportService;

  // 同步限频缓存：记录每个实例最后一次强制同步时间戳
  private static final java.util.concurrent.ConcurrentHashMap<String, Long> SYNC_RATE_LIMIT =
      new java.util.concurrent.ConcurrentHashMap<>();

  /** 平台实例列表 */
  @GetMapping("")
  public R<List<VideoPlatformInstance>> listPlatforms() {
    IoTUser user = loginIoTUnionUser(SecurityUtils.getUnionId());
    List<VideoPlatformInstance> all = instanceService.listAll();
    if (user != null && !user.isAdmin()) {
      String uid = user.getUnionId();
      all = all.stream().filter(i -> uid != null && uid.equals(i.getCreatorId()))
          .collect(java.util.stream.Collectors.toList());
    }
    return R.ok(all);
  }

  /** 根据instanceKey查询实例 */
  @GetMapping("/{instanceKey}")
  public R<VideoPlatformInstance> getInstance(@PathVariable("instanceKey") String instanceKey) {
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.getByInstanceKey(instanceKey));
  }

  /** 创建/更新平台实例（若id为空则创建，否则更新） */
  @PostMapping("")
  @Log(title = "创建/更新视频平台实例", businessType = BusinessType.INSERT)
  public R<VideoPlatformInstance> savePlatform(@RequestBody VideoPlatformInstance body) {
    IoTUser user = loginIoTUnionUser(SecurityUtils.getUnionId());
    VideoPlatformInstance saved;
    if (body.getId() == null) {
      if (user != null) {
        body.setCreatorId(user.getUnionId());
      }
      saved = instanceService.create(body);
    } else {
      VideoPlatformInstance existing = instanceService.getByInstanceKey(body.getInstanceKey());
      if (existing == null) throw new BaseException("平台实例不存在");
      if (user != null && !user.isAdmin()) {
        if (existing.getCreatorId() == null || !existing.getCreatorId().equals(user.getUnionId())) {
          throw new BaseException("无权更新该平台实例");
        }
      }
      saved = instanceService.update(body);
    }
    // 尝试应用订阅配置（若启用）
    try {
      instanceService.applySubscription(saved.getInstanceKey());
    } catch (Exception ignore) {
    }
    return R.ok(saved);
  }

  /** 删除平台实例 */
  @DeleteMapping("/{id}")
  @Log(title = "删除视频平台实例", businessType = BusinessType.DELETE)
  public R<Void> deletePlatform(@PathVariable("id") Long id, @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
    IoTUser user = loginIoTUnionUser(SecurityUtils.getUnionId());
    VideoPlatformInstance inst = instanceService.getById(id);
    if (inst == null) throw new BaseException("平台实例不存在");
    if (user != null && !user.isAdmin()) {
      if (inst.getCreatorId() == null || !inst.getCreatorId().equals(user.getUnionId())) {
        throw new BaseException("无权删除该平台实例");
      }
    }
    instanceService.delete(id, force);
    return R.ok();
  }

  /** 测试连接（占位，后续接入适配器） */
  @PostMapping("/test")
  public R<Map<String, Object>> testConnection(@RequestBody Map<String, Object> body) {
    String instanceKey = (String) body.get("instanceKey");
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.testConnection(instanceKey));
  }

  /** 获取组织树（部分平台支持） */
  @GetMapping("/{instanceKey}/orgs")
  public R<List<Map<String, Object>>> listOrganizations(
      @PathVariable("instanceKey") String instanceKey, @RequestParam Map<String, String> params) {
    Map<String, Object> filters = new java.util.HashMap<>(params);
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.listOrganizations(instanceKey, filters));
  }

  /** 实时拉取设备目录（占位），用于前端页面测试 */
  @GetMapping("/{instanceKey}/devices")
  public R<List<Map<String, Object>>> listDevices(
      @PathVariable("instanceKey") String instanceKey, @RequestParam Map<String, String> params) {
    Map<String, Object> filters = new java.util.HashMap<>(params);
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.listDevices(instanceKey, filters));
  }

  @PostMapping("/{instanceKey}/subscribe")
  public R<Void> subscribe(@PathVariable("instanceKey") String instanceKey) {
    assertInstancePerm(instanceKey);
    instanceService.applySubscription(instanceKey);
    return R.ok();
  }

  @PostMapping("/{instanceKey}/unsubscribe")
  public R<Void> unsubscribe(@PathVariable("instanceKey") String instanceKey) {
    assertInstancePerm(instanceKey);
    instanceService.cancelSubscription(instanceKey);
    return R.ok();
  }

  /** 强制同步组织与设备缓存（限频：每实例5分钟一次） */
  @PostMapping("/{instanceKey}/sync")
  @Log(title = "强制同步视频平台组织与设备缓存", businessType = BusinessType.OTHER)
  public R<?> forceSync(@PathVariable("instanceKey") String instanceKey) {
    assertInstancePerm(instanceKey);
    long now = System.currentTimeMillis();
    Long last = SYNC_RATE_LIMIT.get(instanceKey);
    if (last != null && (now - last) < 5 * 60 * 1000) {
      long remaining = 5 * 60 * 1000 - (now - last);
      return R.error("操作过于频繁，请在" + (remaining / 1000) + "秒后重试");
    }
    SYNC_RATE_LIMIT.put(instanceKey, now);

    // 分页拉取所有设备（循环直到没有下一页）
    int totalDeviceCount = 0;
    int currentPage = 1;
    int pageSize = 100; // 每页拉取100条

    while (true) {
      Map<String, Object> forceParam = new java.util.HashMap<>();
      forceParam.put("forceSync", "true");
      forceParam.put("page", currentPage);
      forceParam.put("count", pageSize);

      // 触发设备同步（具体落库由Service实现到中间表）
      List<Map<String, Object>> devices = instanceService.listDevices(instanceKey, forceParam);

      if (devices == null || devices.isEmpty()) {
        break; // 没有更多数据，退出循环
      }

      totalDeviceCount += devices.size();

      // 如果返回数量小于pageSize，说明已经是最后一页
      if (devices.size() < pageSize) {
        break;
      }

      currentPage++;
    }

    // 同步组织信息（如果有）
    Map<String, Object> orgParam = new java.util.HashMap<>();
    orgParam.put("forceSync", "true");
    List<Map<String, Object>> orgs = instanceService.listOrganizations(instanceKey, orgParam);

    Map<String, Object> result = new java.util.HashMap<>();
    result.put("orgCount", orgs == null ? 0 : orgs.size());
    result.put("deviceCount", totalDeviceCount);
    result.put("instanceKey", instanceKey);
    result.put("pages", currentPage);
    return R.ok(result);
  }

  /**
   * 获取设备通道列表（从中间表返回）
   *
   * @deprecated 请使用 listChannels 从通道表查询
   */
  @Deprecated
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels")
  public R<List<Map<String, Object>>> listDeviceChannels(
      @PathVariable("instanceKey") String instanceKey, @PathVariable("deviceId") String deviceId) {
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.listDeviceChannels(instanceKey, deviceId));
  }

  /** 查询通道列表（从通道表查询） 支持筛选: keyword(通道名称/ID), status(通道状态), ptzType(PTZ类型) */
  @GetMapping("/{instanceKey}/channels")
  public R<List<Map<String, Object>>> listChannels(
      @PathVariable("instanceKey") String instanceKey,
      @RequestParam(required = false) String deviceId,
      @RequestParam Map<String, String> params) {
    Map<String, Object> filters = new java.util.HashMap<>(params);
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.listChannels(instanceKey, deviceId, filters));
  }

  /** 获取通道流地址（预览/回放） */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/stream")
  public R<Map<String, String>> getStreamUrl(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam Map<String, String> params) {
    String streamType = params.getOrDefault("streamType", "main");
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.getStreamUrl(instanceKey, deviceId, channelId, streamType));
  }

  /** PTZ控制 */
  @PostMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/ptz")
  @Log(title = "视频设备PTZ控制", businessType = BusinessType.OTHER)
  public R<Void> controlPTZ(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, Object> body) {
    String command = String.valueOf(body.getOrDefault("command", "stop"));
    int speed = 50;
    try {
      Object s = body.get("speed");
      if (s != null) speed = Integer.parseInt(String.valueOf(s));
    } catch (Exception ignore) {
    }
    assertInstancePerm(instanceKey);
    instanceService.controlPTZ(instanceKey, deviceId, channelId, command, speed);
    return R.ok();
  }

  /** 批量导入设备（勾选使用流程） */
  @PostMapping("/{instanceKey}/devices/import")
  @Log(title = "批量导入视频设备", businessType = BusinessType.IMPORT)
  public R<Map<String, Integer>> importDevices(
      @PathVariable("instanceKey") String instanceKey, @RequestBody Map<String, Object> body) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> devices = (List<Map<String, Object>>) body.get("devices");
    String productKey = (String) body.get("productKey");
    assertInstancePerm(instanceKey);
    Map<String, Integer> result =
        deviceImportService.importDevices(instanceKey, devices, productKey);
    return R.ok(result);
  }

  /** 获取预览流地址 */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/preview")
  public R<Map<String, String>> getPreviewUrl(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam(defaultValue = "main") String streamType) {
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.getStreamUrl(instanceKey, deviceId, channelId, streamType));
  }

  /** 获取回放流地址 */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/playback")
  public R<Map<String, Object>> getPlaybackUrl(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam Map<String, String> params) {
    assertInstancePerm(instanceKey);
    
    // 从参数中获取开始时间和结束时间
    String startTimeStr = params.get("startTime");
    String endTimeStr = params.get("endTime");
    
    if (startTimeStr == null || endTimeStr == null) {
      return R.error("缺少参数: startTime 或 endTime", null);
    }
    
    // 解析时间字符串为时间戳（毫秒）
    long startTime;
    long endTime;
    try {
      // 支持格式：yyyy-MM-dd HH:mm:ss 或时间戳（毫秒）
      if (startTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        startTime = sdf.parse(startTimeStr).getTime();
      } else {
        startTime = Long.parseLong(startTimeStr);
        // 如果是10位秒级时间戳，转换为毫秒
        if (startTimeStr.length() == 10) {
          startTime = startTime * 1000;
        }
      }
      
      if (endTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        endTime = sdf.parse(endTimeStr).getTime();
      } else {
        endTime = Long.parseLong(endTimeStr);
        // 如果是10位秒级时间戳，转换为毫秒
        if (endTimeStr.length() == 10) {
          endTime = endTime * 1000;
        }
      }
    } catch (Exception e) {
      return R.error("时间格式错误: " + e.getMessage(), null);
    }
    
    return R.ok(instanceService.getPlaybackUrl(instanceKey, deviceId, channelId, startTime, endTime));
  }

  /** 查询录像记录 */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/records")
  public R<List<Map<String, Object>>> queryRecords(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam Map<String, String> params) {
    long startTime = Long.parseLong(params.getOrDefault("startTime", "0"));
    long endTime =
        Long.parseLong(params.getOrDefault("endTime", String.valueOf(System.currentTimeMillis())));
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.queryRecords(instanceKey, deviceId, channelId, startTime, endTime));
  }

  // ==================== 国标录像下载 ====================

  /** 开始国标录像下载 */
  @PostMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/gb-records/download/start")
  public R<Map<String, Object>> startGBRecordDownload(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, Object> data) {
    assertInstancePerm(instanceKey);
    return instanceService.startGBRecordDownload(instanceKey, deviceId, channelId, data);
  }

  /** 停止国标录像下载 */
  @PostMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/gb-records/download/stop")
  public R<Void> stopGBRecordDownload(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, Object> data) {
    assertInstancePerm(instanceKey);
    return instanceService.stopGBRecordDownload(instanceKey, deviceId, channelId, data);
  }

  /** 获取国标录像下载进度 */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/gb-records/download/progress")
  public R<Map<String, Object>> getGBRecordDownloadProgress(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam String stream) {
    assertInstancePerm(instanceKey);
    return instanceService.getGBRecordDownloadProgress(instanceKey, deviceId, channelId, stream);
  }

  // ==================== 云端录像 ====================

  /** 查询云端录像日期列表 */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/cloud-records/dates")
  public R<List<String>> queryCloudRecordDates(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam Map<String, String> params) {
    assertInstancePerm(instanceKey);
    return instanceService.queryCloudRecordDates(instanceKey, deviceId, channelId, params);
  }

  /** 查询云端录像列表 */
  @GetMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/cloud-records")
  public R<Map<String, Object>> queryCloudRecords(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestParam Map<String, String> params) {
    assertInstancePerm(instanceKey);
    return instanceService.queryCloudRecords(instanceKey, deviceId, channelId, params);
  }

  /** 加载云端录像文件 */
  @PostMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/cloud-records/load")
  public R<Map<String, Object>> loadCloudRecord(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, Object> data) {
    assertInstancePerm(instanceKey);
    return instanceService.loadCloudRecord(instanceKey, deviceId, channelId, data);
  }

  /** 云端录像定位 */
  @PostMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/cloud-records/seek")
  public R<Void> seekCloudRecord(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, Object> data) {
    assertInstancePerm(instanceKey);
    return instanceService.seekCloudRecord(instanceKey, deviceId, channelId, data);
  }

  /** 设置云端录像倍速 */
  @PostMapping("/{instanceKey}/devices/{deviceId}/channels/{channelId}/cloud-records/speed")
  public R<Void> setCloudRecordSpeed(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @PathVariable("channelId") String channelId,
      @RequestBody Map<String, Object> data) {
    assertInstancePerm(instanceKey);
    return instanceService.setCloudRecordSpeed(instanceKey, deviceId, channelId, data);
  }

  /** 获取云端录像下载地址 */
  @GetMapping("/{instanceKey}/cloud-records/play/path")
  public R<Map<String, Object>> getCloudRecordPlayPath(
      @PathVariable("instanceKey") String instanceKey, @RequestParam Integer recordId) {
    assertInstancePerm(instanceKey);
    return instanceService.getCloudRecordPlayPath(instanceKey, recordId);
  }

  /** 获取设备完整信息（跨主表和扩展表查询） */
  @GetMapping("/{instanceKey}/devices/{deviceId}/detail")
  public R<Map<String, Object>> getDeviceDetail(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId) {
    assertInstancePerm(instanceKey);
    return R.ok(instanceService.getDeviceDetail(instanceKey, deviceId));
  }

  @PutMapping("/{instanceKey}/devices/{deviceId}")
  @Log(title = "更新视频设备信息", businessType = BusinessType.UPDATE)
  public R<Map<String, Object>> updateDevice(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody Map<String, Object> updates) {
    assertInstancePerm(instanceKey);
    Map<String, Object> detail = instanceService.updateDevice(instanceKey, deviceId, updates);
    return R.ok(detail);
  }

  @DeleteMapping("/{instanceKey}/devices/{deviceId}")
  @Log(title = "删除视频设备", businessType = BusinessType.DELETE)
  public R<Void> deleteDevice(
      @PathVariable("instanceKey") String instanceKey,
      @PathVariable("deviceId") String deviceId) {
    assertInstancePerm(instanceKey);
    instanceService.deleteDevice(instanceKey, deviceId);
    return R.ok();
  }

  private void assertInstancePerm(String instanceKey) {
    IoTUser user = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (user == null || user.isAdmin()) return;
    VideoPlatformInstance inst = instanceService.getByInstanceKey(instanceKey);
    if (inst == null) throw new BaseException("平台实例不存在");
    String uid = user.getUnionId();
    if (uid == null || !uid.equals(inst.getCreatorId())) {
      throw new BaseException("无权操作该平台实例");
    }
  }
}

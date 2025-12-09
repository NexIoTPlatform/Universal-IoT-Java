/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: WVP平台适配器实现
 * @Author: gitee.com/NexIoT
 *
 */
package cn.wvp.protocol.adapter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.universal.common.domain.R;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.dm.video.VideoPlatformAdapter;
import cn.universal.dm.video.VideoPlatformInstanceAdapter;
import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.mapper.VideoPlatformInstanceMapper;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.service.WvpDeviceService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/** WVP平台适配器实现 将WvpDeviceService的能力适配到VideoPlatformAdapter统一接口 */
@Slf4j
@Component("wvpPlatformAdapter")
@RequiredArgsConstructor
public class WvpPlatformAdapter implements VideoPlatformAdapter, VideoPlatformInstanceAdapter {

  private final WvpDeviceService wvpDeviceService;

  private final IoTDeviceActionAfterService ioTDeviceActionAfterService;

  private final VideoPlatformInstanceMapper videoPlatformInstanceMapper;

  @Override
  public String getSupportedPlatformType() {
    return "wvp";
  }

  /** 构建通用filters参数 */
  private Map<String, Object> buildFilters(VideoPlatformInstance instance) {
    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", instance.getEndpoint());

    // 从auth字段中提取token
    if (instance.getAuth() != null && !instance.getAuth().isEmpty()) {
      try {
        JSONObject auth = new JSONObject(instance.getAuth());
        if (auth.containsKey("token")) {
          filters.put("token", auth.getStr("token"));
        } else if (auth.containsKey("apiKey")) {
          filters.put("token", auth.getStr("apiKey"));
        }
      } catch (Exception e) {
        log.warn("解析WVP auth配置失败: {}", e.getMessage());
      }
    }

    return filters;
  }

  /** 构建WvpDownRequest */
  private WvpDownRequest buildDownRequest(
      VideoPlatformInstance instance, String deviceId, JSONObject data) {
    WvpDownRequest request = new WvpDownRequest();
    request.setDeviceId(deviceId);
    request.setData(data);
    request.setWvpRequestData(buildFilters(instance));
    return request;
  }

  @Override
  public Map<String, Object> testConnection(VideoPlatformInstance instance) {
    Map<String, Object> result = new HashMap<>();
    long start = System.currentTimeMillis();

    try {
      Map<String, Object> filters = buildFilters(instance);
      filters.put("page", 1);
      filters.put("count", 1);

      R<?> response = wvpDeviceService.listDevices(instance.getInstanceKey(), filters);
      long latency = System.currentTimeMillis() - start;

      if (response.isSuccess()) {
        result.put("reachable", true);
        result.put("latencyMs", latency);
        result.put("message", "WVP连接正常");
      } else {
        result.put("reachable", false);
        result.put("latencyMs", latency);
        result.put("message", "WVP响应异常：" + response.getMsg());
      }
    } catch (Exception e) {
      long latency = System.currentTimeMillis() - start;
      log.error("WVP连接测试失败：{}", e.getMessage());
      result.put("reachable", false);
      result.put("latencyMs", latency);
      result.put("message", "连接失败：" + e.getMessage());
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> listDevices(
      VideoPlatformInstance instance, Map<String, Object> filters) {
    List<Map<String, Object>> devices = new ArrayList<>();

    try {
      Map<String, Object> mergedFilters = buildFilters(instance);
      if (filters != null) {
        mergedFilters.putAll(filters);
      }

      // 如果没有指定分页参数，使用默认值
      if (!mergedFilters.containsKey("page")) {
        mergedFilters.put("page", 1);
      }
      if (!mergedFilters.containsKey("count")) {
        mergedFilters.put("count", 100);
      }

      R<?> response = wvpDeviceService.listDevices(instance.getInstanceKey(), mergedFilters);

      if (response.isSuccess() && response.getData() != null) {

        // 转换响应数据格式
        Object data = response.getData();
        if (data instanceof Map) {
          JSONObject dataJson = new JSONObject(data);
          if (dataJson.containsKey("list")) {
            JSONArray list = dataJson.getJSONArray("list");
            for (int i = 0; i < list.size(); i++) {
              JSONObject item = list.getJSONObject(i);
              Map<String, Object> device = new HashMap<>();
              // 主表公共字段
              String deviceId = item.getStr("deviceId");
              device.put("deviceId", deviceId);
              device.put("name", item.getStr("name", ""));
              device.put("manufacturer", item.getStr("manufacturer", ""));
              device.put("model", item.getStr("model", ""));
              // 根据onLine字段设置状态
              Boolean onLine = item.getBool("onLine", false);
              String status = onLine ? "online" : "offline";
              device.put("status", status);
              device.put("channelCount", item.getInt("channelCount", 0));
              device.put("ip", item.getStr("ip", ""));
              device.put("port", item.getInt("port", 0));
              // WVP扩展表特有字段
              device.put("charset", item.getStr("charset", ""));
              device.put("transport", item.getStr("transport", ""));
              device.put("streamMode", item.getStr("streamMode", ""));
              device.put("hostAddress", item.getStr("hostAddress", ""));
              device.put("registerTime", item.getStr("registerTime", ""));
              device.put("keepaliveTime", item.getStr("keepaliveTime", ""));
              device.put("expires", item.getInt("expires", 3600));
              device.put("mediaServerId", item.getStr("mediaServerId", ""));

              // 只在forceSync时查询通道列表（用于同步写入数据库）
              // 常规查询应该从video_platform_channel表读取
              boolean isForceSync =
                  mergedFilters.containsKey("forceSync")
                      && "true".equalsIgnoreCase(String.valueOf(mergedFilters.get("forceSync")));
              if (isForceSync) {
                List<Map<String, Object>> channelList = getDeviceChannels(instance, deviceId);
                device.put("channelList", channelList);
              }

              devices.add(device);
            }
          }
        }
      } else {
        log.warn("WVP设备列表查询失败：{}", response.getMsg());
      }
    } catch (Exception e) {
      log.error("WVP设备列表查询异常：{}", e.getMessage(), e);
    }

    return devices;
  }

  @Override
  public Map<String, String> getStreamUrl(
      VideoPlatformInstance instance, String deviceId, String channelId, String streamType) {
    Map<String, String> urls = new HashMap<>();

    try {
      JSONObject data = new JSONObject();
      data.set("channelId", channelId != null ? channelId : deviceId);
      data.set("protocol", "flv"); // 默认FLV

      WvpDownRequest request = buildDownRequest(instance, deviceId, data);
      R<?> response = wvpDeviceService.startPreview(request);

      log.info("WVP预览响应: success={}, data={}", response.isSuccess(), response.getData());

      if (response.isSuccess() && response.getData() != null) {
        JSONObject result = new JSONObject(response.getData());
        log.info("WVP预览结果: {}", result.toString());

        if (result.containsKey("urls")) {
          JSONObject urlsObj = result.getJSONObject("urls");
          // 遍历所有返回的流地址
          for (String key : urlsObj.keySet()) {
            Object value = urlsObj.get(key);
            // 过滤掉null值
            if (value != null && !"null".equals(String.valueOf(value))) {
              urls.put(key, String.valueOf(value));
            }
          }
        } else {
          // 如果没有urls字段，直接使用根对象的所有流地址字段
          // 常见的流地址字段
          String[] streamKeys = {
            "flv",
            "https_flv",
            "ws_flv",
            "wss_flv",
            "fmp4",
            "https_fmp4",
            "ws_fmp4",
            "wss_fmp4",
            "hls",
            "https_hls",
            "ws_hls",
            "wss_hls",
            "ts",
            "https_ts",
            "ws_ts",
            "wss_ts",
            "rtmp",
            "rtmps",
            "rtsp",
            "rtsps",
            "rtc",
            "rtcs",
            "webrtc"
          };

          for (String key : streamKeys) {
            if (result.containsKey(key)) {
              Object value = result.get(key);
              // 过滤掉null值
              if (value != null && !"null".equals(String.valueOf(value))) {
                urls.put(key, String.valueOf(value));
              }
            }
          }
        }
        log.info("WVP解析后的URLs: {}", urls);
      }
    } catch (Exception e) {
      log.error("WVP播放地址获取异常：{}", e.getMessage(), e);
    }
    ioTDeviceActionAfterService.online(instance.getInstanceKey(), deviceId);
    return urls;
  }

  @Override
  public void controlPTZ(
      VideoPlatformInstance instance,
      String extDeviceId,
      String channelId,
      String command,
      int speed) {
    try {
      JSONObject data = new JSONObject();
      data.set("channelId", channelId);
      data.set("command", command);
      data.set("speed", speed);

      WvpDownRequest request = buildDownRequest(instance, extDeviceId, data);
      R<?> response = wvpDeviceService.ptzControl(request);

      if (!response.isSuccess()) {
        log.error("WVP PTZ控制失败：{}", response.getMsg());
      }
    } catch (Exception e) {
      log.error("WVP PTZ控制异常：{}", e.getMessage(), e);
    }
  }

  @Override
  public List<Map<String, Object>> queryRecords(
      VideoPlatformInstance instance,
      String extDeviceId,
      String channelId,
      long startTime,
      long endTime) {
    List<Map<String, Object>> records = new ArrayList<>();

    try {
      // WVP要求时间格式为 yyyy-MM-dd HH:mm:ss
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      String reqStartTime = sdf.format(new java.util.Date(startTime));
      String reqEndTime = sdf.format(new java.util.Date(endTime));

      JSONObject data = new JSONObject();
      data.set("channelId", channelId);
      data.set("startTime", reqStartTime);
      data.set("endTime", reqEndTime);

      WvpDownRequest request = buildDownRequest(instance, extDeviceId, data);
      R<?> response = wvpDeviceService.queryGBRecords(request);

      if (response.isSuccess() && response.getData() != null) {
        // 转换响应数据格式
        Object dataObj = response.getData();
        if (dataObj instanceof Map) {
          JSONObject dataJson = new JSONObject(dataObj);
          if (dataJson.containsKey("recordList")) {
            // WVP返回的录像列表在recordList字段中
            JSONArray list = dataJson.getJSONArray("recordList");

            for (int i = 0; i < list.size(); i++) {
              JSONObject item = list.getJSONObject(i);
              Map<String, Object> record = new HashMap<>();

              // 生成唯一ID
              record.put("id", i + 1);

              // 将WVP的时间字符串转为时间戳(毫秒)
              try {
                String respStartTime = item.getStr("startTime", "");
                String respEndTime = item.getStr("endTime", "");
                if (!respStartTime.isEmpty()) {
                  record.put("startTime", sdf.parse(respStartTime).getTime());
                }
                if (!respEndTime.isEmpty()) {
                  record.put("endTime", sdf.parse(respEndTime).getTime());
                }
              } catch (Exception e) {
                log.warn("录像时间解析失败: {}", e.getMessage());
              }

              // 文件大小转换为可读格式
              long fileSize = item.getLong("fileSize", 0L);
              record.put("fileSize", formatFileSize(fileSize));
              record.put("fileSizeBytes", fileSize);

              // 其他字段
              record.put("name", item.getStr("name", ""));
              record.put("filePath", item.getStr("filePath", ""));
              record.put("address", item.getStr("address", ""));
              record.put("secrecy", item.getInt("secrecy", 0));
              record.put("type", item.getStr("type", "time"));

              records.add(record);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("WVP录像查询异常：{}", e.getMessage(), e);
    }

    return records;
  }

  @Override
  public List<Map<String, Object>> listOrganizations(
      VideoPlatformInstance instance, Map<String, Object> filters) {
    // WVP无组织体系，返回空列表
    return List.of();
  }

  @Override
  public void subscribe(VideoPlatformInstance instance, List<String> topics, String callback) {
    // WVP订阅暂不实现，通常通过Webhook配置
    log.info(
        "WVP订阅：instanceKey={}, topics={}, callback={}",
        instance.getInstanceKey(),
        topics,
        callback);
  }

  @Override
  public void unsubscribe(VideoPlatformInstance instance, List<String> topics) {
    log.info("WVP取消订阅：instanceKey={}, topics={}", instance.getInstanceKey(), topics);
  }

  @Override
  public Map<String, Object> getPlaybackUrl(
      VideoPlatformInstance instance,
      String deviceId,
      String channelId,
      long startTime,
      long endTime) {
    Map<String, Object> result = new HashMap<>();

    try {
      JSONObject data = new JSONObject();
      data.set("channelId", channelId != null ? channelId : deviceId);
      data.set("startTime", startTime);
      data.set("endTime", endTime);

      WvpDownRequest request = buildDownRequest(instance, deviceId, data);
      R<?> response = wvpDeviceService.startPlayback(request);

      log.info("WVP回放响应: success={}, data={}", response.isSuccess(), response.getData());

      if (response.isSuccess() && response.getData() != null) {
        // 解析响应数据
        Object responseData = response.getData();
        if (responseData instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> dataMap = (Map<String, Object>) responseData;

          // 直接使用返回的所有字段（包括流地址和其他字段）
          result.putAll(dataMap);

          log.info("WVP解析后的回放数据: {}", result);
        } else {
          log.warn("WVP回放响应数据格式异常: {}", responseData);
        }
      } else {
        log.error("WVP回放失败: {}", response.getMsg());
        throw new RuntimeException("WVP回放失败: " + response.getMsg());
      }
    } catch (Exception e) {
      log.error("WVP回放地址获取异常：{}", e.getMessage(), e);
      throw new RuntimeException("获取回放地址失败: " + e.getMessage(), e);
    }

    return result;
  }

  /** 获取设备的通道列表 */
  private List<Map<String, Object>> getDeviceChannels(
      VideoPlatformInstance instance, String deviceId) {
    List<Map<String, Object>> channels = new ArrayList<>();

    try {
      Map<String, Object> channelFilters = buildFilters(instance);
      channelFilters.put("page", 1);
      channelFilters.put("count", 100); // 通道一般不会太多

      R<?> response = wvpDeviceService.listChannels(deviceId, channelFilters);

      if (response.isSuccess() && response.getData() != null) {
        Object data = response.getData();
        if (data instanceof Map) {
          JSONObject dataJson = new JSONObject(data);
          if (dataJson.containsKey("list")) {
            JSONArray list = dataJson.getJSONArray("list");
            for (int i = 0; i < list.size(); i++) {
              JSONObject item = list.getJSONObject(i);
              Map<String, Object> channel = new HashMap<>();

              // 通道公共字段
              channel.put("channelId", item.getStr("deviceId")); // WVP中通道的deviceId就是通道ID
              channel.put("name", item.getStr("name", ""));
              channel.put("manufacturer", item.getStr("manufacturer", ""));
              channel.put("model", item.getStr("model", ""));
              channel.put("owner", item.getStr("owner", ""));
              channel.put("civilCode", item.getStr("civilCode", ""));
              channel.put("address", item.getStr("address", ""));
              channel.put("parental", item.getInt("parental", 0));
              channel.put("parentId", item.getStr("parentId", ""));
              channel.put("safetyWay", item.getInt("safetyWay"));
              channel.put("registerWay", item.getInt("registerWay"));
              channel.put("secrecy", item.getInt("secrecy", 0));
              channel.put("ipAddress", item.getStr("ipAddress", ""));
              channel.put("port", item.getInt("port"));
              channel.put("status", item.getStr("status", "OFF"));
              channel.put("longitude", item.getStr("longitude"));
              channel.put("latitude", item.getStr("latitude"));
              channel.put("ptzType", item.getInt("ptzType", 0));
              channel.put("positionType", item.getInt("positionType"));

              // WVP通道级特有字段
              channel.put("streamId", item.getStr("streamId", ""));
              channel.put("hasAudio", item.getBool("hasAudio", false));
              channel.put("subCount", item.getInt("subCount", 0));
              channel.put("streamIdentification", item.getStr("streamIdentification", ""));
              channel.put("channelType", item.getInt("channelType", 0));

              channels.add(channel);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("WVP通道列表查询异常：deviceId={}, error={}", deviceId, e.getMessage());
    }

    return channels;
  }

  /** 格式化文件大小 */
  private String formatFileSize(long bytes) {
    if (bytes < 1024) {
      return bytes + " B";
    } else if (bytes < 1024 * 1024) {
      return String.format("%.2f KB", bytes / 1024.0);
    } else if (bytes < 1024 * 1024 * 1024) {
      return String.format("%.2f MB", bytes / (1024.0 * 1024));
    } else {
      return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
  }

  // ==================== 国标录像下载公开方法 ====================

  /** 开始国标录像下载 */
  public R<Map<String, Object>> startGBRecordDownload(
      VideoPlatformInstance instance, String deviceId, String channelId, Map<String, Object> data) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("channelId", channelId);

      //  从 data 中获取参数
      if (data.containsKey("startTime")) {
        requestData.set("startTime", String.valueOf(data.get("startTime")));
      }
      if (data.containsKey("endTime")) {
        requestData.set("endTime", String.valueOf(data.get("endTime")));
      }
      if (data.containsKey("downloadSpeed")) {
        requestData.set("downloadSpeed", data.get("downloadSpeed"));
      }

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.startGBRecordDownload(request);

      if (response.isSuccess() && response.getData() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getData();
        return R.ok(result);
      }
      return R.error(response.getMsg(), null);
    } catch (Exception e) {
      log.error("WVP国标录像下载失败：{}", e.getMessage(), e);
      return R.error("下载失败：" + e.getMessage(), null);
    }
  }

  /** 停止国标录像下载 */
  public R<Void> stopGBRecordDownload(
      VideoPlatformInstance instance, String deviceId, String channelId, Map<String, Object> data) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("channelId", channelId);
      if (data.containsKey("stream")) {
        requestData.set("stream", String.valueOf(data.get("stream")));
      }

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.stopGBRecordDownload(request);

      if (response.isSuccess()) {
        return R.ok();
      }
      return R.error(response.getMsg());
    } catch (Exception e) {
      log.error("WVP停止录像下载失败：{}", e.getMessage(), e);
      return R.error("停止下载失败：" + e.getMessage());
    }
  }

  /** 获取国标录像下载进度 */
  public R<Map<String, Object>> getGBRecordDownloadProgress(
      VideoPlatformInstance instance, String deviceId, String channelId, String stream) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("channelId", channelId);
      requestData.set("stream", stream);

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.getGBRecordDownloadProgress(request);

      if (response.isSuccess() && response.getData() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getData();
        return R.ok(result);
      }
      return R.error(response.getMsg(), null);
    } catch (Exception e) {
      log.error("WVP获取下载进度失败：{}", e.getMessage(), e);
      return R.error("获取进度失败：" + e.getMessage(), null);
    }
  }

  // ==================== 云端录像公开方法 ====================

  /** 查询云端录像日期列表 */
  public R<List<String>> queryCloudRecordDates(
      VideoPlatformInstance instance,
      String deviceId,
      String channelId,
      Map<String, String> params) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("app", params.getOrDefault("app", "rtp"));
      requestData.set("stream", deviceId + "_" + channelId);
      if (params.containsKey("year")) {
        requestData.set("year", Integer.parseInt(params.get("year")));
      }
      if (params.containsKey("month")) {
        requestData.set("month", Integer.parseInt(params.get("month")));
      }

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.queryCloudRecordDates(request);

      if (response.isSuccess() && response.getData() != null) {
        @SuppressWarnings("unchecked")
        List<String> dates = (List<String>) response.getData();
        return R.ok(dates);
      }
      return R.error(response.getMsg(), null);
    } catch (Exception e) {
      log.error("WVP云端录像日期查询失败：{}", e.getMessage(), e);
      return R.error("查询失败：" + e.getMessage(), null);
    }
  }

  /** 查询云端录像列表 */
  public R<Map<String, Object>> queryCloudRecords(
      VideoPlatformInstance instance,
      String deviceId,
      String channelId,
      Map<String, String> params) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("app", params.getOrDefault("app", "rtp"));
      requestData.set("stream", deviceId + "_" + channelId);
      requestData.set("page", Integer.parseInt(params.getOrDefault("page", "1")));
      requestData.set("count", Integer.parseInt(params.getOrDefault("count", "20")));
      if (params.containsKey("startTime")) {
        requestData.set("startTime", params.get("startTime"));
      }
      if (params.containsKey("endTime")) {
        requestData.set("endTime", params.get("endTime"));
      }

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.queryCloudRecords(request);

      if (response.isSuccess() && response.getData() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getData();
        return R.ok(result);
      }
      return R.error(response.getMsg(), null);
    } catch (Exception e) {
      log.error("WVP云端录像查询失败：{}", e.getMessage(), e);
      return R.error("查询失败：" + e.getMessage(), null);
    }
  }

  /** 加载云端录像文件 */
  public R<Map<String, Object>> loadCloudRecord(
      VideoPlatformInstance instance, String deviceId, String channelId, Map<String, Object> data) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("app", data.getOrDefault("app", "rtp"));
      requestData.set("stream", deviceId + "_" + channelId);
      if (data.containsKey("cloudRecordId")) {
        requestData.set("cloudRecordId", data.get("cloudRecordId"));
      }

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.loadCloudRecord(request);

      if (response.isSuccess() && response.getData() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getData();
        return R.ok(result);
      }
      return R.error(response.getMsg(), null);
    } catch (Exception e) {
      log.error("WVP云端录像加载失败：{}", e.getMessage(), e);
      return R.error("加载失败：" + e.getMessage(), null);
    }
  }

  /** 云端录像定位 */
  public R<Void> seekCloudRecord(
      VideoPlatformInstance instance, String deviceId, String channelId, Map<String, Object> data) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.putAll(data);

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.seekCloudRecord(request);

      if (response.isSuccess()) {
        return R.ok();
      }
      return R.error(response.getMsg());
    } catch (Exception e) {
      log.error("WVP云端录像定位失败：{}", e.getMessage(), e);
      return R.error("定位失败：" + e.getMessage());
    }
  }

  /** 设置云端录像倍速 */
  public R<Void> setCloudRecordSpeed(
      VideoPlatformInstance instance, String deviceId, String channelId, Map<String, Object> data) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.putAll(data);

      WvpDownRequest request = buildDownRequest(instance, deviceId, requestData);
      R<?> response = wvpDeviceService.setCloudRecordSpeed(request);

      if (response.isSuccess()) {
        return R.ok();
      }
      return R.error(response.getMsg());
    } catch (Exception e) {
      log.error("WVP云端录像倍速设置失败：{}", e.getMessage(), e);
      return R.error("设置倍速失败：" + e.getMessage());
    }
  }

  /** 获取云端录像下载地址 */
  public R<Map<String, Object>> getCloudRecordPlayPath(
      VideoPlatformInstance instance, Integer recordId) {
    try {
      JSONObject requestData = new JSONObject();
      requestData.set("recordId", recordId);

      WvpDownRequest request = buildDownRequest(instance, null, requestData);
      R<?> response = wvpDeviceService.getCloudRecordPlayPath(request);

      if (response.isSuccess() && response.getData() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getData();
        return R.ok(result);
      }
      return R.error(response.getMsg(), null);
    } catch (Exception e) {
      log.error("WVP获取云端录像下载地址失败：{}", e.getMessage(), e);
      return R.error("获取下载地址失败：" + e.getMessage(), null);
    }
  }

  @Override
  @Cacheable(
      cacheNames = "getVideoPlatformInstance",
      key = "#p0",
      unless = "#result == null")
  public VideoPlatformInstance getVideoPlatformInstance(String instanceKey) {
    if (StrUtil.isBlank(instanceKey)) {
      log.warn("wvp query instanceKey is blank");
      return null;
    }
    VideoPlatformInstance instance = new VideoPlatformInstance();
    instance.setInstanceKey(instanceKey);
    instance = videoPlatformInstanceMapper.selectOne(instance);
    return instance;
  }
}

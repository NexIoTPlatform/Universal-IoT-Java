package cn.wvp.protocol.service.impl;

import cn.hutool.json.JSONObject;
import cn.universal.common.domain.R;
import cn.wvp.protocol.constant.WvpApiConstants;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.service.WvpDeviceService;
import cn.wvp.protocol.util.VideoPlatformHttpUtil;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** WVP设备服务实现 - 使用wvp-GB28181-pro标准接口 基于com.genersoft.iot.vmp.gb28181.controller下的标准API */
@Slf4j
@Service
public class WvpDeviceServiceImpl implements WvpDeviceService {

  /** 从filters中提取endpoint */
  private String getEndpoint(Map<String, Object> filters) {
    return String.valueOf(Optional.ofNullable(filters).map(m -> m.get("endpoint")).orElse(""));
  }

  /** 从WvpDownRequest中提取endpoint */
  private String getEndpoint(WvpDownRequest request) {
    return String.valueOf(
        Optional.ofNullable(request.getWvpRequestData()).map(m -> m.get("endpoint")).orElse(""));
  }

  @Override
  public R<?> listDevices(String instanceKey, Map<String, Object> filters) {
    String endpoint = getEndpoint(filters);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(endpoint, WvpApiConstants.API_DEVICE_QUERY, filters);
    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("获取设备列表失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 直接返回data部分，避免双层包装
    Object data = resp.get("data");
    return R.ok(data != null ? data : resp);
  }

  @Override
  public R<?> listOrgs(String instanceKey, Map<String, Object> filters) {
    // WVP GB28181通常不提供组织树结构,返回空列表
    return R.ok(Collections.emptyList());
  }

  @Override
  public R<?> listChannels(String serial, Map<String, Object> filters) {
    String endpoint = getEndpoint(filters);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String path = String.format(WvpApiConstants.API_DEVICE_CHANNELS, serial);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);
    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("获取通道列表失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 直接返回data部分，避免双层包装
    Object data = resp.get("data");
    return R.ok(data != null ? data : resp);
  }

  @Override
  public R<?> ptzControl(WvpDownRequest request) {
    JSONObject data = request.getData();
    if (data == null) {
      return R.error("缺少参数: data");
    }

    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String command = data.getStr("command"); // left/right/up/down/stop/zoomin/zoomout/focusIn/focusOut/irisIn/irisOut等
    String channelId = data.getStr("channelId");
    Integer speed = data.getInt("speed", 50); // 速度默认50

    if (command == null || command.isEmpty()) {
      return R.error("缺少参数: command");
    }
    if (channelId == null || channelId.isEmpty()) {
      return R.error("缺少参数: channelId");
    }

    // 根据命令类型选择不同的接口
    String path;
    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    
    // 判断是否为聚焦命令
    if ("focusIn".equalsIgnoreCase(command) || "focusOut".equalsIgnoreCase(command)) {
      path = String.format(WvpApiConstants.API_FOCUS_CONTROL, request.getDeviceId(), channelId);
      // 转换命令: focusIn -> near, focusOut -> far
      String focusCmd = "focusIn".equalsIgnoreCase(command) ? "near" : "far";
      filters.put("command", focusCmd);
      filters.put("speed", speed);
    }
    // 判断是否为光圈命令
    else if ("irisIn".equalsIgnoreCase(command) || "irisOut".equalsIgnoreCase(command)) {
      path = String.format(WvpApiConstants.API_IRIS_CONTROL, request.getDeviceId(), channelId);
      // 转换命令: irisIn -> in, irisOut -> out
      String irisCmd = "irisIn".equalsIgnoreCase(command) ? "in" : "out";
      filters.put("command", irisCmd);
      filters.put("speed", speed);
    }
    // 云台方向和缩放命令
    else {
      path = String.format(WvpApiConstants.API_PTZ_CONTROL, request.getDeviceId(), channelId);
      
      // 计算horizonSpeed和verticalSpeed
      int horizonSpeed = 0;
      int verticalSpeed = 0;
      int zoomSpeed = 0;
      
      // 根据命令设置速度
      switch (command.toLowerCase()) {
        case "left":
        case "right":
          horizonSpeed = speed;
          break;
        case "up":
        case "down":
          verticalSpeed = speed;
          break;
        case "leftup":
        case "upleft":
        case "rightup":
        case "upright":
        case "leftdown":
        case "downleft":
        case "rightdown":
        case "downright":
          horizonSpeed = speed;
          verticalSpeed = speed;
          break;
        case "zoomin":
        case "zoomout":
          zoomSpeed = Math.min(speed / 16, 15); // zoomSpeed范围0-15
          break;
        case "stop":
          // stop命令速度都为0
          break;
        default:
          return R.error("不支持的命令: " + command);
      }
      
      filters.put("command", command.toLowerCase());
      filters.put("horizonSpeed", horizonSpeed);
      filters.put("verticalSpeed", verticalSpeed);
      filters.put("zoomSpeed", zoomSpeed);
    }

    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("云台控制失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    Map<String, Object> result = new HashMap<>();
    result.put("executed", true);
    result.put("command", command);
    result.put("channelId", channelId);
    return R.ok(result);
  }

  @Override
  public R<?> startPreview(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String channelId = data == null ? null : data.getStr("channelId");
    if (channelId == null || channelId.isEmpty()) {
      return R.error("缺少参数: channelId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("channelId", channelId);

    String path = String.format(WvpApiConstants.API_PLAY_START, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("启动预览失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }
    return R.ok(resp.getJSONObject("data"));
  }

  @Override
  public R<?> startPlayback(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    if (data == null) {
      return R.error("缺少参数: data");
    }

    String channelId = data.getStr("channelId");
    Object startTimeObj = data.get("startTime");
    Object endTimeObj = data.get("endTime");

    if (channelId == null || startTimeObj == null || endTimeObj == null) {
      return R.error("缺少参数: channelId/startTime/endTime");
    }

    // 将时间戳转换为时间字符串格式（wvp API 需要 yyyy-MM-dd HH:mm:ss 格式）
    String startTimeStr;
    String endTimeStr;
    try {
      long startTime;
      long endTime;
      
      // 处理时间参数：可能是 Long 类型的时间戳，也可能是 String 类型
      if (startTimeObj instanceof Long) {
        startTime = (Long) startTimeObj;
      } else if (startTimeObj instanceof String) {
        startTime = Long.parseLong((String) startTimeObj);
      } else {
        startTime = ((Number) startTimeObj).longValue();
      }
      
      if (endTimeObj instanceof Long) {
        endTime = (Long) endTimeObj;
      } else if (endTimeObj instanceof String) {
        endTime = Long.parseLong((String) endTimeObj);
      } else {
        endTime = ((Number) endTimeObj).longValue();
      }
      
      // 如果是10位秒级时间戳，转换为毫秒
      if (startTime > 0 && startTime < 10000000000L) {
        startTime = startTime * 1000;
      }
      if (endTime > 0 && endTime < 10000000000L) {
        endTime = endTime * 1000;
      }
      
      // 转换为时间字符串格式
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      startTimeStr = sdf.format(new java.util.Date(startTime));
      endTimeStr = sdf.format(new java.util.Date(endTime));
    } catch (Exception e) {
      return R.error("时间参数格式错误: " + e.getMessage());
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    // wvp API 需要时间字符串格式
    filters.put("startTime", startTimeStr);
    filters.put("endTime", endTimeStr);

    String path =
        String.format(WvpApiConstants.API_PLAYBACK_START, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("启动回放失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 从响应中获取 data 对象
    JSONObject dataObj = resp.getJSONObject("data");
    if (dataObj == null) {
      return R.error("启动回放失败: 响应数据为空");
    }

    // 提取所有流地址字段
    Map<String, String> urls = new LinkedHashMap<>();
    String flv = dataObj.getStr("flv");
    String httpsFlv = dataObj.getStr("https_flv");
    String wsFlv = dataObj.getStr("ws_flv");
    String wssFlv = dataObj.getStr("wss_flv");
    String hls = dataObj.getStr("hls");
    String httpsHls = dataObj.getStr("https_hls");
    String wsHls = dataObj.getStr("ws_hls");
    String wssHls = dataObj.getStr("wss_hls");
    String rtmp = dataObj.getStr("rtmp");
    String rtmps = dataObj.getStr("rtmps");
    String rtsp = dataObj.getStr("rtsp");
    String rtsps = dataObj.getStr("rtsps");

    if (flv != null) urls.put("flv", flv);
    if (httpsFlv != null) urls.put("https_flv", httpsFlv);
    if (wsFlv != null) urls.put("ws_flv", wsFlv);
    if (wssFlv != null) urls.put("wss_flv", wssFlv);
    if (hls != null) urls.put("hls", hls);
    if (httpsHls != null) urls.put("https_hls", httpsHls);
    if (wsHls != null) urls.put("ws_hls", wsHls);
    if (wssHls != null) urls.put("wss_hls", wssHls);
    if (rtmp != null) urls.put("rtmp", rtmp);
    if (rtmps != null) urls.put("rtmps", rtmps);
    if (rtsp != null) urls.put("rtsp", rtsp);
    if (rtsps != null) urls.put("rtsps", rtsps);

    // 提取其他重要字段
    String app = dataObj.getStr("app");
    String stream = dataObj.getStr("stream");
    String mediaServerId = dataObj.getStr("mediaServerId");
    String key = dataObj.getStr("key");
    String respStartTimeStr = dataObj.getStr("startTime");
    String respEndTimeStr = dataObj.getStr("endTime");

    Map<String, Object> res = new HashMap<>();
    res.put("channelId", channelId);
    
    // 保存原始时间戳（毫秒）- 使用请求时的时间字符串
    try {
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      res.put("startTime", sdf.parse(startTimeStr).getTime());
      res.put("endTime", sdf.parse(endTimeStr).getTime());
    } catch (Exception e) {
      // 如果解析失败，使用当前时间
      res.put("startTime", System.currentTimeMillis());
      res.put("endTime", System.currentTimeMillis());
    }
    
    // 将所有流地址直接放在根级别，而不是嵌套在 urls 中（前端需要直接访问）
    res.putAll(urls);
    
    // 添加其他重要字段
    if (app != null) res.put("app", app);
    if (stream != null) res.put("stream", stream);
    if (mediaServerId != null) res.put("mediaServerId", mediaServerId);
    if (key != null) res.put("key", key);
    // 使用响应中的时间字符串（如果存在）
    if (respStartTimeStr != null) res.put("startTimeStr", respStartTimeStr);
    else if (startTimeStr != null) res.put("startTimeStr", startTimeStr);
    if (respEndTimeStr != null) res.put("endTimeStr", respEndTimeStr);
    else if (endTimeStr != null) res.put("endTimeStr", endTimeStr);
    
    return R.ok(res);
  }

  @Override
  public R<?> snapshot(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String channelId = data == null ? null : data.getStr("channelId");
    if (channelId == null) {
      return R.error("缺少参数: channelId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("channelId", channelId);
    filters.put("deviceId", request.getDeviceId());
    String path = String.format(WvpApiConstants.API_SNAPSHOT, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("抓拍失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    String snapshotUrl = resp.getStr("snap", resp.getStr("url", ""));
    Map<String, Object> res = new HashMap<>();
    res.put("channelId", channelId);
    res.put("snapshotUrl", snapshotUrl);
    return R.ok(res);
  }

  @Override
  public R<?> stopPreview(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String channelId = data == null ? null : data.getStr("channelId");
    if (channelId == null || channelId.isEmpty()) {
      return R.error("缺少参数: channelId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }

    String path = String.format(WvpApiConstants.API_PLAY_STOP, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("停止预览失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok();
  }

  @Override
  public R<?> queryPresets(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String channelId = data == null ? null : data.getStr("channelId");
    if (channelId == null || channelId.isEmpty()) {
      return R.error("缺少参数: channelId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }

    String path = String.format(WvpApiConstants.API_PRESET_QUERY, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("查询预置位失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok(resp);
  }

  // ==================== 以下为新增录像相关接口 ====================

  @Override
  public R<?> stopPlayback(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺少参数: endpoint");
    }

    String channelId = data == null ? null : data.getStr("channelId");
    if (channelId == null || channelId.isEmpty()) {
      return R.error("缺岑参数: channelId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }

    String path =
        String.format(WvpApiConstants.API_PLAYBACK_STOP, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("停止回放失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok();
  }

  @Override
  public R<?> queryGBRecords(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String channelId = data.getStr("channelId");
    String startTime = data.getStr("startTime");
    String endTime = data.getStr("endTime");

    if (channelId == null || startTime == null || endTime == null) {
      return R.error("缺岑参数: channelId/startTime/endTime");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("startTime", startTime);
    filters.put("endTime", endTime);

    String path =
        String.format(WvpApiConstants.API_GB_RECORD_QUERY, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("国标录像查询失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok(resp.getJSONObject("data"));
  }

  @Override
  public R<?> startGBRecordDownload(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String channelId = data.getStr("channelId");
    String startTime = data.getStr("startTime");
    String endTime = data.getStr("endTime");
    Integer downloadSpeed = data.getInt("downloadSpeed", 1);

    if (channelId == null || startTime == null || endTime == null) {
      return R.error("缺岑参数: channelId/startTime/endTime");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("startTime", startTime);
    filters.put("endTime", endTime);
    filters.put("downloadSpeed", downloadSpeed);

    String path =
        String.format(
            WvpApiConstants.API_GB_RECORD_DOWNLOAD_START, request.getDeviceId(), channelId);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("开始下载失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok(resp.getJSONObject("data"));
  }

  @Override
  public R<?> stopGBRecordDownload(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String channelId = data.getStr("channelId");
    String stream = data.getStr("stream");

    if (channelId == null || stream == null) {
      return R.error("缺岑参数: channelId/stream");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }

    String path =
        String.format(
            WvpApiConstants.API_GB_RECORD_DOWNLOAD_STOP, request.getDeviceId(), channelId, stream);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("停止下载失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok();
  }

  @Override
  public R<?> getGBRecordDownloadProgress(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String channelId = data.getStr("channelId");
    String stream = data.getStr("stream");

    if (channelId == null || stream == null) {
      return R.error("缺岑参数: channelId/stream");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }

    String path =
        String.format(
            WvpApiConstants.API_GB_RECORD_DOWNLOAD_PROGRESS,
            request.getDeviceId(),
            channelId,
            stream);
    JSONObject resp = VideoPlatformHttpUtil.httpGetJson(endpoint, path, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("获取下载进度失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 直接返回data部分，避免双层包装
    Object respData = resp.get("data");
    return R.ok(respData != null ? respData : resp);
  }

  @Override
  public R<?> queryCloudRecordDates(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String app = data.getStr("app");
    String stream = data.getStr("stream");

    if (app == null || stream == null) {
      return R.error("缺岑参数: app/stream");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("app", app);
    filters.put("stream", stream);

    // 可选参数
    if (data.containsKey("year")) filters.put("year", data.getInt("year"));
    if (data.containsKey("month")) filters.put("month", data.getInt("month"));
    if (data.containsKey("mediaServerId"))
      filters.put("mediaServerId", data.getStr("mediaServerId"));

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(
            endpoint, WvpApiConstants.API_CLOUD_RECORD_DATE_LIST, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("查询云端录像日期失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 直接返回data部分，避免双层包装
    Object respData = resp.get("data");
    return R.ok(respData != null ? respData : resp);
  }

  @Override
  public R<?> queryCloudRecords(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }

    // 必填分页参数
    filters.put("page", data.getInt("page", 1));
    filters.put("count", data.getInt("count", 10));

    // 可选参数
    if (data.containsKey("app")) filters.put("app", data.getStr("app"));
    if (data.containsKey("stream")) filters.put("stream", data.getStr("stream"));
    if (data.containsKey("startTime")) filters.put("startTime", data.getStr("startTime"));
    if (data.containsKey("endTime")) filters.put("endTime", data.getStr("endTime"));
    if (data.containsKey("mediaServerId"))
      filters.put("mediaServerId", data.getStr("mediaServerId"));
    if (data.containsKey("callId")) filters.put("callId", data.getStr("callId"));
    if (data.containsKey("query")) filters.put("query", data.getStr("query"));

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(endpoint, WvpApiConstants.API_CLOUD_RECORD_LIST, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("查询云端录像列表失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 直接返回data部分，避免双层包装
    Object respData = resp.get("data");
    return R.ok(respData != null ? respData : resp);
  }

  @Override
  public R<?> loadCloudRecord(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String app = data.getStr("app");
    String stream = data.getStr("stream");
    Integer cloudRecordId = data.getInt("cloudRecordId");

    if (app == null || stream == null || cloudRecordId == null) {
      return R.error("缺岑参数: app/stream/cloudRecordId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("app", app);
    filters.put("stream", stream);
    filters.put("cloudRecordId", cloudRecordId);

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(endpoint, WvpApiConstants.API_CLOUD_RECORD_LOAD, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("加载云端录像失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok(resp.getJSONObject("data"));
  }

  @Override
  public R<?> seekCloudRecord(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String mediaServerId = data.getStr("mediaServerId");
    String app = data.getStr("app");
    String stream = data.getStr("stream");
    Double seek = data.getDouble("seek");

    if (mediaServerId == null || app == null || stream == null || seek == null) {
      return R.error("缺岑参数: mediaServerId/app/stream/seek");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("mediaServerId", mediaServerId);
    filters.put("app", app);
    filters.put("stream", stream);
    filters.put("seek", seek);
    if (data.containsKey("schema")) filters.put("schema", data.getStr("schema"));

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(endpoint, WvpApiConstants.API_CLOUD_RECORD_SEEK, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("云端录像定位失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok();
  }

  @Override
  public R<?> setCloudRecordSpeed(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null) {
      return R.error("缺岑参数: data");
    }

    String mediaServerId = data.getStr("mediaServerId");
    String app = data.getStr("app");
    String stream = data.getStr("stream");
    Integer speed = data.getInt("speed");

    if (mediaServerId == null || app == null || stream == null || speed == null) {
      return R.error("缺岑参数: mediaServerId/app/stream/speed");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("mediaServerId", mediaServerId);
    filters.put("app", app);
    filters.put("stream", stream);
    filters.put("speed", speed);
    if (data.containsKey("schema")) filters.put("schema", data.getStr("schema"));

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(
            endpoint, WvpApiConstants.API_CLOUD_RECORD_SPEED, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("设置云端录像倍速失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    return R.ok();
  }

  @Override
  public R<?> getCloudRecordPlayPath(WvpDownRequest request) {
    JSONObject data = request.getData();
    String endpoint = getEndpoint(request);
    if (endpoint.isEmpty()) {
      return R.error("缺岑参数: endpoint");
    }

    if (data == null || !data.containsKey("recordId")) {
      return R.error("缺岑参数: recordId");
    }

    Map<String, Object> filters = new HashMap<>();
    if (request.getWvpRequestData() != null) {
      filters.putAll(request.getWvpRequestData());
    }
    filters.put("recordId", data.getInt("recordId"));

    JSONObject resp =
        VideoPlatformHttpUtil.httpGetJson(
            endpoint, WvpApiConstants.API_CLOUD_RECORD_PLAY_PATH, filters);

    if (VideoPlatformHttpUtil.hasError(resp)) {
      return R.error("获取云端录像下载地址失败: " + VideoPlatformHttpUtil.getErrorMessage(resp));
    }

    // 解析WVP返回的data字段，避免双重包裹
    JSONObject result = resp.getJSONObject("data");
    if (result == null) {
      result = new JSONObject();
    }
    
    return R.ok(result);
  }
}

/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: WVP设备服务测试类
 * @Author: gitee.com/NexIoT
 *
 */
package cn.wvp.protocol.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.service.impl.WvpDeviceServiceImpl;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WVP设备服务单元测试
 *
 * <p>使用说明： 1. 修改 WVP_BASE_URL 为你的 WVP 平台地址 2. 修改 DEVICE_ID 和 CHANNEL_ID 为真实的设备与通道ID 3. 运行测试方法验证各项功能
 */
public class WvpDeviceServiceTest2 {

  private WvpDeviceServiceImpl wvpDeviceService;

  // ==================== 配置区域，请根据实际环境修改 ====================

  /** WVP平台地址 */
  private static final String WVP_BASE_URL = "http://192.168.31.100:18080";

  /** 测试设备ID（国标编号） */
  private static final String DEVICE_ID = "34020000001320000001";

  /** 测试通道ID（国标编号） */
  private static final String CHANNEL_ID = "34020000001310000001";

  /** WVP实例标识 */
  private static final String INSTANCE_KEY = "wvp20250115ABC";

  /** WVP API Key（如启用鉴权则必填） */
  private static final String API_KEY =
      "eyJhbGciOiJSUzI1NiIsImtpZCI6IjNlNzk2NDZjNGRiYzQwODM4M2E5ZWVkMDlmMmI4NWFlIn0.eyJqdGkiOiJMLUI4cXBqUDdWdlU4VzFjbVU4bWpBIiwiaWF0IjoxNzYyNDI2MzYxLCJleHAiOjEwNTc0NzM0NjkxNDgwOSwibmJmIjoxNzYyNDI2MzYxLCJzdWIiOiJsb2dpbiIsImF1ZCI6IkF1ZGllbmNlIiwidXNlck5hbWUiOiJhZG1pbiIsImFwaUtleUlkIjoxfQ.YK1dbexi66p8dbztPsTWpG2r5pb8gGebH59A-tYZKPqrLstLC6KhiYtPBoB_KItc-OBdlEQDpL5mZAtYgYY-ogS4JwTpOzgvOUh8mbEa68tdiughRrSohO5O43kUF67iwmEk9XalA0OpuOLTZPFcElApAiwUULtWz21AYOU17kFtkQ4AXvugDpOgRDmhHVpAvxW7bMbq70BroieO2iYNVtfCNS-qEWmA7zG0nDiQY3kk4mX7ftVg5IrCnS8xD5vg0GBq3mib06UaagWUFJ10I8vIe5smAQOe2s96aKuT88y_Ok43xVdsAkCS9pO9qjFKRz-aXHnb6zZmMBwaL3KcSQ"; // 请从WVP后台获取

  // ====================================================================

  @BeforeEach
  public void setUp() {
    wvpDeviceService = new WvpDeviceServiceImpl();
    System.out.println("=".repeat(80));
    System.out.println("WVP平台地址: " + WVP_BASE_URL);
    System.out.println("测试设备ID: " + DEVICE_ID);
    System.out.println("测试通道ID: " + CHANNEL_ID);
    System.out.println("=".repeat(80));
  }

  /** 创建WVP下行请求对象 */
  private WvpDownRequest createWvpRequest(String deviceId, JSONObject data) {
    WvpDownRequest request = new WvpDownRequest();
    request.setDeviceId(deviceId);
    request.setData(data);

    Map<String, Object> wvpRequestData = new HashMap<>();
    wvpRequestData.put("endpoint", WVP_BASE_URL);
    // 添加token鉴权
    if (API_KEY != null && !API_KEY.isEmpty()) {
      wvpRequestData.put("token", API_KEY);
    }
    request.setWvpRequestData(wvpRequestData);

    return request;
  }

  /** 打印测试结果 */
  private void printResult(String testName, R<?> result) {
    System.out.println("\n【" + testName + "】");
    System.out.println("成功: " + result.isSuccess());
    System.out.println("消息: " + result.getMsg());
    if (result.getData() != null) {
      System.out.println("数据: " + JSONUtil.toJsonPrettyStr(result.getData()));
    }
    System.out.println("-".repeat(80));
  }

  /** 打印测试结果 */
  private void printResult(String testName, String body) {
    System.out.println("\n【" + testName + "】");
    System.out.println("成功: " + body);
  }

  @Test
  public void testListDevices() {
    System.out.println("\n>>> 测试设备列表查询");

    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("token", API_KEY);
    filters.put("page", 1);
    filters.put("count", 10);

    R<?> result = wvpDeviceService.listDevices(INSTANCE_KEY, filters);
    printResult("设备列表查询", result);
  }

  @Test
  public void testListOrgs() {
    System.out.println("\n>>> 测试组织列表查询");

    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("token", API_KEY);

    R<?> result = wvpDeviceService.listOrgs(INSTANCE_KEY, filters);
    printResult("组织列表查询", result);
  }

  @Test
  public void testListChannels() {
    System.out.println("\n>>> 测试通道列表查询");

    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("token", API_KEY);
    filters.put("page", 1);
    filters.put("count", 20);

    R<?> result = wvpDeviceService.listChannels(DEVICE_ID, filters);
    printResult("通道列表查询", result);
  }

  @Test
  public void testStartPreview() {
    System.out.println("\n>>> 测试直播开始");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("protocol", "flv"); // 优先协议

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.startPreview(request);
    printResult("直播开始", result);

    if (result.isSuccess()) {
      System.out.println("\n播放地址已获取，可用于 jessibuca 播放器：");
      JSONObject resultData = JSONUtil.parseObj(result.getData());
      JSONObject urls = resultData.getJSONObject("urls");
      if (urls != null) {
        System.out.println("FLV: " + urls.getStr("flv"));
        System.out.println("HLS: " + urls.getStr("hls"));
        System.out.println("优先协议: " + resultData.getStr("prefer"));
      }
    }
  }

  @Test
  public void testStopPreview() {
    System.out.println("\n>>> 测试直播停止");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.stopPreview(request);
    printResult("直播停止", result);
  }

  @Test
  public void testPtzControl() {
    System.out.println("\n>>> 测试云台控制");

    // 测试向左转
    JSONObject dataLeft = new JSONObject();
    dataLeft.set("channelId", CHANNEL_ID);
    dataLeft.set("command", "left");
    dataLeft.set("speed", 50);

    WvpDownRequest requestLeft = createWvpRequest(DEVICE_ID, dataLeft);
    R<?> resultLeft = wvpDeviceService.ptzControl(requestLeft);
    printResult("云台控制-左转", resultLeft);

    // 延时
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }

    // 测试停止
    JSONObject dataStop = new JSONObject();
    dataStop.set("channelId", CHANNEL_ID);
    dataStop.set("command", "stop");
    dataStop.set("speed", 0);

    WvpDownRequest requestStop = createWvpRequest(DEVICE_ID, dataStop);
    R<?> resultStop = wvpDeviceService.ptzControl(requestStop);
    printResult("云台控制-停止", resultStop);
  }

  @Test
  public void testQueryPresets() {
    System.out.println("\n>>> 测试预置位查询");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("timeout", 15);

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.queryPresets(request);
    printResult("预置位查询", result);
  }

  @Test
  public void testSnapshot() {
    System.out.println("\n>>> 测试抓拍");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.snapshot(request);
    printResult("抓拍", result);

    if (result.isSuccess()) {
      JSONObject resultData = JSONUtil.parseObj(result.getData());
      System.out.println("\n抓拍图片URL: " + resultData.getStr("snapshotUrl"));
    }
  }

  @Test
  public void testStartPlayback() {
    System.out.println("\n>>> 测试历史回放");

    // 查询最近1小时的录像
    long endTime = System.currentTimeMillis() / 1000;
    long startTime = endTime - 13600; // 1小时前

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("startTime", startTime);
    data.set("endTime", endTime);



    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.startPlayback(request);
    printResult("历史回放", result);

    if (result.isSuccess()) {
      System.out.println("\n回放地址已获取：");
      JSONObject resultData = JSONUtil.parseObj(result.getData());
      JSONObject urls = resultData.getJSONObject("urls");
      if (urls != null) {
        System.out.println("FLV: " + urls.getStr("flv"));
        System.out.println("HLS: " + urls.getStr("hls"));
      }
    }
  }

  @Test
  public void testStopPlayback() {
    System.out.println("\n>>> 测试停止回放");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.stopPlayback(request);
    printResult("停止回放", result);
  }

  @Test
  public void testQueryGBRecords() {
    System.out.println("\n>>> 测试国标录像查询");

    // 查询最近24小时的国标录像
    long endTime = System.currentTimeMillis() / 1000;
    long startTime = endTime - 86400; // 24小时前

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("startTime",DateUtil.formatDateTime(DateUtil.offset(new Date(), DateField.HOUR,-78)));
    data.set("endTime",  DateUtil.now());

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.queryGBRecords(request);
    printResult("国标录像查询", result);
  }

  @Test
  public void testStartGBRecordDownload() {
    System.out.println("\n>>> 测试开始下载国标录像");

    // 查询最近1小时的录像
    long endTime = System.currentTimeMillis() / 1000;
    long startTime = endTime - 3600; // 1小时前

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("startTime", startTime);
    data.set("endTime", endTime);

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.startGBRecordDownload(request);
    printResult("开始下载国标录像", result);

    if (result.isSuccess()) {
      JSONObject resultData = JSONUtil.parseObj(result.getData());
      String downloadId = resultData.getStr("downloadId");
      System.out.println("\n下载ID: " + downloadId);
      System.out.println("可使用此ID查询下载进度或停止下载");
    }
  }

  @Test
  public void testStopGBRecordDownload() {
    System.out.println("\n>>> 测试停止下载国标录像");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("stream", "your_stream_id"); // 需要从开始下载接口获取

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.stopGBRecordDownload(request);
    printResult("停止下载国标录像", result);
  }

  @Test
  public void testGetGBRecordDownloadProgress() {
    System.out.println("\n>>> 测试查询国标录像下载进度");

    JSONObject data = new JSONObject();
    data.set("channelId", CHANNEL_ID);
    data.set("stream", "your_stream_id"); // 需要从开始下载接口获取

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.getGBRecordDownloadProgress(request);
    printResult("查询国标录像下载进度", result);

    if (result.isSuccess()) {
      JSONObject resultData = JSONUtil.parseObj(result.getData());
      System.out.println("\n下载进度: " + resultData.getInt("progress") + "%");
    }
  }

  @Test
  public void testQueryCloudRecordDates() {
    System.out.println("\n>>> 测试查询云端录像日期列表");

    JSONObject data = new JSONObject();
    data.set("app", "rtp");
    data.set("stream", DEVICE_ID + "_" + CHANNEL_ID);

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.queryCloudRecordDates(request);
    printResult("查询云端录像日期列表", result);
  }

  @Test
  public void testQueryCloudRecords() {
    System.out.println("\n>>> 测试查询云端录像列表");

    JSONObject data = new JSONObject();
    data.set("app", "rtp");
    data.set("stream", DEVICE_ID + "_" + CHANNEL_ID);
    data.set("page", 1);
    data.set("count", 20);
    data.set("startTime", "2025-11-01 00:00:00");
    data.set("endTime", "2025-11-31 23:59:59");

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.queryCloudRecords(request);
    printResult("查询云端录像列表", result);
  }

  @Test
  public void testLoadCloudRecord() {
    System.out.println("\n>>> 测试加载云端录像文件");

    JSONObject data = new JSONObject();
    data.set("app", "rtp");
    data.set("stream", DEVICE_ID + "_" + CHANNEL_ID);
    data.set("cloudRecordId", 64);
    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.loadCloudRecord(request);
    printResult("加载云端录像文件", result);
  }

  @Test
  public void testSeekCloudRecord() {
    System.out.println("\n>>> 测试定位云端录像进度");

    JSONObject data = new JSONObject();
    data.set("app", "rtp");
    data.set("stream", DEVICE_ID + "_" + CHANNEL_ID);
    data.set("seek", 60000); // 定位到60秒处

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.seekCloudRecord(request);
    printResult("定位云端录像进度", result);
  }

  @Test
  public void testSetCloudRecordSpeed() {
    System.out.println("\n>>> 测试设置云端录像倍速");

    JSONObject data = new JSONObject();
    data.set("app", "rtp");
    data.set("stream", DEVICE_ID + "_" + CHANNEL_ID);
    data.set("speed", 2.0); // 2倍速播放

    WvpDownRequest request = createWvpRequest(DEVICE_ID, data);
    R<?> result = wvpDeviceService.setCloudRecordSpeed(request);
    printResult("设置云端录像倍速", result);
  }

  /** 完整流程测试：设备列表 -> 通道列表 -> 直播 -> 云台 -> 停止 */
  @Test
  public void testCompleteFlow() {
    System.out.println("\n========== 完整流程测试 ==========");

    // 1. 查询设备列表
    System.out.println("\n步骤1: 查询设备列表");
    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("token", API_KEY);
    filters.put("page", 1);
    filters.put("count", 5);
    R<?> devicesResult = wvpDeviceService.listDevices(INSTANCE_KEY, filters);
    printResult("设备列表", devicesResult);

    // 2. 查询通道列表
    System.out.println("\n步骤2: 查询通道列表");
    Map<String, Object> channelFilters = new HashMap<>();
    channelFilters.put("endpoint", WVP_BASE_URL);
    channelFilters.put("token", API_KEY);
    channelFilters.put("page", 1);
    channelFilters.put("count", 5);
    R<?> channelsResult = wvpDeviceService.listChannels(DEVICE_ID, channelFilters);
    printResult("通道列表", channelsResult);

    // 3. 开始直播
    System.out.println("\n步骤3: 开始直播");
    JSONObject previewData = new JSONObject();
    previewData.set("channelId", CHANNEL_ID);
    previewData.set("protocol", "flv");
    WvpDownRequest previewRequest = createWvpRequest(DEVICE_ID, previewData);
    R<?> previewResult = wvpDeviceService.startPreview(previewRequest);
    printResult("开始直播", previewResult);

    if (previewResult.isSuccess()) {
      // 4. 云台控制
      System.out.println("\n步骤4: 云台控制（向右转2秒）");
      JSONObject ptzData = new JSONObject();
      ptzData.set("channelId", CHANNEL_ID);
      ptzData.set("command", "right");
      ptzData.set("speed", 50);
      WvpDownRequest ptzRequest = createWvpRequest(DEVICE_ID, ptzData);
      R<?> ptzResult = wvpDeviceService.ptzControl(ptzRequest);
      printResult("云台控制", ptzResult);

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
      }

      // 5. 停止云台
      System.out.println("\n步骤5: 停止云台");
      JSONObject stopPtzData = new JSONObject();
      stopPtzData.set("channelId", CHANNEL_ID);
      stopPtzData.set("command", "stop");
      stopPtzData.set("speed", 0);
      WvpDownRequest stopPtzRequest = createWvpRequest(DEVICE_ID, stopPtzData);
      wvpDeviceService.ptzControl(stopPtzRequest);

      // 6. 停止直播
      System.out.println("\n步骤6: 停止直播");
      JSONObject stopData = new JSONObject();
      stopData.set("channelId", CHANNEL_ID);
      WvpDownRequest stopRequest = createWvpRequest(DEVICE_ID, stopData);
      R<?> stopResult = wvpDeviceService.stopPreview(stopRequest);
      printResult("停止直播", stopResult);
    }

    System.out.println("\n========== 流程测试完成 ==========");
  }

  /** 完整录像流程测试：国标录像查询 -> 录像回放 -> 停止回放 */
  @Test
  public void testRecordCompleteFlow() {
    System.out.println("\n========== 完整录像流程测试 ==========");

    long endTime = System.currentTimeMillis() / 1000;
    long startTime = endTime - 86400; // 24小时前

    // 1. 查询国标录像
    System.out.println("\n步骤1: 查询国标录像");
    JSONObject queryData = new JSONObject();
    queryData.set("channelId", CHANNEL_ID);
    queryData.set("startTime", startTime);
    queryData.set("endTime", endTime);
    WvpDownRequest queryRequest = createWvpRequest(DEVICE_ID, queryData);
    R<?> queryResult = wvpDeviceService.queryGBRecords(queryRequest);
    printResult("国标录像查询", queryResult);

    // 2. 开始录像回放
    System.out.println("\n步骤2: 开始录像回放");
    JSONObject playbackData = new JSONObject();
    playbackData.set("channelId", CHANNEL_ID);
    playbackData.set("startTime", startTime);
    playbackData.set("endTime", endTime);
    WvpDownRequest playbackRequest = createWvpRequest(DEVICE_ID, playbackData);
    R<?> playbackResult = wvpDeviceService.startPlayback(playbackRequest);
    printResult("开始录像回放", playbackResult);

    if (playbackResult.isSuccess()) {
      // 3. 等待5秒
      try {
        System.out.println("\n等待5秒...");
        Thread.sleep(5000);
      } catch (InterruptedException e) {
      }

      // 4. 停止回放
      System.out.println("\n步骤3: 停止录像回放");
      JSONObject stopData = new JSONObject();
      stopData.set("channelId", CHANNEL_ID);
      WvpDownRequest stopRequest = createWvpRequest(DEVICE_ID, stopData);
      R<?> stopResult = wvpDeviceService.stopPlayback(stopRequest);
      printResult("停止录像回放", stopResult);
    }

    System.out.println("\n========== 录像流程测试完成 ==========");
  }

  /** 云端录像完整流程测试 */
  @Test
  public void testCloudRecordCompleteFlow() {
    System.out.println("\n========== 云端录像完整流程测试 ==========");

    String app = "rtp";
    String stream = DEVICE_ID + "_" + CHANNEL_ID;

    // 1. 查询云端录像日期列表
    System.out.println("\n步骤1: 查询云端录像日期列表");
    JSONObject dateData = new JSONObject();
    dateData.set("app", app);
    dateData.set("stream", stream);
    WvpDownRequest dateRequest = createWvpRequest(DEVICE_ID, dateData);
    R<?> dateResult = wvpDeviceService.queryCloudRecordDates(dateRequest);
    printResult("云端录像日期列表", dateResult);

    // 2. 查询云端录像列表
    System.out.println("\n步骤2: 查询云端录像列表");
    JSONObject listData = new JSONObject();
    listData.set("app", app);
    listData.set("stream", stream);
    listData.set("page", 1);
    listData.set("count", 20);
    WvpDownRequest listRequest = createWvpRequest(DEVICE_ID, listData);
    R<?> listResult = wvpDeviceService.queryCloudRecords(listRequest);
    printResult("云端录像列表", listResult);

    // 3. 加载云端录像文件
    System.out.println("\n步骤3: 加载云端录像文件");
    long endTime = System.currentTimeMillis();
    long startTime = endTime - 3600000; // 1小时前
    JSONObject loadData = new JSONObject();
    loadData.set("app", app);
    loadData.set("stream", stream);
    loadData.set("startTime", startTime);
    loadData.set("endTime", endTime);
    WvpDownRequest loadRequest = createWvpRequest(DEVICE_ID, loadData);
    R<?> loadResult = wvpDeviceService.loadCloudRecord(loadRequest);
    printResult("加载云端录像文件", loadResult);

    if (loadResult.isSuccess()) {
      // 4. 设置倍速播放
      System.out.println("\n步骤4: 设置2倍速播放");
      JSONObject speedData = new JSONObject();
      speedData.set("app", app);
      speedData.set("stream", stream);
      speedData.set("speed", 2.0);
      WvpDownRequest speedRequest = createWvpRequest(DEVICE_ID, speedData);
      R<?> speedResult = wvpDeviceService.setCloudRecordSpeed(speedRequest);
      printResult("设置倍速播放", speedResult);

      // 5. 定位录像进度
      System.out.println("\n步骤5: 定位到60秒处");
      JSONObject seekData = new JSONObject();
      seekData.set("app", app);
      seekData.set("stream", stream);
      seekData.set("seek", 60000); // 60秒
      WvpDownRequest seekRequest = createWvpRequest(DEVICE_ID, seekData);
      R<?> seekResult = wvpDeviceService.seekCloudRecord(seekRequest);
      printResult("定位录像进度", seekResult);
    }

    System.out.println("\n========== 云端录像流程测试完成 ==========");
  }
}

/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: WVP设备服务测试类
 * @Author: gitee.com/NexIoT
 *
 */
package cn.wvp.protocol.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.domain.R;
import cn.wvp.protocol.entity.WvpDownRequest;
import cn.wvp.protocol.service.impl.WvpDeviceServiceImpl;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WVP设备服务单元测试
 *
 * <p>使用说明： 1. 修改 WVP_BASE_URL 为你的 WVP 平台地址 2. 修改 DEVICE_ID 和 CHANNEL_ID 为真实的设备与通道ID 3. 运行测试方法验证各项功能
 */
public class WvpDeviceServiceTest {

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
    // 添加API Key鉴权
    if (API_KEY != null && !API_KEY.equals("your_api_key_here")) {
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

  @Test
  public void testListDevices() {
    System.out.println("\n>>> 测试设备列表查询");

    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("page", 1);
    filters.put("count", 10);
    JSONObject set = new JSONObject();
    set.put("apiKey", API_KEY);
    Map<String, Object> auth = new HashMap<>();
    filters.put("auth", set);
    R<?> result = wvpDeviceService.listDevices(INSTANCE_KEY, filters);
    printResult("设备列表查询", result);
  }

  @Test
  public void testListChannels() {
    System.out.println("\n>>> 测试通道列表查询");

    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("page", 1); // 当前页
    filters.put("count", 20); // 每页查询数量
    //    filters.put("query", null);
    //    filters.put("online", true);
    filters.put("channelType", 0); // 0：国标设备，1：推流设备，2：拉流代理"
    //    filters.put("groupDeviceId", null);// 0：国标设备，1：推流设备，2：拉流代理"
    JSONObject set = new JSONObject();
    set.put("apiKey", API_KEY);
    Map<String, Object> auth = new HashMap<>();
    filters.put("auth", set);
    R<?> result = wvpDeviceService.listChannels(DEVICE_ID, filters);
    printResult("通道列表查询", result);
  }

  @Test
  public void testStartPreview() {
    System.out.println("\n>>> 测试直播开始");

    JSONObject data = new JSONObject();
    data.put("endpoint", WVP_BASE_URL);
    data.set("channelId", CHANNEL_ID);
    data.set("protocol", "flv"); // 优先协议
    data.set("token", API_KEY);
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
    data.set("mark", "abc" + RandomUtil.randomString(3));

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
    long startTime = endTime - 3600; // 1小时前

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

  /** 完整流程测试：设备列表 -> 通道列表 -> 直播 -> 云台 -> 停止 */
  @Test
  public void testCompleteFlow() {
    System.out.println("\n========== 完整流程测试 ==========");

    // 1. 查询设备列表
    System.out.println("\n步骤1: 查询设备列表");
    Map<String, Object> filters = new HashMap<>();
    filters.put("endpoint", WVP_BASE_URL);
    filters.put("limit", 5);
    R<?> devicesResult = wvpDeviceService.listDevices(INSTANCE_KEY, filters);
    printResult("设备列表", devicesResult);

    // 2. 查询通道列表
    System.out.println("\n步骤2: 查询通道列表");
    Map<String, Object> channelFilters = new HashMap<>();
    channelFilters.put("endpoint", WVP_BASE_URL);
    channelFilters.put("limit", 5);
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
}

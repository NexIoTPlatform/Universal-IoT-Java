/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.web.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.platform.service.BatchFunctionTask;
import cn.universal.common.monitor.NetworkMonitorSingleton;
import cn.universal.persistence.consistent.DeviceShardingRouter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 程序版本 */
@RestController
@RequestMapping("/test")
@Slf4j
public class VersionController {

  @GetMapping("/v1/debug/log")
  public Object update(@RequestParam String name, @RequestParam LogLevel level) {
    log.info("修改日志级别 name={} ,level={}", name, level.name());
    if (LogLevel.INFO.equals(level) || LogLevel.DEBUG.equals(level)) {
      loggingSystem.setLogLevel(name, level);
      return "ok";
    }
    return "只允许debug和info";
  }

  /** TCL门锁推送接口校验 */
  @GetMapping("/check")
  public Object tclCheck() {
    return "success";
  }

  @GetMapping("/serverCheck")
  public Object serverCheck() {
    Map<String, Boolean> connectionStatus = NetworkMonitorSingleton.getConnectionStatus();
    HashMap<String, Boolean> hashMap = new HashMap<>();
    for (Map.Entry<String, Boolean> q : connectionStatus.entrySet()) {
      hashMap.put(q.getKey(), q.getValue());
      if ("tcp://mqtt.iotuniv.cn:1883".equals(q.getKey())) {
        hashMap.put("EMQ", q.getValue());
      } else if ("tcp://218.108.247.88:11883".equals(q.getKey())) {
        hashMap.put("chuanmeiMqtt", q.getValue());
      } else if ("125.210.48.169:9876".equals(q.getKey())) {
        hashMap.put("rocketMq", q.getValue());
      } else if ("msgpush.ctaiot.cn:16651".equals(q.getKey())) {
        hashMap.put("ctwingMqtt", q.getValue());
      } else if ("tcp://218.108.146.92:11883".equals(q.getKey())) {
        hashMap.put("lvzhouMqtt", q.getValue());
      }
    }
    return hashMap;
  }

  @GetMapping("/topicConsumer")
  public Object serverCheck(String topic) {
    //    return rocketMQMonitor.queryDefaultTopicExistConsumer(topic);
    return "success";
  }

  @GetMapping("/version")
  public Object versionInformation() {
    return readGitProperties();
  }

  @Resource private LogbackLoggingSystem loggingSystem;
  @Resource private StringRedisTemplate stringRedisTemplate;
  @Resource private DeviceShardingRouter tableShardStrategyByIotId;

  //  @GetMapping("/debug/tcp/devices")
  //  public Object tcpDevices(@RequestParam String name, @RequestParam LogLevel level) {
  //    log.info("查看设备连接状态");
  //    Map<TcpDeviceCtx, ChannelHandlerContext> tcpMap = TcpDeviceStorage.getInstance()
  //        .nettyChannelList();
  //    return tcpMap;
  //  }

  @GetMapping("/log/shard")
  public Object getTableId(@RequestParam String iotId) {
    return getTableShardIdByIotId(iotId);
  }

  /** 读取文件 */
  private JSONObject readGitProperties() {
    FileSystemResource classPathResource = new FileSystemResource("./version.json");
    InputStream inputStream = null;
    try {
      inputStream = classPathResource.getInputStream();
    } catch (IOException e) {
      log.error("获取文件异常", e);
    }
    return JSONUtil.parseObj(readFromInputStream(inputStream));
  }

  /** 读取文件里面的值 */
  private String readFromInputStream(InputStream inputStream) {
    StringBuilder stringBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
      String line;
      while ((line = br.readLine()) != null) {
        stringBuilder.append(line);
      }
    } catch (IOException e) {
      log.error("读取文件失败", e);
    }
    return stringBuilder.toString();
  }

  private String getTableShardIdByIotId(String iotId) {
    String tableId = tableShardStrategyByIotId.generateTableName("表号为", iotId);
    String[] s = tableId.split("_");
    return s[1];
  }

  @Resource private BatchFunctionTask batchFunctionTask;

  @GetMapping("/log/shaw")
  public void getBatchFunctionTask() {
    batchFunctionTask.doTask();
  }

  @GetMapping("/log/shared")
  public void getBatchFunctionTas2k() {
    batchFunctionTask.consumer();
  }

  /** echo - 打印请求体和请求头 */
  @RequestMapping("/echo")
  public Object testLog(@RequestBody String body, HttpServletRequest request) {

    // 打印所有请求头
    log.info("=== 请求头信息 ===");
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      log.info("Header: {} = {}", headerName, headerValue);
    }
    log.info("=== 请求头信息结束 ===");

    // 打印请求方法、URL、IP等信息
    log.info("请求方法: {}", request.getMethod());
    log.info("请求URL: {}", request.getRequestURL());
    log.info("客户端IP: {}", request.getRemoteAddr());
    log.info("接收第三方消息={}", body);
    return body;
  }
}

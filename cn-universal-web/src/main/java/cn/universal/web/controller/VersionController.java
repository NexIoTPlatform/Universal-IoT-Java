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

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.platform.service.BatchFunctionTask;
import cn.universal.common.monitor.NetworkMonitorSingleton;
import cn.universal.common.utils.DingTalkUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 程序版本 */
@RestController
@RequestMapping("/test")
@Slf4j(topic = "api_log")
public class VersionController {

  @GetMapping("/v1/debug/log")
  public Object update(@RequestParam String name, @RequestParam LogLevel level) {
    log.info("修改日志级别 name={} ,level={}", name, level.name());
    if (!(LogLevel.INFO.equals(level) || LogLevel.DEBUG.equals(level))) {
      return Map.of("success", false, "message", "只允许DEBUG和INFO级别");
    }
    try {
      loggingSystem.setLogLevel(name, level);
      ch.qos.logback.classic.Logger verifyLogger =
          (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(name);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("loggerName", name);
      result.put("requestedLevel", level.name());
      result.put("actualLevel", verifyLogger.getLevel() != null ? verifyLogger.getLevel().toString() : "null");
      result.put("effectiveLevel", verifyLogger.getEffectiveLevel().toString());
      result.put("message", "日志级别修改成功");
      return result;
    } catch (Exception e) {
      log.error("修改日志级别失败: name={}, level={}", name, level, e);
      return Map.of(
        "success", false,
        "loggerName", name,
        "requestedLevel", level.name(),
        "error", e.getMessage(),
        "message", "日志级别修改失败"
      );
    }
  }

  @GetMapping("/v1/debug/log/status")
  public Object getLogStatus(@RequestParam String name) {
    try {
      ch.qos.logback.classic.Logger logger = 
          (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(name);
      
      Map<String, Object> result = new HashMap<>();
      result.put("loggerName", name);
      result.put("currentLevel", logger.getLevel() != null ? logger.getLevel().toString() : "null");
      result.put("effectiveLevel", logger.getEffectiveLevel().toString());
      result.put("isAdditive", logger.isAdditive());
      result.put("appenderCount", logger.iteratorForAppenders().hasNext() ? "有appender" : "无appender");
      
      return result;
    } catch (Exception e) {
      log.error("获取日志状态失败: name={}", name, e);
      Map<String, Object> result = new HashMap<>();
      result.put("success", false);
      result.put("loggerName", name);
      result.put("error", e.getMessage());
      return result;
    }
  }

  @GetMapping("/v1/debug/log/test")
  public Object testLog(@RequestParam String name) {
    try {
      Logger testLogger = org.slf4j.LoggerFactory.getLogger(name);
      
      testLogger.trace("TRACE级别测试日志 - {}", name);
      testLogger.debug("DEBUG级别测试日志 - {}", name);
      testLogger.info("INFO级别测试日志 - {}", name);
      testLogger.warn("WARN级别测试日志 - {}", name);
      testLogger.error("ERROR级别测试日志 - {}", name);
      
      return Map.of(
        "success", true,
        "loggerName", name,
        "message", "测试日志已输出，请检查日志文件"
      );
    } catch (Exception e) {
      log.error("测试日志失败: name={}", name, e);
      return Map.of(
        "success", false,
        "loggerName", name,
        "error", e.getMessage()
      );
    }
  }

  // 移除强制/批量/测试接口，保留最小化的设置与查询

  



  @GetMapping("/version")
  public Object versionInformation() {
    return readGitProperties();
  }

  @Resource private LogbackLoggingSystem loggingSystem;
  @Resource private StringRedisTemplate stringRedisTemplate;
  

  // ========================================== 
  // 协议扩展模块日志管理API
  // ==========================================

  /**
   * 设置协议模块日志级别
   */
  // 以下协议日志管理接口已移除，保留最小化动态级别设置

  /**
   * 批量设置协议模块日志级别
   */
  

  /**
   * 恢复协议模块原始日志级别
   */
  

  /**
   * 获取协议模块日志状态
   */
  

  /**
   * 获取支持的协议模块列表
   */
  

  /**
   * 获取已修改的协议模块信息
   */
  

  /**
   * 测试协议模块日志输出
   */
  

  

  // ========================================== 
  // TCP协议设备统计调试API
  // ==========================================

  /**
   * 获取TCP协议设备统计信息（调试用）
   */
  @GetMapping("/v1/tcp/stats")
  public Object getTcpStats() {
    try {
      // 这里需要注入TcpConnectionManager，暂时返回模拟数据
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "TCP协议设备统计功能已优化");
      result.put("timestamp", System.currentTimeMillis());
      
      // 模拟统计数据
      result.put("currentInstance", "instance-001");
      result.put("currentDeviceCount", 150);
      result.put("activeInstanceCount", 3);
      result.put("totalActiveDevices", 450);
      result.put("totalDevicesIncludingZombie", 500);
      result.put("zombieDeviceCount", 50);
      
      return result;
    } catch (Exception e) {
      log.error("获取TCP协议统计失败", e);
      return Map.of("success", false, "error", e.getMessage());
    }
  }

  /**
   * 调试Redis中的TCP实例设备索引
   */
  @GetMapping("/v1/tcp/debug/redis")
  public Object debugTcpRedis() {
    try {
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "TCP Redis调试信息");
      result.put("timestamp", System.currentTimeMillis());
      
      // Redis Key信息
      result.put("deviceRoutesKey", "tcp:device:routes");
      result.put("instanceDevicesKeyPrefix", "tcp:instance:devices:");
      result.put("expectedPattern", "tcp:instance:devices:*");
      
      // 可能的问题分析
      Map<String, Object> analysis = new HashMap<>();
      analysis.put("ttlIssue", "实例设备索引TTL设置为10分钟，可能已过期");
      analysis.put("registrationIssue", "设备连接时可能没有正确调用registerDeviceRoute");
      analysis.put("scanIssue", "SCAN命令可能没有找到匹配的key");
      analysis.put("redisConnectionIssue", "Redis连接可能有问题");
      
      result.put("analysis", analysis);
      
      // 建议的检查步骤
      String[] checkSteps = {
        "1. 检查Redis中是否存在 tcp:device:routes key",
        "2. 检查Redis中是否存在 tcp:instance:devices:* 模式的key",
        "3. 检查设备连接时是否正确调用了registerDevice方法",
        "4. 检查实例心跳是否正常",
        "5. 检查Redis连接是否正常"
      };
      result.put("checkSteps", checkSteps);
      
      return result;
    } catch (Exception e) {
      log.error("调试TCP Redis失败", e);
      return Map.of("success", false, "error", e.getMessage());
    }
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

  

  @Resource private BatchFunctionTask batchFunctionTask;

  @GetMapping("/log/shaw")
  public void getBatchFunctionTask() {
    batchFunctionTask.doTask();
  }

  @GetMapping("/log/shared")
  public void getBatchFunctionTas2k() {
    batchFunctionTask.consumer();
  }

  /** 接收用户使用情况报告 */
  @PostMapping("/report")
  public Object receiveUserReport(@RequestBody String body, HttpServletRequest request) {
    try {
      // 记录请求信息
      log.info("=== 系统状态报告 ===");
      log.info("请求方法: {}", request.getMethod());
      log.info("请求URL: {}", request.getRequestURL());
      log.info("客户端IP: {}", JakartaServletUtil.getClientIP(request));
      log.info("User-Agent: {}", request.getHeader("User-Agent"));
      log.info("Referer: {}", request.getHeader("Referer"));

      // 解析JSON数据
      if (body != null && !body.trim().isEmpty()) {
        try {
          ObjectMapper mapper = new ObjectMapper();
          JsonNode jsonData = mapper.readTree(body);

          // 记录用户数据（使用简化的字段名）
          log.info("用户IP: {}", jsonData.get("ip"));
          log.info("用户代理: {}", jsonData.get("ua"));
          log.info("时间戳: {}", jsonData.get("ts"));
          log.info("版本: {}", jsonData.get("v"));
          log.info("访问URL: {}", jsonData.get("url"));
          log.info("来源页面: {}", jsonData.get("ref"));
          log.info("屏幕分辨率: {}", jsonData.get("sr"));
          log.info("时区: {}", jsonData.get("tz"));
          log.info("语言: {}", jsonData.get("lang"));
          log.info("客户端ID: {}", jsonData.get("cid"));

          // 发送钉钉通知
          try {
            StringBuilder message = new StringBuilder();
            message.append("🔔 系统状态报告通知\n");
            message.append("📍 用户IP: ").append(jsonData.get("ip")).append("\n");
            message.append("🌐 客户端IP: ").append(JakartaServletUtil.getClientIP(request)).append("\n");
            message.append("🌐 访问URL: ").append(jsonData.get("url")).append("\n");
            message.append("📱 用户代理: ").append(jsonData.get("ua")).append("\n");
            message.append("⏰ 时间戳: ").append(DateUtil.now()).append("\n");
            message.append("🔧 来源页面: ").append(jsonData.get("ref")).append("\n");
            message.append("📱 分辨率: ").append(jsonData.get("sr")).append("\n");
            message.append("🔧 版本: ").append(jsonData.get("v")).append("\n");
            message.append("🆔 客户端ID: ").append(jsonData.get("cid"));
            DingTalkUtil.send(message.toString());
            // DingTalk通知已禁用
          } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
          }

          // 这里可以添加数据库存储逻辑
          // saveUserReport(jsonData);

        } catch (Exception e) {
          log.error("解析JSON数据失败: {}", e.getMessage());
        }
      }

      log.info("=== 系统状态报告结束 ===");

      // 返回成功响应
      return Map.of("success", true, "message", "状态同步成功", "timestamp", System.currentTimeMillis());

    } catch (Exception e) {
      log.error("处理系统状态报告失败", e);
      return Map.of("success", false, "message", "服务器错误", "error", e.getMessage());
    }
  }

  /** 处理图片追踪请求 */
  @GetMapping("/report")
  public Object handleImageTracking(
      @RequestParam(required = false) String m,
      @RequestParam(required = false) String d,
      @RequestParam(required = false) String t,
      @RequestParam(required = false) String k,
      HttpServletRequest request) {

    try {
      // 验证密钥
      if (!"iot_tracking_2025".equals(k)) {
        log.warn("无效的追踪密钥: {}", k);
        return "invalid";
      }

      // 如果是图片追踪请求
      if ("img".equals(m) && d != null) {
        try {
          // 解码数据
          String decodedData = new String(Base64.getDecoder().decode(d));
          ObjectMapper mapper = new ObjectMapper();
          JsonNode jsonData = mapper.readTree(decodedData);

          log.info("=== 图片追踪数据 ===");
          log.info("用户IP: {}", jsonData.get("ip"));
          log.info("时间戳: {}", jsonData.get("ts"));
          log.info("客户端ID: {}", jsonData.get("cid"));
          log.info("=== 图片追踪数据结束 ===");

          // 发送钉钉通知
          try {
            StringBuilder message = new StringBuilder();
            message.append("🖼️ 图片追踪通知\n");
            message.append("📍 用户IP: ").append(jsonData.get("ip")).append("\n");
            message.append("⏰ 时间戳: ").append(jsonData.get("ts")).append("\n");
            message.append("🆔 客户端ID: ").append(jsonData.get("cid"));
            DingTalkUtil.send(message.toString());
            // DingTalk通知已禁用
          } catch (Exception e) {
            log.error("发送钉钉通知失败: {}", e.getMessage());
          }

        } catch (Exception e) {
          log.error("解析图片追踪数据失败: {}", e.getMessage());
        }
      }

      // 返回1x1像素的GIF
      byte[] pixel =
          Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
      return ResponseEntity.ok().contentType(MediaType.IMAGE_GIF).body(pixel);

    } catch (Exception e) {
      log.error("处理图片追踪失败", e);
      return "error";
    }
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

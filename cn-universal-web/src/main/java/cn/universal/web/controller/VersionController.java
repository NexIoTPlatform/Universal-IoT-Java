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

import cn.universal.persistence.consistent.DeviceShardingRouter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 程序版本
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class VersionController {

  @GetMapping("/v1/debug/log")
  public Object update(@RequestParam String name, @RequestParam LogLevel level) {
    log.info("动态修改日志级别 name={} ,level={}", name, level.name());
    if (LogLevel.INFO.equals(level) || LogLevel.DEBUG.equals(level)) {
      loggingSystem.setLogLevel(name, level);
      return "success";
    }
    return "只允许debug和info";
  }


  @Resource
  private LogbackLoggingSystem loggingSystem;

  @Resource
  private DeviceShardingRouter tableShardStrategyByIotId;


  @GetMapping("/log/shard")
  public Object getTableId(@RequestParam String iotId) {
    return getTableShardIdByIotId(iotId);
  }


  private String getTableShardIdByIotId(String iotId) {
    String tableId = tableShardStrategyByIotId.generateTableName("表号为", iotId);
    String[] s = tableId.split("_");
    return s[1];
  }

  /**
   * echo - 打印请求体和请求头
   */
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

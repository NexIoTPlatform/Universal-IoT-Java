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

package cn.universal.admin.system.service;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.universal.common.constant.Constants;
import cn.universal.common.utils.IPUtils;
import cn.universal.common.utils.ServletUtils;
import cn.universal.persistence.entity.SysLogininfor;
import cn.universal.persistence.entity.SysOperLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** 异步工厂（产生任务用） @Author Lion Li */
@Slf4j
@Async
@Component
public class AsyncService {

  @Autowired private ISysLogininforService iSysLogininforService;

  @Autowired private ISysOperLogService iSysOperLogService;

  /**
   * 记录登录信息
   *
   * @param username 用户名
   * @param status 状态
   * @param message 消息
   * @param request HTTP请求对象，可以为null
   */
  public void recordLogininfor(
      final String username,
      final String status,
      final String message,
      final HttpServletRequest request) {
    HttpServletRequest httpRequest = request;
    // 如果传入的request为null，尝试从RequestContextHolder获取
    if (httpRequest == null) {
      try {
        httpRequest = ServletUtils.getRequest();
      } catch (Exception e) {
        log.warn("无法获取HttpServletRequest，使用默认值记录登录信息: {}", e.getMessage());
        // 创建默认的登录信息记录
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr("unknown");
        logininfor.setLoginLocation("unknown");
        logininfor.setBrowser("unknown");
        logininfor.setOs("unknown");
        logininfor.setMsg(message);
        // 日志状态
        if (Constants.LOGIN_SUCCESS.equals(status) || Constants.LOGOUT.equals(status)) {
          logininfor.setStatus(Constants.SUCCESS);
        } else if (Constants.LOGIN_FAIL.equals(status)) {
          logininfor.setStatus(Constants.FAIL);
        }
        // 插入数据
        iSysLogininforService.insertLogininfor(logininfor);
        return;
      }
    }

    // 使用获取到的request参数
    final UserAgent userAgent = UserAgentUtil.parse(httpRequest.getHeader("User-Agent"));
    final String ip = IPUtils.getIpAddr(httpRequest);
    // 操作系统
    String os = userAgent.getOs().getName();
    // 浏览器
    String browser = userAgent.getBrowser().getName();
    SysLogininfor login = new SysLogininfor();
    login.setUserName(username);
    login.setIpaddr(ip);
    login.setBrowser(browser);
    login.setOs(os);
    login.setMsg(message);
    // 日志状态
    if (Constants.LOGIN_SUCCESS.equals(status) || Constants.LOGOUT.equals(status)) {
      login.setStatus(Constants.SUCCESS);
    } else if (Constants.LOGIN_FAIL.equals(status)) {
      login.setStatus(Constants.FAIL);
    }
    // 插入数据
    iSysLogininforService.insertLogininfor(login);
  }

  /**
   * 操作日志记录
   *
   * @param operLog 操作日志信息
   */
  public void recordOper(final SysOperLog operLog) {
    // 远程查询操作地点
    operLog.setOperLocation(operLog.getOperIp());
    iSysOperLogService.insertOperlog(operLog);
  }
}

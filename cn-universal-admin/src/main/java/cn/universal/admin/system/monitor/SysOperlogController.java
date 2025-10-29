/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.system.monitor;

import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.admin.system.service.ISysOperLogService;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.SysOperLog;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 操作日志记录 @Author ruoyi */
@RestController
@RequestMapping("/admin/monitor/operlog")
public class SysOperlogController extends BaseController {

  @Autowired private ISysOperLogService operLogService;

  @GetMapping("/list")
  public TableDataInfo list(SysOperLog operLog) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()) {
      operLog.setOperName(iotUser.getUnionId());
    }
    startPage();
    List<SysOperLog> sysOperLogs = operLogService.selectPageOperLogList(operLog);
    return getDataTable(sysOperLogs);
  }

  @Log(title = "操作日志", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysOperLog operLog) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()) {
      operLog.setOperName(iotUser.getUnionId());
    }
    List<SysOperLog> list = operLogService.selectOperLogList(operLog);
    ExcelUtil<SysOperLog> util = new ExcelUtil<SysOperLog>(SysOperLog.class);
    util.exportExcel(response, list, "操作日志");
  }
}

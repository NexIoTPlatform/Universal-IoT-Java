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

package cn.universal.admin.system.monitor;

import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.system.service.ISysLogininforService;
import cn.universal.security.BaseController;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.SysLogininfor;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 系统访问记录 @Author ruoyi */
@RestController
@RequestMapping("/admin/monitor/logininfor")
public class SysLoginLogController extends BaseController {

  @Resource private ISysLogininforService logininforService;

  @GetMapping("/list")
  public TableDataInfo list(SysLogininfor logininfor) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()) {
      logininfor.setUserName(iotUser.getUnionId());
    }
    startPage();
    List<SysLogininfor> sysLogininfors = logininforService.selectPageLogininforList(logininfor);
    return getDataTable(sysLogininfors);
  }

  @Log(title = "登录日志", businessType = BusinessType.EXPORT)
  @PostMapping("/export")
  public void export(HttpServletResponse response, SysLogininfor logininfor) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.isAdmin()) {
      logininfor.setUserName(iotUser.getUnionId());
    }
    List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
    ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
    util.exportExcel(response, list, "登录日志");
  }

  @Log(title = "登录日志", businessType = BusinessType.DELETE)
  @DeleteMapping("/{infoIds}")
  public AjaxResult remove(@PathVariable Long[] infoIds) {
    return toAjax(logininforService.deleteLogininforByIds(infoIds));
  }

  @Log(title = "登录日志", businessType = BusinessType.CLEAN)
  @DeleteMapping("/clean")
  public AjaxResult clean() {
    logininforService.cleanLogininfor();
    return AjaxResult.success();
  }
}

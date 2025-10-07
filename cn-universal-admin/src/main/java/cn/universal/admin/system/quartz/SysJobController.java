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

package cn.universal.admin.system.quartz;

import cn.hutool.core.util.StrUtil;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.security.BaseController;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.quartz.constant.Constants;
import cn.universal.quartz.domain.SysJob;
import cn.universal.quartz.service.ISysJobService;
import cn.universal.quartz.util.CronUtils;
import java.util.List;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 调度任务信息操作处理 @Author ruoyi */
@RestController
@RequestMapping("/admin/quartz/job")
public class SysJobController extends BaseController {

  @Autowired private ISysJobService jobService;

  /** 查询定时任务列表 */
  @GetMapping("/list")
  public TableDataInfo list(SysJob sysJob) {
    startPage();
    return getDataTable(jobService.selectJobList(sysJob));
  }

  /** 导出定时任务列表 */
  @GetMapping("/export")
  public AjaxResult export(SysJob sysJob) {
    List<SysJob> list = jobService.selectJobList(sysJob);
    ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
    return util.exportExcel(list, "定时任务");
  }

  /** 获取定时任务详细信息 */
  @GetMapping(value = "/{jobId}")
  public AjaxResult getInfo(@PathVariable("jobId") Long jobId) {
    return AjaxResult.success(jobService.selectJobById(jobId));
  }

  /** 新增定时任务 */
  @PostMapping
  public AjaxResult add(@RequestBody SysJob sysJob) throws SchedulerException {
    if (!CronUtils.isValid(sysJob.getCronExpression())) {
      return AjaxResult.error("新增任务'" + sysJob.getJobName() + "'失败，Cron表达式不正确");
    } else if (StrUtil.containsIgnoreCase(sysJob.getInvokeTarget(), Constants.LOOKUP_RMI)) {
      return AjaxResult.error("新增任务'" + sysJob.getJobName() + "'失败，目标字符串不允许'rmi://'调用");
    }
    sysJob.setCreateBy(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    return toAjax(jobService.insertJob(sysJob));
  }

  /** 修改定时任务 */
  @PutMapping
  public AjaxResult edit(@RequestBody SysJob sysJob) throws SchedulerException {
    if (!CronUtils.isValid(sysJob.getCronExpression())) {
      return AjaxResult.error("修改任务'" + sysJob.getJobName() + "'失败，Cron表达式不正确");
    } else if (StrUtil.containsIgnoreCase(sysJob.getInvokeTarget(), Constants.LOOKUP_RMI)) {
      return AjaxResult.error("修改任务'" + sysJob.getJobName() + "'失败，目标字符串不允许'rmi://'调用");
    }
    sysJob.setUpdateBy(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    return toAjax(jobService.updateJob(sysJob));
  }

  /** 定时任务状态修改 */
  @PutMapping("/changeStatus")
  public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException {
    SysJob newJob = jobService.selectJobById(job.getJobId());
    newJob.setStatus(job.getStatus());
    return toAjax(jobService.changeStatus(newJob));
  }

  /** 定时任务立即执行一次 */
  @PutMapping("/run")
  public AjaxResult run(@RequestBody SysJob job) throws SchedulerException {
    jobService.run(job);
    return AjaxResult.success();
  }

  /** 删除定时任务 */
  @DeleteMapping("/{jobIds}")
  public AjaxResult remove(@PathVariable Long[] jobIds) throws SchedulerException {
    jobService.deleteJobByIds(jobIds);
    return AjaxResult.success();
  }
}

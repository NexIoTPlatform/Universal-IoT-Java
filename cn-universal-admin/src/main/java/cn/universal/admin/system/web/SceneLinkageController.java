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

package cn.universal.admin.system.web;

import cn.universal.admin.common.annotation.Log;
import cn.universal.admin.common.enums.BusinessType;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.SceneLinkage;
import cn.universal.persistence.entity.bo.SceneLinkageBO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.rule.model.ExeRunContext;
import cn.universal.rule.scene.service.SceneLinkageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 场景联动管理控制器
 *
 * @since 2023-03-01
 */
@RestController
@Tag(name = "场景联动管理", description = "场景联动管理")
@RequestMapping("/admin/v1/scene/linkage")
public class SceneLinkageController extends BaseController {

  @Resource private SceneLinkageService sceneLinkageService;

  /** 查询场景联动列表 */
  @Operation(summary = "查询场景联动列表")
  @GetMapping("/list")
  public TableDataInfo list(SceneLinkage sceneLinkage) {
    IoTUser user = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    sceneLinkage.setCreateBy(user.isAdmin() ? null : user.getUnionId());
    List<SceneLinkage> list = sceneLinkageService.selectSceneLinkageList(sceneLinkage);
    return getDataTable(list);
  }

  /** 导出场景联动列表 */
  @Operation(summary = "导出场景联动列表")
  @PostMapping("/export")
  @Log(title = "导出场景联动列表", businessType = BusinessType.EXPORT)
  public void export(HttpServletResponse response, SceneLinkage sceneLinkage) {
    List<SceneLinkage> list = sceneLinkageService.selectSceneLinkageList(sceneLinkage);
    ExcelUtil<SceneLinkage> util = new ExcelUtil<SceneLinkage>(SceneLinkage.class);
    util.exportExcel(response, list, "场景联动数据");
  }

  /** 获取场景联动详细信息 */
  @Operation(summary = "获取场景联动详细信息")
  @GetMapping(value = "/{id}")
  public AjaxResult<SceneLinkage> getInfo(@PathVariable("id") Long id) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!sceneLinkageService.checkSelf(id, iotUser.getUnionId()) && !iotUser.isAdmin()) {
      throw new IoTException("场景没有操作权限！");
    }
    return AjaxResult.success(sceneLinkageService.selectSceneLinkageById(id));
  }

  /** 新增场景联动 */
  @Operation(summary = "新增场景联动")
  @PostMapping
  @Log(title = "新增场景联动", businessType = BusinessType.INSERT)
  public AjaxResult<Void> add(@RequestBody SceneLinkageBO sceneLinkage) throws Exception {
    String unionId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    sceneLinkage.setCreateBy(unionId);
    sceneLinkage.setCreateTime(new Date());
    sceneLinkage.setUpdateBy(unionId);
    sceneLinkage.setUpdateTime(new Date());
    int rows = sceneLinkageService.insertSceneLinkage(sceneLinkage);
    return AjaxResult.toAjax(rows);
  }

  /** 修改场景联动 */
  @Operation(summary = "修改场景联动")
  @PutMapping
  @Log(title = "修改场景联动", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> edit(@RequestBody SceneLinkageBO sceneLinkage) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!sceneLinkageService.checkSelf(sceneLinkage.getId(), iotUser.getUnionId())
        && !iotUser.isAdmin()) {
      throw new IoTException("场景没有操作权限！");
    }
    sceneLinkage.setUpdateBy(SecurityUtils.getUnionId());
    sceneLinkage.setUpdateTime(new Date());
    return AjaxResult.toAjax(sceneLinkageService.updateSceneLinkage(sceneLinkage));
  }

  /** 删除场景联动 */
  @Operation(summary = "删除场景联动")
  @DeleteMapping("/{ids}")
  @Log(title = "删除场景联动", businessType = BusinessType.DELETE)
  public AjaxResult<Void> remove(@PathVariable Long[] ids) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!sceneLinkageService.checkSelf(ids[0], iotUser.getUnionId()) && !iotUser.isAdmin()) {
      throw new IoTException("场景没有操作权限！");
    }
    return AjaxResult.toAjax(sceneLinkageService.deleteSceneLinkageByIds(ids));
  }

  /** 手动触发动作 */
  @Operation(summary = "手动触发动作")
  @GetMapping(value = "/exec/{id}")
  public AjaxResult<Void> manualExec(@PathVariable("id") Long id) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!sceneLinkageService.checkSelf(id, iotUser.getUnionId()) && !iotUser.isAdmin()) {
      throw new IoTException("场景没有操作权限！");
    }
    List<ExeRunContext> runContexts = sceneLinkageService.functionDown(id);
    logger.info("手动触发执行成功={}", runContexts);
    return AjaxResult.success("触发成功");
  }

  /** 查询场景联动执行日志（分页） */
  @Operation(summary = "查询场景联动执行日志")
  @GetMapping("/log/{sceneId}")
  public TableDataInfo logList(
      @PathVariable("sceneId") String sceneId,
      @RequestParam(defaultValue = "1") int pageNum,
      @RequestParam(defaultValue = "10") int pageSize) {
    return getDataTable(sceneLinkageService.getSceneLinkageLogPage(sceneId, pageNum, pageSize));
  }
}

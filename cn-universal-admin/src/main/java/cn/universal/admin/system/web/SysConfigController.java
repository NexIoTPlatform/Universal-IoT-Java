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
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.service.ISysConfigService;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.persistence.entity.admin.SysConfig;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 参数配置 信息操作处理 @Author ruoyi */
@RestController
@RequestMapping("admin/system/config")
public class SysConfigController extends BaseController {

  @Autowired private ISysConfigService configService;

  /** 获取参数配置列表 */
  @GetMapping("/list")
  public TableDataInfo list(SysConfig config) {
    startPage();
    List<SysConfig> list = configService.selectConfigList(config);
    return getDataTable(list);
  }

  //    @PostMapping("/export")
  //    public void export(HttpServletResponse response, SysConfig config)
  //    {
  //        List<SysConfig> list = configService.selectConfigList(config);
  //        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
  //        util.exportExcel(response, list, "参数数据");
  //    }

  /** 根据参数编号获取详细信息 */
  @GetMapping(value = "/{configId}")
  public AjaxResult getInfo(@PathVariable Long configId) {
    return AjaxResult.success(configService.selectConfigById(configId));
  }

  /** 根据参数键名查询参数值 */
  @GetMapping(value = "/configKey/{configKey}")
  public AjaxResult getConfigKey(@PathVariable String configKey) {
    return AjaxResult.success(configService.selectConfigByKey(configKey));
  }

  /** 新增参数配置 */
  @PostMapping
  @Log(title = "新增参数配置", businessType = BusinessType.INSERT)
  public AjaxResult add(@Validated @RequestBody SysConfig config) {
    if (IoTUserConstants.NOT_UNIQUE.equals(configService.checkConfigKeyUnique(config))) {
      return AjaxResult.error("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
    }
    config.setCreateBy(SecurityUtils.getUnionId());
    return toAjax(configService.insertConfig(config));
  }

  /** 修改参数配置 */
  @PutMapping
  @Log(title = "修改参数配置", businessType = BusinessType.UPDATE)
  public AjaxResult edit(@Validated @RequestBody SysConfig config) {
    if (IoTUserConstants.NOT_UNIQUE.equals(configService.checkConfigKeyUnique(config))) {
      return AjaxResult.error("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
    }
    config.setUpdateBy(SecurityUtils.getUnionId());
    return toAjax(configService.updateConfig(config));
  }

  /** 删除参数配置 */
  @DeleteMapping("/{configIds}")
  @Log(title = "删除参数配置", businessType = BusinessType.DELETE)
  public AjaxResult remove(@PathVariable Long[] configIds) {
    configService.deleteConfigByIds(configIds);
    return success();
  }

  /** 刷新参数缓存 */
  @DeleteMapping("/refreshCache")
  public AjaxResult refreshCache() {
    configService.resetConfigCache();
    return AjaxResult.success();
  }
}

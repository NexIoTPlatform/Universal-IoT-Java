/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.admin.system.web;

import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.system.service.ISysDictTypeService;
import cn.universal.common.constant.IoTUserConstants;
import cn.universal.persistence.entity.admin.SysDictType;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.security.BaseController;
import java.util.Date;
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

/** 数据字典信息 @Author ruoyi */
@RestController
@RequestMapping("admin/system/dict/type")
public class SysDictTypeController extends BaseController {

  @Autowired private ISysDictTypeService dictTypeService;

  @GetMapping("/list")
  public TableDataInfo list(SysDictType dictType) {
    startPage();
    return getDataTable(dictTypeService.selectPageDictTypeList(dictType));
  }

  //  @GetMapping("/export")
  //  public AjaxResult export(SysDictType dictType) {
  //    List<SysDictType> list = dictTypeService.selectDictTypeList(dictType);
  //    ExcelUtil<SysDictType> util = new ExcelUtil<SysDictType>(SysDictType.class);
  //    return util.exportExcel(list, "字典类型");
  //  }

  /** 查询字典类型详细 */
  @GetMapping(value = "/{dictId}")
  public AjaxResult getInfo(@PathVariable Long dictId) {
    return AjaxResult.success(dictTypeService.selectDictTypeById(dictId));
  }

  /** 新增字典类型 */
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysDictType dict) {
    if (IoTUserConstants.NOT_UNIQUE.equals(dictTypeService.checkDictTypeUnique(dict))) {
      return error("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
    }
    dict.setCreateBy(SecurityUtils.getUnionId());
    dict.setCreateTime(new Date());
    return toAjax(dictTypeService.insertDictType(dict));
  }

  /** 修改字典类型 */
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysDictType dict) {
    if (IoTUserConstants.NOT_UNIQUE.equals(dictTypeService.checkDictTypeUnique(dict))) {
      return error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
    }
    dict.setUpdateBy(SecurityUtils.getUnionId());
    dict.setUpdateTime(new Date());
    return toAjax(dictTypeService.updateDictType(dict));
  }

  /** 删除字典类型 */
  @DeleteMapping("/{dictIds}")
  public AjaxResult remove(@PathVariable Long[] dictIds) {
    dictTypeService.deleteDictTypeByIds(dictIds);
    return success();
  }

  /** 刷新字典缓存 */
  @DeleteMapping("/refreshCache")
  public AjaxResult refreshCache() {
    dictTypeService.resetDictCache();
    return success();
  }

  /** 获取字典选择框列表 */
  @GetMapping("/optionselect")
  public AjaxResult optionselect() {
    List<SysDictType> dictTypes = dictTypeService.selectDictTypeAll();
    return AjaxResult.success(dictTypes);
  }
}

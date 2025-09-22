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

import cn.hutool.core.lang.Validator;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.service.ISysDictDataService;
import cn.universal.admin.system.service.ISysDictTypeService;
import cn.universal.persistence.entity.admin.SysDictData;
import cn.universal.persistence.entity.admin.vo.SysDictDataVo;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import java.util.ArrayList;
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
@RequestMapping("admin/system/dict/data")
public class SysDictDataController extends BaseController {

  @Autowired private ISysDictDataService dictDataService;

  @Autowired private ISysDictTypeService dictTypeService;

  @GetMapping("/list")
  public TableDataInfo list(SysDictDataVo dictData) {
    startPage();
    return getDataTable(dictDataService.selectPageDictDataList(dictData));
  }

  //  @GetMapping("/export")
  //  public AjaxResult export(SysDictData dictData) {
  //    List<SysDictData> list = dictDataService.selectDictDataList(dictData);
  //    ExcelUtil<SysDictData> util = new ExcelUtil<SysDictData>(SysDictData.class);
  //    return util.exportExcel(list, "字典数据");
  //  }

  /** 查询字典数据详细 */
  @GetMapping(value = "/{dictCode}")
  public AjaxResult getInfo(@PathVariable Long dictCode) {
    return AjaxResult.success(dictDataService.selectDictDataById(dictCode));
  }

  /** 根据字典类型查询字典数据信息 */
  @GetMapping(value = "/type/{dictType}")
  public AjaxResult dictType(@PathVariable String dictType) {
    List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
    if (Validator.isNull(data)) {
      data = new ArrayList<SysDictData>();
    }
    return AjaxResult.success(data);
  }

  /** 根据字典类型数组查询字典数据信息 */
  @GetMapping(value = "/types/{dictType}")
  public AjaxResult dictType(@PathVariable String[] dictType) {
    return AjaxResult.success(dictDataService.selectDictDataByTypes(dictType));
  }

  /** 新增字典类型 */
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysDictData dict) {
    dict.setCreateBy(SecurityUtils.getUnionId());
    dict.setCreateTime(new Date());
    return toAjax(dictDataService.insertDictData(dict));
  }

  /** 新增字典类型(进销人员) */
  @PostMapping(value = "/sale")
  public AjaxResult addSaleBuyPerson(@Validated @RequestBody SysDictData dict) {
    return toAjax(dictDataService.insertSaleBuyDictData(dict));
  }

  /** 修改保存字典类型 */
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysDictData dict) {
    dict.setUpdateBy(SecurityUtils.getUnionId());
    dict.setUpdateTime(new Date());
    return toAjax(dictDataService.updateDictData(dict));
  }

  /** 删除字典类型 */
  @DeleteMapping("/{dictCodes}")
  public AjaxResult remove(@PathVariable Long[] dictCodes) {
    dictDataService.deleteDictDataByIds(dictCodes);
    return success();
  }
}

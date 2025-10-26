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

package cn.universal.admin.generator.web;

import cn.hutool.core.convert.Convert;
import cn.universal.admin.generator.domain.GenTable;
import cn.universal.admin.generator.domain.GenTableColumn;
import cn.universal.admin.generator.service.IGenTableColumnService;
import cn.universal.admin.generator.service.IGenTableService;
import cn.universal.security.BaseController;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
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

/** 代码生成 操作处理 @Author ruoyi */
@RestController
@RequestMapping("/admin/tool/gen")
public class GenController extends BaseController {

  @Autowired private IGenTableService genTableService;

  @Autowired private IGenTableColumnService genTableColumnService;

  @GetMapping("/list")
  public TableDataInfo genList(GenTable genTable) {
    startPage();
    List<GenTable> list = genTableService.selectGenTableList(genTable);
    return getDataTable(list);
  }

  /** 修改代码生成业务 */
  @GetMapping(value = "/{talbleId}")
  public AjaxResult getInfo(@PathVariable Long talbleId) {
    GenTable table = genTableService.selectGenTableById(talbleId);
    List<GenTable> tables = genTableService.selectGenTableAll();
    List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(talbleId);
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("info", table);
    map.put("rows", list);
    map.put("tables", tables);
    return AjaxResult.success(map);
  }

  /** 查询数据库列表 */
  @GetMapping("/db/list")
  public TableDataInfo dataList(GenTable genTable) {
    startPage();
    List<GenTable> list = genTableService.selectDbTableList(genTable);
    return getDataTable(list);
  }

  /** 查询数据表字段列表 */
  @GetMapping(value = "/column/{talbleId}")
  public TableDataInfo columnList(Long tableId) {
    TableDataInfo dataInfo = new TableDataInfo();
    List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
    dataInfo.setRows(list);
    dataInfo.setTotal(list.size());
    return dataInfo;
  }

  /** 导入表结构（保存） */
  @PostMapping("/importTable")
  public AjaxResult importTableSave(String tables) {
    String[] tableNames = Convert.toStrArray(tables);
    // 查询表信息
    List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
    genTableService.importGenTable(tableList);
    return AjaxResult.success();
  }

  /** 修改保存代码生成业务 */
  @PutMapping
  public AjaxResult editSave(@Validated @RequestBody GenTable genTable) {
    genTableService.validateEdit(genTable);
    genTableService.updateGenTable(genTable);
    return AjaxResult.success();
  }

  /** 删除代码生成 */
  @DeleteMapping("/{tableIds}")
  public AjaxResult remove(@PathVariable Long[] tableIds) {
    genTableService.deleteGenTableByIds(tableIds);
    return AjaxResult.success();
  }

  /** 预览代码 */
  @GetMapping("/preview/{tableId}")
  public AjaxResult preview(@PathVariable("tableId") Long tableId) throws IOException {
    Map<String, String> dataMap = genTableService.previewCode(tableId);
    return AjaxResult.success(dataMap);
  }

  /** 生成代码（下载方式） */
  @GetMapping("/download/{tableName}")
  public void download(HttpServletResponse response, @PathVariable("tableName") String tableName)
      throws IOException {
    byte[] data = genTableService.downloadCode(tableName);
    genCode(response, data);
  }

  /** 生成代码（自定义路径） */
  @GetMapping("/genCode/{tableName}")
  public AjaxResult genCode(@PathVariable("tableName") String tableName) {
    genTableService.generatorCode(tableName);
    return AjaxResult.success();
  }

  /** 同步数据库 */
  @GetMapping("/synchDb/{tableName}")
  public AjaxResult synchDb(@PathVariable("tableName") String tableName) {
    genTableService.synchDb(tableName);
    return AjaxResult.success();
  }

  /** 批量生成代码 */
  @GetMapping("/batchGenCode")
  public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
    String[] tableNames = Convert.toStrArray(tables);
    byte[] data = genTableService.downloadCode(tableNames);
    genCode(response, data);
  }

  /** 生成zip文件 */
  private void genCode(HttpServletResponse response, byte[] data) throws IOException {
    response.reset();
    response.addHeader("Access-Control-Allow-Origin", "*");
    response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
    response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
    response.addHeader("Content-Length", "" + data.length);
    response.setContentType("application/octet-stream; charset=UTF-8");
    IOUtils.write(data, response.getOutputStream());
  }
}

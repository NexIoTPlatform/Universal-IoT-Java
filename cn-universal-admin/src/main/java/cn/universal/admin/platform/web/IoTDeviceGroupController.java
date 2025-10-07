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

package cn.universal.admin.platform.web;

import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.admin.platform.service.IIoTDeviceGroupService;
import cn.universal.security.BaseController;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.bo.IoTDeviceGroupBO;
import cn.universal.persistence.entity.vo.IoTDeviceGroupVO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备分组Controller
 *
 * @since 2025-12-30
 */
@RestController
@Tag(name = "设备分组管理", description = "设备分组管理")
@RequestMapping("/admin/v1/instance/group")
public class IoTDeviceGroupController extends BaseController {

  @Autowired private IIoTDeviceGroupService devGroupService;

  /** 查询设备分组列表 */
  @Operation(summary = "查询设备分组树")
  @GetMapping("/list")
  public AjaxResult<List<IoTDeviceGroupVO>> list() {
    List<IoTDeviceGroupVO> ioTDeviceGroupVO = devGroupService.selectDevGroupList();
    return AjaxResult.success(ioTDeviceGroupVO);
  }

  /** 获取设备分组详细信息 */
  @Operation(summary = "获取设备分组详细信息")
  @GetMapping(value = "/{id}")
  public AjaxResult<IoTDeviceGroupVO> getInfo(@PathVariable("id") Long id) {
    return AjaxResult.success(devGroupService.selectDevGroupById(id));
  }

  /** 新增设备分组 */
  @Operation(summary = "新增设备分组")
  @PostMapping
  @Log(title = "新增设备分组", businessType = BusinessType.INSERT)
  public AjaxResult<Void> add(@RequestBody IoTDeviceGroupBO devGroup) {
    return devGroupService.insertDevGroup(devGroup);
  }

  /** 修改设备分组 */
  @Operation(summary = "修改设备分组")
  @PutMapping
  @Log(title = "修改设备分组", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> edit(@RequestBody IoTDeviceGroupBO devGroup) {
    devGroupService.updateDevGroup(devGroup);
    return AjaxResult.success();
  }

  /** 删除设备分组 */
  @Operation(summary = "删除设备分组")
  @DeleteMapping("/{id}")
  @Log(title = "删除设备分组", businessType = BusinessType.DELETE)
  public AjaxResult<Void> remove(@PathVariable("id") Long id) {
    return devGroupService.deleteDevGroupById(id);
  }

  /** 批量绑定设备到分组 */
  @Operation(summary = "批量绑定设备到分组")
  @PostMapping("/binds")
  @Log(title = "批量绑定设备到分组", businessType = BusinessType.INSERT)
  public AjaxResult<Void> binds(@RequestBody IoTDeviceGroupBO devGroup) {
    return devGroupService.bindDevToGroup(devGroup);
  }

  /** 查询分组下的设备列表 */
  @GetMapping("/device/list")
  public TableDataInfo getDeviceList(String groupId) {
    startPage();
    List<IoTDevice> list = devGroupService.selectDevInstanceListByGroupId(groupId);
    return getDataTable(list);
  }

  /** 解除绑定 */
  @Operation(summary = "解除绑定")
  @Log(title = "解除绑定", businessType = BusinessType.OTHER)
  @DeleteMapping("/{groupId}/{devId}")
  public AjaxResult<Void> unBind(
      @PathVariable("groupId") String groupId, @PathVariable("devId") String[] devId) {
    return devGroupService.unBindDevByGroupId(groupId, devId);
  }
}

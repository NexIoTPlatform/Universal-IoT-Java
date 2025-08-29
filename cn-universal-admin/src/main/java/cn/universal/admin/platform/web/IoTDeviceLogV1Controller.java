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

import cn.universal.admin.platform.service.IIoTDevLogService;
import cn.universal.admin.system.web.BaseController;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.bo.IoTDeviceLogBO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.page.TableDataInfo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备日志Controller @Author ruoyi
 *
 * @since 2025-09-08
 */
// @Tag(name = "设备日志控制器", description = "设备日志管理")
@RestController
@RequestMapping("/admin/v1/device/log")
public class IoTDeviceLogV1Controller extends BaseController {

  @Autowired private IIoTDevLogService devLogService;

  /** 查询设备日志列表 */
  // @Operation(summary = "查询设备日志列表")
  @GetMapping("/list")
  public TableDataInfo<IoTDeviceLogVO> list(@Validated IoTDeviceLogBO bo) {
    startPage();
    List<IoTDeviceLogVO> ioTDeviceLogVOS = devLogService.queryPageList(bo);
    return getDataTable(ioTDeviceLogVOS);
  }

  /** 获取设备日志详细信息 */
  // @Operation(summary = "获取设备日志详细信息")
  //  @GetMapping("/id")
  //  public AjaxResult<IoTDeviceLogVO> getInfo(@NotNull(message = "主键不能为空") Long id) {
  //    return AjaxResult.success(devLogService.queryById(id));
  //  }

  /**
   * 获取设备事件的统计信息
   *
   * @param productId 产品Id
   * @param iotId 设备id
   */
  // @Operation(summary = "获取设备事件的统计信息")
  @GetMapping("/event/total/{productId}/{iotId}")
  public TableDataInfo<IoTDeviceEvents> getEventTotal(
      @PathVariable("productId") String productId, @PathVariable("iotId") String iotId) {
    List<IoTDeviceEvents> devLogVos = devLogService.queryEventTotal(productId, iotId);
    return getDataTable(devLogVos);
  }
}

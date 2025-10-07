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

import cn.universal.security.BaseController;
import cn.universal.dm.device.service.log.IIoTDeviceDataService;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.LogQuery;
import cn.universal.persistence.query.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** */
@RestController
@RequestMapping("/admin/v2/device/log")
public class IoTDeviceLogController extends BaseController {

  @Autowired private IIoTDeviceDataService iIoTDeviceDataService;

  /** 查询设备日志列表 */
  @GetMapping("/list/inner")
  public PageBean<IoTDeviceLogVO> listInner(@Validated LogQuery logQuery) {
    PageBean<IoTDeviceLogVO> devLogVoPageBean = iIoTDeviceDataService.pageList(logQuery);
    return devLogVoPageBean;
  }

  /** 属性消息 */
  @GetMapping("/meta/list")
  public PageBean<IoTDeviceLogMetadataVO> logMeta(LogQuery logQuery) {
    PageBean<IoTDeviceLogMetadataVO> devLogMetaVoPageBean =
        iIoTDeviceDataService.queryLogMeta(logQuery);
    return devLogMetaVoPageBean;
  }

  /**
   * 获取设备事件的统计信息
   *
   * @param productKey 产品key
   * @param iotId 设备id
   */
  @GetMapping("/event/total/{productKey}/{iotId}")
  public TableDataInfo<IoTDeviceEvents> getEventTotal(
      @PathVariable("productKey") String productKey, @PathVariable("iotId") String iotId) {
    PageBean<IoTDeviceEvents> devEventsPageBean =
        iIoTDeviceDataService.queryEventTotal(productKey, iotId);
    return getDataTable(
        devEventsPageBean.getList(), Integer.parseInt(devEventsPageBean.getTotalCount() + ""));
  }

  /** 查询设备日志列表 */
  @GetMapping("/list")
  public TableDataInfo<IoTDeviceLogVO> list(@Validated LogQuery logQuery) {
    PageBean<IoTDeviceLogVO> devLogVoPageBean = iIoTDeviceDataService.pageList(logQuery);
    return getDataTable(
        devLogVoPageBean.getList(), Integer.parseInt(devLogVoPageBean.getTotalCount() + ""));
  }
}

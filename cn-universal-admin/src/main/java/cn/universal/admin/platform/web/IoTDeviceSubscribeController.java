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

import cn.hutool.core.util.StrUtil;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.admin.system.service.IoTDeviceSubscribeService;
import cn.universal.security.BaseController;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备分组Controller
 *
 * @since 2025-12-30
 */
@RestController
@Tag(name = "网络组件", description = "mqtt,http..")
@RequestMapping("/admin/v1/subscribe")
public class IoTDeviceSubscribeController extends BaseController {

  @Autowired private IoTDeviceSubscribeService devSubscribeService;

  /** 查询订阅列表 */
  @GetMapping(value = "/list")
  @Log(title = "查询订阅列表", businessType = BusinessType.OTHER)
  public TableDataInfo list(IoTDeviceSubscribe sub) {
    startPage();
    if (StrUtil.isBlank(sub.getProductKey()) || StrUtil.isBlank(sub.getIotId())) {
      throw new IoTException("缺少必要参数");
    }
    List<IoTDeviceSubscribe> list = devSubscribeService.selectDevSubscribeList(sub);
    return getDataTable(list);
  }

  /** 查看订阅详情 */
  @GetMapping(value = "/{id}")
  @Log(title = "查看订阅详情", businessType = BusinessType.OTHER)
  public AjaxResult info(@PathVariable("id") String id) {
    return AjaxResult.success(devSubscribeService.selectDevInstanceById(id));
  }

  /** 修改订阅状态 */
  @PostMapping(value = "/update")
  @Log(title = "修改订阅状态", businessType = BusinessType.OTHER)
  public R updateSubscribe(@RequestBody IoTDeviceSubscribe sub) {
    return devSubscribeService.updateDevSubscribe(sub);
  }

  /** 新增订阅 */
  @PostMapping(value = "/add")
  @Log(title = "设备新增订阅", businessType = BusinessType.OTHER)
  public R bindSubscribe(@RequestBody IoTDeviceSubscribe sub) {
    return devSubscribeService.insertDevSubscribe(sub);
  }

  @PostMapping(value = "/delete/{productKey}/{iotId}")
  @Log(title = "设备删除订阅", businessType = BusinessType.OTHER)
  public R unBindSubscribe(
      @PathVariable("productKey") String productKey,
      @PathVariable("iotId") String iotId,
      @RequestBody Long[] ids) {
    return devSubscribeService.deleteDevSubscribe(productKey, iotId, ids);
  }
}

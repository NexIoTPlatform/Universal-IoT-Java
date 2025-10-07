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
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.platform.service.IIoTDeviceFunctionTaskService;
import cn.universal.admin.platform.service.IIoTDeviceService;
import cn.universal.admin.system.service.IIoTUserApplicationService;
import cn.universal.security.BaseController;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionHistoryBO;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionTaskBO;
import cn.universal.persistence.entity.vo.IoTDeviceFunctionHistoryVO;
import cn.universal.persistence.entity.vo.IoTDeviceFunctionTaskVO;
import cn.universal.persistence.entity.vo.IoTUserApplicationVO;
import cn.universal.persistence.page.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 批量指令 @Author ruoyi
 *
 * @since 2024-12-24
 */
@RestController
@Tag(name = "批量指令")
@RequestMapping("/admin/v1/function/batch")
public class IoTBatchFunsController extends BaseController {

  @Autowired private IIoTDeviceService iIotDeviceService;

  @Resource private IIoTDeviceFunctionTaskService iIoTDeviceFunctionTaskService;
  @Resource private IIoTUserApplicationService iotUserApplicationService;

  /** 查询设备列表 */
  @Operation(summary = "查询设备列表")
  @GetMapping("/dev/list")
  public TableDataInfo list(IoTDeviceBO ioTDeviceBO) {
    if (StrUtil.isBlank(ioTDeviceBO.getProductKey())
        && StrUtil.isBlank(ioTDeviceBO.getDeviceId())) {
      throw new IoTException("设备序列号或产品不能为空");
    }
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDevice> list = iIotDeviceService.selectDevInstanceListWithTags(ioTDeviceBO, iotUser);
    return getDataTable(list);
  }

  /** 查询任务列表 */
  @Operation(summary = "查询任务列表")
  @GetMapping("/task/list")
  public TableDataInfo taskList(IoTDeviceFunctionTaskBO bo) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDeviceFunctionTaskVO> list = iIoTDeviceFunctionTaskService.selectTaskList(bo, iotUser);
    return getDataTable(list);
  }

  /** 根据任务id查询下发历史列表 */
  @Operation(summary = "根据任务id查询下发历史列表")
  @GetMapping("/task/history")
  public TableDataInfo taskHistory(IoTDeviceFunctionHistoryBO bo) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDeviceFunctionHistoryVO> list =
        iIoTDeviceFunctionTaskService.queryFunctionListByTaskId(bo, iotUser);
    return getDataTable(list);
  }

  /** 新建批量下发任务 */
  @Operation(summary = "新建批量下发任务")
  @PostMapping("/task")
  public R addFunctionTask(@RequestBody IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    return R.toAjax(
        iIoTDeviceFunctionTaskService.addFunctionTask(ioTDeviceFunctionTaskBO, iotUser) ? 1 : 0);
  }

  /** 重新下发任务 */
  @Operation(summary = "重新下发任务")
  @PostMapping("/task/retry")
  public R retryFunctionTask(@RequestBody IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO) {
    return R.toAjax(
        iIoTDeviceFunctionTaskService.retryFunctionTask(ioTDeviceFunctionTaskBO) ? 1 : 0);
  }

  /** 设备型号类型列表 */
  @Operation(summary = "设备型号类型列表")
  @GetMapping("/product/list/{id}")
  public R<List<IoTProduct>> productList(@PathVariable("id") Long applicationId) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    List<IoTProduct> list =
        iIotDeviceService.selectProductListInBatchFunction(applicationId, iotUser);
    return R.ok(list);
  }

  /** 查询用户应用信息列表 */
  @Operation(summary = "查询用户应用列表")
  @GetMapping("/application/list")
  public R<List<IoTUserApplicationVO>> selectApplicationList(IoTUserApplication application) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    List<IoTUserApplicationVO> list =
        iotUserApplicationService.selectApplicationList(application, iotUser);
    List<IoTUserApplicationVO> result = new ArrayList<>();
    list.forEach(
        iotUserApplicationVo -> {
          IoTUserApplicationVO vo = new IoTUserApplicationVO();
          vo.setAppUniqueId(iotUserApplicationVo.getAppUniqueId());
          vo.setAppName(iotUserApplicationVo.getAppName());
          result.add(vo);
        });
    return R.ok(result);
  }
}

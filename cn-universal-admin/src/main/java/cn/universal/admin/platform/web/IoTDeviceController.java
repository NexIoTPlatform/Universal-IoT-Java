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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.annotation.Log;
import cn.universal.admin.common.enums.BusinessType;
import cn.universal.admin.common.utils.ExcelUtil;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.platform.service.IIoTDeviceService;
import cn.universal.admin.system.service.IIoTUserApplicationService;
import cn.universal.admin.system.service.IoTDeviceProtocolService;
import cn.universal.admin.system.web.BaseController;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTException;
import cn.universal.common.poi.ExcelTemplate;
import cn.universal.core.service.IUP;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.bo.IoTGwDeviceBO;
import cn.universal.persistence.entity.vo.GatewayDeviceVo;
import cn.universal.persistence.entity.vo.IoTDeviceCompanyVO;
import cn.universal.persistence.entity.vo.IoTDeviceModelVO;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 设备Controller @Author ruoyi
 *
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/admin/v1/device")
public class IoTDeviceController extends BaseController {

  @Autowired private IIoTDeviceService iIotDeviceService;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Resource private IoTDeviceProtocolService ioTDeviceProtocolService;

  @Resource private IIoTUserApplicationService iIotUserApplicationService;

  @Resource private IoTProductMapper ioTProductMapper;

  /** 查询设备列表 */
  @GetMapping("/list")
  public TableDataInfo list(IoTDevice ioTDevice) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDevice> list = iIotDeviceService.selectDevInstanceList(ioTDevice, iotUser);
    return getDataTable(list);
  }

  /** 查询分组未绑定设备列表 */
  @GetMapping("/list/{groupId}")
  public TableDataInfo list(@PathVariable("groupId") String groupId, IoTDevice ioTDevice) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDevice> list =
        iIotDeviceService.selectNotBindDevInstanceList(groupId, ioTDevice, iotUser);
    return getDataTable(list);
  }

  /** 查询未绑定的设备列表 */
  @GetMapping("/unlist")
  public TableDataInfo unlist(IoTDevice ioTDevice) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    startPage();
    List<IoTDevice> list = iIotDeviceService.selectDevInstanceUnList(ioTDevice, iotUser);
    return getDataTable(list);
  }

  /** 导出设备列表 */
  @PostMapping("/export")
  @Log(title = "导出设备列表", businessType = BusinessType.EXPORT)
  public void export(HttpServletResponse response, IoTDevice ioTDevice) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    List<IoTDevice> list = iIotDeviceService.selectDevInstanceForExport(ioTDevice, iotUser);
    ExcelUtil<IoTDevice> util = new ExcelUtil<IoTDevice>(IoTDevice.class);
    util.exportExcel(response, list, "设备数据");
  }

  @Operation(summary = "设备模板下载")
  @PostMapping("/import/template")
  public void importTemplate(
      @RequestParam() @Parameter(description = "产品编号", required = true) String productKey,
      HttpServletResponse response) {
    ExcelTemplate excelTemplate = iIotDeviceService.importTemplateExcel(productKey);
    excelTemplate.exportExcel(response);
  }

  /** 导入设备列表 */
  @PostMapping("/import")
  @Log(title = "设备批量导入", businessType = BusinessType.IMPORT)
  public AjaxResult<Void> importDevice(
      MultipartFile file,
      @RequestParam() @Parameter(description = "产品编号", required = true) String productKey,
      @RequestParam(required = false) @Parameter(description = "父设备deviceId", required = false)
          String deviceId)
      throws Exception {

    ExcelUtil<IoTDevice> util = new ExcelUtil<>(IoTDevice.class);
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(productKey);
    if (Objects.isNull(ioTProduct)) {
      return AjaxResult.error("没有找到产品");
    }
    // 每一列的标题，list集合，标题所在行数 - 1
    List<IoTDevice> devicesImport = util.importExcel(file.getInputStream(), 1);

    int num = iIotDeviceService.importDevice(devicesImport, ioTProduct, deviceId);
    return AjaxResult.success(String.format("成功导入%s条数据", num));
  }

  /** 获取设备详细信息 */
  @GetMapping(value = "/{id}")
  public AjaxResult getInfo(@PathVariable("id") String id) {

    IoTDevice ioTDevice = iIotDeviceService.selectDevInstanceById(id);
    //    if (ObjectUtil.isEmpty(ioTDevice)) {
    //      throw new IoTException("设备不存在");
    //    }
    //    IoTUser iotUser = checkParent(SecurityUtils.getUnionId());
    //    if (!iotUser.isAdmin() && !ioTDevice.getCreatorId()
    //        .equals(iotUser.getUnionId())) {
    //      throw new IoTException("你无权操作");
    //    }
    // 是否有编解码插件
    int count = ioTDeviceProtocolService.countProtocol(ioTDevice.getProductKey());
    IoTDeviceBO ioTDeviceBO = new IoTDeviceBO();
    BeanUtil.copyProperties(ioTDevice, ioTDeviceBO);
    // 查询网关产品信息
    if (StrUtil.isNotBlank(ioTDeviceBO.getGwProductKey())) {
      IoTDevice parentDevice =
          iIotDeviceService.selectDeviceByDeviceId(ioTDevice.getGwProductKey());
      if (ObjectUtil.isNotNull(parentDevice)) {
        ioTDeviceBO.setParentName(
            parentDevice.getDeviceId() + "(" + parentDevice.getDeviceName() + ")");
      }
    }
    if (StrUtil.isNotBlank(ioTDevice.getApplication())) {
      ioTDeviceBO.setApplication(
          iIotUserApplicationService.selectApplicationName(ioTDevice.getApplication()));
    }
    ioTDeviceBO.setDevGroupName(iIotDeviceService.selectDevInstanceGroupName(ioTDevice.getIotId()));
    ioTDeviceBO.setHasProtocol(count > 0);
    return AjaxResult.success(ioTDeviceBO);
  }

  /** 新增设备 */
  @PostMapping
  @Log(title = "新增设备", businessType = BusinessType.INSERT)
  public R add(@RequestBody IoTDeviceBO devInstancebo) {
    R result = iIotDeviceService.insertDevInstance(devInstancebo);
    JSONObject jsonObject = JSONUtil.parseObj(result);
    String code = jsonObject.getStr("code");
    String msg = jsonObject.getStr("msg");
    if ("0".equals(code)) {
      return R.ok();
    } else {
      return R.error(msg);
    }
  }

  /** 修改设备 */
  @PutMapping
  @Log(title = "修改设备", businessType = BusinessType.UPDATE)
  public R edit(@RequestBody IoTDeviceBO devInstancebo) {
    IoTDevice ioTDevice =
        iIotDeviceService.selectDevInstanceById(String.valueOf(devInstancebo.getId()));
    R result = iIotDeviceService.updateDevInstance(devInstancebo);
    JSONObject jsonObject = JSONUtil.parseObj(result);
    String code = jsonObject.getStr("code");
    String msg = jsonObject.getStr("msg");
    if ("0".equals(code)) {
      return R.ok();
    } else {
      return R.error(msg);
    }
  }

  /** 绑定设备平台 */
  @PutMapping(value = "/app")
  @Log(title = "绑定设备平台", businessType = BusinessType.OTHER)
  public AjaxResult bindApp(@RequestBody JSONObject object) {
    String ids = object.getStr("ids");
    String appUniqueId = object.getStr("appUniqueId");
    if (StrUtil.isBlank(appUniqueId)) {
      String id = ids.split(",")[0];
      IoTDevice ioTDevice = iIotDeviceService.selectDevInstanceById(id);
      appUniqueId = ioTDevice.getApplication();
    }
    IoTUserApplication application =
        iIotUserApplicationService.selectIotUserApplicationById(appUniqueId);
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (ObjectUtil.isEmpty(application)
        || (!iotUser.isAdmin() && !application.getUnionId().equals(iotUser.getUnionId()))) {
      throw new IoTException("你无权操作");
    }
    return toAjax(iIotDeviceService.bindApp(object.getStr("ids"), object.getStr("appUniqueId")));
  }

  /** 删除设备 */
  @DeleteMapping("/{ids}")
  @Log(title = "删除设备", businessType = BusinessType.DELETE)
  public R remove(@PathVariable String[] ids) {
    R result = iIotDeviceService.deleteDevInstanceByIds(ids);
    JSONObject jsonObject = JSONUtil.parseObj(result);
    String code = jsonObject.getStr("code");
    String msg = jsonObject.getStr("msg");
    if ("0".equals(code)) {
      return R.ok();
    } else {
      return R.error(msg);
    }
  }

  /** 获取所有设备型号 */
  @Operation(summary = "获取所有设备型号")
  @GetMapping("/models")
  public AjaxResult<List<IoTDeviceCompanyVO>> getModels(
      @RequestParam(required = false, defaultValue = "false")
          @Parameter(description = "只查询子设备", required = false)
          Boolean child,
      @Parameter(description = "支持应用", required = false) String apps) {
    List<IoTDeviceCompanyVO> ioTDeviceCompanyVOS =
        iIotDeviceService.queryAllBySupportChild(child, apps);
    return AjaxResult.success(ioTDeviceCompanyVOS);
  }

  /** 获取设备影子 */
  @Operation(summary = "获取设备影子")
  @GetMapping("/shadow/{id}")
  public AjaxResult<IoTDeviceShadow> getDeviceShadow(@PathVariable("id") String id) {
    IoTDeviceShadow deviceShadow = iIotDeviceService.getDeviceShadow(id);
    return AjaxResult.success(deviceShadow);
  }

  /** 下行加保存日志 */
  @RequestMapping("/down/{productKey}")
  @Operation(summary = "下行加保存日志")
  @Log(title = "下行加保存日志", businessType = BusinessType.OTHER)
  public R iotFunctionsDown(
      @PathVariable("productKey") String productKey, @RequestBody String downRequest) {
    return iIotDeviceService.iotFunctionsDown(productKey, downRequest);
  }

  /** 上行 */
  @RequestMapping("/up/{productKey}")
  @Log(title = "上行", businessType = BusinessType.OTHER)
  public R iotUp(@PathVariable("productKey") String productKey, @RequestBody String upMsg) {
    IoTProduct ioTProduct = iotProductDeviceService.getProduct(productKey);
    IUP doUp = IoTDownlFactory.getIUP(ioTProduct.getThirdPlatform());
    doUp.debugAsyncUP(upMsg);
    return R.ok();
  }

  /** 根据产品key查型号 */
  @Operation(summary = "获取产品型号")
  @GetMapping("/model/{productKey}")
  public AjaxResult<IoTDeviceModelVO> model(@PathVariable("productKey") String productKey) {
    IoTDeviceModelVO ioTDeviceModelVO = iIotDeviceService.getModelByProductKey(productKey);
    return AjaxResult.success(ioTDeviceModelVO);
  }

  /** 根据产品key查询匹配的网关设备 */
  @Operation(summary = "根据产品key查询匹配的网关设备")
  @GetMapping("/gateway/{productKey}")
  public AjaxResult<List<GatewayDeviceVo>> getGatewayDeviceList(
      @PathVariable("productKey") String productKey, IoTUser iotUser) {
    IoTGwDeviceBO bo = new IoTGwDeviceBO();
    bo.setProductKey(productKey);
    if (!iotUser.isAdmin()) {
      bo.setCreatorId(iotUser.getUnionId());
    }
    List<GatewayDeviceVo> gatewayDeviceVo = iIotDeviceService.selectGwIotDevices(bo);
    return AjaxResult.success(gatewayDeviceVo);
  }
}

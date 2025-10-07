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
import cn.hutool.core.lang.Validator;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.annotation.Log;
import cn.universal.common.enums.BusinessType;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.network.service.INetworkService;
import cn.universal.admin.platform.dto.ConnectionInfoDTO;
import cn.universal.admin.platform.service.ConnectionInfoService;
import cn.universal.admin.platform.service.IIoTDeviceService;
import cn.universal.admin.platform.service.IIoTProductService;
import cn.universal.admin.platform.service.impl.IoTProductServiceImpl;
import cn.universal.admin.system.service.ISysDictTypeService;
import cn.universal.admin.system.service.IoTDeviceProtocolService;
import cn.universal.security.BaseController;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.constant.IoTConstant.ProtocolModule;
import cn.universal.common.constant.IoTConstant.TcpFlushType;
import cn.universal.common.event.EventTopics;
import cn.universal.common.event.processer.EventPublisher;
import cn.universal.common.exception.IoTException;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.ossm.service.ISysOssService;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceFunction;
import cn.universal.persistence.entity.IoTDeviceProperties;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.admin.SysDictData;
import cn.universal.persistence.entity.bo.IoTProductBO;
import cn.universal.persistence.entity.bo.IoTProductImportBO;
import cn.universal.persistence.entity.vo.IoTDeviceModelVO;
import cn.universal.persistence.entity.vo.IoTProductExportVO;
import cn.universal.persistence.entity.vo.IoTProductVO;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.page.TableDataInfo;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.persistence.query.IoTProductQuery;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
 * 设备产品管理控制器
 *
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/admin/v1/product")
public class IoTProductController extends BaseController {

  @Autowired private IIoTProductService devProductService;
  @Autowired private IIoTDeviceService iIotDeviceService;
  @Autowired private ISysDictTypeService dictTypeService;
  @Resource private INetworkService iNetworkService;
  @Resource private IoTDeviceProtocolService ioTDeviceProtocolService;
  @Resource private IoTProductMapper ioTProductMapper;
  @Autowired private IoTProductServiceImpl ioTProductServiceImpl;
  @Autowired private EventPublisher eventPublisher;
  @Autowired private ISysOssService iSysOssService;
  @Autowired private ConnectionInfoService connectionInfoService;

  /** 查询设备产品列表 */
  @GetMapping("/list")
  public TableDataInfo list(IoTProductQuery query) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.viewAllProduct()) {
      query.setSelf(true);
    }
    if (iotUser.isAdmin()) {
      query.setSelf(false);
    }
    if (query.isSelf()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    if (query.isHasDevice()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    startPage();
    List<IoTProduct> list = devProductService.selectDevProductV2List(query);
    return getDataTable(list);
  }

  /** 获取产品详情通过 ProductKey */
  @GetMapping("/get/pro/{key}")
  public AjaxResult<IoTProduct> getProByKey(@PathVariable("key") String key) {
    return devProductService.selectIoTProductByKey(key);
  }

  /** 获取网关绑定的子产品列表 */
  @GetMapping("/gateway/subProducts/{key}")
  public AjaxResult<List<IoTProduct>> gatewaySubProducts(@PathVariable("key") String gwProductKey) {
    return devProductService.selectGatewaySubProductsByKey(gwProductKey);
  }

  /** 查询设备产品列表 */
  @GetMapping("/v2/list")
  public TableDataInfo v2List(IoTProductQuery query) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.viewAllProduct()) {
      query.setSelf(true);
    }
    if (iotUser.isAdmin()) {
      query.setSelf(false);
    }
    if (query.isSelf()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    if (query.isHasDevice()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    startPage();
    List<IoTProduct> list = devProductService.selectDevProductV2List(query);
    return getDataTable(list);
  }

  /** 查询设备产品列表 */
  @GetMapping("/v3/list")
  public TableDataInfo v3List(IoTProductQuery query) {
    //
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.viewAllProduct()) {
      query.setSelf(true);
    }
    if (iotUser.isAdmin()) {
      query.setSelf(false);
    }
    if (query.isSelf()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    if (query.isHasDevice()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    query.setState(0);
    List<IoTProductVO> list = devProductService.selectDevProductV3List(query);

    return getDataTable(list);
  }

  /** 查询设备产品列表 */
  @GetMapping("/v4/list")
  public TableDataInfo v4List(IoTProductQuery query) {
    //
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    if (!iotUser.viewAllProduct()) {
      query.setSelf(true);
    }
    if (iotUser.isAdmin()) {
      query.setSelf(false);
    }
    if (query.isSelf()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    if (query.isHasDevice()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    query.setState(0);
    List<IoTProductVO> list = devProductService.selectDevProductV4List(query);

    return getDataTable(list);
  }

  /** 查询所有设备产品列表 */
  @GetMapping("/all/list")
  public TableDataInfo v3List() {
    List<IoTProductVO> list = devProductService.selectDevProductAllList();
    return getDataTable(list);
  }

  /** 物模型新增内容 */
  @PostMapping("/metadata/add")
  @Log(title = "物模型新增内容", businessType = BusinessType.INSERT)
  public AjaxResult<Void> metadataAdd(@RequestBody IoTProductBO ioTProductBO) {
    return toAjax(devProductService.insertMetadata(ioTProductBO));
  }

  /** 物模型修改内容 */
  @PostMapping("/metadata/update")
  @Log(title = "物模型修改内容", businessType = BusinessType.UPDATE)
  public AjaxResult<Void> metadataUpdate(@RequestBody IoTProductBO ioTProductBO) {
    return toAjax(devProductService.updateMetadata(ioTProductBO));
  }

  /** 物模型删除内容 */
  @PostMapping("/metadata/delete")
  @Log(title = "物模型删除内容", businessType = BusinessType.DELETE)
  public AjaxResult<Void> metadataDelete(@RequestBody IoTProductBO ioTProductBO) {
    return toAjax(devProductService.deleteMetadata(ioTProductBO));
  }

  /** 物模型查询内容 */
  @PostMapping("/metadata/get")
  public AjaxResult<IoTProductBO> metadataGet(@RequestBody IoTProductBO ioTProductBO) {
    return AjaxResult.success(devProductService.getMetadata(ioTProductBO));
  }

  /** 获取设备产品详细信息 */
  @GetMapping(value = "/{id}")
  public AjaxResult getInfo(@PathVariable("id") String id) {
    return AjaxResult.success(devProductService.selectDevProductById(id));
  }

  /** 查询可绑定网关列表 */
  @Operation(summary = "查询可绑定网关产品列表")
  @GetMapping("/gateway/list")
  public AjaxResult<List<IoTProductVO>> getGatewayList() {
    List<IoTProductVO> list = devProductService.getGatewayList();
    return AjaxResult.success(list);
  }

  /** 查询可绑定网关子设备产品列表 */
  @Operation(summary = "查询可绑定网关子设备产品列表")
  @GetMapping("/sub/device/{productKey}")
  public AjaxResult<List<IoTProductVO>> getGatewaySubDeviceList(
      @PathVariable("productKey") String productKey) {
    List<IoTProductVO> list = devProductService.getGatewaySubDeviceList(productKey);
    return AjaxResult.success(list);
  }

  /** 查询电信公共产品列表 */
  @PostMapping(value = "/ctwing/pubpro")
  public AjaxResult getCtwingPubPro(@RequestBody String downRequest) {
    String creatorId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    JSONObject jsonObject = JSONUtil.parseObj(downRequest);
    JSONObject downProRequest = new JSONObject();
    Map<String, Object> data = new HashMap<>();
    data.put("searchValue", jsonObject.getStr("searchValue"));
    data.put("pageNow", jsonObject.getInt("pageNum"));
    data.put("pageSize", jsonObject.getInt("pageSize"));
    downProRequest.set("creatorId", creatorId);
    downProRequest.set("cmd", "PUBPRO_GET");
    downProRequest.set("data", data);
    return AjaxResult.success(
        IoTDownlFactory.safeInvokeDown(
            ProtocolModule.ctaiot.name(),
            IoTConstant.DOWN_TO_THIRD_PLATFORM,
            downProRequest.toString()));
  }

  /** 新增设备产品 */
  @PostMapping
  @Log(title = "新增产品", businessType = BusinessType.INSERT)
  public AjaxResult add(@RequestBody IoTProduct ioTProduct) {
    String unionId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    ioTProduct.setCreatorId(unionId);
    // 发布产品配置更新事件
    Map<String, Object> eventData = new HashMap<>();
    eventData.put("type", "product");
    eventData.put("productKey", ioTProduct.getProductKey());
    eventData.put("operation", "ADD");
    eventPublisher.publishEvent(EventTopics.PRODUCT_CONFIG_UPDATED, eventData);
    return toAjax(devProductService.insertDevProduct(ioTProduct));
  }

  /** 新增设备产品NB */
  @PostMapping(value = "/nb")
  @Log(title = "新增产品NB", businessType = BusinessType.INSERT)
  public AjaxResult addNb(@RequestBody String downRequest) {
    String creatorId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    JSONObject jsonObject = JSONUtil.parseObj(downRequest);
    IoTProduct ioTProduct = IoTProduct.builder().name(jsonObject.getStr("name")).build();
    List<IoTProduct> ioTProductList = devProductService.selectDevProductList(ioTProduct);
    if (!ioTProductList.isEmpty()) {
      throw new IoTException("产品名称已存在");
    }
    JSONObject downProRequest = new JSONObject();
    Map<String, Object> data = new HashMap<>();
    data.put("productName", jsonObject.getStr("name"));
    data.put("tupDeviceModel", jsonObject.getStr("productId"));
    data.put("tupIsThrough", jsonObject.getInt("tupIsThrough"));
    data.put("payloadFormat", jsonObject.getInt("payloadFormat"));
    data.put("powerModel", jsonObject.getInt("powerModel"));
    data.put("lwm2mEdrxTime", jsonObject.getStr("lwm2mEdrxTime"));

    downProRequest.set("creatorId", creatorId);
    downProRequest.set("cmd", "pro_add");
    downProRequest.set("companyNo", jsonObject.getStr("companyNo"));
    downProRequest.set("classifiedId", jsonObject.getStr("classifiedId"));
    downProRequest.set("classifiedName", jsonObject.getStr("classifiedName"));
    downProRequest.set("data", data);
    return AjaxResult.success(
        IoTDownlFactory.safeInvokeDown(
            ProtocolModule.ctaiot.name(),
            IoTConstant.DOWN_TO_THIRD_PLATFORM,
            downProRequest.toString()));
  }

  /** 新增电信公共产品 */
  @PostMapping(value = "/ctaiot/public/product")
  @Log(title = "新增电信公共产品", businessType = BusinessType.INSERT)
  public AjaxResult addCtAIotPublicProduct(@RequestBody String downRequest) {
    String creatorId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    JSONObject jsonObject = JSONUtil.parseObj(downRequest);
    JSONObject downProRequest = new JSONObject();
    Map<String, Object> data = new HashMap<>();
    Integer publicProductID = Integer.parseInt(jsonObject.getStr("publicProductID"));
    data.put("publicProductId", publicProductID);
    data.put("productId", jsonObject.getStr("productId"));
    downProRequest.set("creatorId", creatorId);
    downProRequest.set("classifiedId", jsonObject.getStr("classifiedId"));
    downProRequest.set("classifiedName", jsonObject.getStr("classifiedName"));
    downProRequest.set("companyNo", jsonObject.getStr("companyNo"));
    downProRequest.set("cmd", DownCmd.PUBPRO_ADD.name());
    downProRequest.set("data", data);
    return AjaxResult.success(
        IoTDownlFactory.safeInvokeDown(
            ProtocolModule.ctaiot.name(),
            IoTConstant.DOWN_TO_THIRD_PLATFORM,
            downProRequest.toString()));
  }

  /** 修改设备产品 */
  @PostMapping(value = "/updateNetworkUnionId")
  @Log(title = "修改网络组件", businessType = BusinessType.UPDATE)
  public AjaxResult editNetwork(@RequestBody IoTProductBO ioTProductBO) {
    if (ioTProductBO.getId() == null) {
      throw new IoTException("id不能为空");
    }
    //    if (StrUtil.isBlank(ioTProductBO.getNetworkUnionId())) {
    //      throw new IoTException("networkUnionId不能为空");
    //    }
    return toAjax(
        ioTProductServiceImpl.updateNetworkUnionId(
            ioTProductBO.getId(), ioTProductBO.getNetworkUnionId()));
  }

  /** 子设备绑定网关 */
  @Operation(summary = "子设备绑定网关")
  @PutMapping("/gateway")
  @Log(title = "子设备绑定网关", businessType = BusinessType.UPDATE)
  public AjaxResult editGateway(@RequestBody IoTProductBO ioTProductBO) {
    Long id = ioTProductBO.getId();
    String gwProductKey = ioTProductBO.getGwProductKey();
    logger.info("子设备产品绑定网关,id={},gwProductKey={}", id, gwProductKey);
    IoTProductVO productVO =
        devProductService.selectDevProductById(String.valueOf(ioTProductBO.getId()));
    if (productVO == null) {
      return AjaxResult.error("网关产品不存在");
    }
    productVO.setGwProductKey(gwProductKey);

    return toAjax(devProductService.updateDevProduct(productVO));
  }

  /** 修改设备产品 */
  @PutMapping
  @Log(title = "修改设备产品", businessType = BusinessType.UPDATE)
  public AjaxResult edit(@RequestBody IoTProductBO ioTProductBO) {
    if (ProtocolModule.ctaiot.name().equals(ioTProductBO.getThirdPlatform())) {
      IoTProduct pro = devProductService.selectDevProductById(ioTProductBO.getId().toString());
      if (pro == null) {
        throw new IoTException("没有该产品");
      }
      Map<String, Object> data = new HashMap<>();
      JSONObject downProRequest = new JSONObject();
      JSONObject jsonObject = JSONUtil.parseObj(pro.getConfiguration());
      Integer powerModel = ioTProductBO.getPowerModel();
      data.put("powerModel", ioTProductBO.getPowerModel());
      data.put("productName", ioTProductBO.getName());
      data.put("productId", jsonObject.getStr("productId"));
      data.put("classifiedId", ioTProductBO.getClassifiedId());
      if (powerModel == 3) {
        data.put("lwm2mEdrxTime", ioTProductBO.getLwm2mEdrxTime());
      }
      downProRequest.set("cmd", "pro_update");
      downProRequest.set("data", data);
      downProRequest.set("productKey", ioTProductBO.getProductKey());
      return AjaxResult.success(
          IoTDownlFactory.safeInvokeDown(
              ProtocolModule.ctaiot.name(),
              IoTConstant.DOWN_TO_THIRD_PLATFORM,
              downProRequest.toString()));
    }
    IoTProduct ioTProduct = BeanUtil.toBean(ioTProductBO, IoTProduct.class);
    int result = devProductService.updateDevProduct(ioTProduct);
    if (result > 0) {
      // 发布产品配置更新事件
      Map<String, Object> eventData = new HashMap<>();
      eventData.put("type", "product");
      eventData.put("productKey", ioTProduct.getProductKey());
      eventData.put("operation", "UPDATE");
      eventPublisher.publishEvent(EventTopics.PRODUCT_CONFIG_UPDATED, eventData);
    }
    return toAjax(result);
  }

  /** 修改产品配置信息 */
  @PutMapping("/config")
  @Log(title = "修改产品配置信息", businessType = BusinessType.UPDATE)
  public AjaxResult editConfig(@RequestBody IoTProductVO devProduct) {
    return toAjax(devProductService.updateDevProductConfig(devProduct));
  }

  /** 修改产品其他配置信息 */
  @PutMapping("/otherConfig")
  @Log(title = "修改产品其他配置信息", businessType = BusinessType.UPDATE)
  public AjaxResult editOtherConfig(@RequestBody String otherConfig) {
    return toAjax(devProductService.updateDevProductOtherConfig(otherConfig));
  }

  /** 修改产品存储策略 */
  @PutMapping("/storeConfig")
  @Log(title = "修改产品存储策略", businessType = BusinessType.UPDATE)
  public AjaxResult editOtherConfig(@RequestBody IoTProductVO devProduct) {
    return toAjax(devProductService.updateDevProductStoreConfig(devProduct));
  }

  /** 删除设备产品 */
  @DeleteMapping("/{ids}")
  @Log(title = "删除设备产品", businessType = BusinessType.DELETE)
  public AjaxResult remove(@PathVariable String[] ids) {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    for (String id : ids) {
      IoTProduct ioTProduct = devProductService.selectDevProductById(id);
      if (ioTProduct == null) {
        throw new IoTException("没有该产品");
      }
      if (!iotUser.isAdmin() && !ioTProduct.getCreatorId().equals(iotUser.getUnionId())) {
        throw new IoTException("没有产品权限");
      }
      IoTDevice ioTDevice = IoTDevice.builder().productKey(ioTProduct.getProductKey()).build();
      List<IoTDevice> ioTDeviceList =
          iIotDeviceService.selectDevInstanceList(
              ioTDevice, loginIoTUnionUser(SecurityUtils.getUnionId()));
      if (!ioTDeviceList.isEmpty()) {
        throw new IoTException("该产品下还有设备");
      }
      if (ProtocolModule.ctaiot.name().equals(ioTProduct.getThirdPlatform())) {
        Map<String, Object> data = new HashMap<>();
        JSONObject downProRequest = new JSONObject();
        JSONObject object = JSONUtil.parseObj(ioTProduct.getConfiguration());
        data.put("productId", object.getStr("productId"));
        data.put("masterKey", object.getStr("masterKey"));
        downProRequest.set("cmd", "pro_del");
        downProRequest.set("data", data);
        downProRequest.set("productKey", ioTProduct.getProductKey());

        return AjaxResult.success(
            IoTDownlFactory.safeInvokeDown(
                ProtocolModule.ctaiot.name(),
                IoTConstant.DOWN_TO_THIRD_PLATFORM,
                downProRequest.toString()));
      }
      // tcp产品删除时同时删除network表
      if (ProtocolModule.tcp.name().equals(ioTProduct.getThirdPlatform())) {
        // 停止监听
        devProductService.flushNettyServer(
            ioTProduct.getConfiguration(), ioTProduct.getProductKey(), TcpFlushType.close);
        iNetworkService.del(ioTProduct.getProductKey());
      }
      // 删除协议
      ioTDeviceProtocolService.deleteDevProtocolById(ioTProduct.getProductKey());

      // 发布产品删除事件
      Map<String, Object> eventData = new HashMap<>();
      eventData.put("type", "product");
      eventData.put("productKey", ioTProduct.getProductKey());
      eventData.put("operation", "DELETE");
      eventPublisher.publishEvent(EventTopics.PRODUCT_CONFIG_UPDATED, eventData);
    }
    return toAjax(devProductService.deleteDevProductByIds(ids));
  }

  /** 查询设备产品物模型属性列表 */
  @GetMapping("/properties/{id}")
  public AjaxResult getPropertiesList(@PathVariable("id") String id) {
    List<IoTDeviceProperties> list = devProductService.selectDevProperties(id);
    return AjaxResult.success(list);
  }

  /** 查询设备产品物模型属性列表(功能下发属性) */
  @GetMapping("/functionProperties/{id}")
  public AjaxResult getFunctionPropertiesList(@PathVariable("id") String id) {
    List<IoTDeviceProperties> list = devProductService.selectDevProperties(id);
    // 过滤可读写属性
    List<IoTDeviceProperties> dataRw =
        list.stream().filter((item) -> "rw".equals(item.getMode())).collect(Collectors.toList());
    // 获取设备公共字段
    List<SysDictData> data =
        dictTypeService.selectDictDataByType(IoTConstant.DEVICE_SHADOW_CUSTOMIZED_PROPERTY);
    // 根据公共字段过滤
    if (Validator.isNotNull(data)) {
      for (SysDictData datum : data) {
        for (int i = 0; i < dataRw.size(); i++) {
          if (dataRw.get(i).getId().equals(datum.getDictValue())) {
            dataRw.remove(i);
            break;
          }
        }
        dataRw.add(
            IoTDeviceProperties.builder()
                .description(datum.getRemark() + "(公共字段)")
                .id(datum.getDictValue())
                .type("string")
                .mode("r")
                .name(datum.getDictLabel())
                .build());
      }
    }
    return AjaxResult.success(dataRw);
  }

  /** 查询设备产品物模型事件列表 */
  @GetMapping("/events/{id}")
  public AjaxResult getEventsList(@PathVariable("id") String id) {
    List<IoTDeviceEvents> list = devProductService.selectDevEvents(id);
    return AjaxResult.success(list);
  }

  /** 查询设备产品物模型方法列表 */
  @GetMapping("/functions/{id}")
  public AjaxResult getFunctionsList(@PathVariable("id") String id) {
    List<IoTDeviceFunction> list = devProductService.selectDevFunctions(id);
    return AjaxResult.success(list);
  }

  /** 根据产品key查model配置 */
  @GetMapping("/model/{id}")
  public AjaxResult getmodel(@PathVariable("id") String id) {
    IoTDeviceModelVO ioTDeviceModelVO = devProductService.getModelByProductKey(id);
    return AjaxResult.success(ioTDeviceModelVO);
  }

  @GetMapping("/metadata/{devId}")
  public AjaxResult getMetaByDevId(@PathVariable("devId") String devId) {
    return AjaxResult.success(devProductService.selectMetadataByDevId(devId));
  }

  @GetMapping("/device/count")
  public AjaxResult countDevNumberByProductKey() {
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    Map<String, Integer> products =
        devProductService.countDevNumberByProductKey(
            iotUser.isAdmin() ? null : iotUser.getUnionId());
    return AjaxResult.success(products);
  }

  /** 导出设备产品列表 */
  @PostMapping("/export")
  @Log(title = "产品导出", businessType = BusinessType.EXPORT)
  public void export(HttpServletResponse response, IoTProductQuery query) {
    //
    IoTUser iotUser = loginIoTUnionUser(SecurityUtils.getUnionId());
    query.setSelf(!iotUser.isAdmin());
    if (query.isSelf()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    if (query.isHasDevice()) {
      query.setCreatorId(loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId());
    }
    List<IoTProductExportVO> list = devProductService.selectAllDevProductV2List(query);

    // 设置响应头为文件下载格式
    response.setContentType("application/octet-stream");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-Disposition", "attachment; filename=products_export.json");
    // 添加缓存控制头，防止浏览器缓存
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    // 强制下载，防止浏览器直接显示
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Download-Options", "noopen");
    // 确保响应不会被压缩
    response.setHeader("Content-Encoding", "identity");

    try {
      // 将数据转换为JSON格式并写入响应
      String jsonData = JSONUtil.toJsonPrettyStr(list);
      byte[] bytes = jsonData.getBytes("UTF-8");
      response.setContentLength(bytes.length);

      // 使用缓冲输出流确保数据完整写入
      try (java.io.OutputStream out = response.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }
    } catch (Exception e) {
      throw new IoTException("导出失败：" + e.getMessage());
    }
  }

  /** 批量导入产品 */
  @PostMapping("/import")
  @Log(title = "产品批量导入", businessType = BusinessType.IMPORT)
  @Transactional(rollbackFor = Exception.class)
  public AjaxResult<Void> importProduct(MultipartFile file) throws Exception {
    // 模板
    //
    String unionId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    try {
      // 读取JSON文件内容
      String jsonContent = new String(file.getBytes(), "UTF-8");

      // 解析JSON数据
      List<IoTProductImportBO> productImportBos =
          JSONUtil.toList(jsonContent, IoTProductImportBO.class);

      if (productImportBos == null || productImportBos.isEmpty()) {
        return AjaxResult.error("导入文件为空或格式不正确");
      }

      String result = devProductService.importProduct(productImportBos, unionId);
      return AjaxResult.success(result);
    } catch (Exception e) {
      return AjaxResult.error("导入失败：" + e.getMessage());
    }
  }

  /** 上传产品图片并保存到产品信息 */
  @Operation(summary = "上传产品图片")
  @PostMapping("/uploadImage")
  @Log(title = "上传产品图片", businessType = BusinessType.UPDATE)
  public AjaxResult<Map<String, String>> uploadProductImage(
      @RequestParam("id") String id, @RequestParam("file") MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IoTException("上传文件不能为空");
    }
    IoTProductVO product = devProductService.selectDevProductById(id);
    if (product == null) {
      throw new IoTException("产品不存在");
    }
    String unionId = loginIoTUnionUser(SecurityUtils.getUnionId()).getUnionId();
    // 上传到 OSS，沿用系统统一上传逻辑
    var oss = iSysOssService.upload(file, unionId);
    String url = oss.getUrl();

    // 更新产品 photoUrl 字段（保持前端期望结构）
    IoTProduct update = new IoTProduct();
    update.setId(product.getId());
    JSONObject photo = new JSONObject();
    photo.set("img", url);
    photo.set("detail", "");
    update.setPhotoUrl(photo.toString());
    devProductService.updateDevProduct(update);

    Map<String, String> data = new HashMap<>(2);
    data.put("url", url);
    String name = oss.getOriginalName();
    data.put("fileName", name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf(".")));
    return AjaxResult.success(data);
  }

  /** 下载导入模版 */
  @PostMapping("/import/template")
  public void downloadTemplate(HttpServletResponse response) {
    List<IoTProductImportBO> list = new ArrayList<>();
    // 设置响应头为文件下载格式
    response.setContentType("application/octet-stream");
    response.setCharacterEncoding("utf-8");
    response.setHeader("Content-Disposition", "attachment; filename=products_import_template.json");
    // 添加缓存控制头，防止浏览器缓存
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    // 强制下载，防止浏览器直接显示
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Download-Options", "noopen");
    // 确保响应不会被压缩
    response.setHeader("Content-Encoding", "identity");

    try {
      // 将数据转换为JSON格式并写入响应
      String jsonData = JSONUtil.toJsonPrettyStr(list);
      byte[] bytes = jsonData.getBytes("UTF-8");
      response.setContentLength(bytes.length);

      // 使用缓冲输出流确保数据完整写入
      try (java.io.OutputStream out = response.getOutputStream()) {
        out.write(bytes);
        out.flush();
      }
    } catch (Exception e) {
      throw new IoTException("模板下载失败：" + e.getMessage());
    }
  }

  /**
   * 产品模糊搜索接口
   *
   * @param query 搜索关键字
   * @return productKey和name列表
   */
  @GetMapping("/search")
  public TableDataInfo searchProductKeyAndName(IoTProductQuery query) {
    List<Map<String, String>> maps = ioTProductMapper.searchProductKeyAndName(query);
    return getDataTable(maps);
  }

  /** 绑定证书到产品 */
  @PostMapping("/bindCertificate")
  @Log(title = "绑定证书", businessType = BusinessType.UPDATE)
  public AjaxResult bindCertificate(@RequestParam String productKey, @RequestParam String sslKey) {
    int result = devProductService.bindCertificate(productKey, sslKey);
    return toAjax(result);
  }

  /** 解绑证书 */
  @PostMapping("/unbindCertificate")
  @Log(title = "解绑证书", businessType = BusinessType.UPDATE)
  public AjaxResult unbindCertificate(@RequestParam String productKey) {
    int result = devProductService.unbindCertificate(productKey);
    return toAjax(result);
  }

  /** 查看连接信息 */
  @PostMapping("/connect/info")
  public AjaxResult connectInfo(@RequestParam String productKey) {
    try {
      ConnectionInfoDTO connectionInfo = connectionInfoService.getConnectionInfo(productKey);
      return AjaxResult.success(connectionInfo);
    } catch (Exception e) {
      return AjaxResult.error("获取连接信息失败：" + e.getMessage());
    }
  }

  /** 查看MQTT密码 */
  @PostMapping("/connect/mqtt/password")
  @Log(title = "查看连接信息密码", businessType = BusinessType.GRANT)
  public AjaxResult connectInfoPassword(@RequestParam String productKey) {
    try {
      ConnectionInfoDTO passwordInfo = connectionInfoService.getMqttPasswordInfo(productKey);
      return AjaxResult.success(passwordInfo);
    } catch (Exception e) {
      return AjaxResult.error("获取密码信息失败：" + e.getMessage());
    }
  }
}

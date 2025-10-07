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

package cn.universal.admin.platform.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.platform.service.IIoTDeviceService;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.common.exception.BaseException;
import cn.universal.common.exception.IoTException;
import cn.universal.common.poi.ExcelTemplate;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTDeviceBO;
import cn.universal.persistence.entity.bo.IoTGwDeviceBO;
import cn.universal.persistence.entity.vo.GatewayDeviceVo;
import cn.universal.persistence.entity.vo.IoTDeviceCompanyVO;
import cn.universal.persistence.entity.vo.IoTDeviceCountVO;
import cn.universal.persistence.entity.vo.IoTDeviceModelVO;
import cn.universal.persistence.entity.vo.IoTDeviceVO;
import cn.universal.persistence.mapper.IoTDeviceFenceRelMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTDeviceProtocolMapper;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.mapper.IoTDeviceTagsMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 设备Service业务层处理
 *
 * @since 2025-12-24
 */
@Service
@Slf4j
public class IoTDeviceServiceImpl extends BaseServiceImpl implements IIoTDeviceService {

  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Resource private IoTProductMapper ioTProductMapper;

  @Resource private IoTDeviceProtocolMapper ioTDeviceProtocolMapper;

  @Resource private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Resource private IoTDeviceTagsMapper ioTDeviceTagsMapper;

  @Resource private IoTDeviceFenceRelMapper ioTDeviceFenceRelMapper;

  @Override
  @Cacheable(cacheNames = "iot_dev_tag_group_name", key = "#iotId")
  public List<String> selectDevInstanceGroupName(String iotId) {
    return ioTDeviceTagsMapper.selectDevGroupName(iotId);
  }

  /**
   * 查询设备
   *
   * @param id 设备主键
   * @return 设备
   */
  @Override
  public IoTDevice selectDevInstanceById(String id) {
    return checkSelf(id);
  }

  public IoTDevice checkSelf(String id) {
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    IoTDevice ioTDevice = ioTDeviceMapper.selectDevInstanceById(id);
    if (Objects.isNull(ioTDevice)) {
      throw new IoTException("该设备不存在", 400);
    }
    if (!iotUser.isAdmin() && !ioTDevice.getCreatorId().equals(iotUser.getUnionId())) {
      throw new IoTException("你无权操作");
    }

    if (ioTDevice.getCoordinate() != null) {
      ioTDevice.getParams().put("locationStr", ioTDevice.getCoordinate());
    }

    return ioTDevice;
  }

  /** 设备归属判断 */
  public String ascription(String creatorId) {
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    if (!creatorId.equals(iotUser.getUnionId()) && !iotUser.isAdmin()) {
      throw new IoTException("您没有权限操作此设备！");
    }
    return iotUser.getUnionId();
  }

  /**
   * 查询每个应用的设备数量
   *
   * @param appUniqueId 应用唯一标识
   */
  @Override
  public List<IoTDeviceCountVO> countDevByApplication(String appUniqueId) {
    return ioTDeviceMapper.countDevByApplication(appUniqueId);
  }

  /**
   * 查询设备列表
   *
   * @param ioTDevice 设备
   * @return 设备
   */
  @Override
  //  @DataScope(alias = "u", created = "d.creator_id")
  public List<IoTDevice> selectDevInstanceList(IoTDevice ioTDevice, IoTUser iotUser) {
    return ioTDeviceMapper.selectDevInstanceList(
        ioTDevice, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  @Override
  public List<IoTDevice> selectDevInstanceListByPlatform(
      IoTDevice ioTDevice, IoTUser iotUser, String thirdPlatform) {
    return ioTDeviceMapper.selectDevInstanceListByPlatform(
        ioTDevice, thirdPlatform, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  /**
   * 查询设备列表
   *
   * @param ioTDevice 设备
   * @return 设备
   */
  @Override
  //  @DataScope(alias = "u", created = "d.creator_id")
  public List<IoTDevice> selectDevInstanceUnList(IoTDevice ioTDevice, IoTUser iotUser) {
    return ioTDeviceMapper.selectDevInstanceUnList(ioTDevice, iotUser.getUnionId());
  }

  @Override
  public List<IoTDevice> selectDevInstanceForExport(IoTDevice ioTDevice, IoTUser iotUser) {
    return ioTDeviceMapper.selectDevInstanceForExport(
        ioTDevice, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  @Override
  public ExcelTemplate importTemplateExcel(String productKey) {
    // 根据produceKey获取产品
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(productKey);
    if (Objects.isNull(ioTProduct)) {
      throw new BaseException("没有找到产品");
    }
    JSONObject obj = JSONUtil.parseObj(ioTProduct.getThirdConfiguration());

    Map<String, String> headers = new LinkedMap<>();
    List<String> demoRow = new ArrayList<>();

    headers.put("deviceId", "设备序列号");
    demoRow.add("310000000000000");

    headers.put("deviceName", "设备名称");
    demoRow.add("模拟设备");

    headers.put("longitude", "经度");
    demoRow.add("120.291842");

    headers.put("latitude", "纬度");
    demoRow.add("30.448014");

    //    headers.put("coordinate", "设备坐标");
    //    demoRow.add("120.291842,30.448014");

    headers.put("detail", "备注");
    demoRow.add("请把此行删除");

    if (obj.containsKey("customField")) {
      JSONArray customFieldArray = obj.getJSONArray("customField");
      if (CollectionUtil.isNotEmpty(customFieldArray)) {
        // 获取其他属性
        for (int i = 1; i <= customFieldArray.size(); i++) {
          String customFieldValueOfId = getCustomFieldBykey(customFieldArray, "id", i - 1);
          headers.put(
              "otherParams." + customFieldValueOfId,
              obj.get("ext" + i + "Label").toString().replace("\"", ""));
        }
      }
    }
    if (ioTProduct.getDeviceNode().equals(DeviceNode.GATEWAY_SUB_DEVICE.name())) {
      headers.put("gwProductKey", "网关产品ProductKey");
      demoRow.add("a900c81a4d544849a2b92a02b3548f16");
    }
    return new ExcelTemplate(headers, demoRow, "设备导入");
  }

  @Override
  public List<GatewayDeviceVo> selectGwIotDevices(IoTGwDeviceBO ioTGwDeviceBO) {
    IoTProduct subDeviceProduct =
        ioTProductMapper.getProductByProductKey(ioTGwDeviceBO.getProductKey());
    if (ObjectUtil.isNull(subDeviceProduct)
        || StrUtil.isBlank(subDeviceProduct.getGwProductKey())) {
      throw new IoTException("请先完成网关绑定");
    }
    ioTGwDeviceBO.setGwProductKey(subDeviceProduct.getGwProductKey());
    return ioTDeviceMapper.getGatewayDeviceList(ioTGwDeviceBO);
  }

  @Override
  public IoTDevice selectIoTDevice(String productKey, String deviceId) {
    return ioTDeviceMapper.selectIoTDevice(productKey, deviceId);
  }

  @Override
  public List<IoTDevice> selectDevInstanceListWithTags(IoTDeviceBO ioTDeviceBO, IoTUser iotUser) {
    return ioTDeviceMapper.selectDevInstanceListWithTags(
        ioTDeviceBO, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  @Override
  public List<IoTProduct> selectProductListInBatchFunction(Long applicationId, IoTUser iotUser) {
    return ioTDeviceMapper.selectProductListInBatchFunction(
        applicationId, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  /**
   * @param jsonArray -> customField数组
   * @param index key: id 、 name 、description
   */
  public String getCustomFieldBykey(JSONArray jsonArray, String key, int index) {
    return JSONUtil.parseObj(jsonArray.get(index)).get(key).toString().replace("\"", "");
  }

  // 导入设备
  @Override
  public int importDevice(List<IoTDevice> devicesImport, IoTProduct ioTProduct, String deviceId) {
    validImportDevice(devicesImport);
    int successImportNum = 0;
    List<String> errorMsgs = new ArrayList<>();

    for (int i = 0; i < devicesImport.size(); i++) {
      try {
        IoTDevice instance = devicesImport.get(i);
        validImportDevice(instance, ioTProduct);
        IoTDeviceBO ioTDeviceBO = new IoTDeviceBO();
        BeanUtils.copyProperties(instance, ioTDeviceBO);
        ioTDeviceBO.setProductKey(ioTProduct.getProductKey());
        if (ioTDeviceBO.getOtherParams().containsKey("deviceKey")) {
          ioTDeviceBO.setSecretKey(ioTDeviceBO.getOtherParams().get("deviceKey").toString());
        }

        if (cn.universal.common.utils.StringUtils.isEmpty(ioTDeviceBO.getLatitude())) {
          ioTDeviceBO.setLatitude("44.61081");
        }
        if (cn.universal.common.utils.StringUtils.isEmpty(ioTDeviceBO.getLongitude())) {
          ioTDeviceBO.setLongitude("80.990372");
        }
        if (StrUtil.isNotBlank(deviceId)) {
          ioTDeviceBO.setGwProductKey(deviceId);
        }
        ioTDeviceBO.setCoordinate(ioTDeviceBO.getLongitude() + "," + ioTDeviceBO.getLatitude());
        R r = ((IIoTDeviceService) AopContext.currentProxy()).insertDevInstance(ioTDeviceBO);
        if (r.getMsg().equals("success") && r.getCode().equals(0)) {
          successImportNum++;
        } else {
          throw new BaseException(r.getMsg());
        }
      } catch (Exception e) {
        errorMsgs.add(String.format("第%s行导入报错,原因【%s】", i + 1, e.getMessage()));
      }
    }

    if (CollectionUtil.isNotEmpty(errorMsgs)) {
      throw new BaseException(StrUtil.join(",\n", errorMsgs));
    }
    return successImportNum;
  }

  private void validImportDevice(List<IoTDevice> devicesImport) {
    if (CollectionUtils.isEmpty(devicesImport)) {
      throw new BaseException("表格数据为空");
    }
    if (devicesImport.size() > 500) {
      throw new BaseException("超过一次最大导入数量500");
    }
  }

  private void validImportDevice(IoTDevice device, IoTProduct ioTProduct) {

    JSONObject jsonObject = JSONUtil.parseObj(ioTProduct.getThirdConfiguration());

    if (StringUtils.isEmpty(device.getDeviceName())) {
      throw new BaseException("设备名称不能为空");
    }
    if (StringUtils.isEmpty(device.getDeviceId())) {
      throw new BaseException("设备序列号不能为空");
    }
    if (jsonObject.containsKey("customField")) {
      // 获取其他属性
      JSONArray customFieldArray = jsonObject.getJSONArray("customField");
      if (CollectionUtil.isNotEmpty(customFieldArray)) {
        for (int j = 0; j < customFieldArray.size(); j++) {
          String customFieldValueOfId = getCustomFieldBykey(customFieldArray, "id", j);
          String customFieldValueOfName = getCustomFieldBykey(customFieldArray, "name", j);
          Object obj = device.getOtherParams().get(customFieldValueOfId);
          if (Objects.isNull(obj) || Objects.toString(obj).trim().isEmpty()) {
            throw new BaseException(customFieldValueOfName + "不能为空");
          }
        }
      }
    }
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list",
        "iot_product_device"
      },
      allEntries = true)
  public int bindApp(String ids, String appUniqueId) {
    String[] a = ids.split(",");
    return ioTDeviceMapper.bindApp(a, appUniqueId);
  }

  /**
   * 新增设备
   *
   * @param devInstancebo 设备
   * @return 结果
   */
  @Override
  public R insertDevInstance(IoTDeviceBO devInstancebo) {
    JSONObject downRequest = new JSONObject();

    // parentUnionId不为空时 认为该账号是子账号，所有操作的创建者均设为父账号
    downRequest.set("appUnionId", queryIotUser(SecurityUtils.getUnionId()).getUnionId());
    // 产品编号
    downRequest.set("productKey", devInstancebo.getProductKey());
    // 设备序列号
    downRequest.set("deviceId", devInstancebo.getDeviceId());
    // Lora密匙
    downRequest.set("deviceKey", devInstancebo.getSecretKey());
    // 添加
    downRequest.set("cmd", DownCmd.DEV_ADD.name());
    // 说明
    downRequest.set("detail", devInstancebo.getDetail());
    // 网关产品ProductKey
    downRequest.set("gwProductKey", devInstancebo.getGwProductKey());
    downRequest.set("gwDeviceId", devInstancebo.getGwDeviceId());
    downRequest.set("extDeviceId", devInstancebo.getExtDeviceId());
    JSONObject ob = new JSONObject();
    // 设备实例名称
    ob.set("deviceName", devInstancebo.getDeviceName());
    // 设备序列号
    ob.set("imei", devInstancebo.getDeviceId());
    // 维度
    ob.set("latitude", devInstancebo.getLatitude());
    // 经度
    ob.set("longitude", devInstancebo.getLongitude());
    // 添加额外参数
    for (Map.Entry<String, Object> entry : devInstancebo.getOtherParams().entrySet()) {
      ob.set(entry.getKey(), entry.getValue());
    }
    downRequest.set("data", ob);
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(devInstancebo.getProductKey());
    return IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
  }

  /**
   * 修改设备
   *
   * @param devInstancebo 设备
   * @return 结果
   */
  @Override
  public R updateDevInstance(IoTDeviceBO devInstancebo) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(devInstancebo.getProductKey())
            .deviceId(devInstancebo.getDeviceId())
            .build();
    String creatorId = ioTDeviceMapper.selectOne(ioTDevice).getCreatorId();
    String appUnionId = ascription(creatorId);
    JSONObject downRequest = new JSONObject();
    downRequest.set("appUnionId", appUnionId);
    downRequest.set("productKey", devInstancebo.getProductKey());
    downRequest.set("deviceId", devInstancebo.getDeviceId());
    downRequest.set("detail", devInstancebo.getDetail());
    downRequest.set("cmd", DownCmd.DEV_UPDATE.name());
    JSONObject ob = new JSONObject();
    ob.set("deviceName", devInstancebo.getDeviceName());
    ob.set("latitude", devInstancebo.getLatitude());
    ob.set("longitude", devInstancebo.getLongitude());
    downRequest.set("data", ob);
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(devInstancebo.getProductKey());
    return IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
  }

  /**
   * 批量删除设备
   *
   * @param ids 需要删除的设备主键
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list"},
      allEntries = true)
  public R deleteDevInstanceByIds(String[] ids) {
    R result = new R();
    for (String id : ids) {
      IoTDevice ioTDevice = ioTDeviceMapper.selectDevInstanceById(id);
      String appUnionId = ascription(ioTDevice.getCreatorId());
      JSONObject downRequest = new JSONObject();
      downRequest.set("appUnionId", appUnionId);
      downRequest.set("productKey", ioTDevice.getProductKey());
      downRequest.set("deviceId", ioTDevice.getDeviceId());
      downRequest.set("gwProductKey", ioTDevice.getGwProductKey());
      downRequest.set("cmd", DownCmd.DEV_DEL.name());
      JSONObject ob = new JSONObject();
      downRequest.set("data", ob);
      IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(ioTDevice.getProductKey());
      result = IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
      if (!result.getCode().equals(0)) {
        return R.error("设备名称: " + ioTDevice.getDeviceName() + " 删除失败，原因: " + result.getMsg());
      }
      ioTDeviceFenceRelMapper.deleteFenceInstance(ioTDevice.getIotId());
    }
    return R.ok("删除成功");
  }

  /**
   * 删除设备信息
   *
   * @param id 设备主键
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list"},
      allEntries = true)
  public R deleteDevInstanceById(String id) {
    IoTDevice ioTDevice = ioTDeviceMapper.selectDevInstanceById(id);
    String appUnionId = ascription(ioTDevice.getCreatorId());
    JSONObject downRequest = new JSONObject();
    downRequest.set("appUnionId", appUnionId);
    downRequest.set("productKey", ioTDevice.getProductKey());
    downRequest.set("deviceId", ioTDevice.getDeviceId());
    downRequest.set("cmd", DownCmd.DEV_DEL.name());
    JSONObject ob = new JSONObject();
    downRequest.set("data", ob);
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(ioTDevice.getProductKey());
    return IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
  }

  /**
   * 根据应用id查找设备
   *
   * @param ids 设备主键
   * @return 结果
   */
  @Override
  public int selectDevByAppUniqueId(String[] ids) {
    return ioTDeviceMapper.selectDevByAppUniqueId(ids);
  }

  /**
   * 查询所以设备型号
   *
   * @param supportChild 等于true 查询支持子设备的型号
   * @return 设备型号
   */
  @Override
  public List<IoTDeviceCompanyVO> queryAllBySupportChild(Boolean supportChild, String apps) {
    return ioTDeviceMapper.selectAllBySupportChild(supportChild, apps);
  }

  /**
   * 查询所以设备型号
   *
   * @return 设备影子
   */
  @Override
  public IoTDeviceShadow getDeviceShadow(String iotId) {
    return ioTDeviceShadowMapper.getDeviceShadow(iotId);
  }

  /** 设备下发 */
  @Override
  public R iotFunctionsDown(String productKey, String downRequest) {
    JSONObject jsonObject = JSONUtil.parseObj(downRequest);
    String deviceId = jsonObject.getStr("deviceId");
    IoTDevice ioTDeviceBo = ioTDeviceMapper.selectIoTDevice(productKey, deviceId);
    if (ioTDeviceBo == null) {
      return R.error("设备不存在");
    }
    if (ObjectUtil.isNotNull(ioTDeviceBo)) {
      ascription(ioTDeviceBo.getCreatorId());
    }

    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(productKey);
    R result = IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
    return result;
  }

  /** 根据产品key查型号配置 */
  @Override
  public IoTDeviceModelVO getModelByProductKey(String productKey) {
    return ioTDeviceMapper.getModelByProductKey(productKey);
  }

  @Override
  public List<IoTDevice> selectNotBindDevInstanceList(
      String groupId, IoTDevice ioTDevice, IoTUser iotUser) {
    return ioTDeviceMapper.selectNotBindDevInstanceList(
        groupId, ioTDevice, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  @Override
  public Page<IoTDeviceVO> selectFenceDevice(IoTDeviceBO ioTDeviceBO, int page, int size) {
    Page<IoTDeviceVO> p = PageHelper.startPage(page, size);
    List<IoTDeviceVO> ioTDeviceVOList = ioTDeviceMapper.selectFenceDevice(ioTDeviceBO);
    return p;
  }
}

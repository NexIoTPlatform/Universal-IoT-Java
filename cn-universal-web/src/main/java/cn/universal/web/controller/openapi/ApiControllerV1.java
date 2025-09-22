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

package cn.universal.web.controller.openapi;

import static cn.universal.common.constant.IoTConstant.DownCmd.DEV_FUNCTION;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTErrorCode;
import cn.universal.core.service.IUP;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.dm.device.entity.IoTDevicePropertiesBO;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTDeviceShadowService;
import cn.universal.dm.device.service.impl.IoTDeviceSubscribeService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.dm.device.service.log.IIoTDeviceDataService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.dto.IoTDeviceSubscribeBO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.entity.vo.IoTDeviceVO;
import cn.universal.persistence.query.IoTAPIQuery;
import cn.universal.persistence.query.LogQuery;
import cn.universal.persistence.query.PageBean;
import cn.universal.web.config.annotation.CodeKey;
import cn.universal.web.config.annotation.Codec;
import cn.universal.web.controller.common.BaseApiController;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0 @Author Aleo
 * @since 2023/3/25
 */
@RestController
@RequestMapping("/api/openapi/v1/iot")
@Slf4j(topic = "api_log")
public class ApiControllerV1 extends BaseApiController {

  @Resource private IoTDeviceService iotDeviceService;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private IoTDeviceShadowService iotDeviceShadowService;
  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Resource private IoTDeviceSubscribeService iotDeviceSubscribeService;

  @Resource private IIoTDeviceDataService iIoTDeviceDataService;

  @Resource(name = "httpUPService")
  private IUP httpUPService;

  /** 设备增加 */
  @PostMapping("/device/{productKey}/add")
  public R newIoTDevice(
      @PathVariable("productKey") String productKey, @RequestBody String downRequest) {
    log.info("V2设备添加设备，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, downRequest);
    String unionId = iotUnionId();
    JSONObject obj = JSONUtil.parseObj(downRequest);
    if (obj.isEmpty()) {
      return R.error(IoTErrorCode.DATA_CAN_NOT_NULL.getCode(), "参数为空");
    }
    if (!DownCmd.DEV_ADD.getValue().equalsIgnoreCase(obj.getStr("cmd", ""))) {
      return R.error(
          IoTErrorCode.DEV_DOWN_ADD_ERROR.getCode(), IoTErrorCode.DEV_DOWN_ADD_ERROR.getName());
    }
    if (StrUtil.isBlank(obj.getStr("deviceId"))) {
      return R.error(
          IoTErrorCode.DEV_UPDATE_DEVICE_NO_ID_EXIST.getCode(),
          IoTErrorCode.DEV_UPDATE_DEVICE_NO_ID_EXIST.getName());
    }
    // 防止参数呗篡改
    obj.set("appUnionId", unionId);
    obj.set("productKey", productKey);
    obj.set("applicationId", iotApplicationId());
    IoTProduct ioTProduct = iotProductDeviceService.getProduct(productKey);
    Boolean ignoreRepeat = obj.getBool("ignoreRepeat", false);
    // 允许同一设备重复添加。返回正确状态
    if (ignoreRepeat) {
      IoTDevice ioTDevice = iotDeviceService.selectDevInstance(productKey, obj.getStr("deviceId"));
      if (ioTDevice != null && unionId.equalsIgnoreCase(ioTDevice.getCreatorId())) {
        // 组件返回字段
        Map<String, Object> result = new HashMap<>();
        result.put("iotId", ioTDevice.getIotId());
        result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
        if (StrUtil.isNotBlank(ioTProduct.getMetadata())) {
          result.put("metadata", JSONUtil.parseObj(ioTProduct.getMetadata()));
        }
        result.put("productKey", productKey);
        result.put("deviceNode", ioTProduct.getDeviceNode());
        return R.ok(result);
      }
    }
    return IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(obj);
  }

  /** 设备批量增加 */
  @PostMapping("/device/{productKey}/batch/add")
  public R newIoTDevices(
      @PathVariable("productKey") String productKey, @RequestBody String downRequest) {
    log.info("V2设备批量添加设备，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, downRequest);
    JSONArray obj = JSONUtil.parseArray(downRequest);
    if (obj == null || obj.isEmpty()) {
      return R.error(IoTErrorCode.DATA_CAN_NOT_NULL.getCode(), "参数为空");
    }
    List<JSONObject> reasons = new ArrayList<>();
    for (Object arr : obj) {
      try {
        JSONObject data = (JSONObject) arr;
        R r = newIoTDevice(productKey, JSONUtil.toJsonStr(data));
        if (!R.SUCCESS.equals(r.getCode())) {
          JSONObject reason = new JSONObject();
          reason.set("deviceId", data.getStr("deviceId"));
          reason.set("reason", r.getMsg());
          reason.set("res", data);
          reasons.add(reason);
        }

      } catch (Exception e) {
        log.warn("设备添加失败={}", JSONUtil.toJsonStr(arr));
      }
    }
    return R.ok(reasons);
  }

  /** 设备上线 */
  @PutMapping("/online/{productKey}/{deviceId}")
  public R online(
      @PathVariable("productKey") String productKey, @PathVariable("deviceId") String deviceId) {
    log.info("V2设备online，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, deviceId);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevOrProductSelf(query);
    ioTDeviceLifeCycle.online(productKey, deviceId);
    return R.ok();
  }

  /** 设备删除 */
  @DeleteMapping("/device/del/{productKey}/{deviceId}")
  public R deleteIoTDevice(
      @PathVariable("productKey") String productKey, @PathVariable("deviceId") String deviceId) {
    log.info("V2设备刪除，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, deviceId);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(query);
    JSONObject param = new JSONObject();
    param.set("appUnionId", iotUnionId());
    param.set("productKey", ioTDeviceDTO.getProductKey());
    param.set("deviceId", ioTDeviceDTO.getDeviceId());
    param.set("cmd", DownCmd.DEV_DEL.getValue());
    param.set("data", new JSONObject());
    param.set("applicationId", iotApplicationId());
    IoTProduct ioTProduct = iotProductDeviceService.getProduct(ioTDeviceDTO.getProductKey());
    return IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(param);
  }

  /**
   * 设备修改（只修改名称，经纬度）
   *
   * <p>{ "deviceId": "24E124535B176069", "deviceName": "对外开放接口测试_改", "longitude":
   * "40.44801283677155", "latitude": "120.29184397901454" }
   */
  @PostMapping("/device/update/{productKey}/{deviceId}")
  public R updateIoTDevice(
      @PathVariable("productKey") String productKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody String downRequest) {
    log.info("V2设备修改，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, deviceId);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(query);
    IoTAPIQuery apiQuery = JSONUtil.toBean(downRequest, IoTAPIQuery.class);
    apiQuery.setIotUnionId(iotUnionId());
    apiQuery.setApplicationId(iotApplicationId());
    apiQuery.setIotId(ioTDeviceDTO.getIotId());
    Map<String, Object> objectMap = iotDeviceService.apiUpdateDevInfo(apiQuery);
    return R.ok(objectMap);
  }

  /** 设备查询,通过deviceId */
  @GetMapping("/device/info/{productKey}/{deviceId}")
  public R deviceInfo(
      @PathVariable("productKey") String productKey, @PathVariable("deviceId") String deviceId) {
    log.info("V2设备查询，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, deviceId);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceVO ioTDeviceVO = iotDeviceService.apiIoTDeviceVOInfo(query);
    if (ioTDeviceVO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(query);
    return R.ok(ioTDeviceVO);
  }

  /** 设备影子查询，设备状态数据查询 */
  @GetMapping(value = "/device/shadow/{productKey}/{deviceId}")
  public R shadow(
      @PathVariable("productKey") String productKey, @PathVariable("deviceId") String deviceId) {
    log.info("V2设备影子，用户={},productKey={} deviceId={} ", iotUnionId(), productKey, deviceId);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(ioTDeviceDTO.getIotId());
    List<IoTDevicePropertiesBO> propertiesBOS =
        iotDeviceShadowService.getDevState(ioTDeviceDTO.getIotId());
    return R.ok(propertiesBOS);
  }

  /** 属性上报 */
  @PostMapping("/device/report/properties/{productKey}/{deviceId}")
  public R reportProperties(
      @PathVariable("productKey") String productKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody JSONObject properties) {
    log.info("V2设备属性上报，用户={},deviceId={} data={} ", iotUnionId(), deviceId, properties);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    if (properties == null) {
      return R.error(IoTErrorCode.DATA_CAN_NOT_NULL.getCode(), "属性消息为空");
    }
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevOrProductSelf(query);
    properties.set("messageType", MessageType.PROPERTIES);
    properties.set("iotId", ioTDeviceDTO.getIotId());
    httpUPService.asyncUP(properties.toString());
    return R.ok();
  }

  /** 事件上报 */
  @PostMapping("/device/report/event/{productKey}/{deviceId}")
  public R reportEvent(
      @PathVariable("productKey") String productKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody JSONObject events) {
    log.info("V2设备事件上报，用户={},deviceId={} data={} ", iotUnionId(), deviceId, events);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    if (events == null) {
      events = new JSONObject();
    }
    checkDevOrProductSelf(query);
    events.set("messageType", MessageType.EVENT);
    events.set("iotId", ioTDeviceDTO.getIotId());
    httpUPService.asyncUP(events.toString());
    return R.ok();
  }

  /** 功能调用 */
  @PostMapping("/device/function/{productKey}/{deviceId}")
  public R actionFunction(
      @PathVariable("productKey") String productKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody JSONObject data) {
    log.info("设备功能调用，用户={},deviceId={} data={} ", iotUnionId(), deviceId, data);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    if (data == null) {
      data = new JSONObject();
    }
    checkDevOrProductSelf(query);
    JSONObject fuction = new JSONObject();
    fuction.set("function", data);
    fuction.set("messageType", MessageType.FUNCTIONS);
    fuction.set("iotId", ioTDeviceDTO.getIotId());
    fuction.put("appUnionId", iotUnionId());
    fuction.put("productKey", ioTDeviceDTO.getProductKey());
    fuction.put("deviceId", ioTDeviceDTO.getDeviceId());
    fuction.put("cmd", DEV_FUNCTION);
    fuction.put("applicationId", iotApplicationId());
    return IoTDownlFactory.getIDown(ioTDeviceDTO.getThirdPlatform()).doAction(fuction);
  }

  /** 通用数据上报 */
  @Codec
  @RequestMapping(value = "/device/report/codec/{productKey}/{deviceId}")
  public Object codecCommon(
      @CodeKey @PathVariable("productKey") String productKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody String msg) {
    log.info("收到来自: HTTP 云云消息 的消息，{}", msg);
    // 接收消息
    if (StrUtil.isNotBlank(msg)) {
      JSONObject json = JSONUtil.parseObj(msg);
      json.set("deviceId", deviceId);
      json.set("productKey", productKey);
      httpUPService.asyncUP(msg);
    }
    return R.ok();
  }

  /** 设备消息订阅 */
  @PostMapping("/device/subscribe/{iotId}")
  public R subscribe(@PathVariable("iotId") String iotId, @RequestBody IoTDeviceSubscribeBO sub) {
    String iotUnionId = iotUnionId();
    String applicationId = iotApplicationId();
    log.info("V2设备查询，用户={},iotId={}  ", iotUnionId, iotId);
    IoTAPIQuery query = IoTAPIQuery.builder().iotId(iotId).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(query);
    return iotDeviceSubscribeService.doSubscribe(
        ioTDeviceDTO.getIotId(), ioTDeviceDTO.getProductKey(), iotUnionId, applicationId, sub);
  }

  /** 设备消息订阅 */
  @PostMapping("/device/subscribe/{productKey}/{deviceId}")
  public R subscribe(
      @PathVariable("productKey") String productKey,
      @PathVariable("deviceId") String deviceId,
      @RequestBody IoTDeviceSubscribeBO sub) {
    String unionId = iotUnionId();
    String applicationId = iotApplicationId();
    log.info("V2设备订阅，用户={},productKey={} deviceId={} ", unionId, productKey, deviceId);
    IoTAPIQuery query = IoTAPIQuery.builder().deviceId(deviceId).productKey(productKey).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(query);
    return iotDeviceSubscribeService.doSubscribe(
        ioTDeviceDTO.getIotId(), productKey, unionId, applicationId, sub);
  }

  /** 属性消息 */
  @GetMapping("/device/log/meta/{iotId}/{messageType}/{property}")
  public R logMeta(
      @PathVariable("iotId") String iotId,
      @PathVariable("messageType") String messageType,
      @PathVariable("property") String property) {
    IoTAPIQuery query = IoTAPIQuery.builder().iotId(iotId).build();
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(BeanUtil.beanToMap(query));
    if (ioTDeviceDTO == null) {
      return R.error(IoTErrorCode.DEV_NOT_FIND.getCode(), IoTErrorCode.DEV_NOT_FIND.getName());
    }
    checkDevSelf(query);
    LogQuery logQuery =
        LogQuery.builder()
            .iotId(ioTDeviceDTO.getIotId())
            .messageType(messageType)
            .productKey(ioTDeviceDTO.getProductKey())
            .build();
    if (MessageType.PROPERTIES.equals(MessageType.find(messageType))) {
      logQuery.setProperty(property);
    } else if (MessageType.EVENT.equals(MessageType.find(messageType))) {
      logQuery.setEvent(property);
    }

    PageBean<IoTDeviceLogMetadataVO> devLogMetaVoPageBean =
        iIoTDeviceDataService.queryLogMeta(logQuery);
    return R.ok(devLogMetaVoPageBean.getList());
  }
}

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

package cn.universal.http.protocol.handle;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DeviceStatus;
import cn.universal.common.constant.IoTConstant.ERROR_CODE;
import cn.universal.common.domain.R;
import cn.universal.common.utils.DelayedTaskUtil;
import cn.universal.common.utils.ThreadLocalCache;
import cn.universal.core.message.DownRequest;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.http.protocol.entity.HttpDownRequest;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.base.IoTDownAdapter;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * http下行实际处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2023/02/24 11:19
 */
@Slf4j
@Component
public class HttpDownHandle extends IoTDownAdapter<HttpDownRequest> {

  @Resource private DelayedTaskUtil delayedTaskUtil;
  @Resource private IoTDeviceService iotDeviceService;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private SupportMapAreasMapper supportMapAreasMapper;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  public R httpDown(HttpDownRequest httpDownRequest) {
    if (httpDownRequest == null || httpDownRequest.getCmd() == null) {
      log.warn("[HTTP下行][参数异常] 下行对象为空,不处理 downRequest={}", httpDownRequest);
      return R.error("http 下行对象为空");
    }
    log.info(
        "[HTTP下行][命令分发] deviceId={} productKey={} cmd={}",
        httpDownRequest.getDeviceId(),
        httpDownRequest.getProductKey(),
        httpDownRequest.getCmd().getValue());
    R preR =
        beforeDownAction(
            httpDownRequest.getIoTProduct(), httpDownRequest.getData(), httpDownRequest);
    if (Objects.nonNull(preR)) {
      return preR;
    }
    R r = null;
    switch (httpDownRequest.getCmd()) {
      case DEV_ADD:
        r = newIoTDevice(httpDownRequest);
        break;
      case DEV_DEL:
        r = deleteIoTDevice(httpDownRequest);
        break;
      case DEV_UPDATE:
        r = updateIoTDevice(httpDownRequest);
        break;
      case DEV_FUNCTION:
        r = funIoTDevice(httpDownRequest);
        break;
      default:
        log.info(
            "[HTTP下行][未匹配到方法] deviceId={} productKey={} cmd={}",
            httpDownRequest.getDeviceId(),
            httpDownRequest.getProductKey(),
            httpDownRequest.getCmd() != null ? httpDownRequest.getCmd().getValue() : null);
    }
    return r;
  }

  private R newIoTDevice(HttpDownRequest downRequest) {
    /** 当编解码请求结果为空 或者 编解码请求结果不为空且状态码等于0时，添加设备到本地 */
    if (StrUtil.isBlank(downRequest.getDownResult())
        || "0".equals(String.valueOf(JSONUtil.parseObj(downRequest.getDownResult()).get("code")))) {
      log.info(
          "添加HTTP设备,deviceId={},productKey={}",
          downRequest.getDeviceId(),
          downRequest.getProductKey());
      IoTDevice ioTDevice =
          IoTDevice.builder()
              .productKey(downRequest.getProductKey())
              .deviceId(downRequest.getDeviceId())
              .build();
      int size = ioTDeviceMapper.selectCount(ioTDevice);
      if (size > 0) {
        // 设备已经存在
        return R.error(
            ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getCode(),
            ERROR_CODE.DEV_ADD_DEVICE_ID_EXIST.getName());
      }
      // 操作数据库
      Map<String, Object> saveResult = saveIoTDevice(downRequest);
      return R.ok(saveResult);
    } else {
      // 添加失败
      return R.error(
          ERROR_CODE.DEV_ADD_ERROR.getCode(),
          (String) JSONUtil.parseObj(downRequest.getDownResult()).get("msg"));
    }
  }

  /** */
  private R funIoTDevice(HttpDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .productKey(downRequest.getProductKey())
            .deviceId(downRequest.getDeviceId())
            .build();
    IoTDevice instance = ioTDeviceMapper.selectOne(ioTDevice);
    if (instance == null) {
      // 设备不存在
      return R.error(
          ERROR_CODE.DEV_CONFIG_DEVICE_NO_ID_EXIST.getCode(),
          ERROR_CODE.DEV_CONFIG_DEVICE_NO_ID_EXIST.getName());
    }

    R r = callGlobalFunction(downRequest.getIoTProduct(), instance, downRequest);
    if (Objects.nonNull(r)) {
      return r;
    }

    Map<String, Object> function = downRequest.getFunction();
    String requestId = MDC.get(IoTConstant.TRACE_ID);
    function.put("deviceId", downRequest.getDeviceId());
    function.put("requestId", requestId);
    function.put("productKey", downRequest.getProductKey());
    IoTDeviceDTO ioTDeviceDTO =
        iotDeviceService.selectDevInstanceBO(
            downRequest.getProductKey(), downRequest.getDeviceId());
    ioTDeviceLifeCycle.command(ioTDeviceDTO, requestId, function);
    return R.ok();
  }

  private Map<String, Object> saveIoTDevice(HttpDownRequest downRequest) {
    IoTDevice ioTDevice =
        IoTDevice.builder()
            .deviceId(downRequest.getDeviceId())
            .createTime(System.currentTimeMillis() / 1000)
            .deviceName(downRequest.getData().getStr("deviceName", downRequest.getDeviceId()))
            .state(DeviceStatus.offline.getCode())
            .iotId(downRequest.getProductKey()+downRequest.getDeviceId())
            .application(downRequest.getApplicationId())
            .creatorId(downRequest.getAppUnionId())
            .productName(downRequest.getIoTProduct().getName())
            .detail(downRequest.getDetail())
            .gwProductKey(downRequest.getGwProductKey())
            .extDeviceId(
                downRequest.getData().getStr("extDeviceId") == null
                    ? downRequest.getGwDeviceId()
                    : downRequest.getData().getStr("extDeviceId"))
            .productKey(downRequest.getProductKey())
            .build();

    if (StrUtil.isNotBlank(downRequest.getData().getStr("latitude"))
        && StrUtil.isNotBlank(downRequest.getData().getStr("longitude"))) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getData().getStr("longitude"),
              downRequest.getData().getStr("latitude")));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getData().getStr("longitude"), downRequest.getData().getStr("latitude"));
      if (supportMapAreas == null) {
        log.info(
            "[HTTP下行][区域查询] 查询区域id为空,lon={},lat={}",
            downRequest.getData().getStr("longitude"),
            downRequest.getData().getStr("latitude"));
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }
    Map<String, Object> config = new HashMap<>();
    finalDown(config, downRequest.getIoTProduct(), downRequest.getCmd(), downRequest.getData());
    ioTDevice.setConfiguration(JSONUtil.toJsonStr(config));
    if (StrUtil.isNotBlank(ThreadLocalCache.localExtDeviceId.get())) {
      ioTDevice.setExtDeviceId(ThreadLocalCache.localExtDeviceId.get());
      ThreadLocalCache.localExtDeviceId.remove();
    }
    ioTDeviceMapper.insertUseGeneratedKeys(ioTDevice);
    // 推送设备创建消息
    ioTDeviceLifeCycle.create(downRequest.getProductKey(), downRequest.getDeviceId(), downRequest);

    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("iotId", ioTDevice.getIotId());
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    if (StrUtil.isNotBlank(downRequest.getIoTProduct().getMetadata())) {
      result.put("metadata", JSONUtil.parseObj(downRequest.getIoTProduct().getMetadata()));
    }
    result.put("productKey", downRequest.getProductKey());
    result.put("deviceNode", downRequest.getIoTProduct().getDeviceNode());
    return result;
  }

  /** */
  private R deleteIoTDevice(HttpDownRequest downRequest) {
    /** 当编解码请求结果为空 或者 编解码请求结果不为空且状态码等于0时，删除本地设备 */
    if (StrUtil.isBlank(downRequest.getDownResult())
        || "0".equals(String.valueOf(JSONUtil.parseObj(downRequest.getDownResult()).get("code")))) {
      IoTDevice ioTDevice =
          IoTDevice.builder()
              .productKey(downRequest.getProductKey())
              .deviceId(downRequest.getDeviceId())
              .build();
      IoTDevice instance = ioTDeviceMapper.selectOne(ioTDevice);
      if (instance == null) {
        // 设备不存在
        return R.error(
            ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getCode(),
            ERROR_CODE.DEV_DEL_DEVICE_NO_ID_EXIST.getName());
      }
      // 操作数据库
      deleteDevInstance(instance, downRequest);
      return R.ok("删除成功");
    } else {
      // 添加失败
      return R.error(
          ERROR_CODE.DEV_DEL_ERROR.getCode(),
          (String) JSONUtil.parseObj(downRequest.getDownResult()).get("msg"));
    }
  }

  /** */
  private void deleteDevInstance(IoTDevice ioTDevice, DownRequest downRequest) {
    Map<String, Object> objectMap = new HashMap<>();
    objectMap.put("iotId", ioTDevice.getIotId());
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(objectMap);
    // 设备生命周期-删除
    ioTDeviceLifeCycle.delete(ioTDeviceDTO, downRequest);
    iotDeviceService.delDevInstance(ioTDevice.getIotId());
  }

  /** */
  private R updateIoTDevice(HttpDownRequest downRequest) {
    /** 当编解码请求结果为空 或者 编解码请求结果不为空且状态码等于0时，更新本地设备 */
    if (StrUtil.isBlank(downRequest.getDownResult())
        || "0".equals(String.valueOf(JSONUtil.parseObj(downRequest.getDownResult()).get("code")))) {
      IoTDevice ioTDevice =
          IoTDevice.builder()
              .productKey(downRequest.getProductKey())
              .deviceId(downRequest.getDeviceId())
              .build();
      int size = ioTDeviceMapper.selectCount(ioTDevice);
      if (size == 0) {
        // 设备不存在
        return R.error(
            ERROR_CODE.DEV_UPDATE_DEVICE_NO_ID_EXIST.getCode(),
            ERROR_CODE.DEV_UPDATE_DEVICE_NO_ID_EXIST.getName());
      }
      // 访问数据库获取设备信息
      IoTDevice dev = ioTDeviceMapper.selectOne(ioTDevice);
      // 修改设备名称
      dev.setDeviceName(downRequest.getData().getStr("deviceName"));
      // 更新设备名称
      Map<String, Object> updateResult = updateDevInstance(dev, downRequest);
      // 设备生命周期-修改
      ioTDeviceLifeCycle.update(dev.getIotId());
      return R.ok(updateResult);
    } else {
      // 添加失败
      return R.error(
          ERROR_CODE.DEV_UPDATE_ERROR.getCode(),
          (String) JSONUtil.parseObj(downRequest.getDownResult()).get("msg"));
    }
  }

  /** */
  private Map<String, Object> updateDevInstance(IoTDevice ioTDevice, HttpDownRequest downRequest) {
    if (StrUtil.isNotBlank(downRequest.getData().getStr("latitude"))
        && StrUtil.isNotBlank(downRequest.getData().getStr("longitude"))) {
      ioTDevice.setCoordinate(
          StrUtil.join(
              ",",
              downRequest.getData().getStr("longitude"),
              downRequest.getData().getStr("latitude")));

      SupportMapAreas supportMapAreas =
          supportMapAreasMapper.selectMapAreas(
              downRequest.getData().getStr("longitude"), downRequest.getData().getStr("latitude"));
      if (supportMapAreas == null) {
        log.info(
            "查询区域id为空,lot={},lat={}",
            downRequest.getData().getStr("longitude"),
            downRequest.getData().getStr("latitude"));
      } else {
        ioTDevice.setAreasId(supportMapAreas.getId());
      }
    }

    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("deviceId", ioTDevice.getDeviceId());
    result.put("areasId", ioTDevice.getAreasId() == null ? "" : ioTDevice.getAreasId());
    ioTDevice.setDetail(downRequest.getDetail());
    ioTDeviceMapper.updateByPrimaryKey(ioTDevice);

    return result;
  }
}

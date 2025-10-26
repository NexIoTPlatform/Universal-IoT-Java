/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.dm.device.service.wrapper;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.dm.device.constant.DeviceManagerConstant;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * 设备上行消息单独处理
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2023/9/21
 */
@Service("ioTDeviceUPIntercept")
@Slf4j
public class IoTDeviceUPIntercept {

  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Resource private SupportMapAreasMapper supportMapAreasMapper;

  @Async
  public void messageProcess(BaseUPRequest upRequest) {
    if (upRequest == null || upRequest.getMessageType() == null) {
      return;
    }
    MessageType messageType = upRequest.getMessageType();
    switch (messageType) {
      case PROPERTIES:
        properties(upRequest);
        break;
      case EVENT:
        event(upRequest);
        break;
      default:
        break;
    }
  }

  private void event(BaseUPRequest baseUPRequest) {
    try {
    } catch (Exception e) {
      log.error("device UP messageProcess [event] error={}", ExceptionUtil.getRootCauseMessage(e));
    }
  }

  private void properties(BaseUPRequest baseUPRequest) {
    Map<String, Object> properties = baseUPRequest.getProperties();
    if (MapUtil.isEmpty(properties)) {
      return;
    }
    try {
      propertiesGeoPoint(baseUPRequest, properties);
      propertiesICCID(baseUPRequest, properties);
    } catch (Exception e) {
      log.error(
          "device UP messageProcess [properties] error={}", ExceptionUtil.getRootCauseMessage(e));
    }
  }

  /** 处理定位设备经纬度 */
  private void propertiesGeoPoint(BaseUPRequest baseUPRequest, Map<String, Object> properties) {
    JSONObject cfg = baseUPRequest.getIoTDeviceDTO().getProductConfig();
    if (cfg != null
        && cfg.containsKey(IoTConstant.IS_GPS_PRODUCT)
        && cfg.getBool(IoTConstant.IS_GPS_PRODUCT)
        && properties.containsKey(DeviceManagerConstant.COORDINATE)) {
      String geoPoint = (String) properties.get(DeviceManagerConstant.COORDINATE);
      log.info("处理定位类经纬度数据处理，deviceId={},coordinate={}", baseUPRequest.getDeviceId(), geoPoint);
      if (StrUtil.isNotBlank(geoPoint)) {
        IoTDevice ioTDevice = new IoTDevice();
        ioTDevice.setIotId(baseUPRequest.getIotId());
        ioTDevice.setCoordinate(geoPoint);
        String[] coors = geoPoint.split(",");
        SupportMapAreas supportMapAreas = supportMapAreasMapper.selectMapAreas(coors[0], coors[1]);
        if (supportMapAreas == null) {
          log.info("查询区域id为空,lot={},lat={}", coors[0], coors[1]);
        } else {
          ioTDevice.setAreasId(supportMapAreas.getId());
        }
        Example example = new Example(IoTDevice.class);
        example.createCriteria().andEqualTo("iotId", baseUPRequest.getIotId());
        ioTDeviceMapper.updateByExampleSelective(ioTDevice, example);
      }
    }
  }

  /** 保存设备解析出来的iccid到设备表 */
  private void propertiesICCID(BaseUPRequest baseUPRequest, Map<String, Object> properties) {
    if (properties.containsKey("iccid") || properties.containsKey("ICCID")) {
      String iccid = (String) properties.get("iccid");
      if (StrUtil.isBlank(iccid)) {
        iccid = (String) properties.get("ICCID");
      }
      if (StrUtil.isBlank(iccid)) {
        return;
      }
      Example example = new Example(IoTDevice.class);
      example.createCriteria().andEqualTo("iotId", baseUPRequest.getIotId());
      IoTDevice ioTDevice = ioTDeviceMapper.selectOneByExample(example);
      JSONObject object = new JSONObject();
      if (StrUtil.isNotBlank(ioTDevice.getConfiguration())) {
        object = JSONUtil.parseObj(ioTDevice.getConfiguration());
      }
      object.set("iccid", iccid);
      ioTDevice.setConfiguration(JSONUtil.toJsonStr(object));
      ioTDeviceMapper.updateDevInstance(ioTDevice);
    }
  }
}

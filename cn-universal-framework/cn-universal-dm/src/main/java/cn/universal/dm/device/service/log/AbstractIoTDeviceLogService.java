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

package cn.universal.dm.device.service.log;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.UPRequest;
import cn.universal.core.metadata.DeviceMetadata;
import cn.universal.dm.device.constant.DeviceManagerConstant;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.IoTDeviceLogMetadata;
import cn.universal.persistence.entity.IoTDeviceLogMetadata.IoTDeviceLogMetadataBuilder;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTProductMapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/9/23
 */
public abstract class AbstractIoTDeviceLogService implements IIoTDeviceLogService {

  @Resource private IoTProductMapper ioTProductMapper;
  @Resource IoTProductDeviceService iotProductDeviceService;

  DeviceMetadata getDeviceMetadata(String metadata) {
    if (StrUtil.isBlank(metadata)) {
      return new DeviceMetadata(new JSONObject());
    }
    return new DeviceMetadata(new JSONObject(metadata));
  }

  IoTDeviceLogMetadataBuilder builder(UPRequest up) {
    final IoTDeviceLogMetadataBuilder ioTDeviceLogMetadataBuilder =
        IoTDeviceLogMetadata.builder()
            .createTime(LocalDateTime.now())
            .deviceId(up.getDeviceId())
            .deviceName(StrUtil.sub(up.getDeviceName(), 0, 30))
            .ext1(JSONUtil.toJsonStr(up.getData()))
            .iotId(up.getIotId())
            .productKey(up.getProductKey())
            .messageType(up.getMessageType().name());
    return ioTDeviceLogMetadataBuilder;
  }

  IoTDeviceLog build(BaseUPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    IoTDeviceLog log =
        IoTDeviceLog.builder()
            .content(peopertiesOrEventData(upRequest))
            .deviceId(ioTDeviceDTO.getDeviceId())
            .iotId(ioTDeviceDTO.getIotId())
            .productKey(ioTDeviceDTO.getProductKey())
            .messageType(upRequest.getMessageType().name())
            .event(functionOrEvent(upRequest))
            .commandId(upRequest.getCommandId())
            .commandStatus(upRequest.getCommandStatus())
            .createTime(LocalDateTime.now())
            .deviceName(StrUtil.sub(ioTDeviceDTO.getDeviceName(), 0, 30))
            .build();
    if (upRequest.getProperties() != null
        && upRequest.getProperties().containsKey(DeviceManagerConstant.COORDINATE)) {
      log.setPoint(ioTDeviceDTO.getCoordinate());
    }

    return log;
  }

  private String peopertiesOrEventData(BaseUPRequest upRequest) {
    if (MessageType.PROPERTIES.equals(upRequest.getMessageType())) {
      return JSONUtil.toJsonStr(upRequest.getProperties());
    } else if (MessageType.EVENT.equals(upRequest.getMessageType())) {
      Map<String, Object> content = new HashMap<>();
      Map<String, Object> data = upRequest.getData();
      Map<String, Object> properties = upRequest.getProperties();
      if (MapUtil.isNotEmpty(properties)) {
        content.putAll(properties);
      }
      if (MapUtil.isNotEmpty(data)) {
        content.putAll(data);
      }
      return JSONUtil.toJsonStr(content);
    }
    return StrUtil.EMPTY;
  }

  private String functionOrEvent(BaseUPRequest upRequest) {
    if (MessageType.FUNCTIONS.equals(upRequest.getMessageType())
        || MessageType.REPLY.equals(upRequest.getMessageType())) {
      // online-上线
      return upRequest.getFunction();
    } else if (MessageType.EVENT.equals(upRequest.getMessageType())) {
      return upRequest.getEvent();
    }
    return StrUtil.EMPTY;
  }

  List<IoTDeviceEvents> selectDevEvents(String productKey) {
    IoTProduct ioTProduct = ioTProductMapper.getProductByProductKey(productKey);
    JSONObject metadata = JSONUtil.parseObj(ioTProduct.getMetadata());
    List<IoTDeviceEvents> ioTDeviceEventsList = new ArrayList<>();
    JSONArray properties = metadata.getJSONArray("events");
    if (properties != null) {
      for (Object object : properties) {
        JSONObject jsonObject = JSONUtil.parseObj(object);
        JSONObject expands = JSONUtil.parseObj(jsonObject.getStr("expands"));
        IoTDeviceEvents ioTDeviceEvents =
            IoTDeviceEvents.builder()
                .id(jsonObject.getStr("id"))
                .name(jsonObject.getStr("name"))
                .description(jsonObject.getStr("description"))
                .level(expands.getStr("level"))
                .build();
        ioTDeviceEventsList.add(ioTDeviceEvents);
      }
    }
    return ioTDeviceEventsList;
  }
}

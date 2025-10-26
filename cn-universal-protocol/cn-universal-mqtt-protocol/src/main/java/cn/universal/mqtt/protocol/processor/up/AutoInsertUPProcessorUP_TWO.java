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

package cn.universal.mqtt.protocol.processor.up;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.domain.R;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.mqtt.protocol.entity.MQTTUPRequest;
import cn.universal.mqtt.protocol.processor.up.common.BaseAutoRegisterProcessor;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 自动新增设备处理器
 *
 * <p>负责处理设备自动注册和初始化逻辑 当检测到新设备时，自动完成设备注册和相关配置
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Slf4j(topic = "mqtt")
@Component
public class AutoInsertUPProcessorUP_TWO extends BaseAutoRegisterProcessor {

  @Value("${iot.register.auto.unionId}")
  private String unionId;

  @Value("${iot.register.auto.latitude}")
  private String latitude;

  @Value("${iot.register.auto.longitude}")
  private String longitude;

  @Override
  public ProcessorResult process(MQTTUPRequest request) {
    if (request.getIoTDeviceDTO() == null) {
      String productKey = request.getProductKey();
      if (StrUtil.isNotBlank(productKey)) {
        JSONObject config = JSONUtil.parseObj(request.getIoTProduct().getConfiguration());
        if (!config.getBool(IoTConstant.ALLOW_INSERT, false)) {
          log.info(
              "[{}] [未开启主动注册] deviceId={} productKey={} content={}",
              request.getDeviceId(),
              productKey);
          return ProcessorResult.STOP;
        }
        log.info("[{}] [开启-允许新增] deviceId={} productKey={}", request.getDeviceId(), productKey);
        boolean flag = insertDevice(request.getDeviceId(), request.getIoTProduct());
        if (!flag) {
          log.info("[{}] [自动新增] 增加失败 deviceId={},productKey={}", request.getDeviceId(), productKey);
          return ProcessorResult.ERROR;
        }
      } else {
        log.info("[{}] [主动注册] deviceId={} ProductKey 为空", request.getDeviceId());
        return ProcessorResult.ERROR;
      }
    }
    IoTDeviceDTO ioTDeviceDTO =
        lifeCycleDevInstance(
            IoTDeviceQuery.builder()
                .deviceId(request.getDeviceId())
                .productKey(request.getProductKey())
                .build());
    // 查出来的结果赋值进去
    request.setIoTDeviceDTO(ioTDeviceDTO);
    return ProcessorResult.CONTINUE;
  }

  @Override
  public boolean supports(MQTTUPRequest request) {
    IoTProduct ioTProduct = getProduct(request.getProductKey());
    JSONObject config = JSONUtil.parseObj(ioTProduct.getConfiguration());
    if (ioTProduct != null && config != null && config.getBool(IoTConstant.ALLOW_INSERT, false)) {
      return true;
    }
    return false;
  }

  @Override
  protected String getTopicType() {
    return "";
  }

  @Override
  protected void enhanceRegisterRequest(MQTTUPRequest request, JSONObject deviceData) {}

  @Override
  protected boolean processTopicSpecificAutoRegister(MQTTUPRequest request) {
    return false;
  }

  private boolean insertDevice(String deviceId, IoTProduct ioTProduct) {
    JSONObject downRequest = new JSONObject();
    downRequest.set("appUnionId", unionId);
    downRequest.set("productKey", ioTProduct.getProductKey());
    downRequest.set("deviceId", deviceId);
    downRequest.set(IoTConstant.ALLOW_INSERT, true);
    downRequest.set("cmd", "dev_add");
    JSONObject ob = new JSONObject();
    ob.set("deviceName", deviceId);
    ob.set("imei", deviceId);
    ob.set("latitude", latitude);
    ob.set("longitude", longitude);
    downRequest.set("data", ob);
    R ok = IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
    log.info("新增返回={}", ok);
    return ok.isSuccess();
  }
}

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

package cn.universal.mqtt.protocol.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.core.service.ICodec;
import cn.universal.dm.device.service.AbstractDownService;
import cn.universal.mqtt.protocol.config.MqttModuleInfo;
import cn.universal.mqtt.protocol.entity.MQTTDownRequest;
import cn.universal.mqtt.protocol.processor.MQTTDownProcessorChain;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统内置MQTT下行处理类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/07/09 22:19
 */
@Service("mqttDownService")
@Slf4j(topic = "mqtt")
public class MQTTDownService extends AbstractDownService<MQTTDownRequest> implements ICodec {

  @Resource
  private MqttModuleInfo mqttModuleInfo;
  @Resource
  private MQTTDownProcessorChain mqttDownProcessorChain;

  @Override
  protected MQTTDownRequest convert(String request) {
    return doConvert(request);
  }

  private MQTTDownRequest doConvert(Object request) {
    MQTTDownRequest value = null;
    if (request instanceof JSONObject) {
      value = JSONUtil.toBean((JSONObject) request, MQTTDownRequest.class);
    } else if (request instanceof String) {
      value = JSONUtil.toBean((String) request, MQTTDownRequest.class);
    } else {
      value = JSONUtil.toBean(JSONUtil.toJsonStr(request), MQTTDownRequest.class);
    }
    IoTProduct ioTProduct = getProduct(value.getProductKey());
    value.setIoTProduct(ioTProduct);
    //设置IoTDeviceDTO
    IoTDeviceDTO ioTDeviceDTO = getIoTDeviceDTO(
        IoTDeviceQuery.builder().productKey(value.getProductKey()).deviceId(value.getDeviceId())
            .build());
    value.setIoTDeviceDTO(ioTDeviceDTO);
    value.getDownCommonData().setConfiguration(JSONUtil.parseObj(ioTProduct.getConfiguration()));
    // 功能且function对象不为空，则编解码，并复制编解码后的内容
    if (DownCmd.DEV_FUNCTION.equals(value.getCmd()) && CollectionUtil.isNotEmpty(
        value.getFunction())) {
      String deResult = spliceDown(value.getProductKey(), JSONUtil.toJsonStr(value.getFunction()));
      //      log.info("电信设备={} 编解码结果={}", value.getDeviceId(), deResult);
      value.setPayload(deResult);
    }
    //    buildMsgId(value);
    return value;
  }

  @Override
  public String code() {
    return mqttModuleInfo.getCode();
  }

  @Override
  public String name() {
    return mqttModuleInfo.getName();
  }

  @Override
  public R doAction(JSONObject msg) {
    return mqttDownProcessorChain.process(doConvert(msg));
  }

  @Override
  public R doAction(String msg) {
    log.info("mqtt down msg={}", msg);
    return mqttDownProcessorChain.process(convert(msg));
  }
}

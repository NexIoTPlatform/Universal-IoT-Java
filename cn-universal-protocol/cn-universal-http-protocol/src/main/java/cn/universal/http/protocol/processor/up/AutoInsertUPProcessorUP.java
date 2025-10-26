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

package cn.universal.http.protocol.processor.up;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.http.protocol.processor.HttpUPMessageProcessor;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import java.util.List;
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
@Slf4j
@Component
public class AutoInsertUPProcessorUP extends AbstratIoTService implements HttpUPMessageProcessor {

  @Value("${iot.register.auto.unionId}")
  private String unionId;

  @Value("${iot.register.auto.latitude}")
  private String latitude;

  @Value("${iot.register.auto.longitude}")
  private String longitude;

  @Override
  public String getName() {
    return "HttpAutoInsertUPProcessor";
  }

  @Override
  public String getDescription() {
    return "HTTP上行消息自动新增设备处理器";
  }

  @Override
  public int getOrder() {
    return 200;
  }

  @Override
  public List<HttpUPRequest> process(
      JSONObject jsonObject, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    log.debug("[HttpAutoInsertUPProcessor] 开始处理自动新增设备，设备ID: {}", ioTDeviceDTO.getDeviceId());
    // 这里实现数据预处理逻辑
    log.debug("[{}}] 处理 {} 个请求", getName(), requests.size());
    if (ioTDeviceDTO == null) {
      String productKey = jsonObject.getStr("productKey");
      if (StrUtil.isNotBlank(productKey)) {
        IoTProduct ioTProduct = getProduct(productKey);
        JSONObject config = JSONUtil.parseObj(ioTProduct.getConfiguration());
        if (!config.getBool(IoTConstant.ALLOW_INSERT, false)) {
          log.info(
              "[HTTP上行][设备不存在-不处理] deviceId={} productKey={} content={}",
              jsonObject.getStr("deviceId"),
              productKey,
              jsonObject);
          return null;
        }
        log.info(
            "[HTTP上行][设备不存在-允许新增] deviceId={} productKey={} content={}",
            jsonObject.getStr("deviceId"),
            productKey,
            jsonObject);
        insertDevice(jsonObject.getStr("deviceId"), ioTProduct);
      } else {
        log.info(
            "[HTTP上行][设备不存在-不处理] deviceId={} content={}",
            jsonObject.getStr("deviceId"),
            jsonObject);
        return null;
      }
    }
    log.debug("[HttpAutoInsertUPProcessor] 自动新增设备处理完成，处理请求数: {}", requests.size());
    return requests;
  }

  @Override
  public boolean supports(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    // 检查是否需要自动注册功能
    return ioTDeviceDTO != null && isAutoRegistrationEnabled(ioTDeviceDTO);
  }

  @Override
  public boolean preCheck(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.debug("[HttpAutoInsertUPProcessor] 请求列表为空，跳过自动新增设备处理");
      return false;
    }
    return true;
  }

  @Override
  public void onError(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests, Exception e) {
    log.error("[HttpAutoInsertUPProcessor] 处理异常，设备ID: {}, 异常: ", ioTDeviceDTO.getDeviceId(), e);
  }

  /** 检查是否应该自动注册设备 */
  private boolean shouldAutoRegister(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    // 检查设备是否已存在
    if (ioTDeviceDTO.getDeviceId() != null) {
      return false; // 设备已存在，不需要自动注册
    }

    // 检查是否包含必要的注册信息
    return source.containsKey("deviceId") || source.containsKey("deviceName");
  }

  /** 处理设备自动注册 */
  private void handleAutoRegistration(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    log.info(
        "[HttpAutoInsertUPProcessor] 开始自动注册设备，设备名: {}", source.getStr("deviceName", "unknown"));

    try {
      // 这里应该调用设备注册服务
      // deviceRegistrationService.autoRegister(ioTDeviceDTO, source);

      // 设置注册标记
      //      request.setAutoRegistered(true);

      log.info("[HttpAutoInsertUPProcessor] 设备自动注册完成");
    } catch (Exception e) {
      log.error("[HttpAutoInsertUPProcessor] 设备自动注册失败: ", e);
      throw e;
    }
  }

  /** 设备初始化处理 */
  private void initializeDevice(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    // 设置设备基础信息
    if (request.getDeviceId() == null && source.containsKey("deviceId")) {
      request.setDeviceId(source.getStr("deviceId"));
    }

    if (request.getDeviceName() == null && source.containsKey("deviceName")) {
      request.setDeviceName(source.getStr("deviceName"));
    }

    // 设置产品信息
    //    if (ioTDeviceDTO.getIoTProductDTO() != null) {
    //      request.setProductKey(ioTDeviceDTO.getIoTProductDTO().getProductKey());
    //    }

    log.debug("[HttpAutoInsertUPProcessor] 设备初始化完成，设备: {}", request.getDeviceId());
  }

  /** 检查是否启用了自动注册功能 */
  private boolean isAutoRegistrationEnabled(IoTDeviceDTO ioTDeviceDTO) {
    return true;
  }

  private void insertDevice(String deviceId, IoTProduct ioTProduct) {
    JSONObject downRequest = new JSONObject();
    downRequest.set("appUnionId", unionId);
    downRequest.set("productKey", ioTProduct.getProductKey());
    downRequest.set("deviceId", deviceId);
    downRequest.set("cmd", "dev_add");
    JSONObject ob = new JSONObject();
    ob.set("deviceName", deviceId);
    ob.set("imei", deviceId);
    ob.set("latitude", latitude);
    ob.set("longitude", longitude);
    downRequest.set("data", ob);
    IoTDownlFactory.getIDown(ioTProduct.getThirdPlatform()).doAction(downRequest);
  }
}

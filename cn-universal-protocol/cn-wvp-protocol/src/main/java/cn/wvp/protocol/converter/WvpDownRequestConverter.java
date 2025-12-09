/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权,未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.wvp.protocol.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.core.downlink.DownlinkContext;
import cn.universal.core.downlink.converter.DownRequestConverter;
import cn.universal.core.message.UnifiedDownlinkCommand;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.dm.video.VideoPlatformInstanceAdapter;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.query.IoTDeviceQuery;
import cn.wvp.protocol.config.WvpModuleInfo;
import cn.wvp.protocol.entity.WvpDownRequest;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * WVP协议转换器 将UnifiedDownlinkCommand转换为WvpDownRequest
 *
 * @version 1.0
 * @since 2025/11/09
 */
@Slf4j
@Component("wvpConverter")
public class WvpDownRequestConverter extends AbstratIoTService
    implements DownRequestConverter<WvpDownRequest> {

  @Autowired
  @Qualifier("wvpPlatformAdapter")
  private VideoPlatformInstanceAdapter videoPlatformInstanceAdapter;

  @Resource private WvpModuleInfo wvpModuleInfo;

  @Override
  public WvpDownRequest convert(UnifiedDownlinkCommand command, DownlinkContext<?> context) {
    try {
      // 1. 加载产品信息
      IoTProduct ioTProduct = getProduct(command.getProductKey());
      if (ioTProduct == null) {
        throw new IllegalArgumentException("产品不存在: " + command.getProductKey());
      }

      // 2. 创建WVP下行请求
      WvpDownRequest request = new WvpDownRequest();
      BeanUtil.copyProperties(command, request, "extensions", "metadata");

      // 3. 设置产品信息
      request.setIoTProduct(ioTProduct);

      // 4. 加载设备信息（对于DEV_ADD命令，设备可能不存在）
      IoTDeviceDTO ioTDeviceDTO = null;
      if (command.getCmd() != DownCmd.DEV_ADD && command.getCmd() != DownCmd.DEV_ADDS) {
        ioTDeviceDTO =
            getIoTDeviceDTO(
                IoTDeviceQuery.builder()
                    .productKey(command.getProductKey())
                    .deviceId(command.getDeviceId())
                    .iotId(command.getIotId())
                    .build());

        if (ioTDeviceDTO == null) {
          throw new IllegalArgumentException(
              "设备不存在: productKey="
                  + command.getProductKey()
                  + ", deviceId="
                  + command.getDeviceId());
        }
      }
      request.setIoTDeviceDTO(ioTDeviceDTO);

      VideoPlatformInstance videoPlatformInstance =
          videoPlatformInstanceAdapter.getVideoPlatformInstance(command.getProductKey());
      if (videoPlatformInstance == null) {
        throw new IllegalArgumentException("视频实例不存在: " + command.getProductKey());
      }
      buildEndpointAuth(request, videoPlatformInstance);
      // 5. 处理扩展字段
      if (command.getExtensions() != null && !command.getExtensions().isEmpty()) {
        Map<String, Object> wvpRequestData = request.getWvpRequestData();
        wvpRequestData.putAll(command.getExtensions());
        request.setWvpRequestData(wvpRequestData);
      }

      // 6. 设置消息ID
      if (StrUtil.isBlank(request.getMsgId())) {
        request.setMsgId(generateMsgId());
      }
      if (command.getFunction() != null && !command.getFunction().isEmpty()) {
        request.setFunction(command.getFunction());
        if (command.getFunction().containsKey("data")) {
          request.setData(JSONUtil.parseObj(command.getFunction().get("data")));
        }
      }
      log.debug(
          "[WVP转换器] 转换完成: productKey={}, deviceId={}, cmd={}",
          request.getProductKey(),
          request.getDeviceId(),
          request.getCmd() != null ? request.getCmd().getValue() : null);

      return request;

    } catch (IllegalArgumentException e) {
      log.error("[WVP转换器] 参数验证失败: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("[WVP转换器] 转换失败", e);
      throw new IllegalStateException("WVP请求转换失败: " + e.getMessage(), e);
    }
  }

  /** 生成消息ID */
  private String generateMsgId() {
    return String.valueOf(System.currentTimeMillis());
  }

  @Override
  public String supportedProtocol() {
    return wvpModuleInfo.getCode();
  }

  @Override
  public int getPriority() {
    return 10;
  }

  @Override
  public boolean supports(UnifiedDownlinkCommand command) {
    return command.getCmd() != null;
  }

  @Override
  public void preConvert(UnifiedDownlinkCommand command, DownlinkContext<?> context) {
    if (StrUtil.isBlank(command.getProductKey())) {
      throw new IllegalArgumentException("productKey不能为空");
    }

    log.debug(
        "[WVP转换器] 开始转换: productKey={}, deviceId={}, cmd={}",
        command.getProductKey(),
        command.getDeviceId(),
        command.getCmd());
  }

  @Override
  public void postConvert(
      UnifiedDownlinkCommand command, WvpDownRequest request, DownlinkContext<?> context) {
    context.setAttribute("convertTime", System.currentTimeMillis());
    context.setAttribute("protocolType", "WVP");

    if (request.getIoTDeviceDTO() != null) {
      context.setAttribute("deviceName", request.getIoTDeviceDTO().getDeviceName());
      context.setAttribute("deviceStatus", request.getIoTDeviceDTO().getState());
    }

    log.debug("[WVP转换器] 转换后处理完成: msgId={}", request.getMsgId());
  }

  private void buildEndpointAuth(WvpDownRequest request, VideoPlatformInstance instance) {
    Map<String, Object> wvpRequestData = request.getWvpRequestData();
    if (wvpRequestData == null) {
      wvpRequestData = new HashMap<>();
    }
    String auth = instance.getAuth();
    if (JSONUtil.isTypeJSON(auth)) {
      wvpRequestData.putAll(JSONUtil.toBean(auth, Map.class));
    }
    String endpoint = instance.getEndpoint();
    if (StrUtil.isNotBlank(endpoint)) {
      wvpRequestData.put("endpoint", endpoint);
    }
    request.setWvpRequestData(wvpRequestData);
  }
}

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

package cn.universal.mqtt.protocol.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.core.downlink.DownlinkContext;
import cn.universal.core.downlink.converter.DownRequestConverter;
import cn.universal.core.message.DownCommonData;
import cn.universal.core.message.UnifiedDownlinkCommand;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.mqtt.protocol.config.MqttModuleInfo;
import cn.universal.mqtt.protocol.entity.MQTTDownRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MQTT协议转换器 将UnifiedDownlinkCommand转换为MQTTDownRequest
 *
 * @version 1.0
 * @since 2025/10/25
 */
@Slf4j
@Component("mqttConverter")
public class MQTTDownRequestConverter extends AbstratIoTService
    implements DownRequestConverter<MQTTDownRequest> {

  @Resource private MqttModuleInfo mqttModuleInfo;

  @Override
  public MQTTDownRequest convert(UnifiedDownlinkCommand command, DownlinkContext<?> context) {
    try {
      // 1. 加载产品信息
      IoTProduct ioTProduct = getProduct(command.getProductKey());
      if (ioTProduct == null) {
        throw new IllegalArgumentException("产品不存在: " + command.getProductKey());
      }

      // 2. 检测是否为网关子设备
      if (isGatewaySubDevice(ioTProduct)) {
        return convertGatewaySubDevice(command, ioTProduct, context);
      } else {
        return convertNormalDevice(command, ioTProduct, context);
      }

    } catch (IllegalArgumentException e) {
      log.error("[MQTT转换器] 参数验证失败: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("[MQTT转换器] 转换失败", e);
      throw new IllegalStateException("MQTT请求转换失败: " + e.getMessage(), e);
    }
  }

  /** 转换网关子设备请求 关键逻辑：编解码使用子设备配置，通信使用网关设备配置 */
  private MQTTDownRequest convertGatewaySubDevice(
      UnifiedDownlinkCommand command, IoTProduct subProduct, DownlinkContext<?> context) {
    log.info(
        "[MQTT转换器] 检测到网关子设备, productKey={}, deviceId={}, cmd={}",
        command.getProductKey(),
        command.getDeviceId(),
        command.getCmd());

    // 1. 判断是否需要网关代理
    boolean needGatewayProxy = isNeedGatewayProxy(command.getCmd());
    
    if (!needGatewayProxy) {
      // 对于不需要网关代理的命令（如DEV_ADD等设备管理命令），直接使用子设备信息
      log.info("[MQTT转换器] 命令{}不需要网关代理，直接使用子设备信息", command.getCmd());
      return convertSubDeviceDirectly(command, subProduct, context);
    }

    // 2. 加载子设备信息（需要网关代理时必须存在）
    IoTDeviceDTO subDevice =
        getIoTDeviceDTO(
            IoTDeviceQuery.builder()
                .productKey(command.getProductKey())
                .deviceId(command.getDeviceId())
                .iotId(command.getIotId())
                .build());

    if (subDevice == null) {
      throw new IllegalArgumentException(
          "子设备不存在: productKey=" + command.getProductKey() + ", deviceId=" + command.getDeviceId());
    }

    // 3. 验证网关绑定信息
    if (StrUtil.isBlank(subDevice.getGwProductKey())
        || StrUtil.isBlank(subDevice.getExtDeviceId())) {
      throw new IllegalArgumentException(
          "网关子设备缺少网关绑定信息: gwProductKey="
              + subDevice.getGwProductKey()
              + ", extDeviceId="
              + subDevice.getExtDeviceId()
              + ", 请先完成子设备与网关的绑定");
    }

    // 4. 使用子设备的ProductKey执行编解码
    String payload = null;
    if (DownCmd.DEV_FUNCTION.equals(command.getCmd())
        && CollectionUtil.isNotEmpty(command.getFunction())) {
      // 关键：这里用子设备的ProductKey编解码
      String functionJson = JSONUtil.toJsonStr(command.getFunction());
      try {
        payload = encode(command.getProductKey(), functionJson, null);
        log.debug("[MQTT转换器] 子设备编解码完成, payload={}", payload);
      } catch (Exception e) {
        log.warn("[MQTT转换器] 子设备编解码失败，返回原始数据: {}", e.getMessage());
        payload = functionJson;
      }
    }

    // 5. 查询网关产品信息
    IoTProduct gatewayProduct = getProduct(subDevice.getGwProductKey());
    if (gatewayProduct == null) {
      throw new IllegalArgumentException("网关产品不存在: " + subDevice.getGwProductKey());
    }

    // 6. 查询网关设备信息
    IoTDeviceDTO gatewayDevice =
        getIoTDeviceDTO(
            IoTDeviceQuery.builder()
                .productKey(subDevice.getGwProductKey())
                .deviceId(subDevice.getExtDeviceId())
                .build());

    if (gatewayDevice == null) {
      throw new IllegalArgumentException(
          "网关设备不存在: gwProductKey="
              + subDevice.getGwProductKey()
              + ", extDeviceId="
              + subDevice.getExtDeviceId());
    }

    // 7. 构建请求对象（通过网关代理）
    MQTTDownRequest request = new MQTTDownRequest();
    BeanUtil.copyProperties(command, request, "extensions", "metadata");

    // 关键：使用网关设备的产品和设备信息进行通信
    request.setIoTProduct(gatewayProduct);
    request.setIoTDeviceDTO(gatewayDevice);
    request.setPayload(payload);
    request.setDownResult(payload);
    // 如果是功能调用，必须使用网关的设备，只能通过它来完成功能调用。很重要！！！！
    if (DownCmd.DEV_FUNCTION.equals(command.getCmd())) {
      request.setProductKey(subDevice.getGwProductKey());
      request.setDeviceId(subDevice.getExtDeviceId());
    }
    // 记录子设备信息（用于日志追踪和下发标识）
    request.setSubDeviceId(subDevice.getDeviceId());
    request.setSubProductKey(command.getProductKey());
    request.setIsGatewayProxy(true);

    // 8. 设置网关的配置信息
    DownCommonData downCommonData = new DownCommonData();
    downCommonData.setConfiguration(parseProductConfigurationSafely(gatewayProduct));
    request.setDownCommonData(downCommonData);

    // 9. 处理扩展字段
    if (command.getExtensions() != null && !command.getExtensions().isEmpty()) {
      request.setData(JSONUtil.parseObj(command.getExtensions()));
    }

    // 10. 设置消息ID
    if (StrUtil.isBlank(request.getMsgId())) {
      request.setMsgId(generateMsgId());
    }

    log.info(
        "[MQTT转换器] 网关子设备转换完成, 将使用网关设备通信: gatewayDeviceId={}, subDeviceId={}",
        gatewayDevice.getDeviceId(),
        subDevice.getDeviceId());

    return request;
  }

  /**
   * 判断命令是否需要网关代理
   * @param cmd 下行命令
   * @return true-需要网关代理, false-直接使用子设备信息
   */
  private boolean isNeedGatewayProxy(DownCmd cmd) {
    if (cmd == null) {
      return false;
    }
    
    // 设备管理类命令不需要网关代理，直接操作子设备
    return switch (cmd) {
      case DEV_ADD, DEV_ADDS,          // 设备新增
           DEV_DEL, DEVICE_DELETE,     // 设备删除  
           DEV_UPDATE, DEVICE_UPDATE   // 设备更新
           -> false;
      // 其他命令（功能调用、状态查询等）需要通过网关代理
      default -> true;
    };
  }

  /**
   * 直接使用子设备信息转换（不需要网关代理的场景）
   * @param command 下行命令
   * @param subProduct 子设备产品信息
   * @param context 下行上下文
   * @return MQTT下行请求
   */
  private MQTTDownRequest convertSubDeviceDirectly(
      UnifiedDownlinkCommand command, IoTProduct subProduct, DownlinkContext<?> context) {
    
    // 1. 创建请求对象
    MQTTDownRequest request = new MQTTDownRequest();
    BeanUtil.copyProperties(command, request, "extensions", "metadata");

    // 2. 设置子设备产品信息
    request.setIoTProduct(subProduct);

    // 3. 尝试加载子设备信息（DEV_ADD时可能不存在）
    IoTDeviceDTO subDevice = null;
    if (command.getCmd() != DownCmd.DEV_ADD && command.getCmd() != DownCmd.DEV_ADDS) {
      subDevice = getIoTDeviceDTO(
          IoTDeviceQuery.builder()
              .productKey(command.getProductKey())
              .deviceId(command.getDeviceId())
              .iotId(command.getIotId())
              .build());
      
      if (subDevice == null) {
        throw new IllegalArgumentException(
            "子设备不存在: productKey=" + command.getProductKey() + ", deviceId=" + command.getDeviceId());
      }
    }
    request.setIoTDeviceDTO(subDevice);

    // 4. 设置子设备的配置信息
    DownCommonData downCommonData = new DownCommonData();
    downCommonData.setConfiguration(parseProductConfigurationSafely(subProduct));
    request.setDownCommonData(downCommonData);

    // 5. 处理扩展字段
    if (command.getExtensions() != null && !command.getExtensions().isEmpty()) {
      request.setData(JSONUtil.parseObj(command.getExtensions()));
    }

    // 6. 设置消息ID
    if (StrUtil.isBlank(request.getMsgId())) {
      request.setMsgId(generateMsgId());
    }
    
    // 7. 标记为子设备（但不通过网关代理）
    request.setIsGatewayProxy(false);

    log.info(
        "[MQTT转换器] 子设备直接转换完成（无需网关代理）: productKey={}, deviceId={}, cmd={}",
        command.getProductKey(),
        command.getDeviceId(),
        command.getCmd());

    return request;
  }

  /** 转换普通设备请求 */
  private MQTTDownRequest convertNormalDevice(
      UnifiedDownlinkCommand command, IoTProduct ioTProduct, DownlinkContext<?> context) {
    // 1. 创建请求对象
    MQTTDownRequest request = new MQTTDownRequest();

    // 2. 复制通用字段
    BeanUtil.copyProperties(command, request, "extensions", "metadata");

    // 3. 设置产品信息
    request.setIoTProduct(ioTProduct);

    // 4. 加载设备信息
    IoTDeviceDTO ioTDeviceDTO =
        getIoTDeviceDTO(
            IoTDeviceQuery.builder()
                .productKey(command.getProductKey())
                .deviceId(command.getDeviceId())
                .iotId(command.getIotId())
                .build());

    if (ioTDeviceDTO == null && command.getCmd() != DownCmd.DEV_ADD) {
      throw new IllegalArgumentException(
          "设备不存在: productKey=" + command.getProductKey() + ", deviceId=" + command.getDeviceId());
    }
    request.setIoTDeviceDTO(ioTDeviceDTO);

    // 5. 设置MQTT特定字段
    DownCommonData downCommonData = new DownCommonData();
    downCommonData.setConfiguration(parseProductConfigurationSafely(ioTProduct));
    request.setDownCommonData(downCommonData);

    // 6. 处理功能下发场景：编解码
    if (DownCmd.DEV_FUNCTION.equals(command.getCmd())
        && CollectionUtil.isNotEmpty(command.getFunction())) {
      String payload = encodeFunction(command);
      request.setPayload(payload);
      request.setDownResult(payload);

      log.debug("[MQTT转换器] 功能下发编解码完成, deviceId={}, payload={}", command.getDeviceId(), payload);
    }

    // 7. 处理扩展字段
    if (command.getExtensions() != null && !command.getExtensions().isEmpty()) {
      // 将扩展字段设置到data字段
      request.setData(JSONUtil.parseObj(command.getExtensions()));
    }

    // 8. 设置消息ID
    if (StrUtil.isBlank(request.getMsgId())) {
      request.setMsgId(generateMsgId());
    }

    log.debug(
        "[MQTT转换器] 转换完成: productKey={}, deviceId={}, cmd={}",
        request.getProductKey(),
        request.getDeviceId(),
        request.getCmd() != null ? request.getCmd().getValue() : null);

    return request;
  }

  /** 判断是否为网关子设备 */
  private boolean isGatewaySubDevice(IoTProduct product) {
    return DeviceNode.GATEWAY_SUB_DEVICE.getValue().equalsIgnoreCase(product.getDeviceNode());
  }

  /**
   * 编解码功能参数
   *
   * @param command 统一命令
   * @return 编解码后的payload
   */
  private String encodeFunction(UnifiedDownlinkCommand command) {
    try {
      String functionJson = JSONUtil.toJsonStr(command.getFunction());
      // 使用父类的encode方法
      return encode(command.getProductKey(), functionJson, null);
    } catch (Exception e) {
      log.warn("[MQTT转换器] 编解码失败，返回原始数据: {}", e.getMessage());
      return JSONUtil.toJsonStr(command.getFunction());
    }
  }

  /**
   * 生成消息ID
   *
   * @return 消息ID
   */
  private String generateMsgId() {
    return String.valueOf(System.currentTimeMillis());
  }

  @Override
  public String supportedProtocol() {
    return mqttModuleInfo.getCode(); // "MQTT"
  }

  @Override
  public int getPriority() {
    return 10; // 高优先级
  }

  @Override
  public boolean supports(UnifiedDownlinkCommand command) {
    // MQTT支持所有指令类型
    return command.getCmd() != null;
  }

  @Override
  public void preConvert(UnifiedDownlinkCommand command, DownlinkContext<?> context) {
    // 预处理：验证必填参数
    if (StrUtil.isBlank(command.getProductKey())) {
      throw new IllegalArgumentException("productKey不能为空");
    }

    // 根据指令类型验证参数
    DownCmd cmd = command.getCmd();
    if (cmd == DownCmd.DEV_FUNCTION && CollectionUtil.isEmpty(command.getFunction())) {
      throw new IllegalArgumentException("功能下发指令必须提供function参数");
    }
  }

  @Override
  public void postConvert(
      UnifiedDownlinkCommand command, MQTTDownRequest request, DownlinkContext<?> context) {
    // 后处理：记录转换信息到上下文
    context.setAttribute("convertTime", System.currentTimeMillis());
    context.setAttribute("protocolType", "MQTT");

    // 保存设备信息到上下文（供后续拦截器使用）
    if (request.getIoTDeviceDTO() != null) {
      context.setAttribute("deviceName", request.getIoTDeviceDTO().getDeviceName());
      context.setAttribute(
          "deviceStatus", request.getIoTDeviceDTO().getState()); // 使用getState()而非getStatus()
    }
  }
}

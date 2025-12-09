package cn.universal.mqtt.protocol.processor.down;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.constant.IoTConstant.ERROR_CODE;
import cn.universal.common.domain.R;
import cn.universal.common.utils.PayloadCodecUtils;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTDownRequest;
import cn.universal.mqtt.protocol.processor.MQTTDownMessageProcessor;
import cn.universal.mqtt.protocol.system.SysMQTTManager;
import cn.universal.mqtt.protocol.third.ThirdMQTTConfigService;
import cn.universal.mqtt.protocol.third.ThirdMQTTServerManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicType;
import cn.universal.persistence.base.DeviceCommandSender;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.base.IoTDownAdapter;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * tcp 设备增加
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/7/10 10:40
 */
@Component
@Slf4j(topic = "mqtt")
public class MQTTDownFunction extends IoTDownAdapter<MQTTDownRequest>
    implements MQTTDownMessageProcessor, DeviceCommandSender {

  @Autowired private ThirdMQTTConfigService mqttConfigService;

  @Autowired private SysMQTTManager sysMQTTManager;
  @Autowired private ThirdMQTTServerManager thirdMqttServerManager;

  @Autowired private IoTProductDeviceService ioTProductDeviceService;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private MQTTTopicManager mqttTopicManager;

  @Resource private IoTProductMapper ioTProductMapper;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  private String CUSTOM_FIELD = "customField";

  // ==================== DeviceCommandSender Implementation ====================

  @Override
  public String getSupportedProtocol() {
    return "MQTT";
  }

  @Override
  public R<?> sendCommand(String productKey, String deviceId, String payload) {
    log.info(
        "[MQTTDownFunction] Gateway polling command: productKey={}, deviceId={}, payload={}",
        productKey,
        deviceId,
        payload);

    try {
      // 构建下发请求
      MQTTDownRequest request = new MQTTDownRequest();
      request.setProductKey(productKey);
      request.setDeviceId(deviceId);
      request.setCmd(DownCmd.DEV_FUNCTION);
      request.setPayload(payload);

      // 设置功能标识
      Map<String, Object> function = new HashMap<>();
      function.put("function", "gateway_polling");
      request.setFunction(function);

      // 查询产品信息
      IoTProduct product =
          ioTProductMapper.selectOne(IoTProduct.builder().productKey(productKey).build());
      if (product != null) {
        request.setIoTProduct(product);
      }

      // 查询设备信息
      IoTDevice device =
          ioTDeviceMapper.selectOne(
              IoTDevice.builder().productKey(productKey).deviceId(deviceId).build());
      if (device != null) {
        IoTDeviceDTO deviceDTO = new IoTDeviceDTO();
        BeanUtils.copyProperties(device, deviceDTO);
        request.setIoTDeviceDTO(deviceDTO);
      }

      // 调用现有处理逻辑
      return this.process(request);

    } catch (Exception e) {
      log.error(
          "[MQTTDownFunction] Gateway polling command failed: productKey={}, deviceId={}",
          productKey,
          deviceId,
          e);
      return R.error("发送指令失败: " + e.getMessage());
    }
  }

  // ==================== Original Process Method ====================

  @Override
  public R<?> process(MQTTDownRequest downRequest) {
    Map<String, Object> rs = new HashMap<>();
    boolean isSuccess = false;
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

    R r = callGlobalFunction(downRequest.getIoTProduct(), instance, downRequest);
    if (Objects.nonNull(r)) {
      return r;
    }
    String networkUnionId =
        ioTProductDeviceService.selectNetworkUnionId(downRequest.getProductKey());
    if (StrUtil.isBlank(networkUnionId)) {
      log.info("network union id is empty,use system network");
      //      return R.error("没有绑定网络");
    }
    IoTProduct ioTProduct = downRequest.getIoTProduct();
    if (ioTProduct == null) {
      // 查询产品信息
      ioTProduct =
          ioTProductMapper.selectOne(
              IoTProduct.builder().productKey(downRequest.getProductKey()).build());
      downRequest.setIoTProduct(ioTProduct);
    }
    // 设置topic
    if (ioTProduct != null && StrUtil.isNotBlank(ioTProduct.getConfiguration())) {
      JSONObject jsonObject = JSONUtil.parseObj(ioTProduct.getConfiguration());
      downRequest.setDownTopic(jsonObject.getStr("downTopic", null));
    }
    String downTopic = buildDownTopic(downRequest);
    if (StrUtil.isBlank(downTopic)) {
      return R.error(
          ERROR_CODE.DEV_DOWN_NO_MQTT_TOPIC.getCode(), ERROR_CODE.DEV_DOWN_NO_MQTT_TOPIC.getName());
    }
    Map<String, String> replace = new HashMap<>();
    replace.put("productKey", downRequest.getProductKey());
    replace.put("deviceId", downRequest.getDeviceId());
    replace.put("imei", downRequest.getDeviceId());
    replace.put("unionId", downRequest.getAppUnionId());
    // 占位符替换
    downTopic = StrUtil.format(downTopic, replace);
    if (StrUtil.isNotBlank(downTopic)) {
      Map<String, Object> function = downRequest.getFunction();
      String commandId = RandomUtil.randomString(6);
      function.put("commandId", commandId);
      String payload = downRequest.getPayload();
      // 如果编解码为空
      if (StrUtil.isBlank(payload)) {
        payload = JSONUtil.toJsonStr(downRequest.getFunction());
      }

      // 根据产品配置的 encoderType 编码 payload
      String productKey = downRequest.getProductKey();
      String encoderType = ioTProductDeviceService.getProductEncoderType(productKey);
      byte[] payloadBytes = PayloadCodecUtils.encode(encoderType, payload);

      boolean success = pushMessage(downRequest, downTopic, payloadBytes);
      if (success) {
        // 给前端返回格式化
        if (JSONUtil.isTypeJSON(payload)) {
          rs.putAll(JSONUtil.parseObj(payload));
        } else {
          rs.put("payload", payload);
        }
        rs.put("success", success);
        // 保存指令下发日志---开始
        JSONObject commandBody = new JSONObject();
        commandBody.set("commandId", commandId);
        commandBody.set("function", downRequest.getFunction().getOrDefault("function", ""));
        commandBody.set("payload", payload);
        ioTDeviceLifeCycle.commandSuccess(downRequest.getIoTDeviceDTO(), commandId, commandBody);
      } else {
        success = false;
        ioTDeviceLifeCycle.offline(ioTDevice.getProductKey(), ioTDevice.getDeviceId());
        rs.put("msg", "设备可能不在线");
      }
      return R.ok(rs);
    }
    return R.error(
        ERROR_CODE.DEV_DOWN_NO_MQTT_TOPIC.getCode(), ERROR_CODE.DEV_DOWN_NO_MQTT_TOPIC.getName());
  }

  private String buildDownTopic(MQTTDownRequest downRequest) {
    // 构建推送主题
    String downTopic = downRequest.getDownTopic();
    if (StrUtil.isBlank(downTopic)) {
      downTopic = buildReplyTopic(downRequest);
    } else {
      // 对已有的下发主题做占位符填充，以支持模板与通配符
      downTopic =
          mqttTopicManager.fillTopicPattern(
              downTopic, downRequest.getProductKey(), downRequest.getDeviceId());
    }
    return downTopic;
  }

  private boolean pushMessage(MQTTDownRequest downRequest, String downTopic, byte[] payloadBytes) {
    if (sysMQTTManager.isEnabled()
        && sysMQTTManager.isProductCovered(downRequest.getProductKey())) {
      return sysMQTTManager.publishMessage(
          downTopic, payloadBytes, sysMQTTManager.getConfig().getDefaultQos(), false);
    } else {
      // 取networkUnionId，通常等于productKey，或根据实际业务获取
      return thirdMqttServerManager.publishMessage(
          downRequest.getIoTProduct().getNetworkUnionId(), downTopic, new String(payloadBytes));
    }
  }

  /** 构建回复topic */
  protected String buildReplyTopic(MQTTDownRequest request) {
    String productKey = request.getProductKey();
    String deviceId = request.getDeviceId();
    String networkUnionId = request.getIoTProduct().getNetworkUnionId();
    // 未绑定第三方的MQTT通道，使用内置MQTT服务，则直接按照内置的主题规则
    if (StrUtil.isBlank(networkUnionId)) {
      return MQTTTopicType.THING_DOWN.buildTopic(productKey, deviceId);
    }
    // 1. 优先查第三方MQTT自定义下发topic
    String thirdPartyDownPattern =
        mqttTopicManager.getThirdPartyDownTopicPattern(networkUnionId, MqttConstant.TYPE_DOWN);
    if (thirdPartyDownPattern != null) {
      return mqttTopicManager.fillTopicPattern(thirdPartyDownPattern, productKey, deviceId);
    }
    return MQTTTopicType.THING_DOWN.buildTopic(productKey, deviceId);
  }

  @Override
  public boolean supports(MQTTDownRequest request) {
    if (!DownCmd.DEV_FUNCTION.equals(request.getCmd())) {
      return false;
    }
    if (request.getIoTDeviceDTO() == null) {}
    return true;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public int getOrder() {
    return 0;
  }
}

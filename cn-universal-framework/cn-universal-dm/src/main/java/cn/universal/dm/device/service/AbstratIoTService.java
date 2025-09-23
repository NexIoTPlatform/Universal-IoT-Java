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

package cn.universal.dm.device.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.UPRequest;
import cn.universal.core.metadata.AbstractEventMetadata;
import cn.universal.core.metadata.AbstractFunctionMetadata;
import cn.universal.core.metadata.DeviceMetadata;
import cn.universal.core.service.ICodecService;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTDeviceShadowService;
import cn.universal.dm.device.service.impl.IoTDeviceSubscribeService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.dm.device.service.log.IIoTDeviceDataService;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * IoT设备服务抽象基类
 *
 * <p>提供IoT平台中设备管理的通用功能，包括： - 产品信息查询和管理 - 设备生命周期管理 - 协议编解码支持 - 设备数据日志记录 - 设备影子服务 - 消息订阅管理 - AES加解密功能
 *
 * <p>子类通过继承此类获得设备管理的核心能力，专注于实现特定的业务逻辑
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/9 8:56
 */
@Slf4j
public abstract class AbstratIoTService {

  @Resource(name = "ioTDeviceActionAfterService")
  protected IoTDeviceActionAfterService ioTDeviceActionAfterService;

  @Autowired protected IoTProductDeviceService iotProductDeviceService;
  @Autowired private IoTDeviceService iotDeviceService;

  @Autowired protected IoTDeviceShadowService iotDeviceShadowService;

  @Resource protected IIoTDeviceDataService iIoTDeviceDataService;

  @Resource private IoTDeviceSubscribeService iotDeviceSubscribeService;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Autowired private ICodecService codecService;

  /**
   * 根据产品唯一编号查询产品信息
   *
   * <p>用于获取产品的配置信息，包括产品元数据、协议配置等
   *
   * @param productKey 产品唯一标识
   * @return 产品信息，如果不存在返回null
   */
  protected IoTProduct getProduct(String productKey) {
    return iotProductDeviceService.getProduct(productKey);
  }

  protected JSONObject getProductConfiguration(String productKey) {
    return iotProductDeviceService.getProductConfiguration(productKey);
  }

  /**
   * 安全解析产品配置JSON
   * 
   * <p>处理以下情况：
   * <ul>
   *   <li>配置为null</li>
   *   <li>配置为空字符串</li>
   *   <li>配置不是有效的JSON格式</li>
   * </ul>
   * 
   * @param product 产品信息
   * @return 解析后的JSONObject，如果解析失败返回空的JSONObject
   */
  protected JSONObject parseProductConfigurationSafely(IoTProduct product) {
    if (product == null) {
      log.warn("产品信息为空，返回空配置");
      return new JSONObject();
    }
    
    String configuration = product.getConfiguration();
    if (StrUtil.isBlank(configuration)) {
      log.debug("产品配置为空，返回空配置: productKey={}", product.getProductKey());
      return new JSONObject();
    }
    
    try {
      JSONObject config = JSONUtil.parseObj(configuration);
      log.debug("产品配置解析成功: productKey={}", product.getProductKey());
      return config;
    } catch (Exception e) {
      log.error("产品配置解析失败，返回空配置: productKey={}, configuration={}", 
                product.getProductKey(), configuration, e);
      return new JSONObject();
    }
  }

  protected IoTDeviceDTO getIoTDeviceDTO(IoTDeviceQuery query) {
    IoTDeviceDTO instanceBO = iotDeviceService.lifeCycleDevInstance(query);
    if (instanceBO == null) {
      return null;
    }
    return instanceBO;
  }

  /**
   * 查询产品的消息订阅URL列表
   *
   * <p>根据产品标识、设备标识和消息类型获取对应的订阅地址 用于消息推送和事件通知
   *
   * @param productKey 产品唯一标识
   * @param iotId 设备唯一标识
   * @param messageType 消息类型
   * @return 订阅URL列表
   */
  protected List<IoTDeviceSubscribe> querySubscribeUrl(
      String productKey, String iotId, MessageType messageType) {
    return iotDeviceSubscribeService.selectByProductKeyAndMsgType(productKey, iotId, messageType);
  }

  /**
   * 设备生命周期管理 - 设备实例查询
   *
   * <p>注意：此方法仅限第三方平台上行消息的入口使用，用作设备上线判断 其他地方禁止使用，避免重复触发生命周期事件
   *
   * <p>功能包括： - 查询设备实例信息 - 检测设备离线变上线状态 - 自动触发设备上线生命周期事件
   *
   * @param query 设备查询条件
   * @return 设备实例信息
   */
  protected IoTDeviceDTO lifeCycleDevInstance(IoTDeviceQuery query) {
    IoTDeviceDTO instanceBO = iotDeviceService.lifeCycleDevInstance(query);
    if (instanceBO == null) {
      return null;
    }

    // 检查Redis中的离线标记
    String offlineKey = "offline:" + instanceBO.getProductKey() + instanceBO.getDeviceId();
    String onlineKey = "online:" + instanceBO.getProductKey() + instanceBO.getDeviceId();
    boolean hasOfflineFlag = stringRedisTemplate.hasKey(offlineKey);
    boolean recentlyOnline = stringRedisTemplate.hasKey(onlineKey);

    // 判断是否需要上线
    boolean needOnline = false;
    String reason = "";

    if (!Boolean.TRUE.equals(instanceBO.getState())) {
      // 数据库状态为离线
      needOnline = true;
      reason = "数据库状态为离线";
    } else if (hasOfflineFlag) {
      // 数据库状态为在线，但Redis有离线标记（状态不一致）
      needOnline = true;
      reason = "Redis有离线标记";
    }

    // 防重复上线：如果最近已经上线过，则不重复执行
    if (needOnline && recentlyOnline) {
      log.debug("设备最近已上线，跳过重复上线: deviceId={}, reason={}", instanceBO.getDeviceId(), reason);
      needOnline = false;
    }

    if (needOnline) {
      log.info("设备需要上线，deviceId={}，原因={}", instanceBO.getDeviceId(), reason);
      ioTDeviceLifeCycle.online(instanceBO.getProductKey(), instanceBO.getDeviceId());
    } else {
      log.debug(
          "设备状态检查: deviceId={}, dbState={}, hasOfflineFlag={}, recentlyOnline={}, needOnline={}",
          instanceBO.getDeviceId(),
          instanceBO.getState(),
          hasOfflineFlag,
          recentlyOnline,
          needOnline);
    }
    return instanceBO;
  }

  /**
   * 保存设备上行日志和影子数据
   *
   * <p>批量处理设备上行请求，包括： - 保存设备数据日志到数据库 - 更新设备影子状态 - 过滤调试模式的消息
   *
   * @param upRequest 上行请求列表
   * @param ioTDeviceDTO 设备信息
   * @param ioTProduct 产品信息
   */
  protected void doLogMetadataAndShadow(
      List<? extends BaseUPRequest> upRequest, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {
    if (CollectionUtil.isNotEmpty(upRequest)) {
      upRequest.stream()
          .filter(s -> !s.isDebug())
          .forEach(
              s -> {
                iIoTDeviceDataService.saveDeviceLog(s, ioTDeviceDTO, ioTProduct);
                iotDeviceShadowService.doShadow(s, ioTDeviceDTO);
              });
    }
  }

  protected <R> List<R> decode(
      String productKey, String payload, Object context, Class<R> elementType) {
    return codecService.decode(productKey, payload, context, elementType);
  }

  protected <R> List<R> decode(String productKey, String payload, Class<R> elementType) {
    return decode(productKey, payload, null, elementType);
  }

  protected List<UPRequest> decode(String productKey, String payload) {
    return decode(productKey, payload, null, UPRequest.class);
  }

  protected String encode(String productKey, String payload, Object context) {
    return codecService.encode(productKey, payload, context);
  }

  protected String encode(String productKey, String payload) {
    return encode(productKey, payload, null);
  }

  protected void buildCodecNotNullBean(
      BaseUPRequest hasData, BaseUPRequest.BaseUPRequestBuilder builder) {
    if (CollUtil.isNotEmpty(hasData.getProperties())) {
      builder.properties(hasData.getProperties());
    }
    // 设置属性值
    if (CollUtil.isNotEmpty(hasData.getData())) {
      builder.data(hasData.getData());
    }
    // 设置原始串
    if (hasData
        .getIoTDeviceDTO()
        .getProductConfig()
        .getBool(IoTConstant.REQUIRE_PAYLOAD, Boolean.FALSE)) {
      builder.payload(hasData.getPayload());
    }
    builder.tags(hasData.getTags());
    // 设置回复值
    builder.function(hasData.getFunction());
  }

  protected void buildCodecNotNullBean(
      JSONObject jsonObject,
      IoTDeviceDTO ioTDeviceDTO,
      UPRequest codec,
      BaseUPRequest.BaseUPRequestBuilder builder) {
    if (codec == null) {
      codec = new UPRequest();
    }
    // 编解码覆盖原始消息
    messageProAndData(jsonObject, codec);
    if (MessageType.EVENT.equals(codec.getMessageType())) {
      String event = codec.getEvent();
      if (StrUtil.isBlank(event)) {
        event = jsonObject.getStr("event");
      }
      builder.event(event);
      builder.messageType(codec.getMessageType());
      if ("offline".equals(event)) {
        ioTDeviceActionAfterService.offline(
            ioTDeviceDTO.getProductKey(), ioTDeviceDTO.getDeviceId());
      }
    }
    // 设置data
    builder.data(codec.getData());
    builder.properties(codec.getProperties());
    // 设置原始串
    if (ioTDeviceDTO.getProductConfig().getBool(IoTConstant.REQUIRE_PAYLOAD, Boolean.FALSE)) {
      builder.payload(ioTDeviceDTO.getPayload());
    }
    builder.tags(codec.getTags());
    // 设置回复值
    if (StrUtil.isNotBlank(codec.getFunction())) {
      builder.function(codec.getFunction());
      DeviceMetadata deviceMetadata =
          iotProductDeviceService.getDeviceMetadata(ioTDeviceDTO.getProductKey());
      AbstractFunctionMetadata functionOrNull =
          deviceMetadata.getFunctionOrNull(codec.getFunction());
      builder.functionName(functionOrNull == null ? "" : functionOrNull.getName());
    }
  }

  private static void messageProAndData(JSONObject jsonObject, UPRequest codec) {
    if (codec.getMessageType() == null) {
      String messageType = jsonObject.getStr("messageType");
      if (StrUtil.isNotBlank(messageType)) {
        codec.setMessageType(MessageType.find(messageType));
      } else {
        codec.setMessageType(MessageType.PROPERTIES);
      }
    }
    if (codec.getProperties() == null) {
      JSONObject properties = jsonObject.getJSONObject("properties");
      if (properties != null) {
        codec.setProperties(properties);
      }
    } else {
      JSONObject properties = jsonObject.getJSONObject("properties");
      if (properties != null) {
        codec.getProperties().putAll(jsonObject.getJSONObject("properties"));
      }
    }
    if (codec.getData() == null) {
      JSONObject data = jsonObject.getJSONObject("data");
      if (data != null) {
        codec.setData(data);
      }
    } else {
      JSONObject data = jsonObject.getJSONObject("data");
      if (data != null) {
        codec.getData().putAll(jsonObject.getJSONObject("data"));
      }
    }
  }

  /** 处理事件名称和订阅消息 */
  protected void doEventNameAndSubscribe(
      List<? extends UPRequest> upRequests, IoTDeviceDTO ioTDeviceDTO) {
    if (CollectionUtil.isNotEmpty(upRequests)) {
      for (UPRequest ur : upRequests) {
        // 如果是事件，则完善事件名称
        if (MessageType.EVENT.equals(ur.getMessageType())) {
          DeviceMetadata deviceMetadata =
              iotProductDeviceService.getDeviceMetadata(ur.getProductKey());
          AbstractEventMetadata metadata = deviceMetadata.getEventOrNull(ur.getEvent());
          if (metadata != null) {
            ur.setEventName(metadata.getName());
          }
        }
        // 如果是订阅消息，则填充subscribeUrl
        List<IoTDeviceSubscribe> ioTDeviceSubscribe =
            querySubscribeUrl(ur.getProductKey(), ur.getIotId(), ur.getMessageType());
        if (ioTDeviceSubscribe != null) {
          ur.setDevSubscribe(ioTDeviceSubscribe);
        }
      }
    }
  }

  protected BaseUPRequest.BaseUPRequestBuilder getBaseUPRequest(IoTDeviceDTO ioTDeviceDTO) {
    return BaseUPRequest.builder()
        .deviceNode(DeviceNode.DEVICE)
        .ioTDeviceDTO(ioTDeviceDTO)
        .iotId(ioTDeviceDTO.getIotId())
        .deviceName(ioTDeviceDTO.getDeviceName())
        .messageType(MessageType.PROPERTIES)
        .deviceId(ioTDeviceDTO.getDeviceId())
        .time(System.currentTimeMillis())
        .productKey(ioTDeviceDTO.getProductKey())
        .userUnionId(ioTDeviceDTO.getUserUnionId());
  }

  protected BaseUPRequest buildCodecNullBean(JSONObject jsonObject, BaseUPRequest mqttUPRequest) {
    final BaseUPRequest.BaseUPRequestBuilder<?, ?> builder =
        getBaseUPRequest(mqttUPRequest.getIoTDeviceDTO());
    if (MessageType.PROPERTIES.name().equalsIgnoreCase(jsonObject.getStr("messageType"))) {
      builder.messageType(MessageType.PROPERTIES);
    }
    if (MessageType.EVENT.name().equalsIgnoreCase(jsonObject.getStr("messageType"))) {
      String event = jsonObject.getStr("event");
      builder.event(event);
      builder.messageType(MessageType.EVENT);
      if ("offline".equals(event)) {
        ioTDeviceActionAfterService.offline(
            mqttUPRequest.getIoTDeviceDTO().getProductKey(),
            mqttUPRequest.getIoTDeviceDTO().getDeviceId());
      }
    }
    if (ObjectUtil.isNotNull(jsonObject.getJSONObject("properties"))) {
      builder.properties(jsonObject.getJSONObject("properties"));
    }
    if (ObjectUtil.isNotNull(jsonObject.getJSONObject("data"))) {
      builder.data(jsonObject.getJSONObject("data"));
    }
    if (ObjectUtil.isNotNull(jsonObject.getJSONObject("tags"))) {
      builder.tags(jsonObject.getJSONObject("tags"));
    }
    // 设置原始串
    builder.payload(mqttUPRequest.getPayload());
    // 设置回复值
    return builder.build();
  }

  protected JSONObject parseJsonPayload(String payload) {
    if (JSONUtil.isTypeJSON(payload)) {
      return JSONUtil.parseObj(payload);
    }
    return new JSONObject().set("payload", payload);
  }
}

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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.metadata.AbstractEventMetadata;
import cn.universal.core.metadata.DeviceMetadata;
import cn.universal.dm.device.service.AbstratIoTService;
import cn.universal.dm.device.service.action.IoTDeviceActionAfterService;
import cn.universal.http.protocol.entity.HttpUPRequest;
import cn.universal.http.protocol.processor.HttpUPMessageProcessor;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 事件和订阅处理器
 *
 * <p>负责处理设备事件上报和订阅消息的逻辑 包括事件分发、订阅管理和消息推送等功能
 *
 * @version 2.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
@Slf4j
@Component
public class EventAndSubscribeUPProcessorUP extends AbstratIoTService
    implements HttpUPMessageProcessor {

  @Override
  public String getName() {
    return "HttpEventAndSubscribeUPProcessor";
  }

  @Override
  public String getDescription() {
    return "HTTP上行消息事件和订阅处理器";
  }

  @Override
  public int getOrder() {
    return 400;
  }

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceActionAfterService ioTDeviceActionAfterService;

  @Override
  public List<HttpUPRequest> process(
      JSONObject jsonObject, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (CollUtil.isNotEmpty(requests)) {
      for (HttpUPRequest ur : requests) {
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
    return requests;
  }

  @Override
  public boolean supports(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    // 检查是否包含事件或订阅相关字段
    return source.containsKey("event")
        || source.containsKey("subscribe")
        || source.containsKey("messageType")
        || hasEventData(source);
  }

  @Override
  public boolean preCheck(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      log.debug("[HttpEventAndSubscribeUPProcessor] 请求列表为空，跳过事件处理");
      return false;
    }
    return true;
  }

  @Override
  public void onError(
      JSONObject source, IoTDeviceDTO ioTDeviceDTO, List<HttpUPRequest> requests, Exception e) {
    log.error(
        "[HttpEventAndSubscribeUPProcessor] 处理异常，设备ID: {}, 异常: ", ioTDeviceDTO.getDeviceId(), e);
  }

  /** 检查是否为事件消息 */
  private boolean isEventMessage(HttpUPRequest request, JSONObject source) {
    return source.containsKey("event") || "EVENT".equalsIgnoreCase(source.getStr("messageType"));
  }

  /** 检查是否为订阅消息 */
  private boolean isSubscribeMessage(HttpUPRequest request, JSONObject source) {
    return source.containsKey("subscribe") || source.containsKey("subscription");
  }

  /** 检查是否包含事件数据 */
  private boolean hasEventData(JSONObject source) {
    return source.containsKey("eventData")
        || source.containsKey("events")
        || (source.containsKey("data") && source.getJSONObject("data").containsKey("event"));
  }

  /** 处理事件消息 */
  private void handleEventMessage(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    String eventType = source.getStr("event", source.getStr("eventType"));
    log.info(
        "[HttpEventAndSubscribeUPProcessor] 处理事件消息，事件类型: {}, 设备: {}",
        eventType,
        ioTDeviceDTO.getDeviceId());
    try {
      // 设置事件信息
      request.setEvent(eventType);
      request.setData(source.getJSONObject("eventData"));

      // 根据事件类型进行特殊处理
      switch (eventType) {
        case "online":
          handleOnlineEvent(request, ioTDeviceDTO);
          break;
        case "offline":
          handleOfflineEvent(request, ioTDeviceDTO);
          break;
        case "alarm":
          handleAlarmEvent(request, source, ioTDeviceDTO);
          break;
        default:
          handleCustomEvent(request, source, ioTDeviceDTO, eventType);
          break;
      }

      log.debug("[HttpEventAndSubscribeUPProcessor] 事件消息处理完成，事件: {}", eventType);
    } catch (Exception e) {
      log.error("[HttpEventAndSubscribeUPProcessor] 事件消息处理失败，事件: {}, 异常: ", eventType, e);
      throw e;
    }
  }

  /** 处理订阅消息 */
  private void handleSubscribeMessage(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    log.info("[HttpEventAndSubscribeUPProcessor] 处理订阅消息，设备: {}", ioTDeviceDTO.getDeviceId());

    try {
      // 处理订阅逻辑
      JSONObject subscribeData = source.getJSONObject("subscribe");
      if (subscribeData != null) {
        //        request.setSubscribeData(subscribeData);

        // 这里应该调用订阅服务
        // subscriptionService.handleSubscription(ioTDeviceDTO, subscribeData);
      }

      log.debug("[HttpEventAndSubscribeUPProcessor] 订阅消息处理完成");
    } catch (Exception e) {
      log.error("[HttpEventAndSubscribeUPProcessor] 订阅消息处理失败: ", e);
      throw e;
    }
  }

  /** 处理消息推送 */
  private void handleMessagePush(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    try {
      // 这里应该调用消息推送服务
      // messagePushService.push(request, ioTDeviceDTO);

      log.debug("[HttpEventAndSubscribeUPProcessor] 消息推送完成");
    } catch (Exception e) {
      log.error("[HttpEventAndSubscribeUPProcessor] 消息推送失败: ", e);
      // 推送失败不影响主流程
    }
  }

  /** 处理设备上线事件 */
  private void handleOnlineEvent(HttpUPRequest request, IoTDeviceDTO ioTDeviceDTO) {
    log.info("[HttpEventAndSubscribeUPProcessor] 设备上线事件，设备: {}", ioTDeviceDTO.getDeviceId());
    // 这里应该调用设备生命周期服务
    // deviceLifecycleService.online(ioTDeviceDTO);
  }

  /** 处理设备下线事件 */
  private void handleOfflineEvent(HttpUPRequest request, IoTDeviceDTO ioTDeviceDTO) {
    log.info("[HttpEventAndSubscribeUPProcessor] 设备下线事件，设备: {}", ioTDeviceDTO.getDeviceId());
    // 这里应该调用设备生命周期服务
    // deviceLifecycleService.offline(ioTDeviceDTO);
  }

  /** 处理告警事件 */
  private void handleAlarmEvent(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO) {
    log.info("[HttpEventAndSubscribeUPProcessor] 告警事件，设备: {}", ioTDeviceDTO.getDeviceId());
    // 这里应该调用告警服务
    // alarmService.handleAlarm(ioTDeviceDTO, source);
  }

  /** 处理自定义事件 */
  private void handleCustomEvent(
      HttpUPRequest request, JSONObject source, IoTDeviceDTO ioTDeviceDTO, String eventType) {
    log.info(
        "[HttpEventAndSubscribeUPProcessor] 自定义事件，类型: {}, 设备: {}",
        eventType,
        ioTDeviceDTO.getDeviceId());
    // 这里应该调用自定义事件处理服务
    // customEventService.handle(eventType, ioTDeviceDTO, source);
  }
}

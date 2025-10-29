/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.rule.scene.deviceUp;

import cn.universal.common.constant.IoTConstant;
import cn.universal.core.message.UPRequest;
import cn.universal.persistence.entity.bo.TriggerBO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeviceEventUp extends AbstractDeviceUp implements DeviceUp {

  @Override
  public String messageType() {
    return IoTConstant.MessageType.EVENT.name();
  }

  @Override
  public boolean testAlarm(List<TriggerBO> triggers, String separator, UPRequest upRequest) {
    String express =
        triggers.stream()
            .map(triggerBo -> String.format("'%s'== event", triggerBo.getModelId()))
            .collect(Collectors.joining(separator));
    Map<String, Object> content = new HashMap<>(2);
    content.put("event", upRequest.getEvent());
    content.put("eventName", upRequest.getEventName());
    content.put("eventData", upRequest.getData());
    log.info("执行场景联动条件,express={},properties={}", express, content);
    return expressTemplate.executeTest(express, content);
  }
}

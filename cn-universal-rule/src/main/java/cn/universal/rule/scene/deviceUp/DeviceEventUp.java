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

package cn.universal.rule.scene.deviceUp;

import cn.hutool.json.JSONObject;
import cn.universal.persistence.entity.bo.TriggerBO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DeviceEventUp extends AbstractDeviceUp implements DeviceUp {

  @Override
  public String messageType() {
    return "EVENT";
  }

  @Override
  public boolean testAlarm(List<TriggerBO> triggers, String separator, JSONObject param) {
    String express =
        triggers.stream()
            .map(triggerBo -> String.format("'%s'== event", triggerBo.getModelId()))
            .collect(Collectors.joining(separator));
    Map<String, Object> content = new HashMap<>(2);
    content.put("event", param.getStr("event"));
    return expressTemplate.executeTest(express, content);
  }
}

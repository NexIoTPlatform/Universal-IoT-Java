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

package cn.universal.persistence.entity;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_device_protocol")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IoTDeviceProtocol implements Serializable {

  private static final long serialVersionUID = 1L;
  private String name;
  private String description;
  private Byte state;
  @Id private String id;
  private String type;
  private String configuration;
  private String example;

  public ProtocolSupportDefinition toDefinition() {
    ProtocolSupportDefinition definition = new ProtocolSupportDefinition();
    JSONObject object = JSONUtil.parseObj(getConfiguration());
    definition.setConfiguration(object);
    definition.setDescription(getDescription());
    definition.setId(getId());
    definition.setName(getName());
    definition.setProvider(object.getStr("provider"));
    definition.setType(getType());
    definition.setState(getState());
    if (object != null && object.containsKey("supportMethods")) {
      JSONArray jsonArray = object.getJSONArray("supportMethods");
      definition.setSupportMethods(
          jsonArray.stream()
              .map(
                  s -> {
                    return (String) s;
                  })
              .collect(Collectors.toSet()));
    }
    return definition;
  }

  public ProtocolSupportDefinition toDefinitionNoScript() {
    ProtocolSupportDefinition definition = new ProtocolSupportDefinition();
    JSONObject object = JSONUtil.parseObj(getConfiguration());
    // 把几百行的内容删掉
    if (object != null && object.containsKey("location")) {
      object.remove("location");
    }
    definition.setConfiguration(object);
    definition.setDescription(getDescription());
    definition.setId(getId());
    definition.setName(getName());
    definition.setProvider(object.getStr("provider"));
    definition.setType(getType());
    definition.setState(getState());
    if (object != null && object.containsKey("supportMethods")) {
      JSONArray jsonArray = object.getJSONArray("supportMethods");
      definition.setSupportMethods(
          jsonArray.stream()
              .map(
                  s -> {
                    return (String) s;
                  })
              .collect(Collectors.toSet()));
    }
    return definition;
  }
}

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

package cn.universal.core.metadata;

import cn.hutool.json.JSONObject;
import cn.universal.core.metadata.types.UnknownType;
import cn.universal.core.metadata.types.ValueTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;

/**
 * @since 1.0.0
 */
public class EventMetadata implements AbstractEventMetadata {

  @JsonIgnore private JSONObject jsonObject;

  @JsonIgnore private ValueType valueType;

  @Getter @Setter private String id;

  @Getter @Setter private String name;

  @Getter @Setter private String description;

  @Getter @Setter private Map<String, Object> expands;

  /** 默认构造函数，用于Jackson反序列化 */
  public EventMetadata() {
    // 初始化空对象，用于Jackson反序列化
  }

  public EventMetadata(JSONObject jsonObject) {
    fromJson(jsonObject);
  }

  public EventMetadata(AbstractEventMetadata another) {
    this.id = another.getId();
    this.name = another.getName();
    this.description = another.getDescription();
    this.expands = another.getExpands();
    this.valueType = another.getValueType();
    this.expands = another.getExpands() == null ? null : new HashMap<>(another.getExpands());
  }

  @Override
  public ValueType getValueType() {
    if (valueType == null) {
      if (jsonObject != null) {
        JSONObject typeJson = jsonObject.getJSONObject("valueType");

        if (typeJson == null) {
          // 如果valueType为空，返回UnknownType
          valueType = new UnknownType();
        } else {
          valueType =
              Optional.ofNullable(typeJson.getStr("type"))
                  .map(ValueTypes::lookup)
                  .map(Supplier::get)
                  .orElseGet(UnknownType::new);

          valueType = ValueTypeCodecs.decode(valueType, typeJson);
        }
      } else {
        // 如果jsonObject为空，返回UnknownType
        valueType = new UnknownType();
      }
    }
    //    if (valueType == null && another != null) {
    //      type = another.getType();
    //    }
    return valueType;
  }

  @Override
  public JSONObject toJson() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.set("id", id);
    jsonObject.set("name", name);
    jsonObject.set("description", description);
    jsonObject.set("valueType", ValueTypeCodecs.encode(getValueType()).orElse(null));
    jsonObject.set("expands", expands);
    return jsonObject;
  }

  @Override
  public void fromJson(JSONObject json) {
    if (json != null) {
      this.jsonObject = json;
      this.valueType = null;
      this.id = json.getStr("id");
      this.name = json.getStr("name");
      this.description = json.getStr("description");
      this.expands = json.getJSONObject("expands");
    }
  }
}

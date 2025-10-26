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

package cn.universal.core.metadata.types;

import cn.universal.core.metadata.Converter;
import cn.universal.core.metadata.ValidateResult;
import cn.universal.core.metadata.ValueType;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileType extends AbstractType<FileType> implements ValueType, Converter<String> {

  public static final String ID = "file";

  private BodyType bodyType = BodyType.url;

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getName() {
    return "文件";
  }

  public FileType bodyType(BodyType type) {
    this.bodyType = type;
    return this;
  }

  @Override
  public ValidateResult validate(Object value) {
    return ValidateResult.success(String.valueOf(value));
  }

  @Override
  public String format(Object value) {
    return String.valueOf(value);
  }

  @Override
  public String convert(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  public enum BodyType {
    url,
    base64,
    binary;

    public static Optional<BodyType> of(String name) {
      if (name == null) {
        return Optional.empty();
      }
      for (BodyType value : values()) {
        if (value.name().equalsIgnoreCase(name)) {
          return Optional.of(value);
        }
      }
      return Optional.empty();
    }
  }
}

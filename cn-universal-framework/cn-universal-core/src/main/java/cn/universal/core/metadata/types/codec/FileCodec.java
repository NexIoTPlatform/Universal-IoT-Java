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

package cn.universal.core.metadata.types.codec;

import cn.universal.core.metadata.types.FileType;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileCodec extends AbstractValueTypeCodec<FileType> {

  @Override
  public String getTypeId() {
    return FileType.ID;
  }

  @Override
  public FileType decode(FileType type, Map<String, Object> config) {
    super.decode(type, config);

    Optional.ofNullable(config.get("bodyType"))
        .map(String::valueOf)
        .flatMap(FileType.BodyType::of)
        .ifPresent(type::setBodyType);

    return type;
  }

  @Override
  protected void doEncode(Map<String, Object> encoded, FileType type) {
    super.doEncode(encoded, type);
    encoded.put("bodyType", type.getBodyType().name());
  }
}

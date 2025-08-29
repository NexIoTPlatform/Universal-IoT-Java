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

package cn.universal.core.metadata.types.codec;

import cn.universal.core.metadata.types.GeoShapeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoShapeCodec extends AbstractValueTypeCodec<GeoShapeType> {

  @Override
  public String getTypeId() {
    return GeoShapeType.ID;
  }
}

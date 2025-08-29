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

package cn.universal.persistence.entity;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IoTDeviceProperties implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 属性标识 */
  private String id;

  /** 属性名称 */
  private String name;

  /** 数据类型 */
  private String type;

  /** 属性值来源 */
  private String source;

  /** 单位 */
  private String unit;

  /** 枚举键值 */
  private String elements;

  /** 描述 */
  private String description;

  /** 读写 */
  private String mode;
}

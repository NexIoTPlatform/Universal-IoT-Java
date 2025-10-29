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

package cn.universal.persistence.entity.bo;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTProductMetadataBO implements Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String name;
  private String source;
  private boolean config;
  private Expands expands;
  private ValueType valueType;
  private List<Inputs> inputs;
  private Output output;
  private String mode;
  private String description;

  @Data
  @NoArgsConstructor
  public static class Expands {

    private String level;
    private String maxLength;
    private String readOnly;
    private String source;
  }

  @Data
  @NoArgsConstructor
  public static class ValueType {

    private String type;
    private Expands expands;
    private List<Elements> elements;
    private String unit;
    private String min;
    private String max;
    private String precision;
  }

  @Data
  @NoArgsConstructor
  public static class Elements {

    private String value;
    private String text;
  }

  @Data
  @NoArgsConstructor
  public static class Inputs {

    private String id;
    private String name;
    private ValueType valueType;
    private String description;
  }

  @Data
  @NoArgsConstructor
  public static class Output {

    private String id;
    private String name;
    private ValueType valueType;
    private String description;
  }
}

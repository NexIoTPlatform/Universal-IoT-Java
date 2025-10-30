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

package cn.universal.persistence.entity.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品导出完整包DTO - 包含产品、协议、网络配置、物模型等完整信息 支持导出后直接导入使用，无需额外配置
 *
 * @author NexIoT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportPackageDTO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 导出版本号，用于兼容性检查 */
  private String exportVersion = "1.0";

  /** 导出时间戳 */
  private Long exportTime;

  /** 导出用户ID */
  private String exportUserId;

  /** 产品基本信息 */
  private ProductInfoDTO product;

  /** 设备协议信息（如果有自定义协议） */
  private DeviceProtocolDTO protocol;

  /** 网络配置信息（TCP/MQTT等） */
  private NetworkConfigDTO network;

  /** 产品基本信息 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ProductInfoDTO implements Serializable {

    /** 产品编号 */
    private String productId;

    /** 产品KEY - 导入时会重新生成 */
    private String productKey;

    /** 产品密钥 - 导入时会重新生成 */
    private String productSecret;

    /** 产品标签 */
    private String tags;

    /** 接入方式：tcp, mqtt, ctaiot, onenet等 */
    private String thirdPlatform;

    /** 第三方平台配置信息 */
    private String thirdConfiguration;

    /** 厂商编号 */
    private String companyNo;

    /** 分类ID - 导入时使用默认分类100100 */
    private String classifiedId;

    /** 分类名称 */
    private String classifiedName;

    /** 设备类型: GATEWAY, DEVICE, GATEWAY_SUB_DEVICE */
    private String deviceNode;

    /** 所属网关ProductKey - 如果是子设备 */
    private String gwProductKey;

    /** 消息协议 */
    private String messageProtocol;

    /** 产品名称 */
    private String name;

    /** 产品状态 */
    private Byte state;

    /** 产品描述 */
    private String describe;

    /** 数据存储策略：mysql, influxdb, iotdb等 */
    private String storePolicy;

    /** 传输协议: TCP, MQTT, COAP, UDP */
    private String transportProtocol;

    /** 图片地址 */
    private String photoUrl;

    /** 协议配置 */
    private String configuration;

    /** 数据存储策略配置 */
    private String storePolicyConfiguration;

    /** 物模型JSON */
    private String metadata;

    /** 第三方平台产品下发信息（仅CTWing等需要） */
    private String thirdDownRequest;
  }

  /** 设备协议信息 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeviceProtocolDTO implements Serializable {

    /** 协议名称 */
    private String name;

    /** 协议描述 */
    private String description;

    /** 协议状态 */
    private Byte state;

    /** 协议类型：magic, jar等 */
    private String type;

    /** 协议配置（包含encode/decode脚本） */
    private String configuration;

    /** 协议示例 */
    private String example;
  }

  /** 网络配置信息 */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NetworkConfigDTO implements Serializable {

    /** 网络类型：TCP_SERVER, TCP_CLIENT, MQTT_SERVER, MQTT_CLIENT等 */
    private String type;

    /** 网络名称 */
    private String name;

    /** 网络描述 */
    private String description;

    /** 网络状态 */
    private Boolean state;

    /** 网络配置JSON */
    private String configuration;
  }
}

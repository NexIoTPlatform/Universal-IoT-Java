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

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTProductBO implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id private Long id;

  /** 产品编号 */
  @Column(name = "product_id")
  private String productId;

  /** 产品KEY */
  @Column(name = "product_key")
  private String productKey;

  /** 产品密钥（1型1密） */
  @Column(name = "product_secret")
  private String productSecret;

  /** 第三方平台:ctwing,onenet,alibaba,baidu */
  @Column(name = "third_platform")
  private String thirdPlatform;

  /** 第三方平台配置信息 */
  @Column(name = "third_configuration")
  private String thirdConfiguration;

  /** 厂商编号 */
  @Column(name = "company_no")
  private String companyNo;

  /** 分类ID */
  @Column(name = "classified_id")
  private String classifiedId;

  /** 网络组件：关联network表 */
  @Column(name = "network_union_id")
  private String networkUnionId;

  /** 设备类型: 网关，设备 */
  @Column(name = "device_node")
  private String deviceNode;

  /** 所属项目 */
  @Column(name = "gw_product_key")
  private String gwProductKey;

  /** 分类名称 */
  @Column(name = "classified_name")
  private String classifiedName;

  /** 消息协议: */
  @Column(name = "message_protocol")
  private String messageProtocol;

  /** 名称 */
  private String name;

  /** 创建者id */
  @Column(name = "creator_id")
  private String creatorId;

  /** 产品状态 */
  private Byte state;

  /** 说明 */
  @Column(name = "`describe`")
  private String describe;

  /** 数据存储策略 */
  @Column(name = "store_policy")
  private String storePolicy;

  /** 传输协议: MQTT,COAP,UDP */
  @Column(name = "transport_protocol")
  private String transportProtocol;

  /** 图片地址 */
  @Column(name = "photo_url")
  private String photoUrl;

  /** 创建时间 */
  @Column(name = "create_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date createTime;

  /** 产品标签 */
  @Column(name = "tags")
  private String tags;

  /** 协议配置 */
  private String configuration;

  /** 数据存储策略配置 */
  @Column(name = "store_policy_configuration")
  private String storePolicyConfiguration;

  /** 物模型 */
  private String metadata;

  /** 电信产品电量模式 */
  private int powerModel;

  /** 电信Edrx时间 */
  private String lwm2mEdrxTime;

  private String path;
  private String jsonStr;
  private String type;
  private String oldId;
  private IoTProductMetadataBO ioTProductMetadataBO;

  public void toJsonObj() {
    jsonStr = JSONUtil.parseObj(ioTProductMetadataBO).toString();
  }
}

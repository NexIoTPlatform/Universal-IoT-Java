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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "iot_network")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Network implements Serializable {

  @Id
  private Integer id;

  /**
   * TCP_CLIENT,MQTT_CLIENT,HTTP_CLIENT,WEB_SOCKET_CLIENT
   */
  private String type;

  /**
   * 唯一标识
   */
  @Column(name = "product_key")
  private String productKey;

  @Column(name = "union_id")
  private String unionId;

  private String name;

  /**
   * 详细描述
   */
  private String description;

  @Column(name = "create_date")
  private Date createDate;

  /**
   * enable,disable
   */
  private Boolean state;

  /**
   * 配置内容
   */
  private String configuration;

  /**
   * 创建用户
   */
  @Column(name = "create_user")
  private String createUser;

  private static final long serialVersionUID = 1L;
}

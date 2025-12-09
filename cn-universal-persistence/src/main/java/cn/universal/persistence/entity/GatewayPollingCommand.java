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

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关轮询指令实体
 * 
 * @author Aleo
 * @date 2025-10-26
 */
@Data
@Table(name = "iot_gateway_polling_command")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayPollingCommand implements Serializable {

  private static final long serialVersionUID = 1L;

  /** 主键ID */
  @Id
  private Long id;

  /** 网关产品KEY */
  @Column(name = "gateway_product_key")
  private String gatewayProductKey;

  /** 网关设备ID */
  @Column(name = "gateway_device_id")
  private String gatewayDeviceId;

  /** 从站设备ID (可选) */
  @Column(name = "slave_device_id")
  private String slaveDeviceId;

  /** 指令名称 */
  @Column(name = "command_name")
  private String commandName;

  /** 执行顺序 */
  @Column(name = "execution_order")
  private Integer executionOrder;

  /** 完整的轮询指令(HEX格式) */
  @Column(name = "command_hex")
  private String commandHex;

  /** 指令类型: MODBUS/S7/OPCUA/CUSTOM */
  @Column(name = "command_type")
  private String commandType;

  /** 协议参数JSON (用于前端回显编辑) */
  @Column(name = "protocol_params")
  private String protocolParams;

  /** 属性映射JSON (寄存器->物模型属性) */
  @Column(name = "property_mapping")
  private String propertyMapping;

  /** 数据解析脚本 (可选) */
  @Column(name = "data_parser_script")
  private String dataParserScript;

  /** 是否启用 */
  @Column(name = "enabled")
  private Boolean enabled;

  /** 超时时间(ms) */
  @Column(name = "timeout_ms")
  private Integer timeoutMs;

  /** 描述 */
  @Column(name = "description")
  private String description;

  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "create_time")
  private Date createTime;

  /** 更新时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "update_time")
  private Date updateTime;
}

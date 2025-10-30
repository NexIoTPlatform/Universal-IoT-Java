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

package cn.universal.databridge.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 资源连接配置实体
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "iot_resource_connection")
public class ResourceConnection implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 资源名称 */
  @Column(name = "name")
  private String name;

  /** 资源类型 */
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private ResourceType type;

  /** 插件类型 */
  @Column(name = "plugin_type")
  private String pluginType;

  /** 主机地址 */
  @Column(name = "host")
  private String host;

  /** 端口号 */
  @Column(name = "port")
  private Integer port;

  /** 用户名 */
  @Column(name = "username")
  private String username;

  /** 密码 */
  @Column(name = "password")
  private String password;

  /** 数据库名 */
  @Column(name = "database_name")
  private String databaseName;

  /** 扩展配置JSON */
  @Column(name = "extra_config")
  private String extraConfig;

  /** 状态：0禁用，1启用 */
  @Column(name = "status")
  private Integer status;

  /** 描述 */
  @Column(name = "description")
  private String description;

  /** 创建者 */
  @Column(name = "create_by")
  private String createBy;

  /** 创建时间 */
  @Column(name = "create_time")
  private LocalDateTime createTime;

  /** 更新者 */
  @Column(name = "update_by")
  private String updateBy;

  /** 更新时间 */
  @Column(name = "update_time")
  private LocalDateTime updateTime;

  /** 方向：IN-输入，OUT-输出，BOTH-双向 */
  @Column(name = "direction")
  @Enumerated(EnumType.STRING)
  private Direction direction;

  /** 数据流向：INPUT-输入，OUTPUT-输出，BIDIRECTIONAL-双向 */
  @Column(name = "data_direction")
  @Enumerated(EnumType.STRING)
  private DataDirection dataDirection;

  /** 动态配置JSON - 使用现有的extra_config字段存储 */
  @Transient private String dynamicConfig;

  /** 资源类型枚举 */
  public enum ResourceType {
    MYSQL,
    KAFKA,
    IOTDB,
    INFLUXDB,
    MQTT,
    HTTP,
    REDIS,
    ELASTICSEARCH,
    ALIYUN_IOT,
    TENCENT_IOT,
    HUAWEI_IOT
  }

  /** 方向枚举 */
  public enum Direction {
    IN, // 输入
    OUT, // 输出
    BOTH // 双向
  }

  /** 数据流向枚举 */
  public enum DataDirection {
    INPUT, // 数据输入
    OUTPUT, // 数据输出
    BIDIRECTIONAL // 双向流转
  }
}

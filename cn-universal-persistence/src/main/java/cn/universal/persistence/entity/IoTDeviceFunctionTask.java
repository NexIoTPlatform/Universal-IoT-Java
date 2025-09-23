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

import cn.universal.persistence.common.inteceptor.SQenGenId;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/3/21
 */
@Table(name = "iot_device_function_task")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceFunctionTask implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @KeySql(genId = SQenGenId.class)
  private Long id;

  private String taskName;
  private String productKey;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date beginTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date endTime;

  private String creator;
  private String creatorId;
  private String command;
  private String commandData;

  /** 状态 0.待执行；1.已执行；2.正在执行 */
  private Integer status;
}

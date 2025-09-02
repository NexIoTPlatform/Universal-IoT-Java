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

package cn.universal.persistence.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/3/21
 */
@Data
public class IoTDeviceFunctionHistoryVO implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  private Long id;
  private String iotId;
  private String productKey;
  private String deviceId;
  private String deviceName;

  @Schema(description = "指令配置状态  0.待下发；1.下发中；2.已下发")
  private Integer downState;

  @Schema(description = "下发结果 0.失败  1.成功")
  private Integer downResult;

  private String downError;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date updateTime;

  private Long taskId;
  private Integer retry;
  private String commandData;
}

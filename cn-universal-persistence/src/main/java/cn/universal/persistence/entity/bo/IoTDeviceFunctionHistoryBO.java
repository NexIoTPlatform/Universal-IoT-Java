/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.entity.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/3/21
 */
@Data
public class IoTDeviceFunctionHistoryBO implements Serializable {

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
}

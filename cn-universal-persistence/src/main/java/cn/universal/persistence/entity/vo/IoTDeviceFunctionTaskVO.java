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

package cn.universal.persistence.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/3/21
 */
@Data
public class IoTDeviceFunctionTaskVO implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id private Long id;
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
  private Integer successNum;
  private Integer totalNum;
  private Integer status;
}

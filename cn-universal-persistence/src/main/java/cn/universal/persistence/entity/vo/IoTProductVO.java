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

import cn.universal.persistence.entity.IoTProduct;
import lombok.Data;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/11/16
 */
@Data
public class IoTProductVO extends IoTProduct {

  private String image;
  private int powerModel;
  private String lwm2mEdrxTime;

  private Long createTime;

  private int devNum;

  private String storePolicy;
  private String type;
  private String gwName;
}

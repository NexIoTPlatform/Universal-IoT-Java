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

package cn.universal.persistence.query;

import lombok.Data;

/**
 * @Author gitee.com/NexIoT
 *
 * @since 2018年12月17日 上午10:59
 */
@Data
public class BasePage {

  private Integer page = 1;
  private Integer size = 10;
  private Integer pageNum = 1;
  private Integer pageSize = 10;
  // 特色场景使用
  private Integer halfSize;

  public Integer getHalfSize() {
    return pageSize / 2;
  }
}

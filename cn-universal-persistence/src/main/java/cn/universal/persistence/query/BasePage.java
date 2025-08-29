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

package cn.universal.persistence.query;

import lombok.Data;

/**
 * @Author Aleo
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

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

package cn.universal.admin.system.service;

import cn.universal.persistence.dto.IndexQueryDTO;

/**
 * 首页统计服务
 *
 * @since 2023/8/12 16:51
 */
public interface IndexQueryService {

  /** 查询首页数量 */
  IndexQueryDTO queryIndexQty(String creator);
}

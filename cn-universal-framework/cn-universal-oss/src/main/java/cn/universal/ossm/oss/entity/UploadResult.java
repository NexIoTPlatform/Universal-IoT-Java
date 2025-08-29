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

package cn.universal.ossm.oss.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/** 上传返回体 @Author Lion Li */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UploadResult {

  /** 文件路径 */
  private String url;

  /** 文件名 */
  private String filename;
}

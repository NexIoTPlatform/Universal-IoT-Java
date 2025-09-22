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

package cn.universal.ossm.oss.enumd;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** minio策略配置 @Author Lion Li */
@Getter
@AllArgsConstructor
public enum PolicyType {

  /** 只读 */
  READ("read-only"),

  /** 只写 */
  WRITE("write-only"),

  /** 读写 */
  READ_WRITE("read-write");

  /** 类型 */
  private final String type;
}

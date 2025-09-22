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

import cn.universal.ossm.oss.service.impl.AliyunCloudStorageServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 云存储服务商枚举 @Author Lion Li */
@Getter
@AllArgsConstructor
public enum CloudServiceEnumd {

  /** 七牛云 */
  //  QINIU("qiniu", QiniuCloudStorageServiceImpl.class),

  /** 阿里云 */
  ALIYUN("aliyun", AliyunCloudStorageServiceImpl.class);

  /** 腾讯云 */
  //  QCLOUD("qcloud", QcloudCloudStorageServiceImpl.class),

  /** minio */
  //  MINIO("minio", MinioCloudStorageServiceImpl.class);

  private final String value;

  private final Class<?> serviceClass;

  public static Class<?> getServiceClass(String value) {
    for (CloudServiceEnumd clazz : values()) {
      if (clazz.getValue().equals(value)) {
        return clazz.getServiceClass();
      }
    }
    return null;
  }
}

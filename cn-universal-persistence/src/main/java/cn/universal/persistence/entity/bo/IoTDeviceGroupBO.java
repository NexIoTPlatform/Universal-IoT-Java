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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IoTDeviceGroupBO {

  private static final long serialVersionUID = 1L;

  /** 分组ID，非自增 */
  private Long id;

  /** 设备id */
  private String[] devIds;

  /** 分组名称 */
  private String groupName;

  /** 分组标识 */
  private String groupCode;

  /** 群组描述 */
  private String groupDescribe;

  /** 父id */
  private Long parentId;
  //  //账号父id
  //  private String parentUnionId;
}

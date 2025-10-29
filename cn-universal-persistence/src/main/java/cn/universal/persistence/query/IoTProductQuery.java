/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.persistence.query;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/11/15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IoTProductQuery extends BasePage {

  private Long id;
  private String productId;
  private String productKey;
  private String networkUnionId;
  private String classifiedName;
  private String companyNo;
  private String messageProtocol;
  private String describe;
  private String storePolicy;
  private String gwProductKey;
  private String thirdPlatform;
  private String deviceNode;
  private String creatorId;
  private String name;
  private String transportProtocol;
  private Integer state;
  private String createTime;
  private String classifiedId;
  private String tags;

  public String[] getTags() {
    if (StrUtil.isNotBlank(tags)) {
      return tags.split(",");
    }
    return null;
  }

  /** 存在设备 */
  private boolean hasDevice;

  /** 是否自有 */
  private boolean self;
}

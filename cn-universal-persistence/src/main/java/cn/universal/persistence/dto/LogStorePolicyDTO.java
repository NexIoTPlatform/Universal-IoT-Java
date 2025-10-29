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

package cn.universal.persistence.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2023/4/27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogStorePolicyDTO {
  @Builder.Default private Map<String, StorePolicy> properties = new HashMap<String, StorePolicy>();
  @Builder.Default private Map<String, StorePolicy> event = new HashMap<String, StorePolicy>();

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class StorePolicy {

    /** 属性英文名 */
    private String id;

    /** 最大存储条数 */
    private int maxStorage;
  }
}

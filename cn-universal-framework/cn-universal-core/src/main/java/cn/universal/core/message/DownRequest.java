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

package cn.universal.core.message;

import cn.universal.common.constant.IoTConstant.DownCmd;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/8/13 9:34
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class DownRequest extends Request implements Serializable {

  /* 网关绑定设备 ProductKey */
  private String gwProductKey;
  /* 网关绑定设备deviceId */
  private String gwDeviceId;

  /** 设备密钥 */
  private String deviceKey;

  /** 命令名称 */
  private DownCmd cmd;

  /** 消息标识符 */
  private String msgId;

  // 方法调用参数
  /** 备注 */
  private String detail;

  private Map<String, Object> function;

  private Long storeTime;

  /** 设备复用 */
  private boolean deviceReuse;

  /** 是否超管 */
  private boolean isAdmin;

  /** 是否自动注册 */
  private boolean allowInsert;
}

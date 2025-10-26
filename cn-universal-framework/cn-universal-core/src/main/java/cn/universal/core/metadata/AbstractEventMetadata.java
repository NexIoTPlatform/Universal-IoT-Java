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

package cn.universal.core.metadata;

/**
 * 事件元数据
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/8/25
 */
public interface AbstractEventMetadata extends Metadata, Jsonable {

  ValueType getValueType();
}

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

package cn.universal.rule.scene.deviceUp;

import cn.hutool.json.JSONObject;
import cn.universal.persistence.dto.IoTDeviceDTO;

public interface DeviceUp {

  String messageType();

  void consumer(JSONObject object, IoTDeviceDTO ioTDeviceDTO);
}

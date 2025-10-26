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

package cn.universal.rule.scene.deviceUp;

import cn.hutool.json.JSONObject;
import cn.universal.persistence.entity.bo.TriggerBO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DeviceReplyUp extends AbstractDeviceUp implements DeviceUp {

  @Override
  public String messageType() {
    return "REPLY";
  }

  @Override
  public boolean testAlarm(List<TriggerBO> triggers, String separator, JSONObject param) {
    return false;
  }
}

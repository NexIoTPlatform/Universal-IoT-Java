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

package cn.universal.rule.scene.deviceUp;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.core.message.UPRequest;
import cn.universal.persistence.entity.bo.TriggerBO;
import cn.universal.persistence.entity.bo.TriggerBO.Operator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DevicePropertiesUp extends AbstractDeviceUp implements DeviceUp {

  @Override
  public String messageType() {
    return IoTConstant.MessageType.PROPERTIES.name();
  }

  @Override
  public boolean testAlarm(List<TriggerBO> triggers, String separator, UPRequest upRequest) {
    Map<String, Object> properties = upRequest.getProperties();
    String express =
        triggers.stream()
            .map(
                triggerBo -> {
                  String filterExpress =
                      triggerBo.getFilters().stream()
                          .filter(item -> properties.containsKey(item.getKey()))
                          .map(
                              filter ->
                                  String.format(
                                      "%s %s %s",
                                      filter.getKey(),
                                      Operator.valueOf(filter.getOperator()).getSymbol(),
                                      NumberUtil.isNumber(filter.getValue())
                                          ? filter.getValue()
                                          : String.format("'%s'", filter.getValue())))
                          .collect(Collectors.joining(separator));
                  return StrUtil.isEmpty(filterExpress) ? "" : String.format("(%s)", filterExpress);
                })
            .filter(StrUtil::isNotEmpty)
            .collect(Collectors.joining(separator));
    if (StrUtil.isEmpty(express)) {
      return false;
    }
    log.info("执行场景联动条件,express={},properties={}", express, properties);
    return expressTemplate.executeTest(express, properties);
  }
}

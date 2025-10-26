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

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.persistence.entity.bo.TriggerBO;
import cn.universal.persistence.entity.bo.TriggerBO.Operator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DevicePropertiesUp extends AbstractDeviceUp implements DeviceUp {

  @Override
  public String messageType() {
    return "PROPERTIES";
  }

  @Override
  public boolean testAlarm(List<TriggerBO> triggers, String separator, JSONObject param) {
    Map<String, Object> properties = JSONUtil.parseObj(param.getStr("properties"));
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
                          .collect(Collectors.joining("||"));
                  return StringUtils.isEmpty(filterExpress)
                      ? ""
                      : String.format("(%s)", filterExpress);
                })
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.joining(separator));
    if (StringUtils.isEmpty(express)) {
      return false;
    }
    return expressTemplate.executeTest(express, properties);
  }
}

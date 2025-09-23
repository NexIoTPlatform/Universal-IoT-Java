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

package cn.universal.dm.device.service.wrapper;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.domain.R;
import cn.universal.core.message.DownRequest;
import cn.universal.dm.device.service.impl.IoTCacheRemoveService;
import cn.universal.persistence.base.IoTDownWrapper;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.shadow.Shadow;
import cn.universal.persistence.shadow.State;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service("iotDeviceDownFunctionWrapper")
@Slf4j
public class IoTDeviceDownFunctionWrapper implements IoTDownWrapper {

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  @Resource private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Resource private StringRedisTemplate stringRedisTemplate;

  // 保存功能期望值
  @Override
  public R beforeFunctionOrConfigDown(
      IoTProduct product, IoTDevice ioTDevice, DownRequest downRequest) {
    if (downRequest.getFunction() == null) {
      return null;
    }
    String function = downRequest.getFunction().get("function").toString();
    JSONObject data = JSONUtil.parseObj(downRequest.getFunction().get("data"));
    // 当方法是set开头且data里不为空 保存data内的数据到影子期望值
    if (function.startsWith("set") && ObjectUtil.isNotEmpty(data)) {
      // TODO: 判断物模型属性是否有可读写的

      doShadow(ioTDevice, data);
    }
    return null;
  }

  private void doShadow(IoTDevice ioTDevice, JSONObject data) {
    IoTDeviceShadow ioTDeviceShadow =
        IoTDeviceShadow.builder()
            .deviceId(ioTDevice.getDeviceId())
            .productKey(ioTDevice.getProductKey())
            .iotId(ioTDevice.getIotId())
            .build();
    ioTDeviceShadow = ioTDeviceShadowMapper.selectOne(ioTDeviceShadow);
    // 影子不存在时创建
    Boolean flag =
        stringRedisTemplate
            .opsForValue()
            .setIfAbsent("doCreateShadow:" + ioTDevice.getIotId(), "1", 10, TimeUnit.MINUTES);
    if (ObjectUtil.isNull(ioTDeviceShadow) && flag) {
      ioTDeviceShadow =
          IoTDeviceShadow.builder()
              .iotId(ioTDevice.getIotId())
              .productKey(ioTDevice.getProductKey())
              .extDeviceId(ioTDevice.getExtDeviceId())
              .deviceId(ioTDevice.getDeviceId())
              .activeTime(new Date())
              .onlineTime(new Date())
              .updateDate(new Date())
              .lastTime(new Date())
              .build();
      State state = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();
      State metadata = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();
      Shadow shadow =
          Shadow.builder()
              .state(state)
              .metadata(metadata)
              .timestamp(DateUtil.currentSeconds())
              .version(1L)
              .build();
      ioTDeviceShadow.setMetadata(JSONUtil.toJsonStr(shadow));
      ioTDeviceShadowMapper.insert(ioTDeviceShadow);
      // 代理调用内部方法，清除缓存
      iotCacheRemoveService.removeDevInstanceBOCache();
    }

    Shadow shadow = null;
    if (StrUtil.isBlank(ioTDeviceShadow.getMetadata())) {
      shadow = Shadow.builder().timestamp(DateUtil.currentSeconds()).version(1L).build();
    } else {
      shadow = JSONUtil.toBean(ioTDeviceShadow.getMetadata(), Shadow.class);
    }
    doDesired(shadow, ioTDevice, data);
    ioTDeviceShadow.setMetadata(JSONUtil.toJsonStr(shadow));
    ioTDeviceShadow.setLastTime(new Date());
    ioTDeviceShadow.setUpdateDate(new Date());
    ioTDeviceShadowMapper.updateByPrimaryKeySelective(ioTDeviceShadow);
  }

  private void doDesired(Shadow shadow, IoTDevice ioTDevice, JSONObject data) {
    // 设置期望值
    shadow.getState().getDesired().putAll(data);
    // 设置期望值时间
    JSONObject metaTime = new JSONObject();
    Timestamp timestamp = new Timestamp(DateUtil.currentSeconds());

    for (String key : data.keySet()) {
      metaTime.set(key, timestamp);
    }
    shadow.getMetadata().getDesired().putAll(metaTime);
  }

  @AllArgsConstructor
  @Getter
  private class Timestamp {

    private Long timestamp;
  }
}

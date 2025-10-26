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

package cn.universal.admin.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.system.service.IoTDeviceSubscribeService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DeviceSubscribe;
import cn.universal.common.domain.R;
import cn.universal.common.exception.IoTErrorCode;
import cn.universal.dm.device.service.impl.IoTCacheRemoveService;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTDeviceSubscribeMapper;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * subscribe @Author ruoyi
 *
 * @since 2023-01-06
 */
@Service
public class IoTDeviceSubscribeServiceImpl extends BaseServiceImpl
    implements IoTDeviceSubscribeService {

  @Resource private IoTDeviceSubscribeMapper ioTDeviceSubscribeMapper;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  @Override
  public List<IoTDeviceSubscribe> selectDevSubscribeList(IoTDeviceSubscribe sub) {
    List<IoTDeviceSubscribe> subscribes = ioTDeviceSubscribeMapper.selectSubscribesBO(sub);
    subscribes.forEach(
        (item) -> {
          if (StrUtil.isNotBlank(item.getUrl()) && StrUtil.isNotBlank(item.getTopic())) {
            item.setUrl(item.getUrl() + "," + item.getTopic());
          } else if (StrUtil.isNotBlank(item.getUrl())) {
            item.setUrl(item.getUrl());
          } else if (StrUtil.isNotBlank(item.getTopic())) {
            item.setUrl(item.getTopic());
          }
        });
    return subscribes;
  }

  @Override
  public IoTDeviceSubscribe selectDevInstanceById(String id) {
    return ioTDeviceSubscribeMapper.selectByPrimaryKey(id);
  }

  @Override
  public R updateDevSubscribe(IoTDeviceSubscribe sub) {
    int update = ioTDeviceSubscribeMapper.updateByPrimaryKey(sub);
    if (update >= 1) {
      return R.ok();
    }
    return R.error("更新失败");
  }

  @Override
  public R insertDevSubscribe(IoTDeviceSubscribe sub) {
    IoTDeviceSubscribe build =
        IoTDeviceSubscribe.builder()
            .subType(DeviceSubscribe.DEVICE.name())
            .iotId(sub.getIotId())
            .productKey(sub.getProductKey())
            .build();
    int i = ioTDeviceSubscribeMapper.selectCount(build);
    if (i > IoTConstant.MAX_DEV_MSG_SUBSCRIBE_NUM) {
      return R.error(
          IoTErrorCode.DEV_SUBSCRIBE_REPEAT_ERROR.getCode(),
          IoTErrorCode.DEV_SUBSCRIBE_REPEAT_ERROR.getName());
    }
    IoTDevice ioTDevice =
        ioTDeviceMapper.selectOne(IoTDevice.builder().iotId(sub.getIotId()).build());
    if (ioTDevice != null) {
      sub.setDeviceId(ioTDevice.getDeviceId());
    }
    sub.setCreater(SecurityUtils.getUnionId());
    sub.setInstance("0");

    sub.setCreateDate(new Date());
    sub.setEnabled(true);
    int insert = ioTDeviceSubscribeMapper.insert(sub);
    if (insert >= 1) {
      iotCacheRemoveService.removeIotDeviceSubscribeCache();
      return R.ok();
    }
    return R.error("添加失败");
  }

  @Override
  public R deleteDevSubscribe(String productKey, String iotId, Long[] ids) {
    IoTDevice ioTDevice =
        ioTDeviceMapper.selectOne(IoTDevice.builder().iotId(iotId).productKey(productKey).build());
    if (Objects.isNull(ioTDevice)) {
      return R.error("删除失败,设备不存在");
    }
    if (!SecurityUtils.getUnionId().equals(ioTDevice.getCreatorId())) {
      return R.error("权限不足");
    }
    StringBuffer str = new StringBuffer();
    for (int i = 0; i < ids.length; i++) {
      str.append(ids[i]);
      if ((i + 1) < ids.length) {
        str.append(",");
      }
    }
    int delete = ioTDeviceSubscribeMapper.deleteByIds(str.toString());
    if (delete >= 1) {
      iotCacheRemoveService.removeIotDeviceSubscribeCache();
      return R.ok();
    }
    return R.error("删除失败");
  }
}

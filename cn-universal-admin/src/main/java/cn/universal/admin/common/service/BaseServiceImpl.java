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

package cn.universal.admin.common.service;

import cn.universal.persistence.entity.IoTUser;
import cn.universal.security.service.IoTUserService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;

public class BaseServiceImpl implements BaseService {

  @Resource private IoTUserService ioTUserService;

  @Override
  @Cacheable(cacheNames = "user_parent_entity", key = "''+#unionId", unless = "#result == null")
  public IoTUser queryIotUser(String unionId) {
    IoTUser iotUser = ioTUserService.selectUserByUnionId(unionId);
    if (iotUser.getParentUnionId() != null) {
      iotUser = ioTUserService.selectUserByUnionId(iotUser.getParentUnionId());
      return iotUser;
    }
    return iotUser;
  }
}

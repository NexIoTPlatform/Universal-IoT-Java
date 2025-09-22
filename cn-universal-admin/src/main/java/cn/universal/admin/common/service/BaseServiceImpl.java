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

package cn.universal.admin.common.service;

import cn.universal.admin.system.service.IIotUserService;
import cn.universal.persistence.entity.IoTUser;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;

public class BaseServiceImpl implements BaseService {

  @Resource private IIotUserService iIotUserService;

  @Override
  @Cacheable(cacheNames = "user_parent_entity", key = "''+#unionId", unless = "#result == null")
  public IoTUser queryIotUser(String unionId) {
    IoTUser iotUser = iIotUserService.selectUserByUnionId(unionId);
    if (iotUser.getParentUnionId() != null) {
      iotUser = iIotUserService.selectUserByUnionId(iotUser.getParentUnionId());
      return iotUser;
    }
    return iotUser;
  }
}

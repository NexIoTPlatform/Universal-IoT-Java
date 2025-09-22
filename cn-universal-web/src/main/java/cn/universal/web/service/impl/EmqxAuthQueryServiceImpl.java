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
package cn.universal.web.service.impl;

import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.IoTUserApplicationMapper;
import cn.universal.persistence.mapper.IoTUserMapper;
import cn.universal.web.service.EmqxAuthQueryService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * EMQX 认证查询服务实现类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j
@Service
public class EmqxAuthQueryServiceImpl implements EmqxAuthQueryService {

  @Autowired private IoTProductMapper ioTProductMapper;

  @Autowired private IoTUserApplicationMapper ioTUserApplicationMapper;

  @Autowired private IoTUserMapper ioTUserMapper;

  @Override
  public IoTProduct queryProductByKey(String productKey) {
    try {
      Example example = new Example(IoTProduct.class);
      example
          .createCriteria()
          .andEqualTo("productKey", productKey)
          .andEqualTo("state", 0) // 产品状态正常
          .andEqualTo("isDeleted", 0); // 未删除

      return ioTProductMapper.selectOneByExample(example);
    } catch (Exception e) {
      log.error("查询产品信息失败: productKey={}, error={}", productKey, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public IoTUserApplication queryApplicationById(String appId) {
    try {
      Example example = new Example(IoTUserApplication.class);
      example
          .createCriteria()
          .andEqualTo("appId", appId)
          .andEqualTo("appStatus", 0) // 应用状态正常
          .andEqualTo("deleted", 0); // 未删除

      return ioTUserApplicationMapper.selectOneByExample(example);
    } catch (Exception e) {
      log.error("查询应用信息失败: appId={}, error={}", appId, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public IoTUser queryUserByUsername(String username) {
    try {
      Example example = new Example(IoTUser.class);
      example
          .createCriteria()
          .andEqualTo("username", username)
          .andEqualTo("status", "0") // 用户状态正常
          .andEqualTo("deleted", 0); // 未删除

      return ioTUserMapper.selectOneByExample(example);
    } catch (Exception e) {
      log.error("查询用户信息失败: username={}, error={}", username, e.getMessage(), e);
      return null;
    }
  }

  @Override
  public List<IoTUserApplication> queryApplicationsByUnionId(String unionId) {
    try {
      Example example = new Example(IoTUserApplication.class);
      example
          .createCriteria()
          .andEqualTo("unionId", unionId)
          .andEqualTo("appStatus", 0) // 应用状态正常
          .andEqualTo("deleted", 0) // 未删除
          .andEqualTo("mqttEnable", 1); // MQTT启用

      return ioTUserApplicationMapper.selectByExample(example);
    } catch (Exception e) {
      log.error("查询用户关联应用失败: unionId={}, error={}", unionId, e.getMessage(), e);
      return null;
    }
  }
}

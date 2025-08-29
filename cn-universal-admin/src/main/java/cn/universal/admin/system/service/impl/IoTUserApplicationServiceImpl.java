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

package cn.universal.admin.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.system.service.IIoTUserApplicationService;
import cn.universal.admin.system.service.IIotUserService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.entity.OAuth2ClientDetails;
import cn.universal.persistence.entity.vo.IoTUserApplicationVO;
import cn.universal.persistence.mapper.IoTUserApplicationMapper;
import cn.universal.persistence.mapper.OauthClientDetailsMapper;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Objects;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * 用户应用信息Service业务层处理
 *
 * @since 2025-12-30
 */
@Service
public class IoTUserApplicationServiceImpl extends BaseServiceImpl
    implements IIoTUserApplicationService {

  @Resource private IoTUserApplicationMapper iotUserApplicationMapper;
  @Resource private IIotUserService iIotUserService;

  @Resource private OauthClientDetailsMapper oauthClientDetailsMapper;

  @Override
  public boolean EnDisableIoTUser(String unionId, boolean isEnable) {
    List<IoTUserApplication> select =
        iotUserApplicationMapper.select(IoTUserApplication.builder().unionId(unionId).build());
    if (CollectionUtil.isNotEmpty(select)) {
      // 处理应用
      for (IoTUserApplication iotUserApplication : select) {
        if (isEnable) {
          iotUserApplication.setAppStatus(IoTConstant.NORMAL);
        } else {
          iotUserApplication.setAppStatus(IoTConstant.UN_NORMAL);
        }
        iotUserApplicationMapper.updateByPrimaryKeySelective(iotUserApplication);
      }
    }
    // 处理oauth2鉴权
    List<OAuth2ClientDetails> oauth2 =
        oauthClientDetailsMapper.select(OAuth2ClientDetails.builder().iotUnionId(unionId).build());
    if (CollectionUtil.isNotEmpty(oauth2)) {
      for (OAuth2ClientDetails details : oauth2) {
        OAuth2ClientDetails build = OAuth2ClientDetails.builder().build();
        if (isEnable) {
          build.setAuthorizedGrantTypes("client_credentials");
        } else {
          build.setAuthorizedGrantTypes("");
        }
        Example example = new Example(OAuth2ClientDetails.class);
        example.createCriteria().andEqualTo("clientId", details.getClientId());
        oauthClientDetailsMapper.updateByExampleSelective(build, example);
      }
    }
    return true;
  }

  @Override
  @Cacheable(cacheNames = "iot_app_user_name", key = "#applicationId")
  public String selectApplicationName(String applicationId) {
    IoTUserApplication iotUserApplication =
        iotUserApplicationMapper.selectOne(
            IoTUserApplication.builder().appUniqueId(applicationId).build());
    return iotUserApplication == null ? "" : iotUserApplication.getAppName();
  }

  /**
   * 查询用户应用信息
   *
   * @param appUniqueId 用户应用信息ID
   * @return 用户应用信息
   */
  @Override
  public IoTUserApplication selectIotUserApplicationById(String appUniqueId) {
    return checkSelf(appUniqueId);
  }

  public IoTUserApplication checkSelf(String appUniqueId) {
    IoTUserApplication a = IoTUserApplication.builder().appUniqueId(appUniqueId).build();
    String unionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    if (!iIotUserService.selectUserByUnionId(unionId).isAdmin()) {
      a.setUnionId(unionId);
    }
    IoTUserApplication iotUserApplication = iotUserApplicationMapper.selectOne(a);
    if (Objects.isNull(iotUserApplication)) {
      throw new IoTException("您没有权限操作此应用！");
    }
    return iotUserApplication;
  }

  /**
   * 查询用户应用信息列表
   *
   * @param iotUserApplication 用户应用信息
   * @return 用户应用信息
   */
  @Override
  public List<IoTUserApplicationVO> selectIotUserApplicationList(
      IoTUserApplication iotUserApplication, IoTUser iotUser) {
    iotUserApplication.setUnionId(iotUser.isAdmin() ? null : iotUser.getUnionId());
    return iotUserApplicationMapper.selectIotUserApplicationList(iotUserApplication);
  }

  /**
   * 新增用户应用信息
   *
   * @param iotUserApplication 用户应用信息
   * @return 结果
   */
  @Override
  public int insertIotAppUser(IoTUserApplication iotUserApplication) {
    IoTUser iotUser = queryIotUser(iotUserApplication.getUnionId());
    iotUserApplication.setUnionId(iotUser.getUnionId());
    return iotUserApplicationMapper.insertIotUserApplication(iotUserApplication);
  }

  /**
   * 修改用户应用信息
   *
   * @param iotUserApplication 用户应用信息
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount"
      },
      allEntries = true)
  public int updateIotUserApplication(IoTUserApplication iotUserApplication) {
    return iotUserApplicationMapper.updateIotUserApplication(iotUserApplication);
  }

  /**
   * 删除用户应用信息对象
   *
   * @param appUniqueId 需要删除的数据ID
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount"
      },
      allEntries = true)
  public int deleteIotUserApplicationByIds(String[] appUniqueId) {
    return iotUserApplicationMapper.deleteIotUserApplicationByIds(appUniqueId);
  }

  @Override
  public List<IoTUserApplication> selectIotUserApplicationByIds(String[] appUniqueId) {
    String a = String.join(",", appUniqueId);
    return iotUserApplicationMapper.selectByIds(a);
  }

  /**
   * 删除用户应用信息信息 x
   *
   * @param appUniqueId 用户应用信息ID
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount"
      },
      allEntries = true)
  public int deleteIotUserApplicationById(String appUniqueId) {
    return iotUserApplicationMapper.deleteIotUserApplicationById(appUniqueId);
  }

  @Override
  public List<IoTUserApplicationVO> selectApplicationList(
      IoTUserApplication application, IoTUser iotUser) {
    application.setUnionId(iotUser.isAdmin() ? null : iotUser.getUnionId());
    return iotUserApplicationMapper.selectApplicationList(application);
  }
}

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
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.security.utils.SecurityUtils;
import cn.universal.admin.system.service.IIoTUserApplicationService;
import cn.universal.security.service.IoTUserService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

/**
 * 用户应用信息Service业务层处理
 *
 * @since 2025-12-30
 */
@Service
@Slf4j
public class IoTUserApplicationServiceImpl extends BaseServiceImpl
    implements IIoTUserApplicationService {

  @Resource private IoTUserApplicationMapper iotUserApplicationMapper;
  @Resource private IoTUserService ioTUserService;

  @Resource private OauthClientDetailsMapper oauthClientDetailsMapper;

  @Value("${mqtt.cfg.enable:true}")
  private boolean sysMqttEnabled;

  @Value("${mqtt.cfg.host:tcp://localhost:1883}")
  private String sysMqttHost;

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
    if (!ioTUserService.selectUserByUnionId(unionId).isAdmin()) {
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

  @Override
  public boolean enableOrDisablePushCfg(String appUniqueId, String pushType, boolean isEnable) {
    IoTUserApplication application =
        iotUserApplicationMapper.selectOne(
            IoTUserApplication.builder().appUniqueId(appUniqueId).build());
    if (application == null) {
      return false;
    }
    // 获取当前推送配置
    String pushCfgStr = application.getCfg();
    JSONObject pushCfg;
    if (StrUtil.isBlank(pushCfgStr)) {
      pushCfg = new JSONObject();
    } else {
      pushCfg = JSONUtil.parseObj(pushCfgStr);
    }

    switch (pushType) {
      case "mqtt":
        if (isEnable) {
          // MQTT启用：设置url为sysMqttHost、support为true、enable为true
          // password为app_secret、username为app_id
          JSONObject mqttConfig = new JSONObject();
          mqttConfig.set("enable", true);
          mqttConfig.set("support", true);
          mqttConfig.set("url", sysMqttHost);
          mqttConfig.set("password", application.getAppSecret());
          mqttConfig.set("username", application.getAppId());
          mqttConfig.set("clientId", application.getAppId());

          pushCfg.set("mqtt", mqttConfig);
        } else {
          // MQTT禁用：只设置enable为false
          if (pushCfg.containsKey("mqtt")) {
            JSONObject mqttConfig = pushCfg.getJSONObject("mqtt");
            mqttConfig.set("enable", false);
            pushCfg.set("mqtt", mqttConfig);
          }
        }
        break;
      case "http":
        if (isEnable) {
          // HTTP启用：设置enable和support为true
          JSONObject httpConfig = pushCfg.getJSONObject("http");
          httpConfig.set("enable", true);
          httpConfig.set("support", true);
          pushCfg.set("http", httpConfig);
        } else {
          // HTTP禁用：设置enable为false
          if (pushCfg.containsKey("http")) {
            JSONObject httpConfig = pushCfg.getJSONObject("http");
            httpConfig.set("enable", false);
            pushCfg.set("http", httpConfig);
          }
        }
        break;
      default:
        return false;
    }
    // 更新推送配置
    application.setCfg(pushCfg.toString());
    int result = iotUserApplicationMapper.updateIotUserApplication(application);
    return result > 0;
  }

  @Override
  @Transactional
  public boolean resetAppSecretAndSyncMqtt(String appUniqueId, String unionId) {
    // 查询应用信息
    IoTUserApplication application =
        iotUserApplicationMapper.selectOne(
            IoTUserApplication.builder().appUniqueId(appUniqueId).build());

    if (application == null) {
      return false;
    }
    // 生成新的应用密钥
    String newAppSecret = cn.hutool.core.util.RandomUtil.randomString(32);
    application.setAppSecret(newAppSecret);

    // 更新应用信息
    if (iotUserApplicationMapper.updateByPrimaryKeySelective(application) == 0) {
      return false;
    }
    // 检查MQTT是否开启，如果开启则同步更新MQTT配置中的密码
    String oldCfg = application.getCfg();
    if (StrUtil.isNotBlank(oldCfg)) {
      JSONObject cfg = JSONUtil.parseObj(oldCfg);
      if (cfg.containsKey("mqtt")) {
        JSONObject mqttConfig = cfg.getJSONObject("mqtt");
        if (mqttConfig.getBool("enable", false)) {
          // MQTT已启用，更新密码
          mqttConfig.set("password", newAppSecret);
          cfg.set("mqtt", mqttConfig);

          // 更新配置
          IoTUserApplication updateCfg = new IoTUserApplication();
          updateCfg.setAppUniqueId(appUniqueId);
          updateCfg.setCfg(cfg.toString());
          iotUserApplicationMapper.updateByPrimaryKeySelective(updateCfg);
          log.info("应用 {} 密钥重置后，MQTT配置密码已同步更新", appUniqueId);
        }
      }
    }
    return true;
  }
}

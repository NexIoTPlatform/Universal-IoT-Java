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

package cn.universal.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.admin.system.service.IoTDeviceProtocolService;
import cn.universal.common.constant.IoTConstant.ProductFlushType;
import cn.universal.common.event.EventTopics;
import cn.universal.common.event.ProtocolUpdatedEvent;
import cn.universal.common.event.processer.EventPublisher;
import cn.universal.common.exception.IoTException;
import cn.universal.core.protocol.jar.ProtocolCodecJar;
import cn.universal.core.protocol.jscrtipt.JsMethodExtractor;
import cn.universal.core.protocol.jscrtipt.ProtocolCodecJscript;
import cn.universal.core.protocol.magic.MagicScriptMethodExtractor;
import cn.universal.core.protocol.magic.ProtocolCodecMagic;
import cn.universal.dm.device.service.impl.IoTCacheRemoveService;
import cn.universal.ossm.service.ISysOssService;
import cn.universal.persistence.entity.IoTDeviceProtocol;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTDeviceProtocolBO;
import cn.universal.persistence.mapper.IoTDeviceProtocolMapper;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备协议Service业务层处理 @Author ruoyi
 *
 * @since 2023-01-06
 */
@Service
@Slf4j
public class IoTDeviceProtocolServiceImpl extends BaseServiceImpl
    implements IoTDeviceProtocolService {

  @Resource private IoTDeviceProtocolMapper ioTDeviceProtocolMapper;
  @Resource private ISysOssService ossService;

  @Resource private IoTCacheRemoveService iotCacheRemoveService;

  @Resource private EventPublisher eventPublisher;

  @Resource private ApplicationEventPublisher applicationEventPublisher;

  @Override
  public int countProtocol(String id) {
    return ioTDeviceProtocolMapper.selectCount(IoTDeviceProtocol.builder().id(id).build());
  }

  /**
   * 查询设备协议
   *
   * @param id 设备协议主键
   * @return 设备协议
   */
  @Override
  public IoTDeviceProtocol selectDevProtocolById(String id, String unionId) {
    return ioTDeviceProtocolMapper.selectDevProtocolById(id, unionId);
  }

  @Override
  public int countByProvider(String provider) {
    return ioTDeviceProtocolMapper.countByProvider(provider);
  }

  /**
   * 查询设备协议列表
   *
   * @param ioTDeviceProtocol 设备协议
   * @return 设备协议
   */
  @Override
  @Transactional
  public List<IoTDeviceProtocol> selectDevProtocolList(
      IoTDeviceProtocol ioTDeviceProtocol, IoTUser iotUser) {
    return ioTDeviceProtocolMapper.selectDevProtocolList(
        ioTDeviceProtocol, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  /**
   * 新增设备协议
   *
   * @param ioTDeviceProtocol 设备协议
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {"iot_dev_product_list", "iot_protocol_def", "selectProtocolDefNoScript"},
      allEntries = true)
  public int insertDevProtocol(IoTDeviceProtocol ioTDeviceProtocol) {
    return ioTDeviceProtocolMapper.insertDevProtocol(ioTDeviceProtocol);
  }

  @Override
  public int insertList(List<IoTDeviceProtocol> ioTDeviceProtocolList) {
    return ioTDeviceProtocolMapper.insertProtocolList(ioTDeviceProtocolList);
  }

  /**
   * 修改设备协议
   *
   * @param ioTDeviceProtocolBO 设备协议
   * @return 结果
   */
  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"iot_dev_product_list", "iot_protocol_def", "selectProtocolDefNoScript"},
      allEntries = true)
  public int updateDevProtocol(IoTDeviceProtocolBO ioTDeviceProtocolBO, IoTUser iotUser) {
    IoTDeviceProtocol ioTDeviceProtocol =
        BeanUtil.toBean(ioTDeviceProtocolBO, IoTDeviceProtocol.class);
    IoTDeviceProtocol oldIoTDeviceProtocol =
        ioTDeviceProtocolMapper.selectDevProtocolById(
            ioTDeviceProtocol.getId(), iotUser.getIdentity() != 0 ? iotUser.getUnionId() : null);
    if (ObjectUtil.isNull(oldIoTDeviceProtocol)) {
      throw new IoTException("该协议没有操作权限！");
    }
    JSONObject config = JSONUtil.parseObj(oldIoTDeviceProtocol.getConfiguration());
    if ("jar".equals(ioTDeviceProtocolBO.getType())) {
      // 状态变更
      if (ioTDeviceProtocolBO.getUrl() == null) {
        if (iotUser.getIdentity() != 0) {
          throw new IoTException("请联系管理员审核插件后发布");
        }
        return ioTDeviceProtocolMapper.updateDevProtocol(ioTDeviceProtocol);

      } else {
        if (!ioTDeviceProtocolBO.getFileName().equals(config.getStr("provider"))) {
          ioTDeviceProtocol.setState((byte) 0);
        }
        if ("jscript".equals(oldIoTDeviceProtocol.getType())) {
          ioTDeviceProtocol.setState((byte) 0);
        }
      }
    }

    if ("jar".equals(oldIoTDeviceProtocol.getType())) {
      // 删除旧oss资源
      if (!config.getStr("location").equals(ioTDeviceProtocolBO.getUrl())) {
        //        JSONObject object1 = JSONUtil.parseObj(oldIoTDeviceProtocol.getConfiguration());
        String[] a = {config.getStr("location")};
        ossService.deleteWithValidByUrls(a, false);
        ioTDeviceProtocol.setState((byte) 0);
      }
    }
    JSONObject object = new JSONObject();
    String provider = oldIoTDeviceProtocol.getId();
    if ("jscript".equals(ioTDeviceProtocolBO.getType())) {
      provider = ioTDeviceProtocolBO.getId();
      object.set("needBs4Decode", ioTDeviceProtocolBO.getNeedBs4Decode());
      object.set("location", ioTDeviceProtocolBO.getJscript());
      object.set("provider", provider);
      Set<String> strings = JsMethodExtractor.extractMethodNames(ioTDeviceProtocolBO.getJscript());
      Collection<String> intersection = CollUtil.intersection(method, strings);
      if (CollUtil.isEmpty(intersection)) {
        throw new IoTException("预编译失败,没有包含任何一个有效方法");
      }
      object.set("supportMethods", intersection);

    } else if ("magic".equals(ioTDeviceProtocolBO.getType())) {
      Set<String> supportMethods =
          MagicScriptMethodExtractor.extractSupportMethods(
              ioTDeviceProtocolBO.getJscript(), method);
      object.set("needBs4Decode", ioTDeviceProtocolBO.getNeedBs4Decode());
      object.set("location", ioTDeviceProtocolBO.getJscript());
      object.set("provider", provider);
      if (CollUtil.isEmpty(supportMethods)) {
        throw new IoTException("预编译失败,没有包含任何一个有效方法");
      }
      object.put("supportMethods", supportMethods);

    } else {
      provider = ioTDeviceProtocolBO.getFileName();
      object.set("provider", ioTDeviceProtocolBO.getFileName());
      object.set("location", ioTDeviceProtocolBO.getUrl());
    }
    ioTDeviceProtocol.setConfiguration(object.toString());
    int rows = ioTDeviceProtocolMapper.updateDevProtocol(ioTDeviceProtocol);
    if (rows > 0) {
      JSONObject object1 = new JSONObject();
      object1.set("type", ProductFlushType.script.name());
      object1.set("provider", provider);
      if ("jscript".equals(ioTDeviceProtocolBO.getType())) {
        object1.set("customType", "jscript");
        eventPublisher.publishEvent(EventTopics.PROTOCOL_UPDATED, object1);
        //        ProtocolCodecJscript.getInstance().remove(provider);
      } else if ("magic".equals(ioTDeviceProtocolBO.getType())) {
        object1.set("customType", "magic");
        eventPublisher.publishEvent(EventTopics.PROTOCOL_UPDATED, object1);
      } else {
        object1.set("customType", "jar");
        eventPublisher.publishEvent(EventTopics.PROTOCOL_UPDATED, object1);
      }
      // 清除协议相关的所有缓存
      iotCacheRemoveService.removeDevProtocolCache();

      // 发布协议更新事件，通知相关组件清理缓存
      applicationEventPublisher.publishEvent(
          new ProtocolUpdatedEvent(this, ioTDeviceProtocol.getId(), ioTDeviceProtocolBO.getType()));
    }
    return rows;
  }

  /**
   * 批量删除设备协议
   *
   * @param ids 需要删除的设备协议主键
   * @return 结果
   */
  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"iot_dev_product_list", "iot_protocol_def", "selectProtocolDefNoScript"},
      allEntries = true)
  public int deleteDevProtocolByIds(String[] ids) {
    List<IoTDeviceProtocol> list = ioTDeviceProtocolMapper.selectDevProtocolByIds(ids);
    String[] a =
        list.stream()
            .filter(s -> "jar".equals(s.getType()))
            .map(
                i -> {
                  JSONObject object1 = JSONUtil.parseObj(i.getConfiguration());
                  return object1.getStr("location");
                })
            .toArray(String[]::new);
    ossService.deleteWithValidByUrls(a, false);
    int rs = ioTDeviceProtocolMapper.deleteDevProtocolByIds(ids);
    // 删除编解码缓存
    list.stream()
        .forEach(
            s -> {
              String provider = null;
              if ("jscript".equals(s.getType())) {
                provider = s.getId();
                ProtocolCodecJscript.getInstance().remove(provider);
              } else if ("magic".equals(s.getType())) {
                provider = s.getId();
                ProtocolCodecMagic.getInstance().remove(provider);
              } else {
                provider = s.toDefinition().getProvider();
                ProtocolCodecJar.getInstance().remove(provider);
              }
            });
    return rs;
  }

  /**
   * 删除设备协议信息
   *
   * @param id 设备协议主键
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {"iot_dev_product_list", "iot_protocol_def", "selectProtocolDefNoScript"},
      allEntries = true)
  public int deleteDevProtocolById(String id) {
    return ioTDeviceProtocolMapper.deleteDevProtocolById(id);
  }
}

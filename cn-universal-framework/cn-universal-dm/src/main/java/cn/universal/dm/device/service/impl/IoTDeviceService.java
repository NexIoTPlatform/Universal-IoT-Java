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

package cn.universal.dm.device.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.cache.annotation.MultiLevelCacheable;
import cn.universal.common.exception.IoTErrorCode;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.dto.IoTDeviceMetadataBO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.entity.IoTDeviceSubscribe;
import cn.universal.persistence.entity.IoTDeviceTags;
import cn.universal.persistence.entity.IoTUserApplication;
import cn.universal.persistence.entity.SupportMapAreas;
import cn.universal.persistence.entity.bo.IoTDeviceHistoryBO;
import cn.universal.persistence.entity.vo.IoTDeviceVO;
import cn.universal.persistence.mapper.IoTDeviceFenceRelMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.mapper.IoTDeviceSubscribeMapper;
import cn.universal.persistence.mapper.IoTDeviceTagsMapper;
import cn.universal.persistence.mapper.IoTUserApplicationMapper;
import cn.universal.persistence.mapper.SupportMapAreasMapper;
import cn.universal.persistence.query.IoTAPIQuery;
import cn.universal.persistence.query.IoTDeviceQuery;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Slf4j
public class IoTDeviceService {

  @Resource
  private SupportMapAreasMapper supportMapAreasMapper;
  @Resource
  private IoTDeviceMapper ioTDeviceMapper;

  @Resource
  private IoTDeviceTagsMapper ioTDeviceTagsMapper;

  @Resource
  private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Resource
  private IoTDeviceSubscribeMapper ioTDeviceSubscribeMapper;
  @Resource
  private IoTUserApplicationMapper iotUserApplicationMapper;
  @Resource
  private IoTDeviceFenceRelMapper ioTDeviceFenceRelMapper;

  @Resource
  private IoTCacheRemoveService iotCacheRemoveService;

  public Page<IoTDeviceVO> apiDeviceList(IoTAPIQuery iotAPIQuery) {
    Page<IoTDeviceVO> page = PageHelper.startPage(iotAPIQuery.getPage(), iotAPIQuery.getSize());
    List<IoTDeviceVO> ioTDeviceVOList = ioTDeviceMapper.apiDeviceList(iotAPIQuery);
    return page;
  }

  /**
   * 设备绑定应用 对外API
   */
  public int apiAppBind(String appid, String iotId) {
    IoTUserApplication iotUserApplication = iotUserApplicationMapper.selectIotUserApplicationByAppId(
        appid);
    if (iotUserApplication == null) {
      throw new IoTException("应用不存在");
    }
    int i = ioTDeviceMapper.apiBindApp(iotUserApplication.getAppUniqueId(), iotId);
    return i;
  }

  /**
   * 设备解绑应用 对外API
   */
  public int apiAppUnBind(String iotId) {
    if (StrUtil.isBlank(iotId)) {
      throw new IoTException("设备编号不能为空");
    }
    return ioTDeviceMapper.apiUnBindApp(iotId);
  }

  /**
   * 更新设备信息
   */
  public Map<String, Object> apiUpdateDevInfo(IoTAPIQuery iotAPIQuery) {
    IoTDevice instance = IoTDevice.builder().creatorId(iotAPIQuery.getIotUnionId())
        .iotId(iotAPIQuery.getIotId()).build();
    instance = ioTDeviceMapper.selectOne(instance);
    if (instance == null) {
      throw new IoTException(IoTErrorCode.DEV_NOT_FIND.getName(),
          IoTErrorCode.DEV_NOT_FIND.getCode());
    }
    if (StrUtil.isNotBlank(iotAPIQuery.getDeviceName())) {
      instance.setDeviceName(iotAPIQuery.getDeviceName());
    }
    if (StrUtil.isNotBlank(iotAPIQuery.getDetail())) {
      instance.setDetail(iotAPIQuery.getDetail());
    }
    if (StrUtil.isNotBlank(iotAPIQuery.getLatitude()) && StrUtil.isNotBlank(
        iotAPIQuery.getLongitude())) {

      instance.setCoordinate(
          StrUtil.join(",", iotAPIQuery.getLongitude(), iotAPIQuery.getLatitude()));

      // 更新设备所属经纬度
      SupportMapAreas supportMapAreas = supportMapAreasMapper.selectMapAreas(
          iotAPIQuery.getLongitude(), iotAPIQuery.getLatitude());
      if (supportMapAreas == null) {
        log.info("查询区域id为空,lot={},lat={}", iotAPIQuery.getLongitude(),
            iotAPIQuery.getLatitude());
      } else {
        instance.setAreasId(supportMapAreas.getId());
      }
    }

    ioTDeviceMapper.updateByPrimaryKeySelective(instance);
    // 组件返回字段
    Map<String, Object> result = new HashMap<>();
    result.put("deviceId", instance.getDeviceId());
    result.put("areasId", instance.getAreasId() == null ? "" : instance.getAreasId());
    return result;
  }

  @Cacheable(cacheNames = "selectDevCount", unless = "#result == null", keyGenerator = "redisKeyGenerate")
  public boolean selectDevCount(IoTAPIQuery iotAPIQuery) {
    if (iotAPIQuery == null) {
      throw new IoTException("参数不能为空");
    }
    IoTDevice instance = IoTDevice.builder().creatorId(iotAPIQuery.getIotUnionId())
        .deviceId(iotAPIQuery.getDeviceId()).application(iotAPIQuery.getApplicationId())
        .productKey(iotAPIQuery.getProductKey()).iotId(iotAPIQuery.getIotId()).build();

    int count = ioTDeviceMapper.selectCount(instance);
    return count > 0;
  }

  @Cacheable(cacheNames = "iot_dev_action", unless = "#result == null", keyGenerator = "redisKeyGenerate")
  public IoTDeviceDTO lifeCycleDevInstance(IoTDeviceQuery query) {
    if (query == null || query.emptyParams()) {
      log.warn("no query condition");
      return null;
    }
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(BeanUtil.beanToMap(query));

    return ioTDeviceDTO;
  }

  /**
   * deviceId 设备编号
   *
   * <p>extDeviceId 扩展设备编号
   *
   * <p>iotId 设备统一编号
   *
   * <p>productKey 产品唯一编号
   */
  @Cacheable(cacheNames = "iot_dev_instance_bo", unless = "#result == null", keyGenerator = "redisKeyGenerate")
  public IoTDeviceDTO selectDevInstanceBO(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      throw new IoTException("deviceId can not be null");
    }
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(map);
    if (ioTDeviceDTO != null) {
    }

    return ioTDeviceDTO;
  }

  @MultiLevelCacheable(cacheNames = "apiIoTDeviceVOInfo", unless = "#result == null", keyGenerator = "redisKeyGenerate")
  public IoTDeviceVO apiIoTDeviceVOInfo(IoTAPIQuery query) {
    IoTDeviceVO ioTDeviceVO = ioTDeviceMapper.apiDeviceInfo(query);
    return ioTDeviceVO;
  }

  /**
   * 不缓存 供设备生命周期调用 deviceId 设备编号
   *
   * <p>extDeviceId 扩展设备编号
   *
   * <p>iotId 设备统一编号
   *
   * <p>productKey 产品唯一编号
   */
  public IoTDeviceDTO selectDevInstanceBONoCache(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
      throw new IoTException("deviceId can not be null");
    }
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(map);
    return ioTDeviceDTO;
  }

  /**
   * deviceId 设备编号
   *
   * <p>extDeviceId 扩展设备编号
   *
   * <p>iotId 设备统一编号
   *
   * <p>productKey 产品唯一编号
   */
  @Cacheable(cacheNames = "iot_dev_instance_bo", unless = "#result == null", key =
      "'selectDevInstanceBO" + ":'+#productKey+#deviceId")
  public IoTDeviceDTO selectDevInstanceBO(String productKey, String deviceId) {
    if (StrUtil.isEmpty(productKey) || StrUtil.isEmpty(deviceId)) {
      throw new IoTException("productKey or deviceId can not be null");
    }
    Map<String, Object> map = new HashMap<>();
    map.put("productKey", productKey);
    map.put("deviceId", deviceId);
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(map);
    if (ioTDeviceDTO != null) {
      return ioTDeviceDTO;
    }
    return null;
  }

  @Cacheable(cacheNames = "iot_dev_instance_bo", unless = "#result == null", key = "''+#productKey+#deviceId")
  public IoTDevice selectDevInstance(String productKey, String deviceId) {
    if (StrUtil.isBlank(productKey) || StrUtil.isBlank(deviceId)) {
      return null;
    }
    IoTDevice ioTDevice = IoTDevice.builder().productKey(productKey).deviceId(deviceId).build();
    ioTDevice = ioTDeviceMapper.selectOne(ioTDevice);
    return ioTDevice;
  }

  @Cacheable(cacheNames = "iot_dev_instance_bo", unless = "#result == null", key = "''+#iotId")
  public IoTDevice selectDevInstance(String iotId) {
    if (StrUtil.isBlank(iotId)) {
      return null;
    }
    IoTDevice ioTDevice = IoTDevice.builder().iotId(iotId).build();
    ioTDevice = ioTDeviceMapper.selectOne(ioTDevice);
    return ioTDevice;
  }

  /**
   * iotId 设备统一编号
   *
   * @return 设备BO
   */
  @Cacheable(cacheNames = "iot_dev_instance_iotId", key = "''+#iotId", unless = "#result==null")
  public IoTDeviceDTO selectDevInstanceBO(String iotId) {
    if (StrUtil.isBlank(iotId)) {
      throw new IoTException("iotId can not be null");
    }
    IoTDeviceDTO ioTDeviceDTO = ioTDeviceMapper.selectIoTDeviceBO(
        BeanUtil.beanToMap(IoTAPIQuery.builder().iotId(iotId).build()));
    return ioTDeviceDTO;
  }

  @Cacheable(cacheNames = "iot_dev_metadata_bo", key = "''+#iotId", unless = "#result==null")
  public IoTDeviceMetadataBO selectDevMetadataBo(String iotId) {
    IoTDeviceMetadataBO metadataBO = ioTDeviceMapper.selectDevMetadataBo(iotId);
    return metadataBO;
  }

  /**
   * 根据 extDeviceId 删除设备信息
   */
  @Transactional
  public int delDevInstance(String iotId) {
    if (StrUtil.isBlank(iotId)) {
      return 0;
    }
    addDevHistory(iotId);
    int dev = ioTDeviceMapper.delete(IoTDevice.builder().iotId(iotId).build());
    int tags = ioTDeviceTagsMapper.delete(IoTDeviceTags.builder().iotId(iotId).build());
    int sha = ioTDeviceShadowMapper.delete(IoTDeviceShadow.builder().iotId(iotId).build());
    int subscribe = ioTDeviceSubscribeMapper.delete(
        IoTDeviceSubscribe.builder().iotId(iotId).build());
    int fence = ioTDeviceFenceRelMapper.deleteFenceInstance(iotId);
    log.info("删除设备 tag={},platform={},shadow={},subscribe={},fence={}", tags, dev, sha,
        subscribe, fence);
    iotCacheRemoveService.removeDevProtocolCache();
    return dev;
  }

  public void addDevHistory(String iotId) {
    IoTDevice ioTDevice = ioTDeviceMapper.selectOne(IoTDevice.builder().iotId(iotId).build());
    if (ioTDevice != null && ioTDevice.getRegistryTime() != null) {
      IoTDeviceHistoryBO ioTDeviceHistoryBO = IoTDeviceHistoryBO.builder()
          .deviceId(ioTDevice.getDeviceId()).deviceName(ioTDevice.getDeviceName())
          .productKey(ioTDevice.getProductKey())
          .firstOnlineTime(ioTDevice.getRegistryTime().longValue())
          .creater(ioTDevice.getCreatorId()).createTime(ioTDevice.getCreateTime())
          .deleteTime(System.currentTimeMillis()).coordinate(ioTDevice.getCoordinate()).build();
//      ioTDeviceMapper.insertDevInstanceHistory(ioTDeviceHistoryBO);
    }
  }
}

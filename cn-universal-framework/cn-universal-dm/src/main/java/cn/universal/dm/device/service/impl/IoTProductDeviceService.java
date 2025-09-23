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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.exception.IoTException;
import cn.universal.core.metadata.AbstractEventMetadata;
import cn.universal.core.metadata.AbstractFunctionMetadata;
import cn.universal.core.metadata.DeviceMetadata;
import cn.universal.core.protocol.support.ProtocolSupportDefinition;
import cn.universal.persistence.dto.IoTDeviceLogMaxStorageTime;
import cn.universal.persistence.dto.IoTDeviceOfflineThesholdBO;
import cn.universal.persistence.dto.LogStorePolicyDTO;
import cn.universal.persistence.dto.LogStorePolicyDTO.StorePolicy;
import cn.universal.persistence.entity.IoTDeviceProtocol;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.vo.IoTProductVO;
import cn.universal.persistence.mapper.IoTDeviceProtocolMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.query.IoTAPIQuery;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 设备和产品鉴权
 *
 * @version 1.0 @Author Aleo
 * @since 2025/8/12 16:10
 */
@Component
@Slf4j
public class IoTProductDeviceService {

  @Resource private IoTProductMapper ioTProductMapper;
  @Resource private IoTDeviceProtocolMapper ioTDeviceProtocolMapper;

  public Page<IoTProductVO> apiProductList(IoTAPIQuery iotAPIQuery) {
    Page<IoTProductVO> page = PageHelper.startPage(iotAPIQuery.getPage(), iotAPIQuery.getSize());
    List<IoTProductVO> devProducts = ioTProductMapper.openAPIProductList(iotAPIQuery);
    return page;
  }

  public Page<IoTProductVO> apiProductListV2(IoTAPIQuery iotAPIQuery) {
    Page<IoTProductVO> page = PageHelper.startPage(iotAPIQuery.getPage(), iotAPIQuery.getSize());
    List<IoTProductVO> devProducts = ioTProductMapper.openAPIProductList(iotAPIQuery);
    List<IoTProductVO> results =
        ioTProductMapper.countDevNumberByProductKey(iotAPIQuery.getIotUnionId());
    if (CollUtil.isNotEmpty(results)) {
      final Map<String, Integer> collect =
          results.stream()
              .collect(Collectors.toMap(IoTProductVO::getProductKey, IoTProductVO::getDevNum));
      for (IoTProductVO devProduct : devProducts) {
        devProduct.setDevNum(collect.getOrDefault(devProduct.getProductKey(), 0));
      }
    }

    return page;
  }

  public IoTProductVO apiProductDetail(String productKey) {
    IoTProductVO productVO = ioTProductMapper.apiProductDetail(productKey);
    return productVO;
  }

  @Cacheable(cacheNames = "iot_product_device", key = "''+#productKey", unless = "#result == null")
  public IoTProduct getProduct(String productKey) {
    if (StrUtil.isBlank(productKey)) {
      throw new IoTException("product [" + productKey + "] can not be null");
    }
    // status-0 正常
    IoTProduct ioTProduct =
        IoTProduct.builder().productKey(productKey).state(IoTConstant.NORMAL.byteValue()).build();
    ioTProduct = ioTProductMapper.selectOne(ioTProduct);
    if (ioTProduct == null) {
      throw new IoTException("product [" + productKey + "] not exist");
    }
    return ioTProduct;
  }

  @Cacheable(cacheNames = "getDeviceMetadata", key = "''+#productKey", unless = "#result == null")
  public DeviceMetadata getDeviceMetadata(String productKey) {
    // status-0 正常
    IoTProduct product =
        IoTProduct.builder().productKey(productKey).state(IoTConstant.NORMAL.byteValue()).build();
    product = ioTProductMapper.selectOne(product);
    if (product == null || StrUtil.isBlank(product.getMetadata())) {
      return new DeviceMetadata(new JSONObject());
    }
    return new DeviceMetadata(new JSONObject(product.getMetadata()));
  }

  @Cacheable(
      cacheNames = "getEventOrFunctionName",
      key = "''+#productKey+#event",
      unless = "#result == null")
  public String getEventOrFunctionName(String productKey, String event) {
    if (StrUtil.isBlank(productKey)) {
      return null;
    }
    // status-0 正常
    IoTProduct product =
        IoTProduct.builder().productKey(productKey).state(IoTConstant.NORMAL.byteValue()).build();
    product = ioTProductMapper.selectOne(product);
    if (product == null || StrUtil.isBlank(product.getMetadata())) {
      return null;
    }
    DeviceMetadata deviceMetadata = new DeviceMetadata(new JSONObject(product.getMetadata()));
    if (deviceMetadata != null && StrUtil.isNotBlank(event)) {
      AbstractEventMetadata eventOrNull = deviceMetadata.getEventOrNull(event);
      if (eventOrNull != null) {
        return eventOrNull == null ? null : eventOrNull.getName();
      }
      AbstractFunctionMetadata functionOrNull = deviceMetadata.getFunctionOrNull(event);
      if (functionOrNull != null) {
        return functionOrNull == null ? null : functionOrNull.getName();
      }
    }
    return null;
  }

  @Cacheable(
      cacheNames = "iot_product_offline_threshold",
      key = "''+#productKey",
      unless = "#result == null")
  public boolean offlineThreshold(String productKey) {
    // status-0 正常
    IoTProduct ioTProduct =
        IoTProduct.builder().productKey(productKey).state(IoTConstant.NORMAL.byteValue()).build();
    ioTProduct = ioTProductMapper.selectOne(ioTProduct);
    if (ioTProduct == null || StrUtil.isBlank(ioTProduct.getConfiguration())) {
      return false;
    }
    JSONObject cfg = JSONUtil.parseObj(ioTProduct.getConfiguration());
    if (!cfg.containsKey(IoTConstant.DEFAULT_OFFLINE_THRESHOLD)) {
      return false;
    }
    return true;
  }

  @Cacheable(
      cacheNames = "iot_product_log_store_policy",
      key = "''+#productKey",
      unless = "#result == null")
  public LogStorePolicyDTO getProductLogStorePolicy(String productKey) {
    IoTProduct product =
        ((IoTProductDeviceService) AopContext.currentProxy()).getProduct(productKey);
    String storePolicy = product.getStorePolicyConfiguration();
    Map<String, StorePolicy> propertiesMap = new HashMap<>();
    // 处理属性
    Map<String, StorePolicy> eventMap = new HashMap<>();
    if (StrUtil.isBlank(storePolicy)) {
      return LogStorePolicyDTO.builder().properties(propertiesMap).event(eventMap).build();
    }
    JSONObject storePolicyObj = JSONUtil.parseObj(storePolicy);
    storePolicyObj.getJSONArray("event").stream()
        .forEach(
            s -> {
              JSONObject ev = (JSONObject) s;
              eventMap.put(
                  ev.getStr("id"),
                  new StorePolicy(
                      ev.getStr("id"), ev.getInt("maxStorage", IoTConstant.maxStorage)));
            });
    storePolicyObj.getJSONArray("properties").stream()
        .forEach(
            s -> {
              JSONObject ev = (JSONObject) s;
              propertiesMap.put(
                  ev.getStr("id"),
                  new StorePolicy(
                      ev.getStr("id"), ev.getInt("maxStorage", IoTConstant.maxStorage)));
            });
    return LogStorePolicyDTO.builder().properties(propertiesMap).event(eventMap).build();
  }

  /** 加载产品解析协议 */
  @Cacheable(cacheNames = "iot_protocol_def", key = "''+#productKey", unless = "#result == null")
  public ProtocolSupportDefinition selectProtocolDef(String productKey) {
    if (StrUtil.isBlank(productKey)) {
      throw new IoTException("product [" + productKey + "] can not be null");
    }
    IoTDeviceProtocol ioTDeviceProtocol =
        ioTDeviceProtocolMapper.selectDevProtocolByProductKey(productKey);
    if (ioTDeviceProtocol != null) {
      return ioTDeviceProtocol.toDefinition();
    }
    return null;
  }

  /** 加载产品解析协议 */
  @Cacheable(
      cacheNames = "selectProtocolDefNoScript",
      key = "''+#productKey",
      unless = "#result == null")
  public ProtocolSupportDefinition selectProtocolDefNoScript(String productKey) {
    if (StrUtil.isBlank(productKey)) {
      throw new IoTException("product [" + productKey + "] can not be null");
    }
    IoTDeviceProtocol ioTDeviceProtocol =
        ioTDeviceProtocolMapper.selectDevProtocolByProductKey(productKey);
    if (ioTDeviceProtocol != null) {
      return ioTDeviceProtocol.toDefinitionNoScript();
    }
    return null;
  }

  /**
   * 根据产品编号和归属人查询总数
   *
   * @param productKey 产品key
   * @param unionId 用户唯一标识
   * @return int 总数
   */
  @Cacheable(
      cacheNames = "iot_product_count",
      key = "''+#unionId+':'+#productKey",
      unless = "#result == null")
  public int selectProductCount(String productKey, String unionId) {
    if (StrUtil.isBlank(productKey) || StrUtil.isBlank(unionId)) {
      return 0;
    }
    return ioTProductMapper.selectCount(
        IoTProduct.builder().productKey(productKey).creatorId(unionId).build());
  }

  /** 获取产品配置的离线阈值，缺省值24小时 */
  @Cacheable(
      cacheNames = "iot_product_offline_threshold",
      key = "''+#productKey",
      unless = "#result == null")
  public int getProductOfflineThreshold(String productKey) {
    IoTProduct product = getProduct(productKey);
    if (StrUtil.isBlank(productKey) || product == null) {
      return IoTConstant.DEFAULT_OFFLINE_THRESHOLD_VALUE;
    }
    JSONObject config = JSONUtil.parseObj(product.getConfiguration());
    return config.getInt(
        IoTConstant.DEFAULT_OFFLINE_THRESHOLD, IoTConstant.DEFAULT_OFFLINE_THRESHOLD_VALUE);
  }

  /** 获取产品配置列表的离线阈值，缺省值24小时 */
  @Cacheable(
      cacheNames = "iot_product_offline_threshold",
      key = "''+#productKey",
      unless = "#result == null")
  public List<IoTDeviceOfflineThesholdBO> getProductOfflineThresholds() {
    // status-0 正常
    IoTProduct ioTProduct = IoTProduct.builder().state(IoTConstant.NORMAL.byteValue()).build();
    List<IoTProduct> select = ioTProductMapper.select(ioTProduct);
    List<IoTDeviceOfflineThesholdBO> productsMap = new ArrayList<>();
    if (!CollectionUtil.isEmpty(select)) {
      for (IoTProduct s : select) {
        int offlineThreshold = IoTConstant.DEFAULT_OFFLINE_THRESHOLD_VALUE;
        IoTDeviceOfflineThesholdBO ioTDeviceOfflineThesholdBO =
            IoTDeviceOfflineThesholdBO.builder()
                .productKey(s.getProductKey())
                .platform(s.getThirdPlatform())
                .build();
        // 如果没有配置使用默认的
        if (StrUtil.isBlank(s.getConfiguration())) {
          ioTDeviceOfflineThesholdBO.setOfflineThreshold(offlineThreshold);
        } else {
          try {
            JSONObject config = JSONUtil.parseObj(s.getConfiguration());
            offlineThreshold =
                config.getInt(
                    IoTConstant.DEFAULT_OFFLINE_THRESHOLD,
                    IoTConstant.DEFAULT_OFFLINE_THRESHOLD_VALUE);
            ioTDeviceOfflineThesholdBO.setOfflineThreshold(offlineThreshold);
          } catch (Exception e) {
            log.error("执行任务报错,productKey={} msg={}", s.getProductKey(), e.getMessage());
            e.printStackTrace();
          }
        }
        productsMap.add(ioTDeviceOfflineThesholdBO);
      }
    }
    return productsMap;
  }

  /** 获取产品配置列表的日志存储时长 */
  @Cacheable(
      cacheNames = "iot_product_logMaxStorageTime",
      key = "''+#productKey",
      unless = "#result == null")
  public List<IoTDeviceLogMaxStorageTime> getProductLogMaxStorage() {
    // status-0 正常
    IoTProduct ioTProduct = IoTProduct.builder().state(IoTConstant.NORMAL.byteValue()).build();
    List<IoTProduct> select = ioTProductMapper.select(ioTProduct);
    List<IoTDeviceLogMaxStorageTime> productsMap = new ArrayList<>();
    if (!CollectionUtil.isEmpty(select)) {
      for (IoTProduct s : select) {
        int maxStorageTime = IoTConstant.DEFAULT_LOG_MAX_STORAGE_TIME;
        IoTDeviceLogMaxStorageTime logMaxStorage =
            IoTDeviceLogMaxStorageTime.builder()
                .productKey(s.getProductKey())
                .platform(s.getThirdPlatform())
                .build();
        // 如果没有配置使用默认的
        if (StrUtil.isBlank(s.getConfiguration())) {
          logMaxStorage.setDays(maxStorageTime);
        } else {
          try {
            JSONObject config = JSONUtil.parseObj(s.getConfiguration());
            maxStorageTime =
                config.getInt(
                    IoTConstant.DEFAULT_LOG_MAX_STORAGE, IoTConstant.DEFAULT_LOG_MAX_STORAGE_TIME);
            logMaxStorage.setDays(maxStorageTime);
          } catch (Exception e) {
            log.error(
                "获取产品配置列表的日志存储时长任务报错,productKey={} msg={}", s.getProductKey(), e.getMessage());
            e.printStackTrace();
          }
        }
        productsMap.add(logMaxStorage);
      }
    }
    return productsMap;
  }

  /** 查询网络类型 */
  @Cacheable(cacheNames = "selectNetworkUnionId", key = "#productKey", unless = "#result == null")
  public String selectNetworkUnionId(String productKey) {
    if (StrUtil.isBlank(productKey)) {
      return null;
    }
    return ioTProductMapper.selectNetworkUnionId(productKey);
  }

  /** 查询产品配置信息 */
  @Cacheable(
      cacheNames = "getProductConfiguration",
      key = "#productKey",
      unless = "#result == null")
  public JSONObject getProductConfiguration(String productKey) {
    // status-0 正常
    IoTProduct product =
        IoTProduct.builder().productKey(productKey).state(IoTConstant.NORMAL.byteValue()).build();
    product = ioTProductMapper.selectOne(product);
    if (product == null || StrUtil.isBlank(product.getConfiguration())) {
      return null;
    }
    return JSONUtil.parseObj(product.getConfiguration());
  }
}

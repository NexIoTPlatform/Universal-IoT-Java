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

package cn.universal.admin.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.admin.platform.service.IIoTProductService;
import cn.universal.admin.system.service.IoTDeviceProtocolService;
import cn.universal.cache.annotation.MultiLevelCacheable;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.constant.IoTConstant.ProductFlushType;
import cn.universal.common.constant.IoTConstant.ProtocolModule;
import cn.universal.common.constant.IoTConstant.TcpFlushType;
import cn.universal.common.domain.R;
import cn.universal.common.event.EventTopics;
import cn.universal.common.event.processer.EventPublisher;
import cn.universal.common.exception.IoTException;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.dm.device.service.protocol.ProtocolClusterService;
import cn.universal.persistence.base.IoTProductAction;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceFunction;
import cn.universal.persistence.entity.IoTDeviceProperties;
import cn.universal.persistence.entity.IoTDeviceProtocol;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.IoTProductSort;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.Network;
import cn.universal.persistence.entity.bo.IoTProductBO;
import cn.universal.persistence.entity.bo.IoTProductImportBO;
import cn.universal.persistence.entity.dto.ProductExportPackageDTO;
import cn.universal.persistence.entity.dto.ProductExportPackageDTO.DeviceProtocolDTO;
import cn.universal.persistence.entity.dto.ProductExportPackageDTO.NetworkConfigDTO;
import cn.universal.persistence.entity.dto.ProductExportPackageDTO.ProductInfoDTO;
import cn.universal.persistence.entity.vo.IoTDeviceModelVO;
import cn.universal.persistence.entity.vo.IoTProductExportVO;
import cn.universal.persistence.entity.vo.IoTProductVO;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.IoTProductSortMapper;
import cn.universal.persistence.mapper.NetworkMapper;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.persistence.query.IoTProductQuery;
import cn.universal.security.utils.SecurityUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * 设备产品Service业务层处理
 *
 * @since 2025-12-24
 */
@Slf4j
@Service
public class IoTProductServiceImpl extends BaseServiceImpl implements IIoTProductService {

  @Resource private IoTProductMapper ioTProductMapper;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private NetworkMapper networkMapper;
  @Resource private IoTDeviceProtocolService devProtocolService;

  @Resource private IoTProductSortMapper ioTProductSortMapper;

  @Resource(name = "ioTProductActionService")
  private IoTProductAction ioTProductAction;

  @Resource private EventPublisher eventPublisher;

  private String defaultMetadata = IoTConstant.defaultMetadata;
  private String thirdConfig = "{\"customField\":[]}";

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "supportMQTTNetwork",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int updateNetworkUnionId(Long id, String networkUnionId) {
    IoTProduct ioTProduct = new IoTProduct();
    ioTProduct.setNetworkUnionId(networkUnionId);
    Example example = new Example(IoTProduct.class);
    example.createCriteria().andEqualTo("id", id);
    return ioTProductMapper.updateByExampleSelective(ioTProduct, example);
  }

  /**
   * 查询设备产品
   *
   * @param id 设备产品主键
   * @return 设备产品
   */
  @Override
  public IoTProductVO selectDevProductById(String id) {
    IoTProduct product = ioTProductMapper.selectDevProductById(id);
    IoTProductVO productVO = BeanUtil.toBean(product, IoTProductVO.class);
    if ("tcp".equals(product.getThirdPlatform())) {
      Network network =
          networkMapper.selectOne(Network.builder().productKey(product.getProductKey()).build());
      if (Objects.nonNull(network)) {
        JSONObject obj = JSONUtil.parseObj(network.getConfiguration());
        JSONObject parserConfiguration = obj.getJSONObject("parserConfiguration");
        if (MapUtil.isNotEmpty(parserConfiguration)) {
          for (Map.Entry<String, Object> q : parserConfiguration.entrySet()) {
            obj.set(q.getKey(), q.getValue());
          }
        }
        JSONObject configuration = JSONUtil.parseObj(product.getConfiguration());
        for (Map.Entry<String, Object> q : obj.entrySet()) {
          configuration.set(q.getKey(), q.getValue());
        }
        configuration.set("enabled", network.getState());
        productVO.setConfiguration(configuration.toString());
      }
    }
    // 网关子产品处理
    if (DeviceNode.GATEWAY_SUB_DEVICE.name().equals(product.getDeviceNode())) {
      productVO.setGwProductKey(product.getGwProductKey());
      IoTProduct gwProduct = ioTProductMapper.getProductByProductKey(product.getGwProductKey());
      if (Objects.nonNull(gwProduct)) {
        productVO.setGwName(gwProduct.getName());
        productVO.setGwPhotoUrl(gwProduct.getPhotoUrl());
      }
    }
    return productVO;
  }

  /**
   * 查询设备产品列表
   *
   * @param ioTProduct 设备产品
   * @return 设备产品
   */
  @Override
  //  @Cacheable(cacheNames = "iot_dev_product_list", unless = "#result == null", keyGenerator =
  // "redisKeyGenerate")
  public List<IoTProduct> selectDevProductList(IoTProduct ioTProduct) {
    return ioTProductMapper.selectDevProductList(ioTProduct);
  }

  @Override
  //  @Cacheable(cacheNames = "iot_dev_product_list", unless = "#result == null", keyGenerator =
  // "redisKeyGenerate")
  public List<IoTProduct> selectDevProductV2List(IoTProductQuery ioTProductQuery) {
    return ioTProductMapper.selectDevProductV2List(ioTProductQuery);
  }

  @Override
  //  @Cacheable(cacheNames = "iot_dev_product_list", unless = "#result == null", keyGenerator =
  // "redisKeyGenerate")
  public List<IoTProductVO> selectDevProductV3List(IoTProductQuery ioTProductQuery) {
    Page<IoTProductVO> page =
        PageHelper.startPage(ioTProductQuery.getPageNum(), ioTProductQuery.getPageSize());
    ioTProductMapper.selectDevProductV3List(ioTProductQuery);
    return page;
  }

  @Override
  // 分页查询不使用缓存，因为分页参数会导致缓存键不同，效率低下
  public List<IoTProductVO> selectDevProductV4List(IoTProductQuery ioTProductQuery) {
    // 设置分页参数
    PageHelper.startPage(ioTProductQuery.getPageNum(), ioTProductQuery.getPageSize());

    // 优化：一次SQL查询获取产品列表和设备数量，避免N+1查询
    List<IoTProductVO> devProductVOS = ioTProductMapper.selectDevProductV3List(ioTProductQuery);

    // 处理图片URL
    if (CollUtil.isNotEmpty(devProductVOS)) {
      for (IoTProductVO devProduct : devProductVOS) {
        if (JSONUtil.isTypeJSON(devProduct.getPhotoUrl())) {
          JSONObject photo = JSONUtil.parseObj(devProduct.getPhotoUrl());
          devProduct.setImage(photo.getStr("img", ""));
        }
      }
    }

    return devProductVOS;
  }

  /** 获取不分页的产品列表（用于缓存） 缓存基础产品数据，不包含分页信息 */
  @MultiLevelCacheable(
      cacheNames = "selectDevProductV4ListNoPage",
      keyGenerator = "redisKeyGenerate",
      unless = "#result == null",
      l1Expire = 30)
  public List<IoTProductVO> selectDevProductV4ListNoPage(IoTProductQuery ioTProductQuery) {
    // 创建不分页的查询条件
    IoTProductQuery noPageQuery = new IoTProductQuery();
    BeanUtil.copyProperties(ioTProductQuery, noPageQuery);
    noPageQuery.setPageNum(1);
    noPageQuery.setPageSize(Integer.MAX_VALUE); // 获取所有数据

    // 优化：一次SQL查询获取产品列表和设备数量，避免N+1查询
    List<IoTProductVO> devProductVOS = ioTProductMapper.selectDevProductV3List(noPageQuery);

    // 处理图片URL
    if (CollUtil.isNotEmpty(devProductVOS)) {
      for (IoTProductVO devProduct : devProductVOS) {
        if (JSONUtil.isTypeJSON(devProduct.getPhotoUrl())) {
          JSONObject photo = JSONUtil.parseObj(devProduct.getPhotoUrl());
          devProduct.setImage(photo.getStr("img", ""));
        }
      }
    }

    return devProductVOS;
  }

  @Override
  //  @Cacheable(cacheNames = "iot_all_dev_product_list", unless = "#result == null", keyGenerator =
  // "redisKeyGenerate")
  public List<IoTProductExportVO> selectAllDevProductV2List(IoTProductQuery ioTProductQuery) {
    return ioTProductMapper.selectAllDevProductV2List(ioTProductQuery);
  }

  /**
   * 新增设备产品
   *
   * @param ioTProduct 设备产品
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "selectAllEnableNetworkProductKey",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey"
      },
      allEntries = true)
  public int insertDevProduct(IoTProduct ioTProduct) {
    Date date = new Date();
    Long time = date.getTime();
    ioTProduct.setCreateTime(new Date());
    // productKey
    String productKey = RandomUtil.randomString(12);
    if (Objects.nonNull(ioTProduct.getClassifiedId())) {
      IoTProductSort ioTProductSort =
          ioTProductSortMapper.selectDevProductSortById(ioTProduct.getClassifiedId());
      ioTProduct.setClassifiedName(ioTProductSort.getClassifiedName());
    }
    if (StrUtil.isBlank(ioTProduct.getProductId())) {
      ioTProduct.setProductId(productKey);
    }
    if (StrUtil.isBlank(ioTProduct.getProductSecret())) {
      ioTProduct.setProductSecret(IdUtil.fastSimpleUUID());
    }
    if (StrUtil.isBlank(ioTProduct.getConfiguration())) {
      ioTProduct.setConfiguration(buildProductCfg(ioTProduct));
    }
    if (StrUtil.isBlank(ioTProduct.getTransportProtocol())) {
      ioTProduct.setTransportProtocol(ioTProduct.getThirdPlatform());
    }
    ioTProduct.setProductKey(productKey);
    ioTProduct.setMetadata(defaultMetadata);
    ioTProduct.setThirdConfiguration(thirdConfig);
    ioTProduct.setStorePolicy("mysql");
    int i = ioTProductMapper.insertDevProduct(ioTProduct);
    if (i > 0) {
      ioTProductAction.create(productKey, ioTProduct.getCreatorId());
    }
    return i;
  }

  /** 默认创建mqtt设置自动注册 */
  private String buildProductCfg(IoTProduct ioTProduct) {
    JSONObject cfg = new JSONObject();
    boolean isMqtt = false;
    String thirdPlatform = ioTProduct == null ? null : ioTProduct.getThirdPlatform();
    if (thirdPlatform == null) {
      return JSONUtil.toJsonStr(cfg);
    }
    if (ProtocolModule.tcp.name().equalsIgnoreCase(thirdPlatform)
        || ProtocolModule.mqtt.name().equalsIgnoreCase(thirdPlatform)
        || ProtocolModule.udp.name().equalsIgnoreCase(thirdPlatform)) {
      cfg.set(IoTConstant.ALLOW_INSERT, true);
    }
    return JSONUtil.toJsonStr(cfg);
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey"
      },
      allEntries = true)
  public int insertList(List<IoTProduct> ioTProductList) {
    int i = ioTProductMapper.insertList(ioTProductList);
    return i;
  }

  /** 产品协议导入 */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "supportMQTTNetwork",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey"
      },
      allEntries = true)
  public String importProduct(List<IoTProductImportBO> productImportBos, String unionId) {
    List<IoTProduct> exitProducts = selectDevProductList(new IoTProduct());
    List<String> exitProductKeys = new ArrayList<>();
    if (CollectionUtil.isNotEmpty(exitProducts)) {
      exitProductKeys =
          exitProducts.stream().map(IoTProduct::getProductKey).collect(Collectors.toList());
    }
    List<IoTDeviceProtocol> protocolList = new ArrayList<>();
    // 普通产品导入
    List<IoTProduct> customProductList = new ArrayList<>();
    // tcp配置表
    List<Network> networkList = new ArrayList<>();
    // 电信产品导入
    List<IoTProduct> ctwingProductList = new ArrayList<>();
    // 失败列表
    List<IoTProduct> failedList = new ArrayList<>();
    List<String> finalExitProductKeys = exitProductKeys;
    AtomicInteger duplicateNum = new AtomicInteger();
    productImportBos.forEach(
        ioTProductImportBO -> {
          if (StrUtil.isNotBlank(ioTProductImportBO.getProtocolType())) {
            IoTDeviceProtocol ioTDeviceProtocol = new IoTDeviceProtocol();
            ioTDeviceProtocol.setId(ioTProductImportBO.getProductKey());
            ioTDeviceProtocol.setType(ioTProductImportBO.getProtocolType());
            ioTDeviceProtocol.setName(ioTProductImportBO.getProductKey()); // 使用productKey作为名称
            ioTDeviceProtocol.setState(ioTProductImportBO.getProtocolState());
            ioTDeviceProtocol.setConfiguration(ioTProductImportBO.getProtocolConfiguration());
            ioTDeviceProtocol.setExample(ioTProductImportBO.getProtocolExample());
            protocolList.add(ioTDeviceProtocol);
          }

          IoTProduct ioTProduct = BeanUtil.toBean(ioTProductImportBO, IoTProduct.class);
          if (ObjectUtil.isEmpty(ioTProduct)) {
            return;
          }
          if (StrUtil.isBlank(ioTProduct.getProductSecret())) {
            ioTProduct.setProductSecret(IdUtil.fastSimpleUUID());
          }
          if (finalExitProductKeys.contains(ioTProduct.getProductKey())) {
            duplicateNum.addAndGet(1);
            failedList.add(ioTProduct);
            return;
          }
          ioTProduct.setCreatorId(unionId);
          ioTProduct.setCreateTime(new Date());
          if (StrUtil.isBlank(ioTProduct.getConfiguration())) {
            ioTProduct.setConfiguration(null);
          }
          if (ProtocolModule.ctaiot.name().equals(ioTProduct.getThirdPlatform())) {
            JSONObject downRequest = JSONUtil.parseObj(ioTProduct.getThirdDownRequest());
            if (ObjectUtil.isNull(downRequest)) {
              failedList.add(ioTProduct);
              return;
            }
            downRequest.set("creatorId", unionId);
            ioTProduct.setThirdDownRequest(JSONUtil.toJsonStr(downRequest));
            ctwingProductList.add(ioTProduct);
          } else if (ProtocolModule.tcp.name().equals(ioTProduct.getThirdPlatform())) {
            customProductList.add(ioTProduct);
          } else {
            customProductList.add(ioTProduct);
          }
        });

    // 产品导入
    int customSuccess = 0;
    if (CollectionUtil.isNotEmpty(customProductList)) {
      customSuccess = insertList(customProductList);
    }
    AtomicInteger ctwingSuccess = new AtomicInteger();
    ctwingProductList.forEach(
        devProduct -> {
          R r =
              IoTDownlFactory.safeInvokeDown(
                  ProtocolModule.ctaiot.name(),
                  IoTConstant.DOWN_TO_THIRD_PLATFORM,
                  devProduct.getThirdDownRequest());
          if (!R.SUCCESS.equals(r.getCode())) {
            failedList.add(devProduct);
          } else {
            ctwingSuccess.addAndGet(1);
          }
        });

    // 获取所有导入失败的产品key
    List<String> productKeyList =
        failedList.stream().map(IoTProduct::getProductKey).collect(Collectors.toList());
    // 只导入已导入产品的协议
    List<IoTDeviceProtocol> addProtocolList =
        protocolList.stream()
            .filter(
                devProtocol -> {
                  return !productKeyList.contains(devProtocol.getId());
                })
            .collect(Collectors.toList());
    int protocolSuccess = 0;
    if (CollectionUtil.isNotEmpty(addProtocolList)) {
      protocolSuccess = devProtocolService.insertList(addProtocolList);
    }
    // 导入network表 并启动监听
    if (CollectionUtil.isNotEmpty(networkList)) {
      networkMapper.insertList(networkList);
      networkList.forEach(
          network -> {
            flushNettyServer(
                JSONUtil.toJsonStr(network.getConfiguration()),
                network.getProductKey(),
                TcpFlushType.start);
          });
    }

    ioTProductAction.create(null, unionId);
    String result =
        String.format(
            "导入普通产品总数：%s 个,成功：%s个；导入电信ctwing产品总数：%s 个，成功：%s 个；导入协议总数：%s 个，成功：%s 个。",
            customProductList.size() + duplicateNum.get(),
            customSuccess,
            ctwingProductList.size(),
            ctwingSuccess.get(),
            protocolList.size(),
            protocolSuccess);
    return result;
  }

  /**
   * 修改设备产品
   *
   * @param ioTProduct 设备产品
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "supportMQTTNetwork",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey",
        "iot_product_device",
        "getProductEncoderType",
        "getProductDecoderType"
      },
      allEntries = true)
  public int updateDevProduct(IoTProduct ioTProduct) {
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    if (iotUser == null) {
      throw new IoTException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED);
    }
    String appUnionId = iotUser.getUnionId();
    IoTProduct pro = ioTProductMapper.selectByPrimaryKey(ioTProduct.getId());
    if (!appUnionId.equals(pro.getCreatorId()) && !iotUser.isAdmin()) {
      throw new IoTException("没有产品权限");
    }
    if (Objects.nonNull(ioTProduct.getClassifiedId())) {
      IoTProductSort ioTProductSort =
          ioTProductSortMapper.selectDevProductSortById(ioTProduct.getClassifiedId());
      ioTProduct.setClassifiedName(ioTProductSort.getClassifiedName());
    }
    ioTProduct.setUpdateTime(new Date());
    int count = ioTProductMapper.updateByPrimaryKeySelective(ioTProduct);
    log.info("updateDevProduct,操作成功={},userId={}", count, appUnionId);
    ioTProductAction.update(ioTProduct);
    return count;
  }

  /**
   * 批量删除设备产品
   *
   * @param ids 需要删除的设备产品主键
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "supportMQTTNetwork",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey"
      },
      allEntries = true)
  public int deleteDevProductByIds(String[] ids) {
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    if (iotUser == null) {
      throw new IoTException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED);
    }
    String appUnionId = iotUser.getUnionId();
    int count = 0;
    for (String id : ids) {
      IoTProduct ioTProduct = ioTProductMapper.selectDevProductById(id);
      if (!appUnionId.equals(ioTProduct.getCreatorId())) {
        throw new IoTException("无产品权限");
      }
      count = +deleteDevProductById(id);
    }
    return count;
  }

  /**
   * 删除设备产品信息
   *
   * @param id 设备产品主键
   * @return 结果
   */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "supportMQTTNetwork",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey"
      },
      allEntries = true)
  public int deleteDevProductById(String id) {
    String appUnionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    IoTProduct ioTProduct = selectDevProductById(id);
    if (ioTProduct == null) {
      throw new IoTException("产品不存在");
    }
    if (!appUnionId.equals(ioTProduct.getCreatorId())) {
      throw new IoTException("无产品权限");
    }
    IoTDevice ioTDevice = IoTDevice.builder().productKey(ioTProduct.getProductKey()).build();
    int count = ioTDeviceMapper.selectCount(ioTDevice);
    if (count > 0) {
      throw new IoTException("" + ioTProduct.getName() + "存在设备，不允许删除");
    }
    int delCount = ioTProductMapper.deleteDevProductById(id);
    if (delCount > 0) {
      ioTProductAction.delete(ioTProduct.getProductKey());
      log.info(
          "删除产品={},删除状态={}，删除人={}",
          ioTProduct.getProductKey() + ioTProduct.getName(),
          delCount,
          appUnionId);
    }
    return delCount;
  }

  /** 修改产品配置信息 */
  @Override
  // @Transactional
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "supportMQTTNetwork",
        "iot_dev_product_list",
        "getProductConfiguration",
        "getProductEncoderType",
        "getProductDecoderType"
      },
      allEntries = true)
  public int updateDevProductConfig(IoTProductVO devProduct) {
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    if (iotUser == null || iotUser.getUnionId() == null) {
      throw new IoTException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED);
    }
    // 根据id获取产品信息
    IoTProduct product = ioTProductMapper.selectDevProductById(String.valueOf(devProduct.getId()));
    if (devProduct == null) {
      throw new IoTException("产品不存在");
    }
    if (!iotUser.getUnionId().equals(product.getCreatorId()) && !iotUser.isAdmin()) {
      throw new IoTException("无产品权限");
    }
    // 获取新的产品配置信息
    JSONObject newConfig = JSONUtil.parseObj(devProduct.getConfiguration());
    // 判断tcp端口号是否冲突
    if ("tcp".equals(product.getThirdPlatform())
        && "privateConfiguration".equals(devProduct.getType())) {
      Integer port = null;
      try {
        port = newConfig.getInt("port");
      } catch (Exception e) {
        throw new IoTException("端口号存在字符");
      }
      // 判断端口号是否冲突
      if (Objects.nonNull(port)) {
        // 判断是否和数据库中其他tcp端口配置冲突
        int count = networkMapper.selectNetWorkByPort(port, product.getProductKey());
        if (count > 0) {
          throw new IoTException(port + "端口" + "已被占用");
        }
      }
    }
    // 获取旧的产品配置信息
    JSONObject oldConfig = JSONUtil.parseObj(product.getConfiguration());
    // 循环遍历：新值替换旧值
    for (Map.Entry<String, Object> entry : newConfig.entrySet()) {
      oldConfig.set(entry.getKey(), entry.getValue());
    }
    devProduct.setConfiguration(oldConfig.toString());
    int updateCount = ioTProductMapper.updateDevProduct(devProduct);
    if (updateCount > 0) {
      ioTProductAction.update(devProduct);
      log.info(
          "更新产品={},更新状态={}，更新人={}",
          devProduct.getProductKey() + devProduct.getName(),
          updateCount,
          iotUser.getUnionId());
    }

    return updateCount;
  }

  /**
   * 查询设备产品物模型属性列表
   *
   * @return 设备产品物模型属性集合
   */
  @Override
  public List<IoTDeviceProperties> selectDevProperties(String Id) {
    IoTProduct ioTProduct = ioTProductMapper.selectDevProductById(Id);
    JSONObject metadata = JSONUtil.parseObj(ioTProduct.getMetadata());
    List<IoTDeviceProperties> ioTDevicePropertiesList = new ArrayList<>();
    JSONArray properties = metadata.getJSONArray("properties");
    if (properties != null) {
      for (Object object : properties) {
        JSONObject jsonObject = JSONUtil.parseObj(object);
        JSONObject valueType = JSONUtil.parseObj(jsonObject.getStr("valueType"));
        JSONObject expands = JSONUtil.parseObj(jsonObject.getStr("expands"));
        IoTDeviceProperties ioTDeviceProperties =
            IoTDeviceProperties.builder()
                .id(jsonObject.getStr("id"))
                .name(jsonObject.getStr("name"))
                .type(valueType.getStr("type"))
                .unit(valueType.getStr("unit"))
                .elements(valueType.getStr("elements"))
                .mode(jsonObject.getStr("mode"))
                .source(jsonObject.getStr("source"))
                .description(jsonObject.getStr("description"))
                .build();
        ioTDevicePropertiesList.add(ioTDeviceProperties);
      }
    }
    return ioTDevicePropertiesList;
  }

  /**
   * 查询设备产品物模型事件列表
   *
   * @return 设备产品物模型事件集合
   */
  @Override
  public List<IoTDeviceEvents> selectDevEvents(String Id) {
    IoTProduct ioTProduct = ioTProductMapper.selectDevProductById(Id);
    JSONObject metadata = JSONUtil.parseObj(ioTProduct.getMetadata());
    List<IoTDeviceEvents> ioTDeviceEventsList = new ArrayList<>();
    JSONArray properties = metadata.getJSONArray("events");
    if (properties != null) {
      for (Object object : properties) {
        JSONObject jsonObject = JSONUtil.parseObj(object);
        JSONObject expands = JSONUtil.parseObj(jsonObject.getStr("expands"));
        IoTDeviceEvents ioTDeviceEvents =
            IoTDeviceEvents.builder()
                .id(jsonObject.getStr("id"))
                .name(jsonObject.getStr("name"))
                .description(jsonObject.getStr("description"))
                .level(expands.getStr("level"))
                .build();
        ioTDeviceEventsList.add(ioTDeviceEvents);
      }
    }
    return ioTDeviceEventsList;
  }

  /**
   * 查询设备产品物模型方法列表
   *
   * @return 设备产品物模型方法集合
   */
  @Override
  public List<IoTDeviceFunction> selectDevFunctions(String Id) {
    IoTProduct ioTProduct = ioTProductMapper.selectDevProductById(Id);
    JSONObject metadata = JSONUtil.parseObj(ioTProduct.getMetadata());
    List<IoTDeviceFunction> ioTDeviceFunctionList = new ArrayList<>();
    JSONArray properties = metadata.getJSONArray("functions");
    if (properties != null) {
      for (Object object : properties) {
        JSONObject jsonObject = JSONUtil.parseObj(object);
        IoTDeviceFunction ioTDeviceFunction =
            IoTDeviceFunction.builder()
                .id(jsonObject.getStr("id"))
                .name(jsonObject.getStr("name"))
                .config(jsonObject.getBool("config") == null ? false : jsonObject.getBool("config"))
                .inputs(jsonObject.getStr("inputs"))
                .source(jsonObject.getStr("source"))
                .description(jsonObject.getStr("description"))
                .build();
        ioTDeviceFunctionList.add(ioTDeviceFunction);
      }
    }
    return ioTDeviceFunctionList;
  }

  // @PostConstruct
  // public void s2s(){
  //    JSONObject object=new JSONObject();
  //    object.set("id","testt222");
  //    object.set("name","测试df");
  ////    object.set("expands","ff");
  //  JSONObject k=new JSONObject();
  //    object.set("valueType",k);
  //    k.set("type","string");
  //    k.set("expands",null);
  //   int a= insertMetadata("test","properties", JSONUtil.toJsonStr(object));
  //  System.out.println(a);
  // }
  //  @PostConstruct
  // public void ss(){
  //    JSONObject object=new JSONObject();
  //    object.set("id","testt222");
  //    object.set("name","测试df");
  ////    object.set("expands","ff");
  //  JSONObject k=new JSONObject();
  //    object.set("valueType",k);
  //    k.set("type","string");
  //    k.set("expands",null);
  //   int a= deleteMetadata("test","properties", "testt222");
  //  System.out.println(a);
  // }

  /** 物模型新增字段 */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "selectAllEnableNetworkProductKey",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int insertMetadata(IoTProductBO ioTProductBO) {
    IoTProduct ioTProduct =
        ioTProductMapper.selectOne(
            IoTProduct.builder().productKey(ioTProductBO.getProductKey()).build());
    if (ioTProduct == null) {
      throw new IoTException("产品不存在");
    }
    ioTProductBO.toJsonObj();
    if (getMetadata(ioTProductBO) != null) {
      throw new IoTException("该id已存在");
    }
    switch (ioTProductBO.getType()) {
      case "properties":
        ioTProductBO.setPath("$.properties");
        break;
      case "events":
        ioTProductBO.setPath("$.events");
        break;
      case "functions":
        ioTProductBO.setPath("$.functions");
        break;
      default:
        throw new IoTException("物模型类型不存在!");
    }
    int updateCount = ioTProductMapper.insertMetadata(ioTProductBO);
    if (updateCount > 0) {
      ioTProductAction.metadataCreate(ioTProductBO.getProductKey());
      log.info(
          "产品新增物模型={},新增物模型状态={}，修改人={}",
          ioTProduct.getProductKey() + ioTProduct.getName(),
          updateCount,
          ioTProduct.getCreatorId());
    }
    return updateCount;
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int deleteMetadata(IoTProductBO ioTProductBO) {
    String appUnionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    IoTProduct product = ioTProductMapper.getProductByProductKey(ioTProductBO.getProductKey());
    if (!appUnionId.equals(product.getCreatorId())) {
      throw new IoTException("无产品权限");
    }
    ioTProductBO.setPath(getPath(ioTProductBO));
    int delCount = ioTProductMapper.deleteMetadata(ioTProductBO);
    if (delCount > 0) {
      ioTProductAction.metadataDelete(ioTProductBO.getProductKey());
      log.info(
          "删除物模型={},删除物模型状态={}，修改人={}",
          ioTProductBO.getProductKey() + ioTProductBO.getName(),
          delCount,
          appUnionId);
    }
    return delCount;
  }

  @Override
  public IoTProductBO getMetadata(IoTProductBO ioTProductBO) {
    ioTProductBO.setPath(getPath(ioTProductBO));
    IoTProductBO result = ioTProductMapper.getMetadata(ioTProductBO);
    // result.jsonToBean();
    return result;
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int updateMetadata(IoTProductBO ioTProductBO) {
    String appUnionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    IoTProduct product = ioTProductMapper.getProductByProductKey(ioTProductBO.getProductKey());
    if (!appUnionId.equals(product.getCreatorId())) {
      throw new IoTException("无产品权限");
    }
    ioTProductBO.toJsonObj();
    //    if(getMetadata(ioTProductBO)!=null){
    //      throw new IoTException("该id已存在");
    //    }
    ioTProductBO.setPath(getPath(ioTProductBO));

    int updateCount = ioTProductMapper.updateMetadata(ioTProductBO);
    if (updateCount > 0) {
      ioTProductAction.metadataUpdate(ioTProductBO.getProductKey());
      log.info(
          "产品新增物模型={},新增物模型状态={}，修改人={}",
          ioTProductBO.getProductKey() + ioTProductBO.getName(),
          updateCount,
          appUnionId);
    }
    return updateCount;
  }

  public String getPath(IoTProductBO ioTProductBO) {
    switch (ioTProductBO.getType()) {
      case "properties":
        return "$.properties**.id";
      case "events":
        return "$.events**.id";
      case "functions":
        return "$.functions**.id";
      default:
        throw new IoTException("物模型键不存在!");
    }
  }

  @Override
  public IoTDeviceModelVO getModelByProductKey(String productKey) {
    return ioTProductMapper.getModelByProductKey(productKey);
  }

  @Override
  public String getMetadataProductKey(String devId) {
    return ioTProductMapper.getMetadataProductKey(devId);
  }

  @Override
  @MultiLevelCacheable(
      cacheNames = "countDevNumberByProductKey",
      keyGenerator = "redisKeyGenerate",
      unless = "#result == null",
      l1Expire = 30)
  public Map<String, Integer> countDevNumberByProductKey(String unionId) {
    List<IoTProductVO> results = ioTProductMapper.countDevNumberByProductKey(unionId);
    return results.stream()
        .collect(Collectors.toMap(IoTProductVO::getProductKey, IoTProductVO::getDevNum));
  }

  /** 设备数量变化时清除相关缓存 当设备新增、删除、修改时调用此方法 */
  @CacheEvict(
      cacheNames = {"countDevNumberByProductKey", "selectDevProductV4ListNoPage"},
      allEntries = true)
  public void evictDeviceCountCache() {
    // 此方法用于手动清除设备数量相关缓存
    // 当设备数据发生变化时调用
  }

  @Override
  public AjaxResult<IoTProduct> selectIoTProductByKey(String key) {
    IoTProduct ioTProduct = new IoTProduct();
    ioTProduct.setProductKey(key);
    return AjaxResult.success(ioTProductMapper.selectOne(ioTProduct));
  }

  @Override
  public AjaxResult<List<IoTProduct>> selectGatewaySubProductsByKey(String gwProductKey) {
    IoTProduct ioTProduct = new IoTProduct();
    ioTProduct.setGwProductKey(gwProductKey);
    ioTProduct.setState((byte) 0);
    return AjaxResult.success(ioTProductMapper.select(ioTProduct));
  }

  /** 修改产品其他配置信息 */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "selectAllEnableNetworkProductKey",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int updateDevProductOtherConfig(String otherConfig) {
    // 解析新的配置
    JSONObject newConfig = JSONUtil.parseObj(otherConfig);
    // 获取产品信息
    IoTProduct product = ioTProductMapper.selectDevProductById(newConfig.getStr("productId"));
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    if (!product.getCreatorId().equals(iotUser.getUnionId())) {
      throw new IoTException("无产品权限");
    }
    // 获取第三方配置
    String thirdConfiguration = product.getThirdConfiguration();
    JSONObject oldConfig = null;
    // 如果配置等于空，设置默认值
    if (StrUtil.isBlank(thirdConfiguration)) {
      oldConfig = new JSONObject();
    } else {
      oldConfig = JSONUtil.parseObj(thirdConfiguration);
    }

    // 获取新的配置信息
    String ext1Id = newConfig.getStr("ext1Id");
    String ext2Id = newConfig.getStr("ext2Id");
    String ext3Id = newConfig.getStr("ext3Id");
    String ext4Id = newConfig.getStr("ext4Id");
    // 构造后端验证
    JSONArray customField = new JSONArray();
    if (StrUtil.isNotEmpty(ext1Id)) {
      JSONObject customeDemo = new JSONObject();
      customeDemo.set("id", ext1Id);
      customeDemo.set("name", newConfig.getStr("ext1Name"));
      customeDemo.set("description", newConfig.getStr("ext1Des"));
      customField.add(customeDemo);
      oldConfig.set("ext1Label", newConfig.getStr("ext1Name"));
      oldConfig.set("ext1LabelDes", newConfig.getStr("ext1Des"));
    }
    if (StrUtil.isNotEmpty(ext2Id)) {
      JSONObject customeDemo = new JSONObject();
      customeDemo.set("id", ext2Id);
      customeDemo.set("name", newConfig.getStr("ext2Name"));
      customeDemo.set("description", newConfig.getStr("ext2Des"));
      customField.add(customeDemo);
      oldConfig.set("ext2Label", newConfig.getStr("ext2Name"));
      oldConfig.set("ext2LabelDes", newConfig.getStr("ext2Des"));
    }
    if (StrUtil.isNotEmpty(ext3Id)) {
      JSONObject customeDemo = new JSONObject();
      customeDemo.set("id", ext3Id);
      customeDemo.set("name", newConfig.getStr("ext3Name"));
      customeDemo.set("description", newConfig.getStr("ext3Des"));
      customField.add(customeDemo);
      oldConfig.set("ext3Label", newConfig.getStr("ext3Name"));
      oldConfig.set("ext3LabelDes", newConfig.getStr("ext3Des"));
    }
    if (StrUtil.isNotEmpty(ext4Id)) {
      JSONObject customeDemo = new JSONObject();
      customeDemo.set("id", ext4Id);
      customeDemo.set("name", newConfig.getStr("ext4Name"));
      customeDemo.set("description", newConfig.getStr("ext4Des"));
      customField.add(customeDemo);
      oldConfig.set("ext4Label", newConfig.getStr("ext4Name"));
      oldConfig.set("ext4LabelDes", newConfig.getStr("ext4Des"));
    }
    if (StrUtil.isEmpty(ext1Id)) {
      String ext1Label = oldConfig.getStr("ext1Label");
      if (StrUtil.isNotEmpty(ext1Label)) {
        oldConfig.remove("ext1Label");
      }
      String ext1LabelDes = oldConfig.getStr("ext1LabelDes");
      if (StrUtil.isNotEmpty(ext1LabelDes)) {
        oldConfig.remove("ext1LabelDes");
      }
    }
    if (StrUtil.isEmpty(ext2Id)) {
      String ext2Label = oldConfig.getStr("ext2Label");
      if (StrUtil.isNotEmpty(ext2Label)) {
        oldConfig.remove("ext2Label");
      }
      String ext2LabelDes = oldConfig.getStr("ext2LabelDes");
      if (StrUtil.isNotEmpty(ext2LabelDes)) {
        oldConfig.remove("ext2LabelDes");
      }
    }
    if (StrUtil.isEmpty(ext3Id)) {
      String ext3Label = oldConfig.getStr("ext3Label");
      if (StrUtil.isNotEmpty(ext3Label)) {
        oldConfig.remove("ext3Label");
      }
      String ext3LabelDes = oldConfig.getStr("ext3LabelDes");
      if (StrUtil.isNotEmpty(ext3LabelDes)) {
        oldConfig.remove("ext3LabelDes");
      }
    }
    if (StrUtil.isEmpty(ext4Id)) {
      String ext4Label = oldConfig.getStr("ext4Label");
      if (StrUtil.isNotEmpty(ext4Label)) {
        oldConfig.remove("ext4Label");
      }
      String ext4LabelDes = oldConfig.getStr("ext4LabelDes");
      if (StrUtil.isNotEmpty(ext4LabelDes)) {
        oldConfig.remove("ext4LabelDes");
      }
    }

    oldConfig.set("customField", customField);
    product.setThirdConfiguration(oldConfig.toString());

    int updateCount = ioTProductMapper.updateDevProduct(product);
    if (updateCount > 0) {
      ioTProductAction.update(product);
      log.info(
          "产品其他配置更改={},产品其他配置更改状态={}，修改人={}",
          product.getProductKey() + product.getName(),
          updateCount,
          product.getCreatorId());
    }
    return updateCount;
  }

  @Override
  @CacheEvict(cacheNames = {"iot_product_log_store_policy"},allEntries = true)
  public int updateDevProductStoreConfig(IoTProductVO storeConfig) {
    // 获取产品信息
    IoTProduct product = ioTProductMapper.selectDevProductById(String.valueOf(storeConfig.getId()));
    if (product == null) {
      throw new IoTException("产品不存在!");
    }
    product.setStorePolicyConfiguration(storeConfig.getStorePolicyConfiguration());
    int updateCount = ioTProductMapper.updateDevProduct(product);
    return updateCount;
  }

  @Override
  public void flushNettyServer(String config, String productKey, TcpFlushType type) {
    JSONObject object = new JSONObject();
    object.set("type", ProductFlushType.server.name());
    object.set("productKey", productKey);
    object.set("customType", type.name());
    eventPublisher.publishEvent(EventTopics.PRODUCT_CONFIG_UPDATED, object);
    // 使用ProtocolClusterService进行集群操作
    Map<String, ProtocolClusterService> beansOfType =
        SpringUtil.getBeansOfType(ProtocolClusterService.class);
    if (MapUtil.isNotEmpty(beansOfType)) {
      beansOfType.forEach(
          (key, value) -> {
            if (key != null) {
              switch (type) {
                case start -> value.start(productKey);
                case reload -> value.restart(productKey);
                case close -> value.stop(productKey);
              }
            }
          });
    }
  }

  @Override
  public List<IoTProductVO> selectDevProductAllList() {
    return ioTProductMapper.selectDevProductAllList();
  }

  @Override
  public List<IoTProductVO> getGatewayList() {
    List<IoTProductVO> devProductVOS = ioTProductMapper.selectDevProductAllList();
    devProductVOS =
        devProductVOS.stream()
            .filter(devProductVO -> DeviceNode.GATEWAY.name().equals(devProductVO.getDeviceNode()))
            .collect(Collectors.toList());
    return devProductVOS;
  }

  @Override
  public List<IoTProductVO> getGatewaySubDeviceList(String productKey) {
    List<IoTProductVO> devProductVOS = ioTProductMapper.getGatewaySubDeviceList(productKey);
    return devProductVOS;
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "supportMQTTNetwork",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int bindCertificate(String productKey, String sslKey) {
    IoTProduct product =
        ioTProductMapper.selectOne(IoTProduct.builder().productKey(productKey).build());
    if (product == null) {
      throw new IoTException("产品不存在");
    }
    JSONObject config = null;
    try {
      config = JSONUtil.parseObj(product.getConfiguration());
    } catch (Exception e) {
      config = new JSONObject();
    }
    config.set("ssl", true);
    config.set("sslKey", sslKey);
    product.setConfiguration(JSONUtil.toJsonStr(config));
    return ioTProductMapper.updateByPrimaryKeySelective(product);
  }

  @Override
  @CacheEvict(
      cacheNames = {
        "iot_dev_instance_bo",
        "iot_dev_metadata_bo",
        "iot_dev_shadow_bo",
        "iot_dev_action",
        "selectDevCount",
        "supportMQTTNetwork",
        "iot_dev_product_list"
      },
      allEntries = true)
  public int unbindCertificate(String productKey) {
    IoTProduct product =
        ioTProductMapper.selectOne(IoTProduct.builder().productKey(productKey).build());
    if (product == null) {
      throw new IoTException("产品不存在");
    }
    JSONObject config = null;
    try {
      config = JSONUtil.parseObj(product.getConfiguration());
    } catch (Exception e) {
      config = new JSONObject();
    }
    config.set("ssl", false);
    config.set("sslKey", "");
    product.setConfiguration(JSONUtil.toJsonStr(config));
    return ioTProductMapper.updateByPrimaryKeySelective(product);
  }

  /** 导出产品完整包（包含协议、网络配置、物模型） 支持导出后直接导入使用，无需额外配置 */
  @Override
  public List<ProductExportPackageDTO> exportProductPackages(IoTProductQuery query) {
    // 查询产品列表
    List<IoTProductExportVO> products = ioTProductMapper.selectAllDevProductV2List(query);
    if (CollectionUtil.isEmpty(products)) {
      return new ArrayList<>();
    }

    LocalDateTime currentTime = LocalDateTime.now();
    String currentUserId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();

    return products.stream()
        .map(
            exportVO -> {
              ProductExportPackageDTO packageDTO = new ProductExportPackageDTO();
              packageDTO.setExportVersion("1.0");
              packageDTO.setExportTime(
                  currentTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
              packageDTO.setExportUserId(currentUserId);

              // 1. 构建产品基本信息
              ProductInfoDTO productInfo =
                  ProductInfoDTO.builder()
                      .productId(exportVO.getProductId())
                      .productKey(exportVO.getProductKey())
                      .productSecret(exportVO.getProductSecret())
                      .thirdPlatform(exportVO.getThirdPlatform())
                      .thirdConfiguration(exportVO.getThirdConfiguration())
                      .companyNo(exportVO.getCompanyNo())
                      .classifiedId(exportVO.getClassifiedId())
                      .classifiedName(exportVO.getClassifiedName())
                      .deviceNode(exportVO.getDeviceNode())
                      .gwProductKey(exportVO.getGwProductKey())
                      .messageProtocol(exportVO.getMessageProtocol())
                      .name(exportVO.getName())
                      .state(exportVO.getState())
                      .describe(exportVO.getDescribe())
                      .storePolicy(exportVO.getStorePolicy())
                      .transportProtocol(exportVO.getTransportProtocol())
                      .photoUrl(exportVO.getPhotoUrl())
                      .configuration(exportVO.getConfiguration())
                      .storePolicyConfiguration(exportVO.getStorePolicyConfiguration())
                      .metadata(exportVO.getMetadata())
                      .thirdDownRequest(exportVO.getThirdDownRequest())
                      .build();
              packageDTO.setProduct(productInfo);

              // 2. 查询并构建设备协议信息
              if (StrUtil.isNotBlank(exportVO.getProductKey())) {
                IoTDeviceProtocol protocol =
                    devProtocolService.selectDevProtocolById(
                        exportVO.getProductKey(), currentUserId);
                if (protocol != null) {
                  DeviceProtocolDTO protocolDTO =
                      DeviceProtocolDTO.builder()
                          .name(protocol.getName())
                          .description(protocol.getDescription())
                          .state(protocol.getState())
                          .type(protocol.getType())
                          .configuration(protocol.getConfiguration())
                          .example(protocol.getExample())
                          .build();
                  packageDTO.setProtocol(protocolDTO);
                }
              }

              // 3. 查询并构建网络配置信息(TCP/MQTT/UDP)
              if (StrUtil.isNotBlank(exportVO.getProductKey())
                  && (ProtocolModule.tcp.name().equalsIgnoreCase(exportVO.getThirdPlatform())
                      || ProtocolModule.mqtt.name().equalsIgnoreCase(exportVO.getThirdPlatform())
                      || ProtocolModule.udp.name().equalsIgnoreCase(exportVO.getThirdPlatform()))) {
                Network network =
                    networkMapper.selectOne(
                        Network.builder().productKey(exportVO.getProductKey()).build());
                if (network != null) {
                  NetworkConfigDTO networkDTO =
                      NetworkConfigDTO.builder()
                          .type(network.getType())
                          .name(network.getName())
                          .description(network.getDescription())
                          .state(network.getState())
                          .configuration(network.getConfiguration())
                          .build();
                  packageDTO.setNetwork(networkDTO);
                }
              }

              return packageDTO;
            })
        .collect(Collectors.toList());
  }

  /** 导入产品完整包 自动处理产品、协议、网络配置、物模型等所有信息 */
  @Override
  @CacheEvict(
      cacheNames = {
        "iot_all_dev_product_list",
        "iot_dev_product_list",
        "supportMQTTNetwork",
        "selectDevProductV4ListNoPage",
        "countDevNumberByProductKey"
      },
      allEntries = true)
  public String importProductPackages(List<ProductExportPackageDTO> packages, String unionId) {
    if (CollectionUtil.isEmpty(packages)) {
      return "导入包为空";
    }

    List<IoTProduct> exitProducts = selectDevProductList(new IoTProduct());
    List<String> exitProductKeys =
        exitProducts.stream().map(IoTProduct::getProductKey).collect(Collectors.toList());

    // 统计信息
    AtomicInteger successProducts = new AtomicInteger(0);
    AtomicInteger successProtocols = new AtomicInteger(0);
    AtomicInteger successNetworks = new AtomicInteger(0);
    AtomicInteger duplicateProducts = new AtomicInteger(0);
    AtomicInteger ctwingSuccess = new AtomicInteger(0);
    AtomicInteger ctwingTotal = new AtomicInteger(0);

    List<IoTProduct> failedProducts = new ArrayList<>();

    packages.forEach(
        packageDTO -> {
          try {
            ProductInfoDTO productInfo = packageDTO.getProduct();
            if (productInfo == null) {
              return;
            }

            // 检查产品是否已存在
            if (exitProductKeys.contains(productInfo.getProductKey())) {
              duplicateProducts.incrementAndGet();
              log.warn(
                  "产品已存在，跳过导入: productKey={}, name={}",
                  productInfo.getProductKey(),
                  productInfo.getName());
              return;
            }

            // 1. 生成新的ProductKey和ProductSecret
            String newProductKey = RandomUtil.randomString(12);
            String newProductSecret = IdUtil.fastSimpleUUID();

            // 2. 构建产品实体
            IoTProduct product =
                IoTProduct.builder()
                    .productId(
                        StrUtil.isNotBlank(productInfo.getProductId())
                            ? productInfo.getProductId()
                            : newProductKey)
                    .productKey(newProductKey)
                    .productSecret(newProductSecret)
                    .tags(productInfo.getTags() != null ? productInfo.getTags() : "")
                    .thirdPlatform(productInfo.getThirdPlatform())
                    .thirdConfiguration(
                        StrUtil.isNotBlank(productInfo.getThirdConfiguration())
                            ? productInfo.getThirdConfiguration()
                            : thirdConfig)
                    .companyNo(productInfo.getCompanyNo())
                    .classifiedId("100100") // 使用默认分类
                    .classifiedName("验证(beta)")
                    .deviceNode(productInfo.getDeviceNode())
                    .gwProductKey(productInfo.getGwProductKey())
                    .messageProtocol(productInfo.getMessageProtocol())
                    .name(productInfo.getName())
                    .creatorId(unionId) // 使用导入人作为创建者
                    .state(productInfo.getState())
                    .describe(productInfo.getDescribe())
                    .storePolicy(
                        StrUtil.isNotBlank(productInfo.getStorePolicy())
                            ? productInfo.getStorePolicy()
                            : "mysql")
                    .transportProtocol(
                        StrUtil.isNotBlank(productInfo.getTransportProtocol())
                            ? productInfo.getTransportProtocol()
                            : productInfo.getThirdPlatform())
                    .photoUrl(productInfo.getPhotoUrl())
                    .configuration(
                        StrUtil.isNotBlank(productInfo.getConfiguration())
                            ? productInfo.getConfiguration()
                            : buildProductCfg(null))
                    .storePolicyConfiguration(productInfo.getStorePolicyConfiguration())
                    .metadata(
                        StrUtil.isNotBlank(productInfo.getMetadata())
                            ? productInfo.getMetadata()
                            : defaultMetadata)
                    .thirdDownRequest(productInfo.getThirdDownRequest())
                    .createTime(new Date())
                    .build();

            // 3. 处理CTWing产品（需要调用第三方平台创建）
            if (ProtocolModule.ctaiot.name().equals(product.getThirdPlatform())) {
              ctwingTotal.incrementAndGet();
              if (StrUtil.isNotBlank(product.getThirdDownRequest())) {
                JSONObject downRequest = JSONUtil.parseObj(product.getThirdDownRequest());
                downRequest.set("creatorId", unionId);
                product.setThirdDownRequest(JSONUtil.toJsonStr(downRequest));

                R r =
                    IoTDownlFactory.safeInvokeDown(
                        ProtocolModule.ctaiot.name(),
                        IoTConstant.DOWN_TO_THIRD_PLATFORM,
                        product.getThirdDownRequest());
                if (!R.SUCCESS.equals(r.getCode())) {
                  failedProducts.add(product);
                  log.error("CTWing产品创建失败: productKey={}, error={}", newProductKey, r.getMsg());
                  return;
                }
                ctwingSuccess.incrementAndGet();
              }
            }

            // 4. 插入产品
            int inserted = ioTProductMapper.insertDevProduct(product);
            if (inserted > 0) {
              successProducts.incrementAndGet();
              exitProductKeys.add(newProductKey); // 添加到已存在列表，防止后续重复

              // 5. 导入设备协议
              DeviceProtocolDTO protocolDTO = packageDTO.getProtocol();
              if (protocolDTO != null && StrUtil.isNotBlank(protocolDTO.getConfiguration())) {
                IoTDeviceProtocol protocol =
                    IoTDeviceProtocol.builder()
                        .id(newProductKey) // 使用新的ProductKey作为协议ID
                        .name(
                            StrUtil.isNotBlank(protocolDTO.getName())
                                ? protocolDTO.getName()
                                : productInfo.getName())
                        .description(protocolDTO.getDescription())
                        .state(protocolDTO.getState())
                        .type(protocolDTO.getType())
                        .configuration(protocolDTO.getConfiguration())
                        .example(protocolDTO.getExample())
                        .createTime(new Date())
                        .build();

                // 更新协议配置中的provider为新的ProductKey
                if (StrUtil.isNotBlank(protocol.getConfiguration())) {
                  JSONObject protocolConfig = JSONUtil.parseObj(protocol.getConfiguration());
                  protocolConfig.set("provider", newProductKey);
                  protocol.setConfiguration(JSONUtil.toJsonStr(protocolConfig));
                }

                int protocolInserted = devProtocolService.insertDevProtocol(protocol);
                if (protocolInserted > 0) {
                  successProtocols.incrementAndGet();
                }
              }

              // 6. 导入网络配置（TCP/MQTT）
              NetworkConfigDTO networkDTO = packageDTO.getNetwork();
              if (networkDTO != null && StrUtil.isNotBlank(networkDTO.getConfiguration())) {
                Network network =
                    Network.builder()
                        .type(networkDTO.getType())
                        .productKey(newProductKey) // 使用新的ProductKey
                        .unionId(newProductKey) // 使用新的ProductKey作为unionId
                        .name(
                            StrUtil.isNotBlank(networkDTO.getName())
                                ? networkDTO.getName()
                                : productInfo.getName())
                        .description(networkDTO.getDescription())
                        .state(networkDTO.getState() != null ? networkDTO.getState() : false)
                        .configuration(networkDTO.getConfiguration())
                        .createUser(unionId)
                        .createDate(new Date())
                        .build();

                int networkInserted = networkMapper.insert(network);
                if (networkInserted > 0) {
                  successNetworks.incrementAndGet();

                  // 如果网络是启用状态，启动监听
                  if (Boolean.TRUE.equals(network.getState())) {
                    try {
                      flushNettyServer(
                          network.getConfiguration(), newProductKey, TcpFlushType.start);
                    } catch (Exception e) {
                      log.error("启动网络监听失败: productKey={}, error={}", newProductKey, e.getMessage());
                    }
                  }
                }
              }

              // 7. 触发产品创建事件
              ioTProductAction.create(newProductKey, unionId);
            }
          } catch (Exception e) {
            log.error("导入产品包失败: {}", e.getMessage(), e);
          }
        });

    // 构建导入结果报告
    String result =
        String.format(
            "导入完成！总计：%d 个产品包。\n"
                + "✓ 成功导入产品：%d 个\n"
                + "✓ 成功导入协议：%d 个\n"
                + "✓ 成功导入网络配置：%d 个\n"
                + "✓ CTWing产品：总计 %d 个，成功 %d 个\n"
                + "⚠ 跳过重复产品：%d 个\n"
                + "✗ 失败产品：%d 个",
            packages.size(),
            successProducts.get(),
            successProtocols.get(),
            successNetworks.get(),
            ctwingTotal.get(),
            ctwingSuccess.get(),
            duplicateProducts.get(),
            failedProducts.size());

    log.info("产品包导入完成: {}", result);
    return result;
  }
}

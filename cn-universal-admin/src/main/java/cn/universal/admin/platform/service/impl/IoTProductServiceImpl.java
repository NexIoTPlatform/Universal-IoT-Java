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
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.platform.service.IIoTProductService;
import cn.universal.admin.system.service.IoTDeviceProtocolService;
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
import cn.universal.persistence.entity.vo.IoTDeviceModelVO;
import cn.universal.persistence.entity.vo.IoTProductExportVO;
import cn.universal.persistence.entity.vo.IoTProductVO;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.IoTProductSortMapper;
import cn.universal.persistence.mapper.NetworkMapper;
import cn.universal.persistence.query.AjaxResult;
import cn.universal.persistence.query.IoTProductQuery;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
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
  //  @Cacheable(cacheNames = "iot_dev_product_list", unless = "#result == null", keyGenerator =
  // "redisKeyGenerate")
  public List<IoTProductVO> selectDevProductV4List(IoTProductQuery ioTProductQuery) {
    Page<IoTProductVO> page =
        PageHelper.startPage(ioTProductQuery.getPageNum(), ioTProductQuery.getPageSize());
    List<IoTProductVO> devProductVOS = ioTProductMapper.selectDevProductV3List(ioTProductQuery);
    List<IoTProductVO> results =
        ioTProductMapper.countDevNumberByProductKey(ioTProductQuery.getCreatorId());
    if (CollUtil.isNotEmpty(results)) {
      final Map<String, Integer> collect =
          results.stream()
              .collect(Collectors.toMap(IoTProductVO::getProductKey, IoTProductVO::getDevNum));
      for (IoTProductVO devProduct : devProductVOS) {
        devProduct.setDevNum(collect.getOrDefault(devProduct.getProductKey(), 0));
        if (JSONUtil.isTypeJSON(devProduct.getPhotoUrl())) {
          JSONObject photo = JSONUtil.parseObj(devProduct.getPhotoUrl());
          devProduct.setImage(photo.getStr("img", ""));
        }
      }
    }
    // 按设备数量倒序排序
    //    if (CollUtil.isNotEmpty(devProductVOS)) {
    //      devProductVOS.sort(Comparator.comparingInt(IoTProductVO::getDevNum).reversed());
    //    }
    return page;
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
        "selectAllEnableNetworkProductKey"
      },
      allEntries = true)
  public int insertDevProduct(IoTProduct ioTProduct) {
    Date date = new Date();
    Long time = date.getTime();
    ioTProduct.setCreateTime(time);
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

  @Override
  @CacheEvict(
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list"},
      allEntries = true)
  public int insertList(List<IoTProduct> ioTProductList) {
    int i = ioTProductMapper.insertList(ioTProductList);
    return i;
  }

  /** 产品协议导入 */
  @Override
  @CacheEvict(
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list", "supportMQTTNetwork"},
      allEntries = true)
  public String importProduct(List<IoTProductImportBO> productImportBos, String unionId) {
    Long timeMillis = System.currentTimeMillis();
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
          ioTProduct.setCreateTime(timeMillis);
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
                  ProtocolModule.ctaiot.name(), "downPro", devProduct.getThirdDownRequest());
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
            "导入普通产品总数：%s 个,成功：%s个；导入电信产品总数：%s 个，成功：%s 个；导入协议总数：%s 个，成功：%s 个。",
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
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list", "supportMQTTNetwork"},
      allEntries = true)
  public int updateDevProduct(IoTProduct ioTProduct) {
    IoTUser iotUser = queryIotUser(SecurityUtils.getUnionId());
    if (iotUser == null) {
      throw new IoTException("获取用户信息异常", HttpStatus.HTTP_UNAUTHORIZED);
    }
    String appUnionId = iotUser.getUnionId();
    IoTProduct pro = ioTProductMapper.selectByPrimaryKey(ioTProduct.getId());
    if (!appUnionId.equals(pro.getCreatorId()) && !iotUser.isAdmin()) {
      throw new IoTException("您没有权限操作此产品！");
    }
    if (Objects.nonNull(ioTProduct.getClassifiedId())) {
      IoTProductSort ioTProductSort =
          ioTProductSortMapper.selectDevProductSortById(ioTProduct.getClassifiedId());
      ioTProduct.setClassifiedName(ioTProductSort.getClassifiedName());
    }

    int count = ioTProductMapper.updateDevProduct(ioTProduct);
    log.info("修改产品记录数={},修改人={}", count, appUnionId);
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
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list", "supportMQTTNetwork"},
      allEntries = true)
  public int deleteDevProductByIds(String[] ids) {
    String appUnionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    int count = 0;
    for (String id : ids) {
      IoTProduct ioTProduct = ioTProductMapper.selectDevProductById(id);
      if (!appUnionId.equals(ioTProduct.getCreatorId())) {
        throw new IoTException("您没有权限操作此产品！");
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
      cacheNames = {"iot_all_dev_product_list", "iot_dev_product_list", "supportMQTTNetwork"},
      allEntries = true)
  public int deleteDevProductById(String id) {
    String appUnionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    IoTProduct ioTProduct = selectDevProductById(id);
    if (ioTProduct == null) {
      throw new IoTException("产品不存在");
    }
    if (!appUnionId.equals(ioTProduct.getCreatorId())) {
      throw new IoTException("您没有权限操作此产品！");
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
        "iot_dev_product_list"
      },
      allEntries = true)
  public int updateDevProductConfig(IoTProductVO devProduct) {
    String appUnionId = queryIotUser(SecurityUtils.getUnionId()).getUnionId();
    // 根据id获取产品信息
    IoTProduct product = ioTProductMapper.selectDevProductById(String.valueOf(devProduct.getId()));
    if (devProduct == null) {
      throw new IoTException("产品不存在");
    }
    if (!appUnionId.equals(product.getCreatorId())) {
      throw new IoTException("您没有权限操作此产品！");
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
      Network network =
          networkMapper.selectOne(Network.builder().productKey(product.getProductKey()).build());
      int updateCount;
      try {
        // 获取配置
        JSONObject tcpConfig = new JSONObject();
        JSONObject parserConfiguration = new JSONObject();
        parserConfiguration.set("cert", newConfig.getStr("cert"));
        parserConfiguration.set("key", newConfig.getStr("key"));
        parserConfiguration.set("rootCa", newConfig.getStr("rootCa"));
        parserConfiguration.set("delimited", newConfig.getStr("delimited"));
        parserConfiguration.set("lineMaxLength", newConfig.getInt("lineMaxLength"));
        parserConfiguration.set("delimitedMaxlength", newConfig.getInt("delimitedMaxlength"));
        parserConfiguration.set("fixedLength", newConfig.getInt("fixedLength"));
        parserConfiguration.set("byteOrderLittle", newConfig.getBool("byteOrderLittle", false));
        parserConfiguration.set("maxFrameLength", newConfig.getInt("maxFrameLength"));
        parserConfiguration.set("lengthFieldOffset", newConfig.getInt("lengthFieldOffset"));
        parserConfiguration.set("lengthFieldLength", newConfig.getInt("lengthFieldLength"));
        parserConfiguration.set("lengthAdjustment", newConfig.getInt("lengthAdjustment"));
        parserConfiguration.set("initialBytesToStrip", newConfig.getInt("initialBytesToStrip"));
        parserConfiguration.set("failFast", newConfig.getBool("failFast", false));
        tcpConfig.set("parserConfiguration", parserConfiguration);
        tcpConfig.set("ssl", newConfig.getBool("ssl", false));
        tcpConfig.set("port", port);
        tcpConfig.set("onlyCache", newConfig.getBool("onlyCache", false));
        tcpConfig.set("productKey", product.getProductKey());
        tcpConfig.set("host", newConfig.getStr("host"));
        tcpConfig.set(IoTConstant.ALLOW_INSERT, newConfig.getBool(IoTConstant.ALLOW_INSERT, false));
        tcpConfig.set("preStore", newConfig.getBool("preStore", false));
        tcpConfig.set("alwaysPreDecode", newConfig.getBool("alwaysPreDecode", false));
        tcpConfig.set("decoderType", newConfig.getStr("decoderType"));
        tcpConfig.set("parserType", newConfig.getStr("parserType"));
        tcpConfig.set("allIdleTime", newConfig.getInt("allIdleTime"));
        tcpConfig.set("readerIdleTime", newConfig.getInt("readerIdleTime"));
        tcpConfig.set("writerIdleTime", newConfig.getInt("writerIdleTime"));
        tcpConfig.set("readTimeout", newConfig.getInt("readTimeout"));
        tcpConfig.set("sendTimeout", newConfig.getInt("sendTimeout"));
        tcpConfig.set("idleInterval", newConfig.getInt("idleInterval"));
        //        if (Objects.isNull(network)) {
        //          // 新增
        //          updateCount = networkMapper.insert(
        //              Network.builder().type("TCP_SERVER").productKey(product.getProductKey())
        //                  .description("tcp_server").createDate(new Date()).createUser(appUnionId)
        //
        // .state(newConfig.getBool("enabled")).configuration(tcpConfig.toString()).build());
        //        } else {
        //          // 更新
        //          network.setConfiguration(tcpConfig.toString());
        //          network.setState(newConfig.getBool("enabled"));
        //          updateCount = networkMapper.updateByPrimaryKey(network);
        //        }
        // 更改配置后重启tcp监听
        //        if (updateCount > 0) {
        // 暂时不重启
        //          flushNettyServer(JSONUtil.toJsonStr(tcpConfig), product.getProductKey(),
        //              TcpFlushType.reload);
        //        }
      } catch (Exception e) {
        log.error("tcp网络组件配置失败", e);
        throw new IoTException("添加失败");
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
          appUnionId);
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
    ioTProductBO.beanToJson();
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
      throw new IoTException("您没有权限操作此产品！");
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
      throw new IoTException("您没有权限操作此产品！");
    }
    ioTProductBO.beanToJson();
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
  public String selectMetadataByDevId(String devId) {
    return ioTProductMapper.selectMetadataByDevId(devId);
  }

  @Override
  public Map<String, Integer> countDevNumberByProductKey(String unionId) {
    List<IoTProductVO> results = ioTProductMapper.countDevNumberByProductKey(unionId);
    return results.stream()
        .collect(Collectors.toMap(IoTProductVO::getProductKey, IoTProductVO::getDevNum));
  }

  @Override
  public AjaxResult<IoTProduct> selectDevProductByKey(String key) {
    IoTProduct ioTProduct = new IoTProduct();
    ioTProduct.setProductKey(key);
    return AjaxResult.success(ioTProductMapper.selectOne(ioTProduct));
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
      throw new IoTException("您没有权限操作此产品！");
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
            .map(
                devProductVO -> {
                  IoTProductVO vo = new IoTProductVO();
                  vo.setName(devProductVO.getName());
                  vo.setProductKey(devProductVO.getProductKey());
                  return vo;
                })
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
}

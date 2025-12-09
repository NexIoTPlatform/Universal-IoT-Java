/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频平台产品自动创建服务实现
 * @Author: gitee.com/NexIoT
 *
 */
package cn.universal.admin.video.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.universal.admin.video.service.VideoPlatformProductService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.exception.BaseException;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.VideoPlatformInstanceMapper;
import cn.universal.security.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

/** 视频平台产品创建策略 WVP：自动创建GB/级联两个产品（autoCreateProducts=1） HIK/ICC：懒创建，首次勾选使用设备时创建平台产品 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoPlatformProductServiceImpl implements VideoPlatformProductService {
  private String metadata =
"""
{"tags":[],"events":[{"id":"online","name":"设备上线","valueType":{"type":"string"}},{"id":"offline","name":"设备下线","valueType":{"type":"string"}}],"functions":[{"id":"cameraLiveStream","name":"实时预览","config":false,"inputs":[{"id":"deviceId","name":"设备ID","valueType":{"type":"string"}},{"id":"channelId","name":"通道ID","valueType":{"type":"string"}},{"id":"streamType","name":"码流类型","valueType":{"type":"enum","elements":[{"text":"主码流","value":"main"},{"text":"子码流","value":"sub"}]}}],"output":{}},{"id":"cameraTurn","name":"云台控制","config":false,"inputs":[{"id":"deviceId","name":"设备ID","valueType":{"type":"string"}},{"id":"channelId","name":"通道ID","valueType":{"type":"string"}},{"id":"command","name":"控制命令","valueType":{"type":"string"}},{"id":"speed","name":"速度","valueType":{"type":"int"}}],"output":{}},{"id":"cameraPlayback","name":"录像回放","config":false,"inputs":[{"id":"deviceId","name":"设备ID","valueType":{"type":"string"}},{"id":"channelId","name":"通道ID","valueType":{"type":"string"}},{"id":"startTime","name":"开始时间","valueType":{"type":"long"}},{"id":"endTime","name":"结束时间","valueType":{"type":"long"}}],"output":{}},{"id":"cameraSnapshot","name":"抓拍","config":false,"inputs":[{"id":"deviceId","name":"设备ID","valueType":{"type":"string"}},{"id":"channelId","name":"通道ID","valueType":{"type":"string"}}],"output":{}},{"id":"queryRecords","name":"查询录像","config":false,"inputs":[{"id":"deviceId","name":"设备ID","valueType":{"type":"string"}},{"id":"channelId","name":"通道ID","valueType":{"type":"string"}},{"id":"startTime","name":"开始时间","valueType":{"type":"string"}},{"id":"endTime","name":"结束时间","valueType":{"type":"string"}}],"output":{}},{"id":"queryCloudRecords","name":"查询云端录像","config":false,"inputs":[{"id":"app","name":"应用名","valueType":{"type":"string"}},{"id":"stream","name":"流ID","valueType":{"type":"string"}},{"id":"page","name":"页码","valueType":{"type":"int"}},{"id":"count","name":"每页数量","valueType":{"type":"int"}}],"output":{}},{"id":"loadCloudRecord","name":"加载云端录像","config":false,"inputs":[{"id":"app","name":"应用名","valueType":{"type":"string"}},{"id":"stream","name":"流ID","valueType":{"type":"string"}},{"id":"cloudRecordId","name":"录像ID","valueType":{"type":"int"}}],"output":{}}],"properties":[{"id":"deviceStatus","mode":"r","name":"设备状态","config":false,"source":"device","valueType":{"type":"enum","elements":[{"text":"在线","value":"online"},{"text":"离线","value":"offline"}]}},{"id":"manufacturer","mode":"r","name":"厂商","config":false,"source":"device","valueType":{"type":"string"}},{"id":"model","mode":"r","name":"型号","config":false,"source":"device","valueType":{"type":"string"}}]}""";
  private final IoTProductMapper productMapper;
  private final VideoPlatformInstanceMapper instanceMapper;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void autoCreateWvpProducts(VideoPlatformInstance instance) {
    if (!"wvp".equals(instance.getPlatformType())) {
      log.warn("仅WVP平台支持自动创建产品，当前平台类型：{}", instance.getPlatformType());
      return;
    }
    if (instance.getAutoCreateProducts() != 1) {
      log.info("WVP平台实例未开启自动创建产品，跳过");
      return;
    }

    // 检查是否已创建GB产品
    String gbProductKey = instance.getInstanceKey() + "_GB";
    IoTProduct gbProduct = productMapper.getProductByProductKey(gbProductKey);
    if (gbProduct == null) {
      createPlatformProduct(instance, "VIDEO_DEVICE", gbProductKey, instance.getName() + "-GB设备");
      log.info("WVP平台自动创建GB产品：{}", gbProductKey);
    }

    // 检查是否已创建级联产品
//    String cascadeProductKey = instance.getInstanceKey() + "_CASCADE";
//    IoTProduct cascadeProduct = productMapper.getProductByProductKey(cascadeProductKey);
//    if (cascadeProduct == null) {
//      createPlatformProduct(
//          instance, "VIDEO_GATEWAY", cascadeProductKey, instance.getName() + "-级联平台");
//      log.info("WVP平台自动创建级联产品：{}", cascadeProductKey);
//    }
  }

  @Override
  @Transactional
  public IoTProduct ensurePlatformProduct(VideoPlatformInstance instance, String deviceNode) {
    String productKey = buildPlatformProductKey(instance, deviceNode);
    IoTProduct product = productMapper.getProductByProductKey(productKey);
    if (product == null) {
      product = createPlatformProduct(instance, deviceNode, productKey, instance.getName());
      log.info("懒创建平台产品：{} [{}]", productKey, deviceNode);
    }
    return product;
  }

  @Override
  public IoTProduct getOrCreatePlatformProduct(String instanceKey, String deviceNode) {
    Example example = new Example(VideoPlatformInstance.class);
    example.createCriteria().andEqualTo("instanceKey", instanceKey);
    VideoPlatformInstance instance =
        instanceMapper.selectByExample(example).stream().findFirst().orElse(null);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    return ensurePlatformProduct(instance, deviceNode);
  }

  /** 创建产品*/
  private IoTProduct createPlatformProduct(
      VideoPlatformInstance instance, String deviceNode, String productKey, String productName) {
    try {
      IoTProduct product = new IoTProduct();
      product.setProductKey(productKey);
      product.setProductId(productKey); // 与productKey一致
      product.setName(productName);
      product.setThirdPlatform(instance.getPlatformType());
      product.setDeviceNode(deviceNode);
      product.setState((IoTConstant.NORMAL.byteValue())); // 启用
      product.setCreatorId(SecurityUtils.getUnionId());
      product.setCreateTime(new Date());
      product.setUpdateTime(new Date());
      product.setStorePolicy("mysql");
      product.setTransportProtocol("以太网");
      product.setIsDeleted(0);
      // 将平台实例配置写入thirdConfiguration
      Map<String, Object> thirdConfig = new HashMap<>();
      thirdConfig.put("instanceKey", instance.getInstanceKey());
      thirdConfig.put("endpoint", instance.getEndpoint());
      thirdConfig.put("auth", instance.getAuth());
      thirdConfig.put("version", instance.getVersion());
      thirdConfig.put("options", instance.getOptions());
      thirdConfig.put("customField", new ArrayList<String>());
      product.setThirdConfiguration(objectMapper.writeValueAsString(thirdConfig));

      // 默认物模型（仅包含online/offline事件）
      product.setMetadata(metadata);

      productMapper.insertSelective(product);
      return product;
    } catch (Exception e) {
      log.error("创建平台产品失败：{}", e.getMessage(), e);
      throw new BaseException("创建平台产品失败：" + e.getMessage());
    }
  }

  /** 根据平台类型与设备节点构建产品Key */
  private String buildPlatformProductKey(VideoPlatformInstance instance, String deviceNode) {
    //    // WVP有特殊规则（已在autoCreateWvpProducts中处理），这里是通用规则
    //    if ("wvp".equals(instance.getPlatformType())) {
    //      if ("VIDEO_DEVICE".equals(deviceNode)) {
    //        return instance.getInstanceKey() + "_GB";
    //      } else if ("VIDEO_GATEWAY".equals(deviceNode)) {
    //        return instance.getInstanceKey() + "_CASCADE";
    //      }
    //    }
    // HIK/ICC/其他平台：instanceKey_deviceNode
    return instance.getInstanceKey();
  }
}

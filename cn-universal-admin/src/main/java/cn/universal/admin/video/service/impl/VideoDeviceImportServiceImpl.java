/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频设备导入服务实现
 * @Author: gitee.com/NexIoT
 *
 */
package cn.universal.admin.video.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.universal.admin.video.service.VideoDeviceImportService;
import cn.universal.admin.video.service.VideoPlatformProductService;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.exception.BaseException;
import cn.universal.common.utils.StringUtils;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.VideoPlatformInstanceMapper;
import cn.universal.security.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

/**
 * 视频设备导入服务
 * 负责将三方平台设备落库到IoTDevice
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoDeviceImportServiceImpl implements VideoDeviceImportService {

  private final VideoPlatformInstanceMapper instanceMapper;
  private final VideoPlatformProductService productService;
  private final IoTDeviceMapper deviceMapper;
  private final IoTProductMapper productMapper;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public Map<String, Integer> importDevices(
      String instanceKey, List<Map<String, Object>> devices, String productKey) {
    Map<String, Integer> result = new HashMap<>();
    int success = 0, failed = 0, exists = 0;

    for (Map<String, Object> device : devices) {
      try {
        String deviceId = importDevice(instanceKey, device, productKey);
        if (deviceId != null) {
          if (deviceId.startsWith("EXISTS:")) {
            exists++;
          } else {
            success++;
          }
        } else {
          failed++;
        }
      } catch (Exception e) {
        log.error("导入设备失败：{}", e.getMessage(), e);
        failed++;
      }
    }

    result.put("success", success);
    result.put("failed", failed);
    result.put("exists", exists);
    return result;
  }

  @Override
  @Transactional
  public String importDevice(String instanceKey, Map<String, Object> device, String productKey) {
    // 1. 获取平台实例
    Example example = new Example(VideoPlatformInstance.class);
    example.createCriteria().andEqualTo("instanceKey", instanceKey);
    VideoPlatformInstance instance =
        instanceMapper.selectByExample(example).stream().findFirst().orElse(null);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }

    // 2. 获取或创建平台产品
    String deviceId = (String) device.get("deviceId");
    if (StringUtils.isEmpty(deviceId)) {
      throw new BaseException("设备DeviceId不能为空");
    }

    // 判断设备节点类型（优先从设备数据获取，否则默认VIDEO_DEVICE）
    String deviceNode = (String) device.getOrDefault("deviceNode", "VIDEO_DEVICE");

    IoTProduct product;
    if (StringUtils.isEmpty(productKey)) {
      // 自动创建/选择平台产品
      product = productService.ensurePlatformProduct(instance, deviceNode);
    } else {
      // 使用指定产品
      product = productMapper.getProductByProductKey(productKey);
      if (product == null) {
        throw new BaseException("指定产品不存在：" + productKey);
      }
    }
    String deviceName = (String) device.getOrDefault("deviceName", "");
    String deviceIp = (String) device.getOrDefault("deviceIp", "");

    // 3. 检查设备是否已存在（幂等性校验：productKey + deviceId）
    Example deviceExample = new Example(IoTDevice.class);
    deviceExample
        .createCriteria()
        .andEqualTo("productKey", product.getProductKey())
        .andEqualTo("deviceId", deviceId);
    IoTDevice existingDevice = deviceMapper.selectByExample(deviceExample).stream().findFirst().orElse(null);
    if (existingDevice != null) {
      log.info("设备已存在，跳过导入：{}/{}", product.getProductKey(), deviceId);
      return "EXISTS:" + existingDevice.getIotId();
    }

    // 4. 创建IoTDevice
    IoTDevice iotDevice = new IoTDevice();
    iotDevice.setIotId(product.getProductKey()+deviceId);
    iotDevice.setProductKey(product.getProductKey());
    iotDevice.setDeviceId(deviceId);
    iotDevice.setDeviceName(StrUtil.isBlank(deviceName)?deviceIp:deviceName);
    iotDevice.setProductName(product.getName());
    iotDevice.setState(parseDeviceStatus(device));
    iotDevice.setCreatorId(SecurityUtils.getUnionId());
    iotDevice.setCreateTime(System.currentTimeMillis() / 1000);

    // 填充网关产品Key（平台实例作为网关）
    iotDevice.setGwProductKey(product.getProductKey());

    // 填充配置字段（包含通道列表等）
    iotDevice.setConfiguration(buildConfiguration(device));

    // 填充派生元数据（媒体能力、通道信息等）
    iotDevice.setDeriveMetadata(buildDeriveMetadata(device));

    // 填充事件元数据到ext1（遵循规范）
    iotDevice.setExt1(buildEventMetadata(device));

    deviceMapper.insertSelective(iotDevice);
    log.info(
        "设备导入成功：{}/{} -> {}", product.getProductKey(), deviceId, iotDevice.getIotId());
    return iotDevice.getIotId();
  }
  /**
   * 解析设备状态
   */
  private Boolean parseDeviceStatus(Map<String, Object> device) {
    Object status = device.get("status");
    if (status == null) return false;
    if (status instanceof Boolean) return (Boolean) status;
    String statusStr = status.toString().toLowerCase();
    return "online".equals(statusStr) || "1".equals(statusStr) || "true".equals(statusStr);
  }

  /**
   * 构建配置字段（保存轻量配置，如通道列表）
   */
  private String buildConfiguration(Map<String, Object> device) {
    try {
      Map<String, Object> config = new HashMap<>();
      // 保存厂商、型号等基础信息
      if (device.containsKey("manufacturer")) {
        config.put("manufacturer", device.get("manufacturer"));
      }
      if (device.containsKey("model")) {
        config.put("model", device.get("model"));
      }
      // 保存通道列表（如果有）
      if (device.containsKey("channels")) {
        config.put("channelList", device.get("channels"));
      }
      if (device.containsKey("channelCount")) {
        config.put("channelCount", device.get("channelCount"));
      }
      return objectMapper.writeValueAsString(config);
    } catch (Exception e) {
      log.warn("构建配置失败：{}", e.getMessage());
      return "{}";
    }
  }

  /**
   * 构建派生元数据（媒体能力、编解码等）
   */
  private String buildDeriveMetadata(Map<String, Object> device) {
    try {
      Map<String, Object> metadata = new HashMap<>();
      // 媒体能力（预览URL、回放能力等）
      if (device.containsKey("capabilities")) {
        metadata.put("capabilities", device.get("capabilities"));
      }
      // PTZ能力
      if (device.containsKey("ptzSupport")) {
        metadata.put("ptzSupport", device.get("ptzSupport"));
      }
      // 编解码信息
      if (device.containsKey("videoCodec")) {
        metadata.put("videoCodec", device.get("videoCodec"));
      }
      if (device.containsKey("audioCodec")) {
        metadata.put("audioCodec", device.get("audioCodec"));
      }
      return objectMapper.writeValueAsString(metadata);
    } catch (Exception e) {
      log.warn("构建派生元数据失败：{}", e.getMessage());
      return "{}";
    }
  }

  /**
   * 构建事件元数据（仅用ext1，遵循规范）
   */
  private String buildEventMetadata(Map<String, Object> device) {
    try {
      Map<String, Object> eventMeta = new HashMap<>();
      // 保存原始三方设备ID
      eventMeta.put("thirdPartyDeviceId", device.get("extDeviceId"));
      // 设备类型（IPC/NVR等）
      if (device.containsKey("deviceType")) {
        eventMeta.put("deviceType", device.get("deviceType"));
      }
      return objectMapper.writeValueAsString(eventMeta);
    } catch (Exception e) {
      log.warn("构建事件元数据失败：{}", e.getMessage());
      return "{}";
    }
  }
}

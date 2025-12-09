/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 视频平台实例服务实现
 * @Author: gitee.com/NexIoT
 *
 */
package cn.universal.admin.video.service.impl;

import cn.universal.admin.video.service.VideoPlatformInstanceService;
import cn.universal.common.exception.BaseException;
import cn.universal.dm.video.VideoPlatformAdapter;
import cn.universal.dm.video.VideoPlatformAdapterRegistry;
import cn.universal.persistence.entity.VideoPlatformChannel;
import cn.universal.persistence.entity.VideoPlatformDevice;
import cn.universal.persistence.entity.VideoPlatformDeviceExt;
import cn.universal.persistence.entity.VideoPlatformInstance;
import cn.universal.persistence.mapper.VideoPlatformChannelMapper;
import cn.universal.persistence.mapper.VideoPlatformDeviceExtMapper;
import cn.universal.persistence.mapper.VideoPlatformDeviceMapper;
import cn.universal.persistence.mapper.VideoPlatformInstanceMapper;
import cn.universal.security.utils.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

@Service
@RequiredArgsConstructor
public class VideoPlatformInstanceServiceImpl implements VideoPlatformInstanceService {

  private final VideoPlatformInstanceMapper instanceMapper;
  private final VideoPlatformAdapterRegistry adapterRegistry;
  private final ObjectMapper objectMapper;
  private final VideoPlatformDeviceMapper deviceMapper;
  private final VideoPlatformDeviceExtMapper deviceExtMapper;
  private final VideoPlatformChannelMapper channelMapper;
  private final cn.universal.persistence.mapper.VideoPlatformOrgCacheMapper orgCacheMapper;
  private final cn.universal.persistence.mapper.IoTDeviceMapper ioTDeviceMapper;
  private final cn.universal.persistence.mapper.IoTProductMapper ioTProductMapper;

  @Override
  public List<VideoPlatformInstance> listAll() {
    Example example = new Example(VideoPlatformInstance.class);
    example.orderBy("createTime").desc();
    return instanceMapper.selectByExample(example);
  }

  @Override
  public List<VideoPlatformInstance> listByType(String platformType) {
    Example example = new Example(VideoPlatformInstance.class);
    example.createCriteria().andEqualTo("platformType", platformType);
    example.orderBy("createTime").desc();
    return instanceMapper.selectByExample(example);
  }

  @Override
  public VideoPlatformInstance getByInstanceKey(String instanceKey) {
    Example example = new Example(VideoPlatformInstance.class);
    example.createCriteria().andEqualTo("instanceKey", instanceKey);
    List<VideoPlatformInstance> list = instanceMapper.selectByExample(example);
    return list.isEmpty() ? null : list.get(0);
  }

  @Override
  public VideoPlatformInstance getById(Long id) {
    return instanceMapper.selectByPrimaryKey(id);
  }

  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"getVideoPlatformInstance"},
      allEntries = true)
  public VideoPlatformInstance create(VideoPlatformInstance instance) {
    // 若未提供instanceKey则按规则自动生成：标识 + yyyyMMdd + 随机后缀
    if (instance.getInstanceKey() == null || instance.getInstanceKey().trim().isEmpty()) {
      instance.setInstanceKey(generateInstanceKey(instance.getPlatformType()));
    }
    // 校验instanceKey唯一性
    VideoPlatformInstance existing = getByInstanceKey(instance.getInstanceKey());
    if (existing != null) {
      throw new BaseException("实例标识已存在：" + instance.getInstanceKey());
    }
    Date now = new Date();
    instance.setCreateTime(now);
    instance.setUpdateTime(now);
    if (instance.getEnabled() == null) {
      instance.setEnabled(1);
    }
    instanceMapper.insertSelective(instance);
    return instance;
  }

  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"getVideoPlatformInstance"},
      allEntries = true)
  public VideoPlatformInstance update(VideoPlatformInstance instance) {
    if (instance.getId() == null) {
      throw new BaseException("更新时ID不能为空");
    }
    instance.setUpdateTime(new Date());
    instanceMapper.updateByPrimaryKeySelective(instance);
    return instanceMapper.selectByPrimaryKey(instance.getId());
  }

  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"getVideoPlatformInstance"},
      allEntries = true)
  public void delete(Long id) {
    delete(id, false);
  }

  @Override
  @Transactional
  @CacheEvict(
      cacheNames = {"getVideoPlatformInstance"},
      allEntries = true)
  public void delete(Long id, boolean force) {
    VideoPlatformInstance inst = instanceMapper.selectByPrimaryKey(id);
    if (inst == null) {
      throw new BaseException("平台实例不存在");
    }
    String instanceKey = inst.getInstanceKey();
    int devCount = ioTDeviceMapper.countByProductKey(instanceKey);
    if (devCount > 0 && !force) {
      throw new BaseException("存在关联设备，请先删除或选择强制删除");
    }
    if (devCount > 0 && force) {
      List<cn.universal.persistence.entity.IoTDevice> devList = ioTDeviceMapper.selectListByProductKey(instanceKey);
      for (cn.universal.persistence.entity.IoTDevice d : devList) {
        ioTDeviceMapper.deleteDevInstanceById(String.valueOf(d.getId()));
      }
    }
    cn.universal.persistence.entity.IoTProduct product = ioTProductMapper.getProductByProductKey(instanceKey);
    if (product != null && product.getId() != null) {
      ioTProductMapper.deleteDevProductById(String.valueOf(product.getId()));
    }
    Example devEx = new Example(VideoPlatformDevice.class);
    devEx.createCriteria().andEqualTo("instanceKey", instanceKey);
    List<VideoPlatformDevice> vpDevices = deviceMapper.selectByExample(devEx);
    for (VideoPlatformDevice d : vpDevices) {
      Long pk = d.getId();
      if (pk != null) {
        Example chEx = new Example(VideoPlatformChannel.class);
        chEx.createCriteria().andEqualTo("deviceId", pk);
        channelMapper.deleteByExample(chEx);
        Example extEx = new Example(VideoPlatformDeviceExt.class);
        extEx.createCriteria().andEqualTo("deviceId", pk);
        deviceExtMapper.deleteByExample(extEx);
      }
      deviceMapper.deleteByPrimaryKey(d.getId());
    }
    instanceMapper.deleteByPrimaryKey(id);
  }

  @Override
  public Map<String, Object> testConnection(String instanceKey) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    return adapter.testConnection(instance);
  }

  @Override
  public List<Map<String, Object>> listDevices(
      String instanceKey, Map<String, Object> filters) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    // 是否强制同步
    boolean forceSync = false;
    if (filters != null) {
      Object v = filters.get("forceSync");
      forceSync = v != null && String.valueOf(v).equalsIgnoreCase("true");
    }
    if (forceSync) {
      // 远端拉取并写入主表+扩展表
      Map<String, Object> passFilters = new java.util.HashMap<>();
      if (filters != null) {
        // 保留forceSync参数,让Adapter知道这是强制同步,需要获取通道数据
        filters.forEach((k, v) -> passFilters.put(k, v));
      }
      VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
      List<Map<String, Object>> remote = adapter.listDevices(instance, passFilters);
      String currentUserId = SecurityUtils.getUnionId();
      Date now = new Date();
      for (Map<String, Object> dev : remote) {
        String deviceId = String.valueOf(dev.getOrDefault("deviceId", dev.get("id")));
        if (deviceId == null || deviceId.equals("null")) continue;
        
        // 主表公共字段
        String deviceName = (String) dev.getOrDefault("deviceName", dev.get("name"));
        String status = (String) dev.getOrDefault("deviceStatus", dev.get("status"));
        String model = (String) dev.getOrDefault("deviceModel", dev.get("model"));
        String manufacturer = (String) dev.getOrDefault("manufacturer", "");
        String orgId = (String) dev.getOrDefault("orgId", dev.get("organizationId"));
        String orgName = (String) dev.getOrDefault("orgName", "");
        String deviceIp = (String) dev.getOrDefault("ip", dev.get("deviceIp"));
        Integer devicePort = null;
        try {
          Object port = dev.getOrDefault("port", dev.get("devicePort"));
          if (port != null) devicePort = Integer.parseInt(String.valueOf(port));
        } catch (Exception ignore) {}
        
        // 构建configuration JSON(包含channelList等)
        String configuration = null;
        try {
          java.util.Map<String, Object> cfg = new java.util.HashMap<>();
          Object channelList = dev.get("channelList");
          if (channelList != null) cfg.put("channelList", channelList);
          if (!cfg.isEmpty()) configuration = objectMapper.writeValueAsString(cfg);
        } catch (Exception ignore) {}
        
        // upsert主表
        VideoPlatformDevice deviceEntity = null;
        boolean isNewDevice = false;
        {
          Example ex = new Example(VideoPlatformDevice.class);
          ex.createCriteria().andEqualTo("instanceKey", instanceKey).andEqualTo("deviceId", deviceId);
          List<VideoPlatformDevice> existList = deviceMapper.selectByExample(ex);
          deviceEntity = existList.isEmpty() ? null : existList.get(0);
        }
        if (deviceEntity == null) {
          isNewDevice = true;
          deviceEntity = VideoPlatformDevice.builder()
              .instanceKey(instanceKey)
              .deviceId(deviceId)
              .deviceName(deviceName)
              .deviceStatus(status)
              .deviceModel(model)
              .manufacturer(manufacturer)
              .deviceIp(deviceIp)
              .devicePort(devicePort)
              .orgId(orgId)
              .orgName(orgName)
              .configuration(configuration)
              .enabled(1)
              .createId(currentUserId)
              .updateId(currentUserId)
              .createTime(now)
              .updateTime(now)
              .build();
          deviceMapper.insertSelective(deviceEntity);
          // 重新查询获取自动生成的ID
          Example reloadEx = new Example(VideoPlatformDevice.class);
          reloadEx.createCriteria().andEqualTo("instanceKey", instanceKey).andEqualTo("deviceId", deviceId);
          List<VideoPlatformDevice> reloadList = deviceMapper.selectByExample(reloadEx);
          if (!reloadList.isEmpty()) {
            deviceEntity = reloadList.get(0);
          }
        } else {
          deviceEntity.setDeviceName(deviceName);
          deviceEntity.setDeviceStatus(status);
          deviceEntity.setDeviceModel(model);
          deviceEntity.setManufacturer(manufacturer);
          deviceEntity.setDeviceIp(deviceIp);
          deviceEntity.setDevicePort(devicePort);
          deviceEntity.setOrgId(orgId);
          deviceEntity.setOrgName(orgName);
          deviceEntity.setConfiguration(configuration);
          deviceEntity.setUpdateId(currentUserId);
          deviceEntity.setUpdateTime(now);
          deviceMapper.updateByPrimaryKeySelective(deviceEntity);
        }
        
        // upsert扩展表(平台特有字段)
        Long mainDeviceId = deviceEntity.getId();
        VideoPlatformDeviceExt extEntity = null;
        {
          Example ex = new Example(VideoPlatformDeviceExt.class);
          ex.createCriteria().andEqualTo("deviceId", mainDeviceId);
          List<VideoPlatformDeviceExt> existList = deviceExtMapper.selectByExample(ex);
          extEntity = existList.isEmpty() ? null : existList.get(0);
        }
        
        // 根据平台类型提取特有字段
        VideoPlatformDeviceExt.VideoPlatformDeviceExtBuilder extBuilder = extEntity == null 
            ? VideoPlatformDeviceExt.builder().deviceId(mainDeviceId).instanceKey(instanceKey).platformDeviceId(deviceId)
            : VideoPlatformDeviceExt.builder().id(extEntity.getId()).deviceId(mainDeviceId).instanceKey(instanceKey).platformDeviceId(deviceId);
        
        String platformType = instance.getPlatformType();
        if ("wvp".equalsIgnoreCase(platformType)) {
          // WVP特有字段
          extBuilder.charset((String) dev.get("charset"));
          extBuilder.transport((String) dev.get("transport"));
          extBuilder.streamMode((String) dev.get("streamMode"));
          extBuilder.hostAddress((String) dev.get("hostAddress"));
          extBuilder.mediaServerId((String) dev.get("mediaServerId"));
          try {
            Object expires = dev.get("expires");
            if (expires != null) extBuilder.expires(Integer.parseInt(String.valueOf(expires)));
          } catch (Exception ignore) {}
        } else if ("ics".equalsIgnoreCase(platformType)) {
          // 海康ISC设备级特有字段
          extBuilder.encodeDevIndexCode((String) dev.get("encodeDevIndexCode"));
          extBuilder.deviceCapabilitySet((String) dev.get("deviceCapabilitySet"));
        } else if ("icc".equalsIgnoreCase(platformType)) {
          // 大华ICC特有字段
          extBuilder.deviceSn((String) dev.get("deviceSn"));
          extBuilder.deviceType((String) dev.get("deviceType"));
          extBuilder.ownerCode((String) dev.get("ownerCode"));
          extBuilder.isOnline((String) dev.get("isOnline"));
          extBuilder.subSystem((String) dev.get("subSystem"));
          try {
            Object unitsInfo = dev.get("unitsInfo");
            if (unitsInfo != null) extBuilder.unitsInfo(objectMapper.writeValueAsString(unitsInfo));
          } catch (Exception ignore) {}
        }
        
        VideoPlatformDeviceExt finalExt = extBuilder
            .createId(extEntity == null ? currentUserId : extEntity.getCreateId())
            .updateId(currentUserId)
            .createTime(extEntity == null ? now : extEntity.getCreateTime())
            .updateTime(now).build();
        if (extEntity == null) {
          deviceExtMapper.insertSelective(finalExt);
        } else {
          deviceExtMapper.updateByPrimaryKeySelective(finalExt);
        }
        
        // upsert通道表(从设备数据中提取channelList)
        syncDeviceChannels(mainDeviceId, instanceKey, deviceId, dev, platformType, now);
      }
    }
    // 返回主表中的设备列表
    Example queryExample = new Example(VideoPlatformDevice.class);
    queryExample.createCriteria().andEqualTo("instanceKey", instanceKey);
    List<VideoPlatformDevice> list = deviceMapper.selectByExample(queryExample);
    List<Map<String, Object>> result = new java.util.ArrayList<>();
    for (VideoPlatformDevice d : list) {
      Map<String, Object> item = new java.util.HashMap<>();
      item.put("instanceKey", d.getInstanceKey());
      item.put("deviceId", d.getDeviceId());
      item.put("deviceName", d.getDeviceName());
      item.put("deviceStatus", d.getDeviceStatus());
      item.put("deviceModel", d.getDeviceModel());
      item.put("manufacturer", d.getManufacturer());
      item.put("deviceIp", d.getDeviceIp());
      item.put("devicePort", d.getDevicePort());
      item.put("orgId", d.getOrgId());
      item.put("orgName", d.getOrgName());
      // 从video_platform_channel表查询通道列表
      List<Map<String, Object>> channelList = new java.util.ArrayList<>();
      try {
        Example channelEx = new Example(VideoPlatformChannel.class);
        channelEx.createCriteria().andEqualTo("deviceId", d.getId());
        List<VideoPlatformChannel> channels = channelMapper.selectByExample(channelEx);
        for (VideoPlatformChannel ch : channels) {
          Map<String, Object> channelItem = new java.util.HashMap<>();
          channelItem.put("channelId", ch.getChannelId());
          channelItem.put("name", ch.getChannelName());
          channelItem.put("status", ch.getChannelStatus());
          channelItem.put("ptzType", ch.getPtzType());
          channelItem.put("manufacturer", ch.getManufacturer());
          channelItem.put("model", ch.getModel());
          // 添加平台特有字段
          if (ch.getStreamId() != null) channelItem.put("streamId", ch.getStreamId());
          if (ch.getCameraIndexCode() != null) channelItem.put("cameraIndexCode", ch.getCameraIndexCode());
          if (ch.getChannelCode() != null) channelItem.put("channelCode", ch.getChannelCode());
          channelList.add(channelItem);
        }
      } catch (Exception ignore) {}
      item.put("channelList", channelList);
      result.add(item);
    }
    // 服务器端筛选与分页
    java.util.List<Map<String, Object>> filtered = new java.util.ArrayList<>();
    String keyword = filters == null ? null : (String) filters.get("keyword");
    String statusFilter = filters == null ? null : (String) filters.get("status");
    String orgIdFilter = filters == null ? null : (String) filters.get("orgId");
    for (Map<String, Object> it : result) {
      boolean ok = true;
      if (keyword != null && !keyword.isEmpty()) {
        String name = String.valueOf(it.getOrDefault("deviceName", ""));
        String devId = String.valueOf(it.getOrDefault("deviceId", ""));
        ok &= (name.contains(keyword) || devId.contains(keyword));
      }
      if (statusFilter != null && !statusFilter.isEmpty()) {
        String st = String.valueOf(it.getOrDefault("deviceStatus", ""));
        ok &= st.equalsIgnoreCase(statusFilter);
      }
      if (orgIdFilter != null && !orgIdFilter.isEmpty()) {
        String oid = String.valueOf(it.getOrDefault("orgId", ""));
        ok &= oid.equals(orgIdFilter);
      }
      if (ok) filtered.add(it);
    }
    int page = 1;
    int pageSize = filtered.size();
    if (filters != null) {
      Object p = filters.get("page");
      Object ps = filters.get("pageSize");
      try { if (p != null) page = Integer.parseInt(String.valueOf(p)); } catch (Exception ignore) {}
      try { if (ps != null) pageSize = Integer.parseInt(String.valueOf(ps)); } catch (Exception ignore) {}
    }
    if (page < 1) page = 1;
    if (pageSize < 1) pageSize = 10;
    int start = (page - 1) * pageSize;
    if (start >= filtered.size()) return java.util.Collections.emptyList();
    int end = Math.min(start + pageSize, filtered.size());
    return filtered.subList(start, end);
  }

  @Override
  public List<Map<String, Object>> listOrganizations(
      String instanceKey, Map<String, Object> filters) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    boolean forceSync = false;
    if (filters != null) {
      Object v = filters.get("forceSync");
      forceSync = v != null && String.valueOf(v).equalsIgnoreCase("true");
    }
    if (forceSync) {
      Map<String, Object> passFilters = new java.util.HashMap<>();
      if (filters != null) {
        filters.forEach((k, v) -> { if (!"forceSync".equals(k)) passFilters.put(k, v); });
      }
      VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
      List<Map<String, Object>> remote = adapter.listOrganizations(instance, passFilters);
      String currentUserId = SecurityUtils.getUnionId();
      Date now = new Date();
      for (Map<String, Object> org : remote) {
        String orgId = (String) org.getOrDefault("orgId", org.get("id"));
        if (orgId == null) continue;
        String parentId = (String) org.getOrDefault("parentOrgId", org.get("parentId"));
        String orgName = (String) org.getOrDefault("orgName", org.get("name"));
        String path = (String) org.getOrDefault("path", org.get("path"));
        cn.universal.persistence.entity.VideoPlatformOrgCache one = null;
        {
          tk.mybatis.mapper.entity.Example ex = new tk.mybatis.mapper.entity.Example(cn.universal.persistence.entity.VideoPlatformOrgCache.class);
          ex.createCriteria().andEqualTo("instanceKey", instanceKey).andEqualTo("orgId", orgId);
          java.util.List<cn.universal.persistence.entity.VideoPlatformOrgCache> existList = orgCacheMapper.selectByExample(ex);
          one = existList.isEmpty() ? null : existList.get(0);
        }
        if (one == null) {
          one = cn.universal.persistence.entity.VideoPlatformOrgCache.builder()
              .instanceKey(instanceKey)
              .orgId(orgId)
              .parentOrgId(parentId)
              .orgName(orgName)
              .path(path)
              .createId(currentUserId)
              .updateId(currentUserId)
              .createTime(now)
              .updateTime(now)
              .build();
          orgCacheMapper.insertSelective(one);
        } else {
          one.setParentOrgId(parentId);
          one.setOrgName(orgName);
          one.setPath(path);
          one.setUpdateId(currentUserId);
          one.setUpdateTime(now);
          orgCacheMapper.updateByPrimaryKeySelective(one);
        }
      }
    }
    // 返回缓存组织树（平铺或自行构建树）
    java.util.List<cn.universal.persistence.entity.VideoPlatformOrgCache> list =
        orgCacheMapper.selectByExample(new tk.mybatis.mapper.entity.Example(cn.universal.persistence.entity.VideoPlatformOrgCache.class) {{ this.createCriteria().andEqualTo("instanceKey", instanceKey); }});
    java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
    for (cn.universal.persistence.entity.VideoPlatformOrgCache o : list) {
      java.util.Map<String, Object> item = new java.util.HashMap<>();
      item.put("instanceKey", o.getInstanceKey());
      item.put("orgId", o.getOrgId());
      item.put("parentOrgId", o.getParentOrgId());
      item.put("orgName", o.getOrgName());
      item.put("path", o.getPath());
      result.add(item);
    }
    // 服务器端筛选与分页
    java.util.List<Map<String, Object>> filtered = new java.util.ArrayList<>();
    String keyword = filters == null ? null : (String) filters.get("keyword");
    for (Map<String, Object> it : result) {
      boolean ok = true;
      if (keyword != null && !keyword.isEmpty()) {
        String name = String.valueOf(it.getOrDefault("orgName", ""));
        ok &= name.contains(keyword);
      }
      if (ok) filtered.add(it);
    }
    int page = 1;
    int pageSize = filtered.size();
    if (filters != null) {
      Object p = filters.get("page");
      Object ps = filters.get("pageSize");
      try { if (p != null) page = Integer.parseInt(String.valueOf(p)); } catch (Exception ignore) {}
      try { if (ps != null) pageSize = Integer.parseInt(String.valueOf(ps)); } catch (Exception ignore) {}
    }
    if (page < 1) page = 1;
    if (pageSize < 1) pageSize = 10;
    int start = (page - 1) * pageSize;
    if (start >= filtered.size()) return java.util.Collections.emptyList();
    int end = Math.min(start + pageSize, filtered.size());
    return filtered.subList(start, end);
  }

  @Override
  public void applySubscription(String instanceKey) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    try {
      if (instance.getOptions() == null) return;
      JsonNode root = objectMapper.readTree(instance.getOptions());
      JsonNode sub = root.path("subscription");
      boolean enabled = sub.path("enabled").asBoolean(false);
      if (!enabled) return;
      java.util.List<String> topics = new java.util.ArrayList<>();
      if (sub.path("topics").isArray()) {
        sub.path("topics").forEach(n -> topics.add(n.asText()));
      }
      String callback = sub.path("callback").asText("");
      VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
      adapter.subscribe(instance, topics, callback);
    } catch (Exception e) {
      throw new BaseException("应用订阅失败：" + e.getMessage());
    }
  }

  @Override
  public void cancelSubscription(String instanceKey) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    try {
      if (instance.getOptions() == null) return;
      JsonNode root = objectMapper.readTree(instance.getOptions());
      JsonNode sub = root.path("subscription");
      java.util.List<String> topics = new java.util.ArrayList<>();
      if (sub.path("topics").isArray()) {
        sub.path("topics").forEach(n -> topics.add(n.asText()));
      }
      VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
      adapter.unsubscribe(instance, topics);
    } catch (Exception e) {
      throw new BaseException("取消订阅失败：" + e.getMessage());
    }
  }

  /**
   * 生成平台实例唯一编号：标识 + yyyyMMdd + 随机后缀
   * 标识：wvp | ics(ics) | icc(icc)
   */
  private String generateInstanceKey(String platformType) {
    String prefix;
    if ("wvp".equalsIgnoreCase(platformType)) {
      prefix = "wvp";
    } else if ("isc".equalsIgnoreCase(platformType) || "ics".equalsIgnoreCase(platformType)) {
      prefix = "isc";
    } else if ("icc".equalsIgnoreCase(platformType)) {
      prefix = "icc";
    } else {
      // 兜底：使用平台类型本身作为前缀
      prefix = String.valueOf(platformType == null ? "plat" : platformType).replaceAll("[^a-zA-Z0-9]", "");
    }
    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd");
    String day = java.time.LocalDate.now().format(fmt);
    // 随机后缀：三位字母或数字
    String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    java.util.Random r = new java.util.Random();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 3; i++) {
      sb.append(charset.charAt(r.nextInt(charset.length())));
    }
    return prefix + day + sb.toString();
  }

  @Override
  public Map<String, String> getStreamUrl(String instanceKey, String deviceId, String channelId, String streamType) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    return adapter.getStreamUrl(instance, deviceId, channelId, streamType);
  }

  @Override
  public Map<String, Object> getPlaybackUrl(String instanceKey, String deviceId, String channelId, long startTime, long endTime) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    return adapter.getPlaybackUrl(instance, deviceId, channelId, startTime, endTime);
  }

  @Override
  public void controlPTZ(String instanceKey, String deviceId, String channelId, String command, int speed) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    adapter.controlPTZ(instance, deviceId, channelId, command, speed);
  }

  @Override
  public List<Map<String, Object>> queryRecords(String instanceKey, String deviceId, String channelId, long startTime, long endTime) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      throw new BaseException("平台实例不存在：" + instanceKey);
    }
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    return adapter.queryRecords(instance, deviceId, channelId, startTime, endTime);
  }

  @Override
  public List<Map<String, Object>> listDeviceChannels(String instanceKey, String deviceId) {
    Example ex = new Example(VideoPlatformDevice.class);
    ex.createCriteria().andEqualTo("instanceKey", instanceKey).andEqualTo("deviceId", deviceId);
    List<VideoPlatformDevice> list = deviceMapper.selectByExample(ex);
    if (list.isEmpty()) {
      return java.util.Collections.emptyList();
    }
    VideoPlatformDevice d = list.get(0);
    List<Map<String, Object>> channelList = new java.util.ArrayList<>();
    try {
      if (d.getConfiguration() != null) {
        Map<String, Object> cfg = objectMapper.readValue(d.getConfiguration(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>(){});
        Object cl = cfg.get("channelList");
        if (cl instanceof List) {
          channelList = (List<Map<String, Object>>) cl;
        }
      }
    } catch (Exception ignore) {}
    return channelList;
  }

  @Override
  public List<Map<String, Object>> listChannels(String instanceKey, String deviceId, Map<String, Object> filters) {
    // 构建查询条件
    Example ex = new Example(VideoPlatformChannel.class);
    Example.Criteria criteria = ex.createCriteria();
    criteria.andEqualTo("instanceKey", instanceKey);
    
    if (deviceId != null && !deviceId.isEmpty()) {
      // 如果指定了设备ID,需要先查询设备的主表ID
      Example deviceEx = new Example(VideoPlatformDevice.class);
      deviceEx.createCriteria()
          .andEqualTo("instanceKey", instanceKey)
          .andEqualTo("deviceId", deviceId);
      List<VideoPlatformDevice> deviceList = deviceMapper.selectByExample(deviceEx);
      if (!deviceList.isEmpty()) {
        criteria.andEqualTo("deviceId", deviceList.get(0).getId());
      } else {
        return java.util.Collections.emptyList();
      }
    }
    
    // 应用筛选条件
    if (filters != null) {
      String status = (String) filters.get("status");
      if (status != null && !status.isEmpty()) {
        criteria.andEqualTo("channelStatus", status);
      }
      
      Object ptzType = filters.get("ptzType");
      if (ptzType != null) {
        criteria.andEqualTo("ptzType", parseIntOrNull(ptzType));
      }
    }
    
    ex.orderBy("createTime").desc();
    List<VideoPlatformChannel> channels = channelMapper.selectByExample(ex);
    
    // 转换为Map列表
    List<Map<String, Object>> result = new java.util.ArrayList<>();
    for (VideoPlatformChannel ch : channels) {
      Map<String, Object> item = new java.util.HashMap<>();
      item.put("id", ch.getId());
      item.put("deviceId", ch.getDeviceId());
      item.put("channelId", ch.getChannelId());
      item.put("channelName", ch.getChannelName());
      item.put("channelStatus", ch.getChannelStatus());
      item.put("channelType", ch.getChannelType());
      item.put("ptzType", ch.getPtzType());
      item.put("manufacturer", ch.getManufacturer());
      item.put("model", ch.getModel());
      item.put("address", ch.getAddress());
      item.put("ipAddress", ch.getIpAddress());
      item.put("port", ch.getPort());
      
      // 平台特有字段
      item.put("cameraIndexCode", ch.getCameraIndexCode()); // 海康
      item.put("channelNo", ch.getChannelNo()); // 海康
      item.put("ptz", ch.getPtz()); // 海康
      item.put("streamId", ch.getStreamId()); // WVP
      item.put("channelCode", ch.getChannelCode()); // 大华
      
      result.add(item);
    }
    
    // 关键词筛选
    if (filters != null) {
      String keyword = (String) filters.get("keyword");
      if (keyword != null && !keyword.isEmpty()) {
        result = result.stream()
            .filter(item -> {
              String name = String.valueOf(item.getOrDefault("channelName", ""));
              String chId = String.valueOf(item.getOrDefault("channelId", ""));
              return name.contains(keyword) || chId.contains(keyword);
            })
            .collect(java.util.stream.Collectors.toList());
      }
    }
    
    return result;
  }

  /**
   * 同步设备通道数据到通道表
   */
  private void syncDeviceChannels(Long deviceId, String instanceKey, String platformDeviceId, 
                                   Map<String, Object> deviceData, String platformType, Date now) {
    // 提取channelList
    Object channelListObj = deviceData.get("channelList");
    if (channelListObj == null || !(channelListObj instanceof List)) {
      return;
    }
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> channelList = (List<Map<String, Object>>) channelListObj;
    String currentUserId = SecurityUtils.getUnionId();
    
    for (Map<String, Object> ch : channelList) {
      String channelId = String.valueOf(ch.getOrDefault("channelId", ch.get("id")));
      if (channelId == null || channelId.equals("null")) continue;
      
      // 查询通道是否已存在
      VideoPlatformChannel channelEntity = null;
      {
        Example ex = new Example(VideoPlatformChannel.class);
        ex.createCriteria()
            .andEqualTo("deviceId", deviceId)
            .andEqualTo("channelId", channelId);
        List<VideoPlatformChannel> existList = channelMapper.selectByExample(ex);
        channelEntity = existList.isEmpty() ? null : existList.get(0);
      }
      
      // 通道公共字段
      String channelName = (String) ch.getOrDefault("channelName", ch.get("name"));
      String channelStatus = (String) ch.getOrDefault("channelStatus", ch.get("status"));
      String channelType = (ch.get("channelType")+"");
      String parentId = (String) ch.get("parentId");
      String manufacturer = (String) ch.get("manufacturer");
      String model = (String) ch.get("model");
      String owner = (String) ch.get("owner");
      String civilCode = (String) ch.get("civilCode");
      String address = (String) ch.get("address");
      Integer parental = parseIntOrNull(ch.get("parental"));
      Integer safetyWay = parseIntOrNull(ch.get("safetyWay"));
      Integer registerWay = parseIntOrNull(ch.get("registerWay"));
      Integer secrecy = parseIntOrNull(ch.get("secrecy"));
      String ipAddress = (String) ch.get("ipAddress");
      Integer port = parseIntOrNull(ch.get("port"));
      String longitude = (String) ch.get("longitude");
      String latitude = (String) ch.get("latitude");
      Integer ptzType = parseIntOrNull(ch.get("ptzType"));
      Integer positionType = parseIntOrNull(ch.get("positionType"));
      
      // 构建 Builder
      VideoPlatformChannel.VideoPlatformChannelBuilder builder = channelEntity == null
          ? VideoPlatformChannel.builder()
              .deviceId(deviceId)
              .instanceKey(instanceKey)
              .platformDeviceId(platformDeviceId)
              .channelId(channelId)
          : VideoPlatformChannel.builder()
              .id(channelEntity.getId())
              .deviceId(deviceId)
              .instanceKey(instanceKey)
              .platformDeviceId(platformDeviceId)
              .channelId(channelId);
      
      // 设置公共字段
      builder.channelName(channelName)
          .channelStatus(channelStatus)
          .channelType(channelType)
          .parentId(parentId)
          .manufacturer(manufacturer)
          .model(model)
          .owner(owner)
          .civilCode(civilCode)
          .address(address)
          .parental(parental)
          .safetyWay(safetyWay)
          .registerWay(registerWay)
          .secrecy(secrecy)
          .ipAddress(ipAddress)
          .port(port)
          .longitude(longitude)
          .latitude(latitude)
          .ptzType(ptzType)
          .positionType(positionType);
      
      // 根据平台类型设置特有字段
      if ("wvp".equalsIgnoreCase(platformType)) {
        // WVP通道级特有字段
        builder.streamId((String) ch.get("streamId"));
        builder.gbStreamId((String) ch.get("gbStreamId"));
        builder.hasAudio(parseIntOrNull(ch.get("hasAudio")));
      } else if ("ics".equalsIgnoreCase(platformType)) {
        // 海康ISC通道级特有字段
        builder.cameraIndexCode((String) ch.get("cameraIndexCode"));
        builder.channelNo((String) ch.get("channelNo"));
        builder.cameraType(parseIntOrNull(ch.get("cameraType")));
        builder.ptz(parseIntOrNull(ch.get("ptz")));
        builder.capabilitySet((String) ch.get("capabilitySet"));
        builder.installLocation((String) ch.get("installLocation"));
      } else if ("icc".equalsIgnoreCase(platformType)) {
        // 大华ICC通道级特有字段
        builder.channelCode((String) ch.get("channelCode"));
        builder.channelSeq(parseIntOrNull(ch.get("channelSeq")));
        builder.encodeFormat((String) ch.get("encodeFormat"));
        builder.resolution((String) ch.get("resolution"));
      }
      
      // 能力集和流配置(如果JON格式)
      try {
        Object capabilities = ch.get("capabilities");
        if (capabilities != null) {
          builder.capabilities(objectMapper.writeValueAsString(capabilities));
        }
        Object streamConfig = ch.get("streamConfig");
        if (streamConfig != null) {
          builder.streamConfig(objectMapper.writeValueAsString(streamConfig));
        }
      } catch (Exception ignore) {}
      
      builder.enabled(1)
          .createId(channelEntity == null ? currentUserId : channelEntity.getCreateId())
          .updateId(currentUserId)
          .createTime(channelEntity == null ? now : channelEntity.getCreateTime())
          .updateTime(now);
      
      VideoPlatformChannel finalChannel = builder.build();
      if (channelEntity == null) {
        channelMapper.insertSelective(finalChannel);
      } else {
        channelMapper.updateByPrimaryKeySelective(finalChannel);
      }
    }
  }
  
  /**
   * 安全解析Integer
   */
  private Integer parseIntOrNull(Object obj) {
    if (obj == null) return null;
    try {
      return Integer.parseInt(String.valueOf(obj));
    } catch (Exception e) {
      return null;
    }
  }

  // ==================== 国标录像下载 ====================

  @Override
  public cn.universal.common.domain.R<Map<String, Object>> startGBRecordDownload(
      String instanceKey, String deviceId, String channelId, Map<String, Object> data) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey, null);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      // 反射调用WvpPlatformAdapter的公开方法
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "startGBRecordDownload",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Map<String, Object>> result = 
          (cn.universal.common.domain.R<Map<String, Object>>) method.invoke(
              adapter, instance, deviceId, channelId, data);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持国标录像下载", null);
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage(), null);
    }
  }

  @Override
  public cn.universal.common.domain.R<Void> stopGBRecordDownload(
      String instanceKey, String deviceId, String channelId, Map<String, Object> data) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "stopGBRecordDownload",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Void> result = 
          (cn.universal.common.domain.R<Void>) method.invoke(adapter, instance, deviceId, channelId, data);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持国标录像下载");
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage());
    }
  }

  @Override
  public cn.universal.common.domain.R<Map<String, Object>> getGBRecordDownloadProgress(
      String instanceKey, String deviceId, String channelId, String stream) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey, null);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "getGBRecordDownloadProgress",
          VideoPlatformInstance.class, String.class, String.class, String.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Map<String, Object>> result = 
          (cn.universal.common.domain.R<Map<String, Object>>) method.invoke(
              adapter, instance, deviceId, channelId, stream);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持国标录像下载", null);
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage(), null);
    }
  }

  // ==================== 云端录像 ====================

  @Override
  public cn.universal.common.domain.R<List<String>> queryCloudRecordDates(
      String instanceKey, String deviceId, String channelId, Map<String, String> params) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey, null);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "queryCloudRecordDates",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<List<String>> result = 
          (cn.universal.common.domain.R<List<String>>) method.invoke(
              adapter, instance, deviceId, channelId, params);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持云端录像", null);
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage(), null);
    }
  }

  @Override
  public cn.universal.common.domain.R<Map<String, Object>> queryCloudRecords(
      String instanceKey, String deviceId, String channelId, Map<String, String> params) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey, null);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "queryCloudRecords",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Map<String, Object>> result = 
          (cn.universal.common.domain.R<Map<String, Object>>) method.invoke(
              adapter, instance, deviceId, channelId, params);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持云端录像", null);
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage(), null);
    }
  }

  @Override
  public cn.universal.common.domain.R<Map<String, Object>> loadCloudRecord(
      String instanceKey, String deviceId, String channelId, Map<String, Object> data) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey, null);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "loadCloudRecord",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Map<String, Object>> result = 
          (cn.universal.common.domain.R<Map<String, Object>>) method.invoke(
              adapter, instance, deviceId, channelId, data);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持云端录像", null);
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage(), null);
    }
  }

  @Override
  public cn.universal.common.domain.R<Void> seekCloudRecord(
      String instanceKey, String deviceId, String channelId, Map<String, Object> data) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "seekCloudRecord",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Void> result = 
          (cn.universal.common.domain.R<Void>) method.invoke(adapter, instance, deviceId, channelId, data);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持云端录像");
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage());
    }
  }

  @Override
  public cn.universal.common.domain.R<Void> setCloudRecordSpeed(
      String instanceKey, String deviceId, String channelId, Map<String, Object> data) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "setCloudRecordSpeed",
          VideoPlatformInstance.class, String.class, String.class, Map.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Void> result = 
          (cn.universal.common.domain.R<Void>) method.invoke(adapter, instance, deviceId, channelId, data);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持云端录像");
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage());
    }
  }

  @Override
  public cn.universal.common.domain.R<Map<String, Object>> getCloudRecordPlayPath(
      String instanceKey, Integer recordId) {
    VideoPlatformInstance instance = getByInstanceKey(instanceKey);
    if (instance == null) {
      return cn.universal.common.domain.R.error("平台实例不存在：" + instanceKey, null);
    }
    
    VideoPlatformAdapter adapter = adapterRegistry.getAdapter(instance.getPlatformType());
    try {
      java.lang.reflect.Method method = adapter.getClass().getMethod(
          "getCloudRecordPlayPath",
          VideoPlatformInstance.class, Integer.class);
      @SuppressWarnings("unchecked")
      cn.universal.common.domain.R<Map<String, Object>> result = 
          (cn.universal.common.domain.R<Map<String, Object>>) method.invoke(adapter, instance, recordId);
      return result;
    } catch (NoSuchMethodException e) {
      return cn.universal.common.domain.R.error("当前平台不支持云端录像下载", null);
    } catch (Exception e) {
      return cn.universal.common.domain.R.error("调用失败：" + e.getMessage(), null);
    }
  }

  @Override
  public Map<String, Object> getDeviceDetail(String instanceKey, String deviceId) {
    // 查询主表设备信息
    Example deviceEx = new Example(VideoPlatformDevice.class);
    deviceEx.createCriteria()
        .andEqualTo("instanceKey", instanceKey)
        .andEqualTo("deviceId", deviceId);
    List<VideoPlatformDevice> deviceList = deviceMapper.selectByExample(deviceEx);
    
    if (deviceList.isEmpty()) {
      throw new BaseException("设备不存在：" + deviceId);
    }
    
    VideoPlatformDevice device = deviceList.get(0);
    Map<String, Object> result = new java.util.HashMap<>();
    
    // 主表字段
    result.put("id", device.getId());
    result.put("instanceKey", device.getInstanceKey());
    result.put("deviceId", device.getDeviceId());
    result.put("deviceName", device.getDeviceName());
    result.put("deviceStatus", device.getDeviceStatus());
    result.put("deviceModel", device.getDeviceModel());
    result.put("deviceIp", device.getDeviceIp());
    result.put("devicePort", device.getDevicePort());
    result.put("manufacturer", device.getManufacturer());
    result.put("orgId", device.getOrgId());
    result.put("orgName", device.getOrgName());
    result.put("gpsX", device.getGpsX());
    result.put("gpsY", device.getGpsY());
    result.put("gpsZ", device.getGpsZ());
    result.put("remark", device.getRemark());
    result.put("configuration", device.getConfiguration());
    result.put("enabled", device.getEnabled());
    result.put("createTime", device.getCreateTime());
    result.put("updateTime", device.getUpdateTime());
    
    // 查询扩展表信息
    Example extEx = new Example(VideoPlatformDeviceExt.class);
    extEx.createCriteria().andEqualTo("deviceId", device.getId());
    List<VideoPlatformDeviceExt> extList = deviceExtMapper.selectByExample(extEx);
    
    if (!extList.isEmpty()) {
      VideoPlatformDeviceExt ext = extList.get(0);
      Map<String, Object> extInfo = new java.util.HashMap<>();
      
      // WVP特有字段
      if (ext.getCharset() != null) extInfo.put("charset", ext.getCharset());
      if (ext.getTransport() != null) extInfo.put("transport", ext.getTransport());
      if (ext.getStreamMode() != null) extInfo.put("streamMode", ext.getStreamMode());
      if (ext.getHostAddress() != null) extInfo.put("hostAddress", ext.getHostAddress());
      if (ext.getExpires() != null) extInfo.put("expires", ext.getExpires());
      if (ext.getKeepaliveTime() != null) extInfo.put("keepaliveTime", ext.getKeepaliveTime());
      if (ext.getRegisterTime() != null) extInfo.put("registerTime", ext.getRegisterTime());
      if (ext.getMediaServerId() != null) extInfo.put("mediaServerId", ext.getMediaServerId());
      
      // 海康ISC特有字段
      if (ext.getEncodeDevIndexCode() != null) extInfo.put("encodeDevIndexCode", ext.getEncodeDevIndexCode());
      if (ext.getDeviceCapabilitySet() != null) extInfo.put("deviceCapabilitySet", ext.getDeviceCapabilitySet());
      
      // 大华ICC特有字段
      if (ext.getDeviceSn() != null) extInfo.put("deviceSn", ext.getDeviceSn());
      if (ext.getDeviceCategory() != null) extInfo.put("deviceCategory", ext.getDeviceCategory());
      if (ext.getDeviceType() != null) extInfo.put("deviceType", ext.getDeviceType());
      if (ext.getOwnerCode() != null) extInfo.put("ownerCode", ext.getOwnerCode());
      if (ext.getIsOnline() != null) extInfo.put("isOnline", ext.getIsOnline());
      if (ext.getSleepStat() != null) extInfo.put("sleepStat", ext.getSleepStat());
      if (ext.getThirdProxyPort() != null) extInfo.put("thirdProxyPort", ext.getThirdProxyPort());
      if (ext.getThirdProxyServerCode() != null) extInfo.put("thirdProxyServerCode", ext.getThirdProxyServerCode());
      if (ext.getLicenseLimit() != null) extInfo.put("licenseLimit", ext.getLicenseLimit());
      if (ext.getOfflineReason() != null) extInfo.put("offlineReason", ext.getOfflineReason());
      if (ext.getSubSystem() != null) extInfo.put("subSystem", ext.getSubSystem());
      if (ext.getUnitsInfo() != null) extInfo.put("unitsInfo", ext.getUnitsInfo());
      
      result.put("extInfo", extInfo);
    }
    
    return result;
  }

  @Override
  @Transactional
  public Map<String, Object> updateDevice(String instanceKey, String deviceId, Map<String, Object> updates) {
    Example ex = new Example(VideoPlatformDevice.class);
    ex.createCriteria().andEqualTo("instanceKey", instanceKey).andEqualTo("deviceId", deviceId);
    List<VideoPlatformDevice> list = deviceMapper.selectByExample(ex);
    if (list.isEmpty()) {
      throw new BaseException("设备不存在：" + deviceId);
    }
    VideoPlatformDevice device = list.get(0);
    if (updates != null) {
      Object v;
      v = updates.get("deviceName"); if (v != null) device.setDeviceName(String.valueOf(v));
      v = updates.get("orgId"); if (v != null) device.setOrgId(String.valueOf(v));
      v = updates.get("orgName"); if (v != null) device.setOrgName(String.valueOf(v));
      v = updates.get("gpsX"); if (v != null) device.setGpsX(String.valueOf(v));
      v = updates.get("gpsY"); if (v != null) device.setGpsY(String.valueOf(v));
      v = updates.get("gpsZ"); if (v != null) device.setGpsZ(String.valueOf(v));
      v = updates.get("remark"); if (v != null) device.setRemark(String.valueOf(v));
      v = updates.get("enabled"); if (v != null) {
        try { device.setEnabled(Integer.parseInt(String.valueOf(v))); } catch (Exception ignore) {}
      }
    }
    device.setUpdateId(SecurityUtils.getUnionId());
    device.setUpdateTime(new Date());
    deviceMapper.updateByPrimaryKeySelective(device);
    return getDeviceDetail(instanceKey, deviceId);
  }

  @Override
  @Transactional
  public void deleteDevice(String instanceKey, String deviceId) {
    Example ex = new Example(VideoPlatformDevice.class);
    ex.createCriteria().andEqualTo("instanceKey", instanceKey).andEqualTo("deviceId", deviceId);
    List<VideoPlatformDevice> list = deviceMapper.selectByExample(ex);
    if (list.isEmpty()) {
      throw new BaseException("设备不存在：" + deviceId);
    }
    VideoPlatformDevice device = list.get(0);
    Long pk = device.getId();
    if (pk != null) {
      Example chEx = new Example(VideoPlatformChannel.class);
      chEx.createCriteria().andEqualTo("deviceId", pk);
      channelMapper.deleteByExample(chEx);

      Example extEx = new Example(VideoPlatformDeviceExt.class);
      extEx.createCriteria().andEqualTo("deviceId", pk);
      deviceExtMapper.deleteByExample(extEx);
    }
    deviceMapper.deleteByPrimaryKey(device.getId());
  }
}

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

package cn.universal.dm.device.service.action;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DevLifeCycle;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.message.DownRequest;
import cn.universal.core.metadata.AbstractEventMetadata;
import cn.universal.core.metadata.AbstractFunctionMetadata;
import cn.universal.core.metadata.DeviceMetadata;
import cn.universal.dm.device.service.IoTUPPushAdapter;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.dm.device.service.log.IIoTDeviceDataService;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceLogMapper;
import cn.universal.persistence.mapper.IoTDeviceLogShardMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTUserMapper;
import jakarta.annotation.Resource;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

/**
 * 生命周期后置处理
 *
 * @version 1.0 @Author Aleo
 * @since 2023/1/20
 */
@Service("ioTDeviceActionAfterService")
@Slf4j
public class IoTDeviceActionAfterService extends IoTUPPushAdapter<BaseUPRequest>
    implements IoTDeviceLifeCycle {

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private IIoTDeviceDataService iIoTDeviceDataService;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Resource private IoTDeviceService iotDeviceService;

  @Resource private IoTDeviceLogMapper ioTDeviceLogMapper;
  @Resource private IoTDeviceLogShardMapper ioTDeviceLogShardMapper;
  @Resource private IoTUserMapper iotUserMapper;

  /** 日志分表是否开启 */
  @Value("${shard.log.enable}")
  private Boolean enable;

  /** 加载第三方平台的生命周期支持情况 */
  private Map<String, Set<String>> lifecycleSupportedConfig = new HashMap<>();

  @Autowired
  void setLifecycleConfig(
      @Value("${universal.iot.third.lifecycle.supported}") String lifecyclesupported) {
    if (StrUtil.isBlank(lifecyclesupported)) {
      return;
    }
    JSONObject obj = JSONUtil.parseObj(lifecyclesupported);
    obj.keySet().stream()
        .forEach(
            s -> {
              lifecycleSupportedConfig.put(
                  s,
                  obj.getJSONArray(s).stream().map(c -> c.toString()).collect(Collectors.toSet()));
            });
  }

  /** 校验三分平台是否支持此生命周期 */
  @Override
  public boolean thirdSupport(IoTDeviceDTO ioTDeviceDTO, DevLifeCycle devLifeCycle) {
    if (lifecycleSupportedConfig.containsKey(ioTDeviceDTO.getThirdPlatform())) {
      return lifecycleSupportedConfig
          .get(ioTDeviceDTO.getThirdPlatform())
          .contains(devLifeCycle.name());
    }
    return false;
  }

  @Override
  public void create(String productKey, String deviceId, DownRequest downRequest) {
    if (downRequest.getAppUnionId() != null) {
      iotUserMapper.licenseBuckle(downRequest.getAppUnionId());
    }
    log.info("设备创建productKey={},deviceId={}", productKey, deviceId);
    IoTDeviceDTO ioTDeviceDTO = queryDevInstance(null, deviceId, productKey);
    if (ioTDeviceDTO == null) {
      return;
    }
    BaseUPRequest upRequest = build(ioTDeviceDTO);
    upRequest.setEvent(DevLifeCycle.create.name());
    upRequest.setEventName(DevLifeCycle.create.getValue());
    // 暂不推送消息，会错误认为设备已经产生通信
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
  }

  @Override
  public void online(String productKey, String deviceId) {
    boolean flag =
        stringRedisTemplate
            .opsForValue()
            .setIfAbsent("online:" + productKey + deviceId, "1", 8, TimeUnit.MINUTES);
    IoTDeviceDTO dev = queryDevInstance(null, productKey, deviceId);
    if (dev == null || !flag) {
      log.debug("设备不存在={}，或者在不久刚离线={}", deviceId, flag);
      return;
    }
    stringRedisTemplate.delete("offline:" + productKey + deviceId);
    finalOnline(dev);
  }

  private void finalOnline(IoTDeviceDTO dev) {
    BaseUPRequest upRequest = build(dev);
    upRequest.setEvent(DevLifeCycle.online.name());
    upRequest.setEventName(DevLifeCycle.online.getValue());
    saveLog(upRequest, dev);
    doOnOffline(dev);
    // 避免短期时间内无法下线
    log.info(
        "设备上线,deviceId={},设备名称={},所属用户={},上次通信时间={},",
        dev.getDeviceId(),
        dev.getDeviceName(),
        dev.getUserUnionId(),
        dev.getOnlineTime() == null
            ? 0
            : DateUtil.format(DateUtil.date(dev.getOnlineTime() * 1000L), "yyyy-MM-dd HH:mm:ss"));
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
  }

  /** 上下线事件 */
  private void doOnOffline(IoTDeviceDTO ioTDeviceDTO) {
    DeviceMetadata deviceMetadata = ioTDeviceDTO.getDeviceMetadata();
    AbstractEventMetadata onOffline = deviceMetadata.getEventOrNull("onOffline");
    if (onOffline != null) {
      BaseUPRequest upRequest = build(ioTDeviceDTO);
      upRequest.setEvent(DevLifeCycle.onOffline.name());
      upRequest.setEventName(DevLifeCycle.onOffline.getValue());
      doUp(Stream.of(upRequest).collect(Collectors.toList()));
    }
  }

  private void finalOffline(IoTDeviceDTO dev) {
    BaseUPRequest upRequest = build(dev);
    upRequest.setEvent(DevLifeCycle.offline.name());
    upRequest.setEventName(DevLifeCycle.offline.getValue());
    saveLog(upRequest, dev);
    doOnOffline(dev);
    log.info(
        "设备离线,deviceId={},设备名称={},所属用户={},上次通信时间={},",
        dev.getDeviceId(),
        dev.getDeviceName(),
        dev.getUserUnionId(),
        dev.getOnlineTime() == null
            ? 0
            : DateUtil.format(DateUtil.date(dev.getOnlineTime() * 1000L), "yyyy-MM-dd HH:mm:ss"));
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
  }

  @Override
  public void offline(String productKey, String deviceId) {
    IoTDeviceDTO dev = queryDevInstance(null, productKey, deviceId);
    if (dev == null) {
      return;
    }
    stringRedisTemplate.delete("online:" + productKey + deviceId);
    int count = doDbOffline(productKey, deviceId);
    if (count > 0) {
      finalOffline(dev);
    }
  }

  @Override
  public void update(String iotId) {
    IoTDeviceDTO dev = queryDevInstance(iotId, null, null);
    if (dev == null) {
      return;
    }
    BaseUPRequest upRequest = build(dev);
    upRequest.setEvent(DevLifeCycle.update.name());
    upRequest.setEventName(DevLifeCycle.update.getValue());
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
    log.info(
        "设备修改,deviceId={},设备名称={},所属用户={},上次通信时间={},",
        dev.getDeviceId(),
        dev.getDeviceName(),
        dev.getUserUnionId(),
        dev.getOnlineTime() == null
            ? 0
            : DateUtil.format(DateUtil.date(dev.getOnlineTime() * 1000L), "yyyy-MM-dd HH:mm:ss"));
  }

  /** 执行离线状态变更 */
  private int doDbOffline(String iotId) {
    if (StrUtil.isBlank(iotId)) {
      return 0;
    }
    Example example = new Example(IoTDevice.class);
    example.createCriteria().andEqualTo("iotId", iotId);
    IoTDevice ioTDevice = new IoTDevice();
    // true=1 false=0
    ioTDevice.setState(Boolean.FALSE);
    // 添加条件：只有当当前状态为true时才更新为false
    example.and().andEqualTo("state", Boolean.TRUE);
    int count = ioTDeviceMapper.updateByConditionSelective(ioTDevice, example);
    log.info("执行设备离线状态变更，iotId={},影响条数={}", iotId, count);
    return count;
  }

  /** 执行离线状态变更 */
  private int doDbOffline(String productKey, String deviceId) {
    if (StrUtil.isBlank(productKey) || StrUtil.isBlank(deviceId)) {
      return 0;
    }
    Example example = new Example(IoTDevice.class);
    example.createCriteria().andEqualTo("productKey", productKey).andEqualTo("deviceId", deviceId);
    IoTDevice ioTDevice = new IoTDevice();
    // true=1 false=0
    ioTDevice.setState(Boolean.FALSE);
    // 添加条件：只有当当前状态为true时才更新为false
    example.and().andEqualTo("state", Boolean.TRUE);
    int count = ioTDeviceMapper.updateByConditionSelective(ioTDevice, example);
    log.info("执行设备离线状态变更，deviceId={},影响条数={}", deviceId, count);
    return count;
  }

  @Override
  public void enable(String iotId) {
    IoTDeviceDTO dev = queryDevInstance(iotId, null, null);
    if (dev == null) {
      return;
    }
    BaseUPRequest upRequest = build(dev);
    upRequest.setEvent(DevLifeCycle.enable.name());
    upRequest.setEventName(DevLifeCycle.enable.getValue());
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
    log.info(
        "设备启用,deviceId={},设备名称={},所属用户={},上次通信时间={},",
        dev.getDeviceId(),
        dev.getDeviceName(),
        dev.getUserUnionId(),
        dev.getOnlineTime() == null
            ? 0
            : DateUtil.format(DateUtil.date(dev.getOnlineTime() * 1000L), "yyyy-MM-dd HH:mm:ss"));
  }

  @Override
  public void disable(String iotId) {
    IoTDeviceDTO dev = queryDevInstance(iotId, null, null);
    if (dev == null) {
      return;
    }
    BaseUPRequest upRequest = build(dev);
    upRequest.setEvent(DevLifeCycle.disable.name());
    upRequest.setEventName(DevLifeCycle.disable.getValue());
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
    log.info(
        "设备禁用,deviceId={},设备名称={},所属用户={},上次通信时间={},",
        dev.getDeviceId(),
        dev.getDeviceName(),
        dev.getUserUnionId(),
        dev.getOnlineTime() == null
            ? 0
            : DateUtil.format(DateUtil.date(dev.getOnlineTime() * 1000L), "yyyy-MM-dd HH:mm:ss"));
  }

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
  public void delete(IoTDeviceDTO ioTDeviceDTO, DownRequest downRequest) {
    if (ioTDeviceDTO == null) {
      return;
    }
    if (downRequest.getAppUnionId() != null) {
      iotUserMapper.licenseAdd(downRequest.getAppUnionId());
    }
    log.info(
        "设备删除,deviceId={},productKey={},unionId={}",
        ioTDeviceDTO.getDeviceId(),
        ioTDeviceDTO.getProductKey(),
        ioTDeviceDTO.getUserUnionId());
    BaseUPRequest upRequest = build(ioTDeviceDTO);
    upRequest.setEvent(DevLifeCycle.delete.name());
    upRequest.setEventName(DevLifeCycle.delete.getValue());
    doUp(Stream.of(upRequest).collect(Collectors.toList()));
  }

  @Override
  public void commandSuccess(IoTDeviceDTO ioTDeviceDTO, String commandId, Object functions) {
    command(ioTDeviceDTO, commandId, functions, true);
  }

  private void command(
      IoTDeviceDTO ioTDeviceDTO, String commandId, Object functions, boolean success) {
    command(ioTDeviceDTO, commandId, functions);
  }

  @Override
  public void command(IoTDeviceDTO ioTDeviceDTO, String commandId, Object functions) {
    // 默认function
    String REPLY_DEFAULT_FUNCTION = "codecReply";
    String REPLY_DEFAULT_FUNCTION_NAME = "编解码自动回复";

    if (ioTDeviceDTO == null) {
      return;
    }
    JSONObject jsonObject = null;
    String functionObj = StrUtil.str(functions, Charset.defaultCharset());
    if (JSONUtil.isTypeJSON(functionObj)) {
      jsonObject = JSONUtil.parseObj(functions);
    } else {
      jsonObject = new JSONObject();
      jsonObject.set("payload", functionObj);
    }

    // 坚决
    if (!jsonObject.containsKey("function")) {
      jsonObject.set("function", REPLY_DEFAULT_FUNCTION);
      jsonObject.set("functionName", REPLY_DEFAULT_FUNCTION_NAME);
    }
    log.info(
        "设备功能下发,deviceId={},productKey={},unionId={},commandId={},functions={}",
        ioTDeviceDTO.getDeviceId(),
        ioTDeviceDTO.getProductKey(),
        ioTDeviceDTO.getUserUnionId(),
        commandId,
        functionObj);
    IoTDeviceLog ioTDeviceLog = buildCommandLog(ioTDeviceDTO, commandId);
    ioTDeviceLog.setContent(JSONUtil.toJsonStr(jsonObject));
    DeviceMetadata deviceMetadata =
        iotProductDeviceService.getDeviceMetadata(ioTDeviceDTO.getProductKey());
    AbstractFunctionMetadata functionMetadata =
        deviceMetadata.getFunctionOrNull(jsonObject.getStr("function"));
    if (functionMetadata != null && StrUtil.isNotBlank(functionMetadata.getName())) {
      ioTDeviceLog.setEvent(functionMetadata.getId() + "(" + functionMetadata.getName() + ")");
    } else {
      ioTDeviceLog.setEvent(functionMetadata.getId());
    }
    saveLog(ioTDeviceLog, ioTDeviceDTO);
    // 不推送日志
  }

  @Override
  public void commandResp(IoTDeviceDTO ioTDeviceDTO, String commandId, Object resp) {
    if (ioTDeviceDTO == null) {
      return;
    }
    log.info(
        "设备功能下发回复,deviceId={},productKey={},unionId={},commandId={},functions={}",
        ioTDeviceDTO.getDeviceId(),
        ioTDeviceDTO.getProductKey(),
        ioTDeviceDTO.getUserUnionId(),
        commandId,
        JSONUtil.toJsonStr(resp));
    // 只查询2天前内的指令
    long twoDaysAgo = DateUtil.currentSeconds() - (2 * 24 * 60 * 60);
    //    Example example = new Example(IoTDeviceLog.class);
    //    example.createCriteria().
    //        andEqualTo("iotId", ioTDeviceDTO.getIotId()).
    //        andEqualTo("commandId", commandId).
    //        andGreaterThanOrEqualTo("createTime", twoDaysAgo).
    //        andEqualTo("commandStatus", 0);
    //    IoTDeviceLog devLog = ioTDeviceLogMapper.selectOneByExample(example);
    IoTDeviceLog devShardLog =
        ioTDeviceLogShardMapper.selectOneForCtwing(
            ioTDeviceDTO.getIotId(), commandId, twoDaysAgo, 0);
    //
    //    if (enable) {
    //      IoTDeviceLog newlog = IoTDeviceLog.builder().commandStatus(1).build();
    //      Example updateExample = new Example(IoTDeviceLog.class);
    //      updateExample.createCriteria().andEqualTo("id", devShardLog.getId());
    //      //日志分表 暂时双写单读
    //      if (devShardLog != null) {
    //        ioTDeviceLogShardMapper.updateByExampleSelective(newlog, updateExample);
    //      }
    //    }
    if (devShardLog != null) {
      IoTDeviceLog newlog =
          IoTDeviceLog.builder()
              .commandStatus(1)
              .iotId(devShardLog.getIotId())
              .id(devShardLog.getId())
              .build();
      //      Example updateExample = new Example(IoTDeviceLog.class);
      //      updateExample.createCriteria().andEqualTo("id", devShardLog.getId());
      //      ioTDeviceLogShardMapper.updateByExampleSelective(newlog, updateExample);
      ioTDeviceLogShardMapper.updateLogByIdForCtwing(newlog, devShardLog.getIotId());
      /** 推送消息 */
      BaseUPRequest upRequest = build(ioTDeviceDTO);
      upRequest.setMessageType(MessageType.REPLY);
      upRequest.setFunction(devShardLog.getEvent());
      //      upRequest.setEvent(devLog.getEvent());
      upRequest.setCommandId(commandId);
      upRequest.setCommandStatus(1);
      // 保存回复日志
      saveLog(upRequest, ioTDeviceDTO);
      doUp(Stream.of(upRequest).collect(Collectors.toList()));
    }
  }

  private IoTDeviceLog buildCommandLog(IoTDeviceDTO instanceBO, String commandId) {
    IoTDeviceLog ioTDeviceLog =
        IoTDeviceLog.builder()
            .deviceId(instanceBO.getDeviceId())
            .deviceName(instanceBO.getDeviceName())
            .productKey(instanceBO.getProductKey())
            // .extDeviceId(instanceBO.getExtDeviceId())
            .messageType(MessageType.FUNCTIONS.name())
            .iotId(instanceBO.getIotId())
            .createId(instanceBO.getUserUnionId())
            .createTime(DateUtil.currentSeconds())
            .commandStatus(0)
            // 使用此字段作为校验标识
            .commandId(StrUtil.length(commandId) > 10 ? StrUtil.sub(commandId, 0, 10) : commandId)
            .build();
    return ioTDeviceLog;
  }

  private BaseUPRequest build(IoTDeviceDTO instanceBO) {
    BaseUPRequest request = new BaseUPRequest();
    // 基础信息
    request.setIotId(instanceBO.getIotId());
    request.setDeviceName(instanceBO.getDeviceName());
    request.setDeviceId(instanceBO.getDeviceId());
    request.setTime(System.currentTimeMillis());
    request.setProductKey(instanceBO.getProductKey());
    request.setUserUnionId(instanceBO.getUserUnionId());
    request.setMessageType(MessageType.EVENT);
    request.setDeviceNode(instanceBO.getDeviceNode());
    request.setIoTDeviceDTO(instanceBO);
    return request;
  }

  private IoTDeviceDTO queryDevInstance(String iotId, String productKey, String deviceId) {
    Map<String, Object> param = new HashMap<>();
    param.put("iotId", iotId);
    param.put("deviceId", deviceId);
    param.put("productKey", productKey);
    IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBONoCache(param);
    return ioTDeviceDTO;
  }

  public void saveLog(BaseUPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    IoTProduct product = iotProductDeviceService.getProduct(upRequest.getProductKey());
    iIoTDeviceDataService.saveDeviceLog(upRequest, ioTDeviceDTO, product);
  }

  public void saveLog(IoTDeviceLog ioTDeviceLog, IoTDeviceDTO ioTDeviceDTO) {
    IoTProduct product = iotProductDeviceService.getProduct(ioTDeviceDTO.getProductKey());
    iIoTDeviceDataService.saveDeviceLog(ioTDeviceLog, ioTDeviceDTO, product);
  }
}

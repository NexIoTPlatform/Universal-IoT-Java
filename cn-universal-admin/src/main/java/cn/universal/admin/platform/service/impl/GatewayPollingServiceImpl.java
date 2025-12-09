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
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.platform.service.DeviceCommandDispatcher;
import cn.universal.admin.platform.service.IGatewayPollingService;
import cn.universal.common.constant.IoTConstant.DeviceNode;
import cn.universal.common.domain.R;
import cn.universal.persistence.dto.GatewayPollingConfigDTO;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.GatewayPollingCommand;
import cn.universal.persistence.entity.GatewayPollingConfig;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.mapper.GatewayPollingCommandMapper;
import cn.universal.persistence.mapper.GatewayPollingConfigMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import java.util.Date;
import java.util.List;

import cn.universal.persistence.query.IoTAPIQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 网关轮询服务实现
 *
 * @author Aleo
 * @date 2025-10-26
 */
@Service
@Slf4j
public class GatewayPollingServiceImpl implements IGatewayPollingService {

  @Autowired private GatewayPollingConfigMapper pollingConfigMapper;

  @Autowired private GatewayPollingCommandMapper pollingCommandMapper;

  @Autowired private IoTDeviceMapper deviceMapper;

  @Autowired private DeviceCommandDispatcher commandDispatcher;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public R savePollingConfig(GatewayPollingConfigDTO dto) {
    try {
      // 1. 验证是否为网关设备
      // 1. 验证是否为网关设备
      IoTDeviceDTO device =
          deviceMapper.selectIoTDeviceBO(
              BeanUtil.beanToMap(
                  IoTDevice.builder()
                      .productKey(dto.getProductKey())
                      .deviceId(dto.getDeviceId())
                      .build()));
      if (device == null) {
        return R.error("设备不存在");
      }

      if (!DeviceNode.GATEWAY.equals(device.getDeviceNode())) {
        return R.error("只有网关设备才能配置云端轮询");
      }

      // 2. 保存或更新轮询配置
      GatewayPollingConfig config = new GatewayPollingConfig();
      BeanUtils.copyProperties(dto, config);

      // 计算下次轮询时间
      if (Boolean.TRUE.equals(config.getEnabled())) {
        config.setNextPollTime(calculateNextPollTime(config.getIntervalSeconds()));
      }

      // 初始化状态
      if (config.getPollingStatus() == null) {
        config.setPollingStatus("NORMAL");
      }

      // 判断是新增还是更新 (使用 productKey + deviceId 组合键)
      GatewayPollingConfig existConfig =
          pollingConfigMapper.selectByDevice(dto.getProductKey(), dto.getDeviceId());
      if (existConfig != null) {
        config.setId(existConfig.getId());
        config.setUpdateTime(new Date());
        pollingConfigMapper.updateByPrimaryKeySelective(config);
      } else {
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        pollingConfigMapper.insertSelective(config);
      }

      // 3. 保存轮询指令
      if (CollUtil.isNotEmpty(dto.getCommands())) {
        // 先删除旧指令 (使用 productKey + deviceId)
        pollingCommandMapper.deleteByGateway(dto.getProductKey(), dto.getDeviceId());

        // 插入新指令
        for (int i = 0; i < dto.getCommands().size(); i++) {
          GatewayPollingCommand cmd = dto.getCommands().get(i);
          cmd.setGatewayProductKey(dto.getProductKey());
          cmd.setGatewayDeviceId(dto.getDeviceId());
          cmd.setExecutionOrder(i);
          cmd.setCreateTime(new Date());
          cmd.setUpdateTime(new Date());
          pollingCommandMapper.insertSelective(cmd);
        }
      }

      log.info(
          "保存网关轮询配置成功: productKey={}, deviceId={}, interval={}s, commands={}",
          dto.getProductKey(),
          dto.getDeviceId(),
          dto.getIntervalSeconds(),
          dto.getCommands() != null ? dto.getCommands().size() : 0);

      return R.ok("保存成功");

    } catch (Exception e) {
      log.error(
          "保存网关轮询配置失败: productKey={}, deviceId={}", dto.getProductKey(), dto.getDeviceId(), e);
      return R.error("保存失败: " + e.getMessage());
    }
  }

  @Override
  public R getPollingConfig(String productKey, String deviceId) {
    try {
      GatewayPollingConfig config = pollingConfigMapper.selectByDevice(productKey, deviceId);
      if (config == null) {
        return R.ok(null);
      }

      // 查询指令列表 (使用 productKey + deviceId)
      List<GatewayPollingCommand> commands =
          pollingCommandMapper.selectByGateway(productKey, deviceId);

      // 构建DTO返回
      GatewayPollingConfigDTO dto = new GatewayPollingConfigDTO();
      BeanUtils.copyProperties(config, dto);
      dto.setCommands(commands);

      return R.ok(dto);

    } catch (Exception e) {
      log.error("查询网关轮询配置失败: productKey={}, deviceId={}", productKey, deviceId, e);
      return R.error("查询失败: " + e.getMessage());
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public R deletePollingConfig(String productKey, String deviceId) {
    try {
      // 删除配置 (使用 productKey + deviceId)
      GatewayPollingConfig config = pollingConfigMapper.selectByDevice(productKey, deviceId);
      if (config != null) {
        pollingConfigMapper.deleteByPrimaryKey(config.getId());
      }

      // 删除指令 (使用 productKey + deviceId)
      pollingCommandMapper.deleteByGateway(productKey, deviceId);

      log.info("删除网关轮询配置成功: productKey={}, deviceId={}", productKey, deviceId);
      return R.ok("删除成功");

    } catch (Exception e) {
      log.error("删除网关轮询配置失败: productKey={}, deviceId={}", productKey, deviceId, e);
      return R.error("删除失败: " + e.getMessage());
    }
  }

  @Override
  public void pollGatewayDevice(GatewayPollingConfig config) {
    try {
      // 1. 检查网关在线状态
      IoTDevice gateway =
          deviceMapper.selectIoTDevice(config.getProductKey(), config.getDeviceId());

//      if (gateway == null || !Boolean.TRUE.equals(gateway.getState())) {
//        log.warn(
//            "网关离线，跳过轮询: productKey={}, deviceId={}", config.getProductKey(), config.getDeviceId());
//        return;
//      }

      // 2. 查询轮询指令 (使用 productKey + deviceId)
      List<GatewayPollingCommand> commands =
          pollingCommandMapper.selectByGateway(config.getProductKey(), config.getDeviceId());

      if (CollUtil.isEmpty(commands)) {
        log.warn(
            "网关没有配置轮询指令: productKey={}, deviceId={}", config.getProductKey(), config.getDeviceId());
        return;
      }

      log.info(
          "开始轮询网关设备: productKey={}, deviceId={}, commands={}",
          config.getProductKey(),
          config.getDeviceId(),
          commands.size());

      // 3. 按顺序执行指令，每条指令间添加延迟避免设备缓冲区溢出
      boolean allSuccess = true;
      int executedCount = 0;
      // 获取配置的指令间隔，默认300ms
      int commandIntervalMs = config.getCommandIntervalMs() != null ? config.getCommandIntervalMs() : 300;
      
      for (GatewayPollingCommand command : commands) {
        if (Boolean.TRUE.equals(command.getEnabled())) {
          // 非第一条指令先等待，给设备响应时间
          if (executedCount > 0) {
            try {
              Thread.sleep(commandIntervalMs);
              log.debug(
                  "[指令间隔] 等待{}ms，防止设备缓冲区溢出: deviceId={}",
                  commandIntervalMs,
                  config.getDeviceId());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              log.warn("指令间隔等待被中断", e);
            }
          }
          
          boolean success = executeCommand(gateway, command);
          if (!success) {
            allSuccess = false;
          }
          executedCount++;
        }
      }

      // 4. 更新轮询状态
      if (allSuccess) {
        Date nextPollTime = calculateNextPollTime(config.getIntervalSeconds());
        pollingConfigMapper.updatePollingSuccess(config.getId(), nextPollTime);
        log.info(
            "网关轮询成功: productKey={}, deviceId={}, nextPollTime={}",
            config.getProductKey(),
            config.getDeviceId(),
            DateUtil.formatDateTime(nextPollTime));
      } else {
        pollingConfigMapper.updatePollingFail(config.getId());
        log.warn(
            "网关轮询部分失败: productKey={}, deviceId={}", config.getProductKey(), config.getDeviceId());
      }

    } catch (Exception e) {
      log.error(
          "网关轮询异常: productKey={}, deviceId={}", config.getProductKey(), config.getDeviceId(), e);
      pollingConfigMapper.updatePollingFail(config.getId());
    }
  }

  @Override
  public List<GatewayPollingConfig> getDuePollingDevices(int intervalSeconds) {
    return pollingConfigMapper.selectDuePolling(intervalSeconds);
  }

  @Override
  public R testPolling(String productKey, String deviceId) {
    try {
      // 1. 查询轮询配置
      GatewayPollingConfig config = pollingConfigMapper.selectByDevice(productKey, deviceId);
      if (config == null) {
        return R.error("未配置轮询，请先保存轮询配置");
      }

      // 2. 验证是否为网关设备
      IoTDeviceDTO device =
          deviceMapper.selectIoTDeviceBO(
              BeanUtil.beanToMap(
                  IoTDevice.builder()
                      .productKey(productKey)
                      .deviceId(deviceId)
                      .build()));

      if (device == null) {
        return R.error("设备不存在");
      }

      if (!DeviceNode.GATEWAY.equals(device.getDeviceNode())) {
        return R.error("只有网关设备才能进行轮询测试");
      }

      // 3. 查询轮询指令
      List<GatewayPollingCommand> commands =
          pollingCommandMapper.selectByGateway(productKey, deviceId);

      if (CollUtil.isEmpty(commands)) {
        return R.error("未配置轮询指令，请先添加轮询指令");
      }

      log.info("开始测试轮询: productKey={}, deviceId={}, commands={}", 
          productKey, deviceId, commands.size());

      // 4. 立即执行轮询
      pollGatewayDevice(config);

      return R.ok("测试轮询已执行，共发送 " + commands.stream().filter(cmd -> Boolean.TRUE.equals(cmd.getEnabled())).count() + " 条指令");

    } catch (Exception e) {
      log.error("测试轮询失败: productKey={}, deviceId={}", productKey, deviceId, e);
      return R.error("测试轮询失败: " + e.getMessage());
    }
  }

  // ==================== 私有方法 ====================

  /** 执行单条指令 */
  private boolean executeCommand(IoTDevice gateway, GatewayPollingCommand command) {
    try {
      log.info(
          "执行轮询指令: productKey={}, deviceId={}, commandName={}, hex={}",
          gateway.getProductKey(),
          gateway.getDeviceId(),
          command.getCommandName(),
          command.getCommandHex());

      // 通过统一调度器发送指令
      R<?> result =
          commandDispatcher.sendCommand(
              gateway.getProductKey(), gateway.getDeviceId(), command.getCommandHex());

      if (result.isSuccess()) {
        log.info(
            "轮询指令发送成功: productKey={}, deviceId={}, commandName={}",
            gateway.getProductKey(),
            gateway.getDeviceId(),
            command.getCommandName());
        return true;
      } else {
        log.warn(
            "轮询指令发送失败: productKey={}, deviceId={}, commandName={}, reason={}",
            gateway.getProductKey(),
            gateway.getDeviceId(),
            command.getCommandName(),
            result.getMsg());
        return false;
      }

    } catch (Exception e) {
      log.error(
          "执行轮询指令异常: productKey={}, deviceId={}, commandName={}",
          gateway.getProductKey(),
          gateway.getDeviceId(),
          command.getCommandName(),
          e);
      return false;
    }
  }

  /** 计算下次轮询时间 */
  private Date calculateNextPollTime(int intervalSeconds) {
    return DateUtil.offsetSecond(new Date(), intervalSeconds);
  }
}

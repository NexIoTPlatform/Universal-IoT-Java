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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.common.service.BaseServiceImpl;
import cn.universal.admin.common.utils.SecurityUtils;
import cn.universal.admin.platform.service.BatchFunctionTask;
import cn.universal.admin.platform.service.IIoTDeviceFunctionTaskService;
import cn.universal.common.exception.IoTException;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceFunctionHistory;
import cn.universal.persistence.entity.IoTDeviceFunctionTask;
import cn.universal.persistence.entity.IoTUser;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionHistoryBO;
import cn.universal.persistence.entity.bo.IoTDeviceFunctionTaskBO;
import cn.universal.persistence.entity.vo.IoTDeviceFunctionHistoryVO;
import cn.universal.persistence.entity.vo.IoTDeviceFunctionTaskVO;
import cn.universal.persistence.mapper.IoTDeviceFunctionHistoryMapper;
import cn.universal.persistence.mapper.IoTDeviceFunctionTaskMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备Service业务层处理
 *
 * @since 2025-12-24
 */
@Service
@Slf4j
public class IoTDeviceFunctionTaskServiceImpl extends BaseServiceImpl
    implements IIoTDeviceFunctionTaskService {

  @Resource private IoTDeviceFunctionTaskMapper ioTDeviceFunctionTaskMapper;
  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private IoTDeviceFunctionHistoryMapper ioTDeviceFunctionHistoryMapper;
  @Resource private BatchFunctionTask batchFunctionTask;

  @Override
  public List<IoTDeviceFunctionTaskVO> selectTaskList(IoTDeviceFunctionTaskBO bo, IoTUser iotUser) {
    return ioTDeviceFunctionTaskMapper.selectTaskList(
        bo, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  @Override
  public List<IoTDeviceFunctionHistoryVO> queryFunctionListByTaskId(
      IoTDeviceFunctionHistoryBO bo, IoTUser iotUser) {
    return ioTDeviceFunctionTaskMapper.queryFunctionListByTaskId(
        bo, iotUser.isAdmin() ? null : iotUser.getUnionId());
  }

  @Override
  public Boolean addFunctionTask(IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO, IoTUser iotUser) {
    List<IoTDevice> ioTDevices;
    // 全选
    if (ioTDeviceFunctionTaskBO.getChooseAll()) {
      IoTDevice param = new IoTDevice();
      param.setApplication(ioTDeviceFunctionTaskBO.getApplicationId());
      param.setProductKey(ioTDeviceFunctionTaskBO.getProductKey());
      ioTDevices =
          ioTDeviceMapper.selectDevInstanceList(
              param, iotUser.isAdmin() ? null : iotUser.getUnionId());
      if (ioTDeviceFunctionTaskBO.getRemoveIds() != null
          && ioTDeviceFunctionTaskBO.getRemoveIds().length > 0) {
        Map<String, String> collect =
            Arrays.stream(ioTDeviceFunctionTaskBO.getRemoveIds())
                .collect(Collectors.toMap(String::valueOf, Function.identity()));
        ioTDevices =
            ioTDevices.stream()
                .filter(
                    devInstance -> {
                      if (collect.containsKey(devInstance.getId().toString())) {
                        return false;
                      }
                      return true;
                    })
                .collect(Collectors.toList());
      }
    } else {
      ioTDevices = ioTDeviceMapper.listByIds(ioTDeviceFunctionTaskBO.getIds());
    }
    if (CollectionUtil.isEmpty(ioTDevices)) {
      throw new IoTException("无匹配设备");
    }
    // 保存任务
    IoTDeviceFunctionTask ioTDeviceFunctionTask = new IoTDeviceFunctionTask();
    ioTDeviceFunctionTask.setTaskName(ioTDeviceFunctionTaskBO.getTaskName());
    ioTDeviceFunctionTask.setCreator(SecurityUtils.getUnionId());
    ioTDeviceFunctionTask.setCreatorId(SecurityUtils.getUnionId());
    ioTDeviceFunctionTask.setProductKey(ioTDeviceFunctionTaskBO.getProductKey());
    ioTDeviceFunctionTask.setCommand(ioTDeviceFunctionTaskBO.getCommand());
    ioTDeviceFunctionTask.setCommandData(ioTDeviceFunctionTaskBO.getCommandData());
    ioTDeviceFunctionTask.setStatus(0);
    ioTDeviceFunctionTaskMapper.insert(ioTDeviceFunctionTask);
    // 保存初始指令记录
    List<IoTDeviceFunctionHistory> histories = new ArrayList<>();
    ioTDevices.forEach(
        devInstance -> {
          IoTDeviceFunctionHistory h = new IoTDeviceFunctionHistory();
          h.setId(IdUtil.getSnowflake().nextId());
          h.setDeviceId(devInstance.getDeviceId());
          h.setDeviceName(devInstance.getDeviceName());
          h.setTaskId(ioTDeviceFunctionTask.getId());
          h.setProductKey(devInstance.getProductKey());
          h.setIotId(devInstance.getIotId());
          h.setDownState(0);
          h.setRetry(0);
          // 额外字段 如表号等
          if (CollectionUtil.isNotEmpty(ioTDeviceFunctionTaskBO.getExtParam())) {
            JSONObject config = JSONUtil.parseObj(devInstance.getConfiguration());
            JSONObject extParam = new JSONObject();
            ioTDeviceFunctionTaskBO
                .getExtParam()
                .forEach(
                    s -> {
                      if (config.containsKey(s)) {
                        extParam.set(s, config.getStr(s));
                      }
                    });
            h.setExtParam(JSONUtil.toJsonStr(extParam));
          }
          histories.add(h);
        });
    ioTDeviceFunctionHistoryMapper.batchInsert(histories);
    batchFunctionTask.doTask();
    return true;
  }

  @Override
  public Boolean retryFunctionTask(IoTDeviceFunctionTaskBO ioTDeviceFunctionTaskBO) {
    int row = ioTDeviceFunctionTaskMapper.retryTask(ioTDeviceFunctionTaskBO.getTaskId());
    if (row == 0) {
      return false;
    }
    int row2 = ioTDeviceFunctionHistoryMapper.retryHistory(ioTDeviceFunctionTaskBO);
    if (row2 == 0) {
      return false;
    }
    batchFunctionTask.doTask();
    return true;
  }
}

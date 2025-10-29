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

package cn.universal.admin.platform.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.DownCmd;
import cn.universal.common.domain.R;
import cn.universal.core.message.UnifiedDownlinkCommand;
import cn.universal.core.service.IoTDownlFactory;
import cn.universal.persistence.dto.IoTDeviceFunctionHistoryDTO;
import cn.universal.persistence.entity.IoTDeviceFunctionHistory;
import cn.universal.persistence.entity.IoTDeviceFunctionTask;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.mapper.IoTDeviceFunctionHistoryMapper;
import cn.universal.persistence.mapper.IoTDeviceFunctionTaskMapper;
import cn.universal.persistence.mapper.IoTProductMapper;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/3/21
 */
@Component
@Slf4j
public class BatchFunctionTask {

  @Resource private IoTProductMapper ioTProductMapper;
  @Resource private IoTDeviceFunctionTaskMapper ioTDeviceFunctionTaskMapper;
  @Resource private IoTDeviceFunctionHistoryMapper ioTDeviceFunctionHistoryMapper;

  private AtomicBoolean scheduled = new AtomicBoolean();

  private static ScheduledExecutorService executor =
      Executors.newScheduledThreadPool(1, new NamedThreadFactory("function-batch-task-log", true));

  private Set<IoTDeviceFunctionHistoryDTO> functionSet = new ConcurrentHashSet<>();

  /** 定时 */
  @Profile({"prod", "dev", "out"})
  @Scheduled(cron = "0 0/10 * * *  ? ")
  public void doTask() {
    if (functionSet.size() > 0) {
      return;
    } else {
      IoTDeviceFunctionTask bo = new IoTDeviceFunctionTask();
      bo.setStatus(0);
      IoTDeviceFunctionTask ioTDeviceFunctionTask = ioTDeviceFunctionTaskMapper.selectOneTask(bo);
      if (ObjectUtil.isEmpty(ioTDeviceFunctionTask)) {
        return;
      }
      log.info(
          "开始批量功能下发，taskId={},任务名称={},执行指令={}",
          ioTDeviceFunctionTask.getId(),
          ioTDeviceFunctionTask.getTaskName(),
          ioTDeviceFunctionTask.getCommand());
      Example ex2 = new Example(IoTDeviceFunctionHistory.class);
      // 重新下发时不更改任务时间
      ioTDeviceFunctionTask.setBeginTime(
          ioTDeviceFunctionTask.getBeginTime() != null
              ? ioTDeviceFunctionTask.getBeginTime()
              : new Date());
      ioTDeviceFunctionTask.setStatus(2);
      ex2.createCriteria()
          .andEqualTo("taskId", ioTDeviceFunctionTask.getId())
          .andEqualTo("downState", 0);
      List<IoTDeviceFunctionHistory> histories =
          ioTDeviceFunctionHistoryMapper.selectByExample(ex2);
      List<IoTDeviceFunctionHistoryDTO> dtos = new ArrayList<>();
      if (CollectionUtil.isEmpty(histories)) {
        ioTDeviceFunctionTask.setEndTime(
            ioTDeviceFunctionTask.getEndTime() != null
                ? ioTDeviceFunctionTask.getEndTime()
                : new Date());
        ioTDeviceFunctionTask.setStatus(1);
      }
      ioTDeviceFunctionTaskMapper.updateByPrimaryKey(ioTDeviceFunctionTask);
      IoTProduct productByProductKey =
          ioTProductMapper.getProductByProductKey(ioTDeviceFunctionTask.getProductKey());
      String thirdPlatform = productByProductKey.getThirdPlatform();
      histories.forEach(
          ioTDeviceFunctionHistory -> {
            IoTDeviceFunctionHistoryDTO dto =
                BeanUtil.copyProperties(
                    ioTDeviceFunctionHistory, IoTDeviceFunctionHistoryDTO.class);
            JSONObject commandData = JSONUtil.parseObj(ioTDeviceFunctionTask.getCommandData());
            if (ObjectUtil.isNotEmpty(ioTDeviceFunctionHistory.getExtParam())) {
              JSONObject data = commandData.getJSONObject("data");
              data.putAll(JSONUtil.parseObj(ioTDeviceFunctionHistory.getExtParam()));
              commandData.set("data", data);
            }
            dto.setCommand(ioTDeviceFunctionTask.getCommand());
            dto.setCommandData(JSONUtil.toJsonStr(commandData));
            dto.setThirdPlatform(thirdPlatform);
            dtos.add(dto);
          });
      functionSet.addAll(dtos);
      consumer();
    }
    if (scheduled.compareAndSet(false, true)) {
      executor.scheduleWithFixedDelay(this::consumer, 5, 5, TimeUnit.SECONDS);
    }
  }

  public void consumer() {
    try {
      if (!functionSet.isEmpty()) {
        Iterator<IoTDeviceFunctionHistoryDTO> iterator = functionSet.iterator();
        if (iterator.hasNext()) {
          IoTDeviceFunctionHistoryDTO history = iterator.next();
          iterator.remove();
          down(history);
          // 最后一个设备指令时 修改任务完成状态
          if (!iterator.hasNext()) {
            IoTDeviceFunctionTask task = new IoTDeviceFunctionTask();
            task.setId(history.getTaskId());
            IoTDeviceFunctionTask ioTDeviceFunctionTask =
                ioTDeviceFunctionTaskMapper.selectOneTask(task);
            if (ioTDeviceFunctionTask.getEndTime() == null) {
              ioTDeviceFunctionTask.setEndTime(new Date());
            }
            ioTDeviceFunctionTask.setStatus(1);
            ioTDeviceFunctionTaskMapper.updateByPrimaryKey(ioTDeviceFunctionTask);
            log.info(
                "结束批量功能下发，taskId={},任务名称={},执行指令={}",
                ioTDeviceFunctionTask.getId(),
                ioTDeviceFunctionTask.getTaskName(),
                ioTDeviceFunctionTask.getCommand());
          }
        }
      }
    } catch (Exception e) {
      log.error("批量功能下发异常", e);
    }
  }

  private void down(IoTDeviceFunctionHistoryDTO h) {
    JSONObject param = new JSONObject();
    param.set("appUnionId", "innerBatchTask");
    param.set("productKey", h.getProductKey());
    param.set("deviceId", h.getDeviceId());
    param.set("cmd", DownCmd.DEV_FUNCTION.getValue());
    param.set("function", JSONUtil.parseObj(h.getCommandData()));
    param.set("applicationId", "innerBatchTask");
    // 转换为UnifiedDownlinkCommand（保持所有参数）
    UnifiedDownlinkCommand command = UnifiedDownlinkCommand.fromJson(param);
    R r = IoTDownlFactory.getIDown(h.getThirdPlatform()).doAction(command);
    IoTDeviceFunctionHistory history = BeanUtil.copyProperties(h, IoTDeviceFunctionHistory.class);
    history.setDownState(2);
    history.setDownResult(r.getCode() == 0 ? 1 : 0);
    history.setDownError(r.getMsg());
    history.setRetry(history.getRetry() + 1);
    history.setUpdateTime(new Date());
    ioTDeviceFunctionHistoryMapper.updateByPrimaryKeySelective(history);
  }
}

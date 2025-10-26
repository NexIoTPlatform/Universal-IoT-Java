/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.dm.device.service.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.IoTDeviceLifeCycle;
import cn.universal.persistence.dto.IoTDeviceLogMaxStorageTime;
import cn.universal.persistence.mapper.IoTDeviceLogMapper;
import cn.universal.persistence.mapper.IoTDeviceLogMetadataMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 日志清除任务
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2023/2/15
 */
@Component
@Slf4j
public class IoTDeviceLogClearTask {

  private final String KEY = "job:devLogClear";

  @Value("${shard.log.table.number:1}")
  private int logTableNumber;

  @Resource private StringRedisTemplate stringRedisTemplate;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource(name = "ioTDeviceActionAfterService")
  private IoTDeviceLifeCycle ioTDeviceLifeCycle;

  @Resource private IoTDeviceService iotDeviceService;

  @Resource private IoTDeviceLogMapper ioTDeviceLogMapper;

  @Resource private IoTDeviceLogMetadataMapper ioTDeviceLogMetadataMapper;

  /** 加载第三方平台的生命周期支持情况 */
  private Map<String, Set<String>> lifecycleSupportedConfig = new HashMap<>();

  @Scheduled(cron = "0 10 1 * *  ? ")
  public void doLogDelWithOutCondition() {
    Long t1 = System.currentTimeMillis();

    try {
      // 分布式锁,只执行1次，必须设置失效时间
      boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(KEY, "0", 5, TimeUnit.MINUTES);
      if (!flag) {
        return;
      }
      log.info("开始执行日志清除扫描");

      int days = IoTConstant.DEFAULT_LOG_MAX_STORAGE_TIME;
      // 计算出超过过期的unix时间戳
      DateTime offsetDay = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, -days);
      Long time = offsetDay.getTime() / 1000L;
      // 此处tablesIndex=-1 是因为表名问题
      for (int tablesIndex = -1; tablesIndex < logTableNumber; tablesIndex++) {
        // 开始删除iot_device_log表
        Long id = ioTDeviceLogMapper.queryLogIdByTime(tablesIndex, time);
        int total = 0;
        if (id != null && id > 0L) {
          int count;
          do {
            count = ioTDeviceLogMapper.deleteLogById(tablesIndex, id);
            total += count;
          } while (count != 0);
          log.info("iot_device_log 任务清除,表index={},条数={},节点时间={}", tablesIndex, total, offsetDay);
        }
        // 开始删除 iot_device_log_metadata 表
        Long metaId = ioTDeviceLogMetadataMapper.queryLogMetaIdByTime(tablesIndex, time);
        int totalMeta = 0;
        if (metaId != null && metaId > 0L) {
          int metaCount;
          do {
            metaCount = ioTDeviceLogMetadataMapper.deleteLogMetaById(tablesIndex, metaId);
            totalMeta += metaCount;
          } while (metaCount != 0);
          log.info(
              "iot_device_log_metadata 任务清除,表index={},条数={},节点时间={}",
              tablesIndex,
              totalMeta,
              offsetDay);
        }
      }
    } catch (Exception e) {
      log.error("日志清除任务执行异常={}", e);
    }
    Long t2 = System.currentTimeMillis();

    log.info("结束执行日志清除扫描,耗时={}", (t2 - t1) / 1000L);
  }

  /** 暂时停用 */
  //  @Scheduled(cron = "0 30 2 * *  ? ")
  public void doLogDel() {
    try {
      // 分布式锁,只执行1次，必须设置失效时间
      boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(KEY, "0", 5, TimeUnit.MINUTES);
      if (!flag) {
        return;
      }
      log.info("开始执行日志清除扫描");
      // 查询所有产品的列表
      List<IoTDeviceLogMaxStorageTime> logMaxStorageList =
          iotProductDeviceService.getProductLogMaxStorage();
      if (CollectionUtil.isNotEmpty(logMaxStorageList)) {
        // 开始按照不同产品
        for (IoTDeviceLogMaxStorageTime s : logMaxStorageList) {
          String productKey = s.getProductKey();
          int days = s.getDays();
          // 计算出超过过期的unix时间戳
          DateTime offsetDay = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, -days);
          Long time = offsetDay.getTime() / 1000L;
          // 此处tablesIndex=-1 是因为表名问题
          for (int tablesIndex = -1; tablesIndex < logTableNumber; tablesIndex++) {
            // 开始删除iot_device_log表
            Long id =
                ioTDeviceLogMapper.queryLogIdByProductKeyAndTime(tablesIndex, productKey, time);
            if (id != null && id > 0L) {
              int count = ioTDeviceLogMapper.deleteLogByTask(tablesIndex, productKey, id);
              log.info(
                  "iot_device_log 任务清除,表index={},productKey={},条数={},节点时间={}",
                  tablesIndex,
                  productKey,
                  count,
                  offsetDay);
            }
            // 开始删除 iot_device_log_metadata 表
            Long metaId =
                ioTDeviceLogMetadataMapper.queryLogMetaIdByProductKeyAndTime(
                    tablesIndex, productKey, time);
            if (metaId != null && metaId > 0L) {
              int metaCount =
                  ioTDeviceLogMetadataMapper.deleteLogMetaByTask(tablesIndex, productKey, metaId);
              log.info(
                  "iot_device_log_metadata 任务清除,表index={},productKey={},条数={},节点时间={}",
                  tablesIndex,
                  productKey,
                  metaCount,
                  offsetDay);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("日志清除任务执行异常={}", e);
    }
    log.info("结束执行日志清除扫描");
  }
}

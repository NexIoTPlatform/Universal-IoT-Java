/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 *
 * @Author: gitee.com/NexIoT
 *

 *
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.dm.device.service.log;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.StrUtil;
import cn.universal.common.constant.IoTConstant;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.query.LogQuery;
import cn.universal.persistence.query.PageBean;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/9/23
 */
@Component
@Slf4j
public class DefaultIoTDeviceLogService implements IIoTDeviceDataService, ApplicationContextAware {

  @Resource private IoTDeviceMapper ioTDeviceMapper;

  @Resource private IoTProductDeviceService iotProductDeviceService;
  private Map<String, IIoTDeviceLogService> policies = new ConcurrentHashMap<>();

  @Autowired
  @Qualifier("virtualScheduledExecutor")
  private ScheduledExecutorService scheduledExecutorService;

  private AtomicBoolean scheduled = new AtomicBoolean();

  private static final int LOG_MAX_SIZE = 1000;

  private static final long LOG_OUTPUT_INTERVAL = 12;
  //
  // private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(4,
  // new NamedThreadFactory("univ-platform-log", true));

  private Set<String> devBoSet = new ConcurrentHashSet<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    Map<String, IIoTDeviceLogService> beansOfType =
        applicationContext.getBeansOfType(IIoTDeviceLogService.class);
    beansOfType.forEach(
        (k, v) -> {
          policies.put(v.getPolicy().toLowerCase(), v);
        });
    if (scheduled.compareAndSet(false, true)) {
      scheduledExecutorService.scheduleWithFixedDelay(
          this::doOnline, LOG_OUTPUT_INTERVAL, LOG_OUTPUT_INTERVAL, TimeUnit.SECONDS);
    }
  }

  @Override
  public void saveDeviceLog(
      BaseUPRequest upRequest, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {
    IIoTDeviceLogService logService =
        policies.getOrDefault(ioTProduct.getStorePolicy(), policies.get("none"));
    logService.saveDeviceLog(upRequest, ioTDeviceDTO, ioTProduct);
    // 如果是设备真实上报数据或事件，处理在线状态
    if (!IoTConstant.DevNotReallyReportEvent.contains(upRequest.getEvent())
        && MessageType.devReallyReport(upRequest.getMessageType())) {
      updateOnline(ioTDeviceDTO.getIotId());
    }
  }

  @Override
  public void saveDeviceLog(
      IoTDeviceLog ioTDeviceLog, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {
    IIoTDeviceLogService logService =
        policies.getOrDefault(ioTProduct.getStorePolicy(), policies.get("none"));
    logService.saveDeviceLog(ioTDeviceLog, ioTDeviceDTO, ioTProduct);
    // 如果是设备真实上报数据或事件，处理在线状态
    if (!IoTConstant.DevNotReallyReportEvent.contains(ioTDeviceLog.getEvent())
        && MessageType.devReallyReport(MessageType.find(ioTDeviceLog.getMessageType()))) {
      updateOnline(ioTDeviceDTO.getIotId());
    }
  }

  @Override
  public PageBean<IoTDeviceLogVO> pageList(LogQuery logQuery) {
    IoTProduct product = iotProductDeviceService.getProduct(logQuery.getProductKey());
    IIoTDeviceLogService logService =
        policies.getOrDefault(product.getStorePolicy(), policies.get("none"));
    PageBean<IoTDeviceLogVO> ioTDeviceLogVOPageBean = logService.pageList(logQuery);
    if (CollectionUtil.isNotEmpty(ioTDeviceLogVOPageBean.getList())) {
      ioTDeviceLogVOPageBean.getList().stream()
          .forEach(
              ioTDeviceLogVO -> {
                if (ioTDeviceLogVO.getEvent() != null) {
                  String eventName =
                      iotProductDeviceService.getEventOrFunctionName(
                          ioTDeviceLogVO.getProductKey(), ioTDeviceLogVO.getEvent());
                  if (StrUtil.isNotBlank(eventName)) {
                    ioTDeviceLogVO.setEvent(ioTDeviceLogVO.getEvent() + " " + eventName);
                  }
                }
              });
    }
    return ioTDeviceLogVOPageBean;
  }

  @Override
  public IoTDeviceLogVO queryById(LogQuery logQuery) {
    IoTProduct product = iotProductDeviceService.getProduct(logQuery.getProductKey());
    IIoTDeviceLogService logService =
        policies.getOrDefault(product.getStorePolicy(), policies.get("none"));
    return logService.queryById(logQuery);
  }

  @Override
  public PageBean<IoTDeviceEvents> queryEventTotal(String productKey, String iotId) {
    IoTProduct product = iotProductDeviceService.getProduct(productKey);
    IIoTDeviceLogService logService =
        policies.getOrDefault(product.getStorePolicy(), policies.get("none"));
    return logService.queryEventTotal(productKey, iotId);
  }

  @Override
  public PageBean<IoTDeviceLogMetadataVO> queryLogMeta(LogQuery logQuery) {
    IoTProduct product = iotProductDeviceService.getProduct(logQuery.getProductKey());
    IIoTDeviceLogService logService =
        policies.getOrDefault(product.getStorePolicy(), policies.get("none"));
    return logService.queryLogMeta(logQuery);
  }

  private void updateOnline(String iotId) {
    if (devBoSet.size() < LOG_MAX_SIZE) {
      devBoSet.add(iotId);
    } else {
      log.warn("设备最后更新时间达到5000阈值");
      // just write current devBoSet to file.
      doOnline();
      // after force writing, add accessLogData to current devBoSet
      devBoSet.add(iotId);
    }
  }

  public void flushLogBatch(Set<String> iotIds) {
    if (iotIds == null || iotIds.isEmpty()) {
      return;
    }
    try {
      ioTDeviceMapper.batchFlushLog(iotIds);
      log.info("批量更新最后通信时间iotIds={}", iotIds);
    } catch (Exception e) {
      log.error("批量更新设备最后一次通信时间异常={}", e);
    }
  }

  private void doOnline() {
    // log.debug("开始进行设备最后在线时间处理");
    try {
      Set<String> snapshot;
      synchronized (devBoSet) {
        if (devBoSet.isEmpty()) {
          return;
        }
        snapshot = new HashSet<>(devBoSet);
        devBoSet.clear();
      }
      flushLogBatch(snapshot);
    } catch (Exception e) {
      log.error("更新设备最后一次通信时间进程异常={}", e);
    }
  }
}

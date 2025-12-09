/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 *
 * @Author: gitee.com/NexIoT
 *
 * @Email: wo8335224@gmail.com
 *
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.dm.device.service.log;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.RandomUtil;
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
 * @author gitee.com/NexIoT
 * @version 1.0
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
  // 尽量减少，让他走索引
  private static final int LOG_MAX_SIZE = 200;

  // 1. 基础间隔（固定12秒）
  private static final long BASE_INTERVAL = 12 * 1000;
  // 2. 节点级偏移范围（0~8秒，打散集群节点的基准时间）
  private static final int NODE_OFFSET_RANGE = 8000;
  // 3. 每次执行的随机抖动范围（±200ms，避免长期运行后趋同）
  private static final int RANDOM_JITTER_RANGE = 200;
  // 节点专属的固定偏移量（每个节点启动时生成一次）
  private long nodeFixedOffset;

  // 初始化节点偏移量（构造方法中执行，保证每个节点唯一）
  public DefaultIoTDeviceLogService() {
    // 生成0~8000ms的固定偏移量，不同节点启动时生成的值不同
    this.nodeFixedOffset = RandomUtil.randomInt(0, NODE_OFFSET_RANGE);
    log.info("当前节点定时任务基础偏移量：{}ms", nodeFixedOffset);
  }

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
      // 4. 初始延迟 = 节点固定偏移 + 小范围随机抖动（首次执行就打散）
      long initialDelay =
          nodeFixedOffset + RandomUtil.randomInt(-RANDOM_JITTER_RANGE, RANDOM_JITTER_RANGE);
      // 确保初始延迟不为负数
      initialDelay = Math.max(0, initialDelay);

      // 5. 循环间隔 = 基础间隔 + 每次随机抖动（避免固定间隔趋同）
      scheduledExecutorService.scheduleWithFixedDelay(
          this::doOnline,
          initialDelay, // 节点专属初始延迟，保证集群启动时不同时执行
          BASE_INTERVAL + RandomUtil.randomInt(-RANDOM_JITTER_RANGE, RANDOM_JITTER_RANGE),
          TimeUnit.MILLISECONDS);
      // 为集群的节点不在统一时间给数据库造成压力
      log.info(
          "当前节点定时任务已启动，初始延迟：{}ms，循环间隔：{}ms",
          initialDelay,
          BASE_INTERVAL + RandomUtil.randomInt(-RANDOM_JITTER_RANGE, RANDOM_JITTER_RANGE));
    }
  }

  @Override
  public void saveDeviceLog(
      BaseUPRequest upRequest, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {
    IIoTDeviceLogService logService =
        policies.getOrDefault(ioTProduct.getStorePolicy(), policies.get("none"));
    logService.saveDeviceLog(upRequest, ioTDeviceDTO, ioTProduct);
    // 如果是设备真实上报数据或事件，处理在线状态
    if (!IoTConstant.notDeviceDataReportEvent.contains(upRequest.getEvent())
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
    if (!IoTConstant.notDeviceDataReportEvent.contains(ioTDeviceLog.getEvent())
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

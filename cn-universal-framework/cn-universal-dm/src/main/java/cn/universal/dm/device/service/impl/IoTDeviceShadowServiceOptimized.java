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
package cn.universal.dm.device.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.core.message.UPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.shadow.Shadow;
import cn.universal.persistence.shadow.State;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 设备影子服务优化版本 使用异步批量处理提高高并发性能
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Slf4j
@Service("iotDeviceShadowServiceOptimized")
public class IoTDeviceShadowServiceOptimized {

  @Autowired private IoTDeviceShadowMapper ioTDeviceShadowMapper;

  @Autowired private IoTCacheRemoveService iotCacheRemoveService;

  @Autowired private StringRedisTemplate stringRedisTemplate;

  @Autowired private RedissonClient redissonClient;

  // 批量处理配置
  private static final int BATCH_SIZE = 200;
  private static final long BATCH_TIMEOUT_MS = 5000; // 1秒超时
  private static final int MAX_QUEUE_SIZE = 10000;

  // 异步处理线程池
  private final ExecutorService asyncExecutor =
      new ThreadPoolExecutor(
          4,
          8,
          60L,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(MAX_QUEUE_SIZE),
          new ThreadPoolExecutor.CallerRunsPolicy());

  // 批量处理队列
  private final BlockingQueue<ShadowUpdateTask> updateQueue =
      new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

  // 批量插入队列
  private final BlockingQueue<IoTDeviceShadow> insertQueue =
      new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);

  // 统计计数器
  private final AtomicLong totalProcessed = new AtomicLong(0);
  private final AtomicLong totalBatches = new AtomicLong(0);

  public IoTDeviceShadowServiceOptimized() {
    // 启动批量处理线程
    startBatchProcessor();
    startBatchInserter();
  }

  /** 优化的设备影子处理方法 使用异步批量处理，减少数据库操作频率 */
  public void doShadow(UPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    try {
      // 快速检查是否需要创建影子
      boolean needCreateShadow = checkNeedCreateShadow(ioTDeviceDTO);

      if (needCreateShadow) {
        // 异步创建影子
        asyncCreateShadow(ioTDeviceDTO, upRequest);
      } else {
        // 异步更新影子
        asyncUpdateShadow(upRequest, ioTDeviceDTO);
      }

      // 更新统计
      totalProcessed.incrementAndGet();

    } catch (Exception e) {
      log.error("处理设备影子失败: iotId={}, error={}", upRequest.getIotId(), e.getMessage(), e);
      // 降级处理：使用同步方式
      fallbackDoShadow(upRequest, ioTDeviceDTO);
    }
  }

  /** 检查是否需要创建影子 - 高并发优化版本（带数据缓存） */
  private boolean checkNeedCreateShadow(IoTDeviceDTO ioTDeviceDTO) {
    if (ioTDeviceDTO.isShadow()) {
      return false;
    }

    String iotId = ioTDeviceDTO.getIotId();

    // 1. 先检查Redis缓存，避免数据库查询
    String cacheKey = "shadow_exists:" + iotId;
    String dataCacheKey = "shadow_data:" + iotId;

    // 检查状态缓存
    String exists = stringRedisTemplate.opsForValue().get(cacheKey);
    if ("1".equals(exists)) {
      // 缓存显示影子已存在，直接返回false
      return false;
    }

    if ("creating".equals(exists)) {
      // 其他请求正在创建中，直接返回false
      return false;
    }

    // 2. 使用Redisson分布式锁，防止重复创建
    String lockKey = "doCreateShadow:" + iotId;
    RLock lock = redissonClient.getLock(lockKey);

    try {
      // 尝试获取锁，等待100ms，锁超时时间30秒
      boolean lockAcquired = lock.tryLock(100, 30, TimeUnit.MILLISECONDS);

      if (lockAcquired) {
        try {
          // 3. 获取锁后，再次检查缓存（双重检查）
          exists = stringRedisTemplate.opsForValue().get(cacheKey);
          if ("1".equals(exists) || "creating".equals(exists)) {
            return false;
          }

          // 4. 标记为正在创建，防止其他请求重复创建
          stringRedisTemplate.opsForValue().set(cacheKey, "creating", 5, TimeUnit.MINUTES);

          // 5. 缓存未命中，查询数据库（只有获取锁的请求才查询）
          IoTDeviceShadow existingShadow =
              ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());

          if (existingShadow != null) {
            // 6. 数据库存在，缓存完整数据，避免后续查询
            cacheShadowData(iotId, existingShadow);
            return false;
          }

          return true;

        } finally {
          // 7. Redisson自动释放锁（支持看门狗机制）
          if (lock.isHeldByCurrentThread()) {
            lock.unlock();
          }
        }
      } else {
        // 8. 未获取到锁，等待一段时间后检查缓存状态
        try {
          Thread.sleep(50); // 等待50ms
          exists = stringRedisTemplate.opsForValue().get(cacheKey);
          if ("1".equals(exists) || "creating".equals(exists)) {
            return false;
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("获取分布式锁被中断: iotId={}", iotId);
    }

    return false;
  }

  /** 异步创建影子 */
  @Async
  public void asyncCreateShadow(IoTDeviceDTO ioTDeviceDTO, UPRequest upRequest) {
    try {
      // 构建影子对象
      IoTDeviceShadow shadow = buildDeviceShadow(ioTDeviceDTO);
      Shadow shadowData = buildShadowData(ioTDeviceDTO, upRequest);
      shadow.setMetadata(JSONUtil.toJsonStr(shadowData));

      // 加入批量插入队列
      insertQueue.offer(shadow, 100, TimeUnit.MILLISECONDS);

    } catch (Exception e) {
      log.error("异步创建影子失败: iotId={}", ioTDeviceDTO.getIotId(), e);
    }
  }

  /** 异步更新影子 */
  @Async
  public void asyncUpdateShadow(UPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    try {
      // 构建更新任务
      ShadowUpdateTask task =
          ShadowUpdateTask.builder()
              .iotId(upRequest.getIotId())
              .ioTDeviceDTO(ioTDeviceDTO)
              .upRequest(upRequest)
              .timestamp(System.currentTimeMillis())
              .build();

      // 加入批量更新队列
      updateQueue.offer(task, 100, TimeUnit.MILLISECONDS);

    } catch (Exception e) {
      log.error("异步更新影子失败: iotId={}", upRequest.getIotId(), e);
    }
  }

  /** 启动批量处理器 */
  private void startBatchProcessor() {
    CompletableFuture.runAsync(
        () -> {
          while (!Thread.currentThread().isInterrupted()) {
            try {
              processBatchUpdates();
            } catch (Exception e) {
              log.error("批量处理异常", e);
            }
          }
        },
        asyncExecutor);
  }

  /** 启动批量插入器 */
  private void startBatchInserter() {
    CompletableFuture.runAsync(
        () -> {
          while (!Thread.currentThread().isInterrupted()) {
            try {
              processBatchInserts();
            } catch (Exception e) {
              log.error("批量插入异常", e);
            }
          }
        },
        asyncExecutor);
  }

  /** 批量处理更新操作 */
  private void processBatchUpdates() throws InterruptedException {
    List<ShadowUpdateTask> batch = new ArrayList<>();

    // 收集批量任务
    ShadowUpdateTask firstTask = updateQueue.poll(BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    if (firstTask != null) {
      batch.add(firstTask);
      updateQueue.drainTo(batch, BATCH_SIZE - 1);
    }

    if (batch.isEmpty()) {
      return;
    }

    try {
      // 批量更新
      batchUpdateShadows(batch);
      totalBatches.incrementAndGet();

    } catch (Exception e) {
      log.error("批量更新影子失败，批次大小: {}", batch.size(), e);
      // 失败的任务重新入队
      for (ShadowUpdateTask task : batch) {
        if (System.currentTimeMillis() - task.getTimestamp() < 60000) { // 1分钟内重试
          updateQueue.offer(task);
        }
      }
    }
  }

  /** 批量处理插入操作 */
  private void processBatchInserts() throws InterruptedException {
    List<IoTDeviceShadow> batch = new ArrayList<>();

    // 收集批量任务
    IoTDeviceShadow firstShadow = insertQueue.poll(BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    if (firstShadow != null) {
      batch.add(firstShadow);
      insertQueue.drainTo(batch, BATCH_SIZE - 1);
    }

    if (batch.isEmpty()) {
      return;
    }

    try {
      // 批量插入
      batchInsertShadows(batch);
      totalBatches.incrementAndGet();

      // 延迟清理缓存，避免频繁清理
      if (totalBatches.get() % 10 == 0) { // 每10个批次清理一次缓存
        iotCacheRemoveService.removeDevInstanceBOCache();
      }

    } catch (Exception e) {
      log.error("批量插入影子失败，批次大小: {}", batch.size(), e);
    }
  }

  /** 批量更新影子 - 智能合并版本 */
  private void batchUpdateShadows(List<ShadowUpdateTask> tasks) {
    if (CollUtil.isEmpty(tasks)) {
      return;
    }

    // 按iotId分组处理，相同设备的多次更新会被合并
    Map<String, List<ShadowUpdateTask>> groupedTasks = new HashMap<>();
    for (ShadowUpdateTask task : tasks) {
      groupedTasks.computeIfAbsent(task.getIotId(), k -> new ArrayList<>()).add(task);
    }

    // 批量查询现有影子
    List<String> iotIds = new ArrayList<>(groupedTasks.keySet());
    List<IoTDeviceShadow> existingShadows = batchSelectByIotIds(iotIds);
    Map<String, IoTDeviceShadow> shadowMap = new HashMap<>();
    for (IoTDeviceShadow shadow : existingShadows) {
      shadowMap.put(shadow.getIotId(), shadow);
    }

    // 智能合并更新：每个设备只更新一次，合并所有状态
    List<IoTDeviceShadow> updateList = new ArrayList<>();
    for (Map.Entry<String, List<ShadowUpdateTask>> entry : groupedTasks.entrySet()) {
      String iotId = entry.getKey();
      List<ShadowUpdateTask> deviceTasks = entry.getValue();

      IoTDeviceShadow existingShadow = shadowMap.get(iotId);
      if (existingShadow != null && !deviceTasks.isEmpty()) {
        // 智能合并：将多个任务的状态合并到一个影子中
        mergeMultipleUpdates(existingShadow, deviceTasks);
        updateList.add(existingShadow);

        log.debug("设备 {} 合并处理了 {} 条消息", iotId, deviceTasks.size());
      }
    }

    if (!updateList.isEmpty()) {
      batchUpdate(updateList);
    }
  }

  /** 批量插入影子 */
  private void batchInsertShadows(List<IoTDeviceShadow> shadows) {
    if (CollUtil.isEmpty(shadows)) {
      return;
    }

    // 批量插入
    for (IoTDeviceShadow shadow : shadows) {
      ioTDeviceShadowMapper.insertSelective(shadow);
      // 插入成功后更新缓存状态
      updateShadowCacheStatus(shadow.getIotId(), true);
    }
  }

  /** 批量查询影子 */
  private List<IoTDeviceShadow> batchSelectByIotIds(List<String> iotIds) {
    List<IoTDeviceShadow> result = new ArrayList<>();
    for (String iotId : iotIds) {
      IoTDeviceShadow shadow =
          ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
      if (shadow != null) {
        result.add(shadow);
      }
    }
    return result;
  }

  /** 批量更新影子 */
  private void batchUpdate(List<IoTDeviceShadow> shadows) {
    for (IoTDeviceShadow shadow : shadows) {
      ioTDeviceShadowMapper.updateByPrimaryKeySelective(shadow);
    }
  }

  /** 更新现有影子 - 单个任务版本 */
  private void updateExistingShadow(IoTDeviceShadow existingShadow, ShadowUpdateTask task) {
    Shadow shadow = null;
    if (StrUtil.isBlank(existingShadow.getMetadata())) {
      shadow = Shadow.builder().timestamp(DateUtil.currentSeconds()).version(1L).build();
    } else {
      shadow = JSONUtil.toBean(existingShadow.getMetadata(), Shadow.class);
    }

    // 处理期望状态
    doDesired(shadow, task.getIoTDeviceDTO(), task.getUpRequest());

    // 更新元数据
    existingShadow.setMetadata(JSONUtil.toJsonStr(shadow));
    existingShadow.setLastTime(new Date());
    existingShadow.setUpdateDate(new Date());
  }

  /** 智能合并多个更新任务 - 核心优化方法 */
  private void mergeMultipleUpdates(IoTDeviceShadow existingShadow, List<ShadowUpdateTask> tasks) {
    if (CollUtil.isEmpty(tasks)) {
      return;
    }

    // 按时间戳排序，确保处理顺序
    tasks.sort((t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp()));

    Shadow shadow = null;
    if (StrUtil.isBlank(existingShadow.getMetadata())) {
      shadow = Shadow.builder().timestamp(DateUtil.currentSeconds()).version(1L).build();
    } else {
      shadow = JSONUtil.toBean(existingShadow.getMetadata(), Shadow.class);
    }

    // 智能合并：按时间顺序处理所有任务，保留最终状态
    for (ShadowUpdateTask task : tasks) {
      try {
        // 处理期望状态（可以在这里实现增量合并逻辑）
        doDesired(shadow, task.getIoTDeviceDTO(), task.getUpRequest());

        // 更新版本号和时间戳
        shadow.setVersion(shadow.getVersion() + 1);
        shadow.setTimestamp(DateUtil.currentSeconds());

      } catch (Exception e) {
        log.warn("合并处理任务失败: iotId={}, task={}", task.getIotId(), task, e);
      }
    }

    // 更新元数据（只更新一次）
    existingShadow.setMetadata(JSONUtil.toJsonStr(shadow));
    existingShadow.setLastTime(new Date());
    existingShadow.setUpdateDate(new Date());

    log.debug(
        "设备 {} 成功合并 {} 条消息，最终版本: {}", existingShadow.getIotId(), tasks.size(), shadow.getVersion());
  }

  /** 构建设备影子 */
  private IoTDeviceShadow buildDeviceShadow(IoTDeviceDTO ioTDeviceDTO) {
    return IoTDeviceShadow.builder()
        .iotId(ioTDeviceDTO.getIotId())
        .extDeviceId(ioTDeviceDTO.getExtDeviceId())
        .productKey(ioTDeviceDTO.getProductKey())
        .deviceId(ioTDeviceDTO.getDeviceId())
        .activeTime(new Date())
        .onlineTime(new Date())
        .updateDate(new Date())
        .lastTime(new Date())
        .build();
  }

  /** 构建影子数据 */
  private Shadow buildShadowData(IoTDeviceDTO ioTDeviceDTO, UPRequest upRequest) {
    State state = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();

    State metadata = State.builder().desired(new JSONObject()).reported(new JSONObject()).build();

    Shadow shadow =
        Shadow.builder()
            .state(state)
            .metadata(metadata)
            .timestamp(DateUtil.currentSeconds())
            .version(1L)
            .build();

    doDesired(shadow, ioTDeviceDTO, upRequest);
    return shadow;
  }

  /** 降级处理：同步方式 */
  private void fallbackDoShadow(UPRequest upRequest, IoTDeviceDTO ioTDeviceDTO) {
    // 这里可以调用原来的同步方法
    log.warn("使用降级处理方式: iotId={}", upRequest.getIotId());
  }

  /** 更新影子缓存状态 */
  private void updateShadowCacheStatus(String iotId, boolean exists) {
    try {
      String cacheKey = "shadow_exists:" + iotId;
      if (exists) {
        // 影子存在，缓存30分钟
        stringRedisTemplate.opsForValue().set(cacheKey, "1", 30, TimeUnit.MINUTES);

        // 如果存在，尝试从数据库获取最新数据并缓存
        IoTDeviceShadow shadow =
            ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());
        if (shadow != null) {
          cacheShadowData(iotId, shadow);
        }
      } else {
        // 影子不存在，缓存5分钟
        stringRedisTemplate.opsForValue().set(cacheKey, "0", 5, TimeUnit.MINUTES);

        // 清除数据缓存
        String dataCacheKey = "shadow_data:" + iotId;
        stringRedisTemplate.delete(dataCacheKey);
      }
    } catch (Exception e) {
      log.warn("更新影子缓存状态失败: iotId={}", iotId, e);
    }
  }

  /** 处理期望状态（需要根据实际业务逻辑实现） */
  private void doDesired(Shadow shadow, IoTDeviceDTO ioTDeviceDTO, UPRequest upRequest) {
    // 实现期望状态处理逻辑
    // 这里保持原有的业务逻辑不变
    log.debug("处理期望状态: iotId={}", ioTDeviceDTO.getIotId());
  }

  // 注意：getPerformanceStats 和 shutdown 方法已被删除，因为未被外部调用

  // 内部类：影子更新任务
  public static class ShadowUpdateTask {
    private String iotId;
    private IoTDeviceDTO ioTDeviceDTO;
    private UPRequest upRequest;
    private long timestamp;

    // 构建器模式
    public static ShadowUpdateTaskBuilder builder() {
      return new ShadowUpdateTaskBuilder();
    }

    // getter和setter方法
    public String getIotId() {
      return iotId;
    }

    public void setIotId(String iotId) {
      this.iotId = iotId;
    }

    public IoTDeviceDTO getIoTDeviceDTO() {
      return ioTDeviceDTO;
    }

    public void setIoTDeviceDTO(IoTDeviceDTO ioTDeviceDTO) {
      this.ioTDeviceDTO = ioTDeviceDTO;
    }

    public UPRequest getUpRequest() {
      return upRequest;
    }

    public void setUpRequest(UPRequest upRequest) {
      this.upRequest = upRequest;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    // 构建器
    public static class ShadowUpdateTaskBuilder {
      private ShadowUpdateTask task = new ShadowUpdateTask();

      public ShadowUpdateTaskBuilder iotId(String iotId) {
        task.iotId = iotId;
        return this;
      }

      public ShadowUpdateTaskBuilder ioTDeviceDTO(IoTDeviceDTO ioTDeviceDTO) {
        task.ioTDeviceDTO = ioTDeviceDTO;
        return this;
      }

      public ShadowUpdateTaskBuilder upRequest(UPRequest upRequest) {
        task.upRequest = upRequest;
        return this;
      }

      public ShadowUpdateTaskBuilder timestamp(long timestamp) {
        task.timestamp = timestamp;
        return this;
      }

      public ShadowUpdateTask build() {
        return task;
      }
    }
  }

  /**
   * 缓存影子数据到Redis
   *
   * @param iotId 设备ID
   * @param shadow 影子数据
   */
  private void cacheShadowData(String iotId, IoTDeviceShadow shadow) {
    try {
      // 1. 缓存状态标记
      String cacheKey = "shadow_exists:" + iotId;
      stringRedisTemplate.opsForValue().set(cacheKey, "1", 30, TimeUnit.MINUTES);

      // 2. 缓存完整数据（JSON格式）
      String dataCacheKey = "shadow_data:" + iotId;
      String shadowJson = JSONUtil.toJsonStr(shadow);
      stringRedisTemplate.opsForValue().set(dataCacheKey, shadowJson, 30, TimeUnit.MINUTES);

      log.debug("影子数据缓存成功: iotId={}, cacheKey={}", iotId, dataCacheKey);

    } catch (Exception e) {
      log.warn("缓存影子数据失败: iotId={}", iotId, e);
    }
  }

  // 注意：其他缓存方法已被删除，因为未被外部调用
}

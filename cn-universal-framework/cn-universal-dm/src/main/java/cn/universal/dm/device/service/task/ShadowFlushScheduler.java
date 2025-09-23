package cn.universal.dm.device.service.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.config.InstanceIdProvider;
import cn.universal.dm.device.util.DistributedLockUtil;
import cn.universal.persistence.entity.IoTDevice;
import cn.universal.persistence.entity.IoTDeviceShadow;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import cn.universal.persistence.mapper.IoTDeviceShadowMapper;
import cn.universal.persistence.query.IoTDeviceQuery;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 设备影子定时缓存
 *
 * <p>Periodically flush device shadows from Redis to DB. Uses Redis ZSET (score = nextFlushAtMs)
 * and Redisson distributed lock.
 */
@Slf4j
@Component
public class ShadowFlushScheduler {

  @Resource private StringRedisTemplate stringRedisTemplate;
  @Resource private IoTDeviceShadowMapper ioTDeviceShadowMapper;
  @Resource private IoTDeviceMapper ioTDeviceMapper;
  @Resource private DistributedLockUtil distributedLockUtil;

  @Value("${shadow.cache.enabled:true}")
  private boolean shadowCacheEnabled;

  @Value("${shadow.cache.key-prefix:shadow}")
  private String shadowKeyPrefix;

  @Value("${shadow.flush.enabled:true}")
  private boolean flushEnabled;

  @Value("${shadow.flush.zset-key:shadow:flush}")
  private String shadowFlushZsetKey;

  @Value("${shadow.flush.scan-interval-ms:300000}") // 5 minutes
  private long scanIntervalMs;

  @Value("${shadow.flush.batch-size:1000}")
  private int batchSize;

  @Value("${shadow.flush.lock-key:shadow:flush:lock}")
  private String lockKey;

  @Value("${shadow.flush.lock-wait-time:10}")
  private long lockWaitTime;

  @Value("${shadow.flush.lock-lease-time:60}")
  private long lockLeaseTime;

  @Value("${shadow.flush.max-retries:3}")
  private int maxRetries;

  @Resource private InstanceIdProvider instanceIdProvider;

  @Scheduled(fixedDelayString = "${shadow.flush.scan-interval-ms:300000}")
  public void flushDueShadows() {
    if (!shadowCacheEnabled || !flushEnabled) {
      log.debug(
          "[ShadowFlush] 影子刷盘已禁用: shadowCacheEnabled={}, flushEnabled={}",
          shadowCacheEnabled,
          flushEnabled);
      return;
    }

    long now = System.currentTimeMillis();
    String instanceId = instanceIdProvider.getInstanceId();
    log.info("[ShadowFlush] 开始设备影子扫描: scanTime={}, instanceId={}", now, instanceId);

    // 使用分布式锁工具类执行任务，确保集群环境下只有一个实例执行
    Integer result =
        distributedLockUtil.tryLockAndExecute(
            lockKey, lockWaitTime, lockLeaseTime, TimeUnit.SECONDS, this::executeFlushTask);

    if (result != null) {
      log.info(
          "[ShadowFlush] 影子扫描完成: processed={}, cost={}ms, instanceId={}",
          result,
          (System.currentTimeMillis() - now),
          instanceId);
    } else {
      log.debug("[ShadowFlush] 获取分布式锁失败，跳过本次扫描: instanceId={}", instanceId);
    }
  }

  private String buildShadowKey(String iotId) {
    return shadowKeyPrefix + ":" + iotId;
  }

  /** 获取缓存数据 */
  private Map<String, String> getCacheData(Set<String> iotIds) {
    Map<String, String> result = new HashMap<>();

    for (String iotId : iotIds) {
      try {
        String cacheJson = stringRedisTemplate.opsForValue().get(buildShadowKey(iotId));
        if (StrUtil.isNotBlank(cacheJson)) {
          result.put(iotId, cacheJson);
        }
      } catch (Exception e) {
        log.warn("[ShadowFlush] get cache error iotId={}, err={}", iotId, e.getMessage());
      }
    }

    return result;
  }

  /** 处理单个设备影子 - 带重试机制 */
  private boolean processShadowWithRetry(String iotId, String cacheJson) {
    for (int retry = 0; retry < maxRetries; retry++) {
      try {
        return processShadow(iotId, cacheJson);
      } catch (Exception e) {
        if (retry == maxRetries - 1) {
          log.error("[ShadowFlush] final retry failed iotId={}, err={}", iotId, e.getMessage());
          return false;
        } else {
          log.warn("[ShadowFlush] retry {} failed iotId={}, err={}", retry, iotId, e.getMessage());
          try {
            Thread.sleep(1000); // 短暂延迟后重试
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
          }
        }
      }
    }
    return false;
  }

  /** 处理单个设备影子 */
  private boolean processShadow(String iotId, String cacheJson) {
    IoTDeviceShadow existed =
        ioTDeviceShadowMapper.selectOne(IoTDeviceShadow.builder().iotId(iotId).build());

    if (existed == null) {
      IoTDeviceShadow entity = new IoTDeviceShadow();
      entity.setIotId(iotId);
      entity.setMetadata(cacheJson);
      entity.setUpdateDate(new Date());
      entity.setLastTime(new Date());
      entity.setActiveTime(new Date());
      ioTDeviceShadowMapper.insertSelective(entity);
    } else {
      String merged = mergeShadowJson(existed.getMetadata(), cacheJson);
      existed.setMetadata(merged);
      existed.setUpdateDate(new Date());
      existed.setLastTime(new Date());
      ioTDeviceShadowMapper.updateByPrimaryKeySelective(existed);
    }
    return true;
  }

  /** 批量处理设备影子 - 优化数据库操作 */
  private int processBatchShadows(List<String> iotIds, Map<String, String> cacheData) {
    if (CollUtil.isEmpty(iotIds)) {
      return 0;
    }
    try {
      // 批量查询已存在的记录
      List<IoTDeviceShadow> existingShadows = ioTDeviceShadowMapper.selectByIotIds(iotIds);
      Map<String, IoTDeviceShadow> existingMap =
          existingShadows.stream()
              .collect(Collectors.toMap(IoTDeviceShadow::getIotId, Function.identity()));

      // 分离需要插入和更新的记录
      List<IoTDeviceShadow> toInsert = new ArrayList<>();
      List<IoTDeviceShadow> toUpdate = new ArrayList<>();
      Date now = new Date();

      for (String iotId : iotIds) {
        String cacheJson = cacheData.get(iotId);
        if (StrUtil.isBlank(cacheJson)) {
          continue;
        }
        if (existingMap.containsKey(iotId)) {
          IoTDeviceShadow updated = buildUpdatedShadow(existingMap.get(iotId), cacheJson, now);
          toUpdate.add(updated);
        } else {
          IoTDeviceShadow created = buildNewShadow(iotId, cacheJson, now);
          if (created != null) {
            toInsert.add(created);
          }
        }
      }
      // 批量插入
      if (!toInsert.isEmpty()) {
        ioTDeviceShadowMapper.batchInsert(toInsert);
      }

      // 批量更新
      if (!toUpdate.isEmpty()) {
        ioTDeviceShadowMapper.batchUpdate(toUpdate);
      }

      return toInsert.size() + toUpdate.size();

    } catch (Exception e) {
      log.error("[ShadowFlush] batch process error: {}", e.getMessage(), e);
      // 回退到单个处理
      int processed = 0;
      for (String iotId : iotIds) {
        String cacheJson = cacheData.get(iotId);
        if (StrUtil.isNotBlank(cacheJson) && processShadowWithRetry(iotId, cacheJson)) {
          processed++;
        }
      }
      return processed;
    }
  }

  private IoTDeviceShadow buildUpdatedShadow(IoTDeviceShadow existed, String cacheJson, Date now) {
    if (existed == null) {
      return null;
    }
    String merged = mergeShadowJson(existed.getMetadata(), cacheJson);
    existed.setMetadata(merged);
    existed.setUpdateDate(now);
    existed.setLastTime(now);
    return existed;
  }

  private IoTDeviceShadow buildNewShadow(String iotId, String cacheJson, Date now) {
    IoTDeviceShadow entity = new IoTDeviceShadow();
    entity.setIotId(iotId);
    entity.setMetadata(cacheJson);
    entity.setUpdateDate(now);
    entity.setLastTime(now);
    entity.setActiveTime(now);
    try {
      IoTDeviceQuery query = new IoTDeviceQuery();
      query.setIotId(iotId);
      IoTDevice dev = ioTDeviceMapper.getOneByIotId(query);
      if (dev != null) {
        entity.setProductKey(dev.getProductKey());
        entity.setDeviceId(dev.getDeviceId());
        entity.setExtDeviceId(dev.getExtDeviceId());
        entity.setInstance(dev.getApplication());
        if (dev.getOnlineTime() != null) {
          entity.setOnlineTime(DateUtil.date(dev.getOnlineTime() * 1000));
        }
      }
    } catch (Exception ignore) {
    }

    return entity;
  }

  /**
   * 将数据库中的影子 JSON 与缓存中的影子 JSON 进行深度合并，避免覆盖丢失。 规则： - 对象与对象递归合并； - 非对象类型以 incoming 覆盖 existing； -
   * 数组整体覆盖； - 针对顶层的 timestamp/version，取二者较大值。
   */
  private String mergeShadowJson(String existingJson, String incomingJson) {
    if (StrUtil.isBlank(incomingJson)) {
      return StrUtil.blankToDefault(existingJson, "{}");
    }
    if (StrUtil.isBlank(existingJson)) {
      return incomingJson;
    }
    JSONObject existing;
    JSONObject incoming;
    try {
      existing = JSONUtil.parseObj(existingJson);
    } catch (Exception e) {
      existing = new JSONObject();
    }
    try {
      incoming = JSONUtil.parseObj(incomingJson);
    } catch (Exception e) {
      // 如果新数据都无法解析，直接返回旧数据
      log.warn("[ShadowFlush]新数据都无法解析，直接返回旧数据 error: {}", incomingJson, e);
      return existingJson;
    }

    JSONObject merged = deepMergeObject(existing, incoming);

    // 顶层特殊字段处理
    try {
      Long tsExisting = merged.getLong("timestamp");
      Long tsIncoming = incoming.getLong("timestamp");
      if (tsExisting != null || tsIncoming != null) {
        long maxTs =
            Math.max(tsExisting == null ? 0L : tsExisting, tsIncoming == null ? 0L : tsIncoming);
        merged.set("timestamp", maxTs);
      }
    } catch (Exception ignore) {
      log.warn("[ShadowFlush顶层特殊字段处理] merge shadow json error: {}", incomingJson, ignore);
    }
    try {
      Long verExisting = merged.getLong("version");
      Long verIncoming = incoming.getLong("version");
      if (verExisting != null || verIncoming != null) {
        long maxVer =
            Math.max(
                verExisting == null ? 0L : verExisting, verIncoming == null ? 0L : verIncoming);
        merged.set("version", maxVer);
      }
    } catch (Exception ignore) {
      log.warn("[ShadowFlush] merge shadow json error: {}", incomingJson);
    }

    return JSONUtil.toJsonStr(merged);
  }

  /** 对 JSONObject 进行递归深度合并 */
  private JSONObject deepMergeObject(JSONObject base, JSONObject incoming) {
    if (base == null) {
      return incoming == null ? new JSONObject() : incoming;
    }
    if (incoming == null) {
      return base;
    }
    JSONObject result = JSONUtil.parseObj(base.toString());
    for (String key : incoming.keySet()) {
      Object inVal = incoming.get(key);
      Object baseVal = result.get(key);

      if (inVal instanceof JSONObject && baseVal instanceof JSONObject) {
        result.set(key, deepMergeObject((JSONObject) baseVal, (JSONObject) inVal));
      } else {
        // 数组或标量：直接覆盖
        result.set(key, inVal);
      }
    }
    return result;
  }

  /** 执行影子刷新任务 */
  private Integer executeFlushTask() {
    long start = System.currentTimeMillis();
    int processed = 0;
    String instanceId = instanceIdProvider.getInstanceId();

    try {
      long now = System.currentTimeMillis();

      // 检查ZSet中总共有多少待刷盘的设备
      Long totalCount =
          stringRedisTemplate
              .opsForZSet()
              .count(shadowFlushZsetKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
      log.debug("[ShadowFlush] ZSet中待刷盘设备总数: {}, instanceId={}", totalCount, instanceId);

      // fetch due iotIds by score <= now
      Set<String> dueIds =
          stringRedisTemplate
              .opsForZSet()
              .rangeByScore(
                  shadowFlushZsetKey, Double.NEGATIVE_INFINITY, (double) now, 0, batchSize);

      if (dueIds == null || dueIds.isEmpty()) {
        log.debug("[ShadowFlush] 没有到期的设备需要刷盘: now={}, instanceId={}", now, instanceId);
        return 0;
      }

      log.info("[ShadowFlush] 发现{}个到期设备需要刷盘: {}, instanceId={}", dueIds.size(), dueIds, instanceId);

      // 获取缓存数据
      Map<String, String> cacheData = getCacheData(dueIds);
      if (cacheData.isEmpty()) {
        log.warn("[ShadowFlush] 缓存数据为空，跳过刷盘: dueIds={}, instanceId={}", dueIds, instanceId);
        return 0;
      }

      // 使用原子操作从ZSet中移除已获取的设备ID，防止集群环境下的重复处理
      List<String> validIotIds = new ArrayList<>(cacheData.keySet());
      Long removedCount =
          stringRedisTemplate.opsForZSet().remove(shadowFlushZsetKey, validIotIds.toArray());

      if (removedCount == null || removedCount == 0L) {
        log.warn(
            "[ShadowFlush] 从ZSet中移除设备失败，可能被其他实例处理: validIotIds={}, instanceId={}",
            validIotIds,
            instanceId);
        return 0;
      }

      log.info(
          "[ShadowFlush] 从ZSet中移除{}个设备: {}, instanceId={}", removedCount, validIotIds, instanceId);

      // 批量处理设备影子
      processed = processBatchShadows(validIotIds, cacheData);

      log.info(
          "[ShadowFlush] 刷盘任务完成: processed={}, cost={}ms, validIotIds={}, instanceId={}",
          processed,
          (System.currentTimeMillis() - start),
          validIotIds,
          instanceId);

      return processed;

    } catch (Exception e) {
      log.error("[ShadowFlush] 刷盘任务异常: instanceId={}, error={}", instanceId, e.getMessage(), e);
      return 0;
    }
  }

  /** 测试Redisson分布式锁是否正常工作 可以通过API调用此方法进行测试 */
  public String testDistributedLock() {
    String testLockKey = "test:shadow:flush:lock";

    return distributedLockUtil.tryLockAndExecute(
        testLockKey,
        5, // 等待5秒
        10, // 持有10秒
        TimeUnit.SECONDS,
        () -> {
          log.info("[ShadowFlush] 测试锁获取成功，当前时间: {}", DateUtil.now());
          try {
            Thread.sleep(2000); // 模拟处理时间
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          return "锁测试成功，处理完成";
        });
  }

  /**
   * 检查强制刷盘状态 - 用于调试
   *
   * @param iotId 设备ID
   * @return 强制刷盘状态信息
   */
  public String checkForceFlushStatus(String iotId) {
    try {
      // 检查ZSet中的刷盘时间
      Double score = stringRedisTemplate.opsForZSet().score(shadowFlushZsetKey, iotId);
      if (score == null) {
        return "设备" + iotId + "不在刷盘队列中";
      }

      long flushTime = score.longValue();
      long now = System.currentTimeMillis();
      long timeUntilFlush = flushTime - now;

      // 检查缓存数据
      String cacheKey = buildShadowKey(iotId);
      String cacheData = stringRedisTemplate.opsForValue().get(cacheKey);
      boolean hasCacheData = StrUtil.isNotBlank(cacheData);

      return String.format(
          "设备%s刷盘状态: 刷盘时间=%d, 当前时间=%d, 距离刷盘=%dms, 有缓存数据=%s",
          iotId, flushTime, now, timeUntilFlush, hasCacheData);

    } catch (Exception e) {
      return "检查强制刷盘状态失败: " + e.getMessage();
    }
  }

  /**
   * 检查集群状态 - 用于调试和监控
   *
   * @return 集群状态信息
   */
  public String checkClusterStatus() {
    try {
      String instanceId = instanceIdProvider.getInstanceId();

      // 检查分布式锁状态
      boolean isLocked = distributedLockUtil.isLocked(lockKey);
      boolean isHeldByCurrentThread = distributedLockUtil.isHeldByCurrentThread(lockKey);

      // 检查ZSet状态
      Long totalCount =
          stringRedisTemplate
              .opsForZSet()
              .count(shadowFlushZsetKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
      Long dueCount =
          stringRedisTemplate
              .opsForZSet()
              .count(
                  shadowFlushZsetKey,
                  Double.NEGATIVE_INFINITY,
                  (double) System.currentTimeMillis());

      return String.format(
          "集群状态: 当前实例=%s, 锁状态=%s, 当前实例持有锁=%s, 待刷盘总数=%d, 到期数量=%d",
          instanceId, isLocked, isHeldByCurrentThread, totalCount, dueCount);

    } catch (Exception e) {
      return "检查集群状态失败: " + e.getMessage();
    }
  }

  /**
   * 手动触发刷盘 - 用于测试和紧急情况
   *
   * @return 处理结果
   */
  public String manualFlush() {
    try {
      String instanceId = instanceIdProvider.getInstanceId();
      log.info("[ShadowFlush] 手动触发刷盘: instanceId={}", instanceId);

      Integer result =
          distributedLockUtil.tryLockAndExecute(
              lockKey + ":manual", 10, 120, TimeUnit.SECONDS, this::executeFlushTask);

      if (result != null) {
        return String.format("手动刷盘完成: processed=%d, instanceId=%s", result, instanceId);
      } else {
        return String.format("手动刷盘失败: 获取锁失败, instanceId=%s", instanceId);
      }

    } catch (Exception e) {
      return "手动刷盘异常: " + e.getMessage();
    }
  }
}

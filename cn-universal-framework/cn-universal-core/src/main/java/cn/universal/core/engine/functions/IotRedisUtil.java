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

package cn.universal.core.engine.functions;

import cn.universal.core.engine.annotation.Comment;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IotRedisUtil {

  private final StringRedisTemplate redisTemplate;
  private static final String DEVICE_PREFIX = "univiot:device:";

  public IotRedisUtil(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  // ==================== 基础数据结构操作（带过期时间）====================
  @Comment("设置字符串值（带过期时间）")
  public void setString(String key, String value, long timeout, String unit) {
    try {
      redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.valueOf(unit));
    } catch (RedisConnectionFailureException e) {
      log.error("Redis连接失败 - Key: {}, Error: {}", key, e.getMessage());
    } catch (RedisSystemException e) {
      log.error("Redis系统异常 - Key: {}, Error: {}", key, e.getCause().getMessage());
    } catch (Exception e) {
      log.error("设置字符串值异常 - Key: {}, Error: {}", key, e.getMessage());
    }
  }

  @Comment("设置Hash字段值（带过期时间）")
  public void setHashField(String key, String field, String value, long timeout, String unit) {
    try {
      redisTemplate.opsForHash().put(key, field, value);
      redisTemplate.expire(key, timeout, TimeUnit.valueOf(unit));
    } catch (RedisConnectionFailureException e) {
      log.error("Redis连接失败 - Key: {}, Field: {}", key, field);
    } catch (Exception e) {
      log.error("设置Hash字段异常 - Key: {}, Field: {}, Error: {}", key, field, e.getMessage());
    }
  }

  @Comment("从左侧插入List（带过期时间）")
  public Long leftPush(String key, String value, long timeout, String unit) {
    try {
      Long result = redisTemplate.opsForList().leftPush(key, value);
      redisTemplate.expire(key, timeout, TimeUnit.valueOf(unit));
      return result;
    } catch (RedisConnectionFailureException e) {
      log.error("Redis连接失败 - Key: {}", key);
      return 0L;
    } catch (Exception e) {
      log.error("List左插入异常 - Key: {}, Error: {}", key, e.getMessage());
      return 0L;
    }
  }

  @Comment("添加Set成员（带过期时间）")
  public Long addToSet(String key, String value, long timeout, String unit) {
    try {
      Long result = redisTemplate.opsForSet().add(key, value);
      redisTemplate.expire(key, timeout, TimeUnit.valueOf(unit));
      return result;
    } catch (RedisConnectionFailureException e) {
      log.error("Redis连接失败 - Key: {}", key);
      return 0L;
    } catch (Exception e) {
      log.error("Set添加成员异常 - Key: {}, Error: {}", key, e.getMessage());
      return 0L;
    }
  }

  @Comment("添加ZSet成员（带过期时间）")
  public Boolean addToZSet(String key, String value, double score, long timeout, String unit) {
    try {
      Boolean result = redisTemplate.opsForZSet().add(key, value, score);
      redisTemplate.expire(key, timeout, TimeUnit.valueOf(unit));
      return result;
    } catch (RedisConnectionFailureException e) {
      log.error("Redis连接失败 - Key: {}", key);
      return false;
    } catch (Exception e) {
      log.error("ZSet添加成员异常 - Key: {}, Error: {}", key, e.getMessage());
      return false;
    }
  }

  // ==================== 物联网场景专用方法 ====================
  @Comment("更新设备状态（自动续期）")
  public void updateDeviceStatus(
      String deviceId, String field, String value, long timeout, String unit) {
    String key = DEVICE_PREFIX + "status:" + deviceId;
    try {
      redisTemplate.opsForHash().put(key, field, value);
      redisTemplate.expire(key, timeout, TimeUnit.valueOf(unit));
    } catch (RedisConnectionFailureException e) {
      log.error("设备状态更新失败 - Device: {}, Field: {}", deviceId, field);
    } catch (Exception e) {
      log.error("更新设备状态异常 - Device: {}, Field: {}, Error: {}", deviceId, field, e.getMessage());
    }
  }

  @Comment("获取设备状态")
  public Map<String, String> getDeviceStatus(String deviceId) {
    String key = DEVICE_PREFIX + "status:" + deviceId;
    try {
      Map<Object, Object> rawMap = redisTemplate.opsForHash().entries(key);
      return convertToStringMap(rawMap);
    } catch (RedisConnectionFailureException e) {
      log.error("获取设备状态失败 - Device: {}", deviceId);
      return Collections.emptyMap();
    } catch (Exception e) {
      log.error("获取设备状态异常 - Device: {}, Error: {}", deviceId, e.getMessage());
      return Collections.emptyMap();
    }
  }

  @Comment("推送设备指令到队列（带过期时间）")
  public void pushDeviceCommand(
      String deviceId, String commandType, String params, long timeout, String unit) {
    String key = DEVICE_PREFIX + "cmd:" + deviceId;
    try {
      String command =
          String.format(
              "{\"type\":\"%s\",\"params\":%s,\"timestamp\":%d}",
              commandType, params, System.currentTimeMillis());

      // 使用管道操作确保原子性
      redisTemplate.executePipelined(
          (RedisCallback<Object>)
              connection -> {
                connection.rPush(key.getBytes(), command.getBytes());
                connection.expire(key.getBytes(), TimeUnit.valueOf(unit).toSeconds(timeout));
                return null;
              });
    } catch (RedisConnectionFailureException e) {
      log.error("指令推送失败 - Device: {}, Command: {}", deviceId, commandType);
    } catch (Exception e) {
      log.error(
          "指令推送异常 - Device: {}, Command: {}, Error: {}", deviceId, commandType, e.getMessage());
    }
  }

  @Comment("获取并移除下一个指令")
  public String popNextCommand(String deviceId) {
    String key = DEVICE_PREFIX + "cmd:" + deviceId;
    try {
      return redisTemplate.opsForList().leftPop(key);
    } catch (RedisConnectionFailureException e) {
      log.error("获取指令失败 - Device: {}", deviceId);
      return null;
    } catch (Exception e) {
      log.error("获取指令异常 - Device: {}, Error: {}", deviceId, e.getMessage());
      return null;
    }
  }

  @Comment("记录设备位置")
  public void recordDeviceLocation(String deviceId, double lng, double lat) {
    try {
      redisTemplate
          .opsForGeo()
          .add(
              DEVICE_PREFIX + "geo:devices",
              new RedisGeoCommands.GeoLocation<>(deviceId, new Point(lng, lat)));
    } catch (RedisConnectionFailureException e) {
      log.error("位置记录失败 - Device: {}", deviceId);
    } catch (Exception e) {
      log.error("位置记录异常 - Device: {}, Error: {}", deviceId, e.getMessage());
    }
  }

  @Comment("记录设备事件")
  public void recordDeviceEvent(String deviceId, String eventType, Map<String, String> eventData) {
    String key = DEVICE_PREFIX + "events:" + deviceId;
    try {
      Map<String, String> fields = new HashMap<>();
      fields.put("eventType", eventType);
      fields.put("timestamp", String.valueOf(System.currentTimeMillis()));
      fields.putAll(eventData);
      redisTemplate.opsForStream().add(key, fields);
    } catch (RedisConnectionFailureException e) {
      log.error("事件记录失败 - Device: {}, Event: {}", deviceId, eventType);
      logToLocalStorage(eventData); // 降级到本地存储
    } catch (Exception e) {
      log.error("事件记录异常 - Device: {}, Event: {}, Error: {}", deviceId, eventType, e.getMessage());
      logToLocalStorage(eventData); // 降级到本地存储
    }
  }

  @Comment("缓存设备数据并记录历史")
  public void cacheDeviceData(
      String deviceId, String dataType, String value, long timeout, String unit) {
    String currentKey = DEVICE_PREFIX + "data:current:" + deviceId + ":" + dataType;
    try {
      // 获取上一次值
      String lastValue = redisTemplate.opsForValue().get(currentKey);

      // 存储历史记录
      if (lastValue != null) {
        String historyKey = DEVICE_PREFIX + "data:history:" + deviceId + ":" + dataType;
        redisTemplate.executePipelined(
            (RedisCallback<Object>)
                connection -> {
                  connection.zAdd(
                      historyKey.getBytes(), System.currentTimeMillis(), lastValue.getBytes());
                  connection.zRemRange(historyKey.getBytes(), 0, -101); // 保留最近100条
                  return null;
                });
      }

      // 更新当前值
      redisTemplate.opsForValue().set(currentKey, value, timeout, TimeUnit.valueOf(unit));
    } catch (RedisConnectionFailureException e) {
      log.error("数据缓存失败 - Device: {}, Type: {}", deviceId, dataType);
    } catch (Exception e) {
      log.error("数据缓存异常 - Device: {}, Type: {}, Error: {}", deviceId, dataType, e.getMessage());
    }
  }

  // ==================== 分布式锁（设备操作互斥）====================
  private static final String LOCK_SCRIPT =
      "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then "
          + "   redis.call('pexpire', KEYS[1], ARGV[2]) "
          + "   return 1 "
          + "else return 0 end";

  private static final String UNLOCK_SCRIPT =
      "if redis.call('get', KEYS[1]) == ARGV[1] then "
          + "   return redis.call('del', KEYS[1]) "
          + "else return 0 end";

  @Comment("获取设备操作锁")
  public boolean acquireDeviceLock(String deviceId, String lockId, long expireMs) {
    String lockKey = DEVICE_PREFIX + "lock:" + deviceId;
    try {
      RedisScript<Boolean> script = new DefaultRedisScript<>(LOCK_SCRIPT, Boolean.class);
      return Boolean.TRUE.equals(
          redisTemplate.execute(
              script, Collections.singletonList(lockKey), lockId, String.valueOf(expireMs)));
    } catch (RedisConnectionFailureException e) {
      log.error("获取锁失败 - Device: {}, LockID: {}", deviceId, lockId);
      return false;
    } catch (Exception e) {
      log.error("获取锁异常 - Device: {}, LockID: {}, Error: {}", deviceId, lockId, e.getMessage());
      return false;
    }
  }

  @Comment("释放设备锁")
  public boolean releaseDeviceLock(String deviceId, String lockId) {
    String lockKey = DEVICE_PREFIX + "lock:" + deviceId;
    try {
      RedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
      Long result = redisTemplate.execute(script, Collections.singletonList(lockKey), lockId);
      return result != null && result == 1;
    } catch (RedisConnectionFailureException e) {
      log.error("释放锁失败 - Device: {}, LockID: {}", deviceId, lockId);
      return false;
    } catch (Exception e) {
      log.error("释放锁异常 - Device: {}, LockID: {}, Error: {}", deviceId, lockId, e.getMessage());
      return false;
    }
  }

  // ==================== 辅助方法 ====================
  private Map<String, String> convertToStringMap(Map<Object, Object> rawMap) {
    Map<String, String> result = new HashMap<>();
    rawMap.forEach((k, v) -> result.put(k.toString(), v.toString()));
    return result;
  }

  private void logToLocalStorage(Map<String, String> eventData) {
    // 实现本地临时存储逻辑
    log.warn("事件降级到本地存储: {}", eventData);
    // 实际项目中可写入本地文件或数据库
  }
}

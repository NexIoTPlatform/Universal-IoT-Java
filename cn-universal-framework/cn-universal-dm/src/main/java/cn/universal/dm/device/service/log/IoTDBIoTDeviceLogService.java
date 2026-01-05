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

package cn.universal.dm.device.service.log;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.common.constant.IoTConstant.MessageType;
import cn.universal.core.metadata.AbstractPropertyMetadata;
import cn.universal.dm.device.entity.IoTDevicePropertiesBO;
import cn.universal.dm.device.service.impl.IoTDeviceService;
import cn.universal.dm.device.service.impl.IoTProductDeviceService;
import cn.universal.persistence.base.BaseUPRequest;
import cn.universal.persistence.dto.IoTDeviceDTO;
import cn.universal.persistence.dto.LogStorePolicyDTO;
import cn.universal.persistence.entity.IoTDeviceEvents;
import cn.universal.persistence.entity.IoTDeviceLog;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.vo.IoTDeviceLogMetadataVO;
import cn.universal.persistence.entity.vo.IoTDeviceLogVO;
import cn.universal.persistence.query.LogQuery;
import cn.universal.persistence.query.PageBean;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.isession.SessionDataSet;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.rpc.StatementExecutionException;
import org.apache.iotdb.session.Session;
import org.apache.iotdb.tsfile.read.common.Field;
import org.apache.iotdb.tsfile.read.common.RowRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * IoTDB 存储日志
 *
 * <p>通过配置文件动态加载
 *
 * <p><b>重连机制说明：</b> IoTDB Session 客户端没有内置自动重连机制（官方文档明确说明）， 因此本类实现了统一的重连管理机制，符合官方推荐的最佳实践。 所有业务代码通过
 * getSession() 方法获取 session，无需关心重连细节。
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/15 16:10
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "iotdb", name = "enable", havingValue = "true")
public class IoTDBIoTDeviceLogService extends AbstractIoTDeviceLogService {

  private String storePolicy = "iotdb";

  @Value("${iotdb.host:127.0.0.1}")
  private String host;

  @Value("${iotdb.port:6667}")
  private Integer port;

  @Value("${iotdb.username:root}")
  private String username;

  @Value("${iotdb.password:root}")
  private String password;

  @Value("${iotdb.storage.group:root.device}")
  private String storageGroup;

  @Resource private IoTDeviceService iotDeviceService;

  @Resource private IoTProductDeviceService iotProductDeviceService;

  private volatile Session session;

  private final Object sessionLock = new Object();

  @Value("${iotdb.reconnect.maxRetries:3}")
  private int maxRetries;

  @Value("${iotdb.reconnect.backoffMillis:}")
  private String backoffConfig;

  private long[] backoffMillis = new long[] {200, 500, 1000};

  // 重连冷却时间（毫秒），防止频繁重连
  @Value("${iotdb.reconnect.cooldownMillis:5000}")
  private long reconnectCooldownMillis;

  // 最后一次重连尝试时间
  private volatile long lastReconnectAttemptTime = 0;

  // 重连状态：false=未重连中, true=重连中
  private volatile boolean reconnecting = false;

  @Value("${iotdb.alert.enabled:false}")
  private boolean alertEnabled;

  @Value("${iotdb.alert.templateId:0}")
  private Long alertTemplateId;

  @Value("${iotdb.alert.receivers:}")
  private String alertReceivers;

  @PostConstruct
  public void init() {
    try {
      createAndOpenSession();
      log.info("初始化IoTDB连接成功，host={}, port={}", host, port);
    } catch (Exception e) {
      log.error("初始化IoTDB连接失败，host={}, port={}", host, port, e);
      // 初始化失败时确保 session 为 null
      session = null;
    }
  }

  /**
   * 将时间戳转换为IoTDB需要的毫秒格式
   *
   * <p>判断逺辑：
   *
   * <ul>
   *   <li>如果时间戳 > 10000000000L，说明已经是毫秒，直接返回
   *   <li>否则说明是秒，乘以1000转换为毫秒
   * </ul>
   *
   * @param timestamp 时间戳（秒或毫秒）
   * @return 毫秒时间戳
   */
  private long toMillisTimestamp(Long timestamp) {
    if (timestamp == null) {
      return 0L;
    }
    // 如果时间戳 > 10000000000 (2286-11-20 17:46:40)，则认为已经是毫秒
    // 否则认为是秒，需要乘以1000
    return timestamp > 10000000000L ? timestamp : timestamp * 1000;
  }

  /** 创建并打开 IoTDB Session 注意：此方法会抛出异常，调用方需要处理 */
  private void createAndOpenSession() throws Exception {
    Session newSession = null;
    try {
      // 1. 创建新 Session
      newSession =
          new Session.Builder().host(host).port(port).username(username).password(password).build();

      // 2. 打开连接（这一步会初始化内部的 deviceIdToEndpoint 等 Map）
      newSession.open();

      // 3. 创建存储组
      try {
        newSession.setStorageGroup(storageGroup);
        log.info("创建存储组成功: {}", storageGroup);
      } catch (Exception e) {
        // 存储组可能已存在，忽略错误
        log.debug("存储组可能已存在: {}", storageGroup);
      }

      // 4. 创建时间序列（类似MySQL的表结构）
      createTimeseries();

      // 5. 只有完全成功后才赋值给成员变量
      session = newSession;

    } catch (Exception e) {
      // 失败时关闭新创建的 Session
      if (newSession != null) {
        try {
          newSession.close();
        } catch (Exception ignore) {
        }
      }
      throw e; // 重新抛出异常，让调用方知道初始化失败
    }
  }

  /**
   * 创建时间序列（动态创建，类似自动建表） 预定义常用字段的数据类型，防止 IoTDB 自动推断错误
   *
   * <p>MySQL 表结构映射： - iot_device_log: device_name(varchar32), message_type(varchar20),
   * command_id(varchar32), command_status(tinyint), event(varchar80), point(varchar128),
   * content(text) - iot_device_log_metadata: property(varchar32), ext1(varchar255),
   * ext2(varchar655), ext3(varchar255)
   */
  private void createTimeseries() {
    try {
      log.debug("IoTDB 将在数据插入时自动创建时间序列（类型定义与 MySQL 表结构对齐）");
      log.debug("=== iot_device_log 字段类型映射 ===");
      log.debug("  device_name    : varchar(32)  -> TEXT");
      log.debug("  message_type   : varchar(20)  -> TEXT");
      log.debug("  command_id     : varchar(32)  -> TEXT");
      log.debug("  command_status : tinyint      -> INT32");
      log.debug("  event          : varchar(80)  -> TEXT");
      log.debug("  point          : varchar(128) -> TEXT");
      log.debug("  content        : text         -> TEXT");
      log.debug("=== iot_device_log_metadata 字段类型映射 ===");
      log.debug("  property       : varchar(32)  -> TEXT");
      log.debug("  ext1           : varchar(255) -> TEXT");
      log.debug("  ext2           : varchar(655) -> TEXT");
      log.debug("  ext3           : varchar(255) -> TEXT");
      log.debug("  create_time    : datetime     -> INT64(timestamp)");
      log.debug("  max_storage    : int          -> INT32");
    } catch (Exception e) {
      log.warn("预创建时间序列失败（不影响使用）: {}", e.getMessage());
    }
  }

  // === IoTDB重试与重连机制 ===
  private long[] getBackoffMillis() {
    try {
      if (backoffConfig != null && !backoffConfig.isEmpty()) {
        String[] arr = backoffConfig.split(",");
        long[] res = new long[arr.length];
        for (int i = 0; i < arr.length; i++) {
          res[i] = Long.parseLong(arr[i].trim());
        }
        return res;
      }
    } catch (Exception e) {
      log.warn("解析重试退避配置失败: {}", backoffConfig);
    }
    return backoffMillis;
  }

  /**
   * 统一获取 Session，自动处理重连逻辑
   *
   * <p>这是所有业务代码获取 session 的唯一入口。 IoTDB Session 本身没有内置自动重连机制（官方文档明确说明）， 因此需要在应用层实现重连逻辑，这是官方推荐的做法。
   *
   * <p>重连策略： 1. 冷却时间机制：避免频繁重连（默认5秒） 2. 错开重连：使用锁确保同一时间只有一个线程执行重连 3. 等待机制：其他线程等待重连完成，避免重复重连
   *
   * @return Session 实例，如果重连失败返回 null
   */
  private Session getSession() {
    // 快速路径：如果 session 已存在，直接返回
    Session currentSession = session;
    if (currentSession != null) {
      return currentSession;
    }

    // 检查冷却时间，避免频繁重连
    long now = System.currentTimeMillis();
    long timeSinceLastAttempt = now - lastReconnectAttemptTime;

    if (timeSinceLastAttempt < reconnectCooldownMillis) {
      // 还在冷却期内，等待其他线程的重连结果
      long waitTime = reconnectCooldownMillis - timeSinceLastAttempt;
      log.debug("IoTDB重连冷却中，等待 {}ms", waitTime);

      synchronized (sessionLock) {
        // 等待期间可能其他线程已经重连成功
        if (session != null) {
          return session;
        }

        // 等待一段时间，让其他线程完成重连
        try {
          sessionLock.wait(Math.min(waitTime, 1000)); // 最多等待1秒
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        // 再次检查
        if (session != null) {
          return session;
        }
      }
    }

    // 执行重连（带锁，确保只有一个线程执行重连）
    synchronized (sessionLock) {
      // 双重检查：可能其他线程已经重连成功
      if (session != null) {
        return session;
      }

      // 检查是否正在重连中
      if (reconnecting) {
        log.debug("其他线程正在重连IoTDB，等待重连完成");
        // 等待重连完成（最多等待5秒）
        long waitStart = System.currentTimeMillis();
        while (reconnecting && (System.currentTimeMillis() - waitStart) < 5000) {
          try {
            sessionLock.wait(500); // 每500ms检查一次
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
          if (session != null) {
            return session; // 其他线程重连成功
          }
        }
        // 如果等待超时，尝试自己重连
        if (reconnecting) {
          log.warn("等待其他线程重连超时，尝试自己重连");
        }
      }

      // 更新重连尝试时间
      lastReconnectAttemptTime = System.currentTimeMillis();

      // 标记正在重连
      reconnecting = true;

      try {
        // 1. 先关闭旧连接
        if (session != null) {
          try {
            session.close();
            log.debug("已关闭旧的 IoTDB Session");
          } catch (Exception ignore) {
            // 关闭失败不影响重连
          }
        }

        // 2. 清空 session
        session = null;

        // 3. 尝试创建新连接
        createAndOpenSession();
        log.info("✓ IoTDB重连成功");
        return session;

      } catch (Exception e) {
        log.error("✗ IoTDB重连失败，错误: {}", e.getMessage());
        // 重连失败时确保 session 为 null
        session = null;
        return null;
      } finally {
        // 重置重连标志
        reconnecting = false;
        // 通知等待的线程
        sessionLock.notifyAll();
      }
    }
  }

  /** 标记 session 无效，触发下次重连 当检测到连接错误时调用此方法 */
  private void invalidateSession() {
    synchronized (sessionLock) {
      if (session != null) {
        try {
          session.close();
        } catch (Exception ignore) {
          // 忽略关闭异常
        }
        session = null;
      }
    }
  }

  /** 判断异常是否需要重连 */
  private boolean isReconnectNeeded(Exception e) {
    if (e == null) {
      return false;
    }
    String errMsg = e.getMessage();
    if (errMsg == null) {
      return false;
    }
    return errMsg.contains("deviceIdToEndpoint")
        || errMsg.contains("Connection refused")
        || errMsg.contains("Broken pipe")
        || errMsg.contains("Connection reset")
        || errMsg.contains("Session is closed")
        || errMsg.contains("session未初始化");
  }

  /**
   * 带重试机制的执行器
   *
   * @param action 要执行的操作
   * @param operation 操作名称（用于日志）
   * @param path IoTDB路径
   * @param iotId 设备ID
   * @return 操作结果
   * @throws Exception 所有重试失败后抛出最后一次异常
   */
  private <T> T executeWithRetry(
      java.util.concurrent.Callable<T> action, String operation, String path, String iotId)
      throws Exception {
    Exception last = null;
    long[] delays = getBackoffMillis();
    int retries = Math.max(maxRetries, 1);

    for (int i = 0; i < retries; i++) {
      try {
        // 统一获取 session（自动处理重连）
        Session currentSession = getSession();
        if (currentSession == null) {
          throw new IllegalStateException("IoTDB Session 不可用且重连失败");
        }

        // 执行操作
        return action.call();

      } catch (Exception e) {
        last = e;
        String errMsg = e.getMessage();

        // 判断是否需要重连
        boolean needReconnect = isReconnectNeeded(e);

        log.warn(
            "IoTDB {} 失败，path={}, iotId={}, attempt={}/{}, needReconnect={}, error={}",
            operation,
            path,
            iotId,
            (i + 1),
            retries,
            needReconnect,
            errMsg);

        // 需要重连时，标记 session 无效
        if (needReconnect) {
          invalidateSession();
        }

        // 最后一次重试失败，不再等待
        if (i < retries - 1 && i < delays.length) {
          try {
            Thread.sleep(delays[i]);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }

    // 所有重试都失败
    triggerIoTDBAlert("IoTDB操作失败: " + operation, "path=" + path + ", iotId=" + iotId, last);
    throw last;
  }

  private void triggerIoTDBAlert(String title, String content, Throwable e) {
    log.error("{} - {}, err={}", title, content, e == null ? "" : e.toString());
    // 如需推送到外部通知渠道，请在 cn-universal-notice 模块配置模板，并在此处接入对应服务
  }

  @PreDestroy
  public void closeSession() {
    if (session != null) {
      try {
        session.close();
        log.info("IoTDB连接已关闭");
      } catch (Exception e) {
        log.error("关闭IoTDB连接失败", e);
      }
    }
  }

  @Override
  @Async("taskExecutor")
  public void saveDeviceLog(BaseUPRequest upRequest, IoTDeviceDTO noUse, IoTProduct ioTProduct) {
    /** 产品数据存储策略，不为空则保存日志 */
    if (StrUtil.isNotBlank(ioTProduct.getStorePolicy())) {
      IoTDeviceDTO ioTDeviceDTO =
          iotDeviceService.selectDevInstanceBO(upRequest.getProductKey(), upRequest.getDeviceId());
      try {
        IoTDeviceLog ioTDeviceLog = build(upRequest, ioTDeviceDTO);
        saveDeviceLogToIoTDB(ioTDeviceLog, upRequest);
        log.info("IoTDB插入设备日志成功，iotId={}", upRequest.getIotId());
      } catch (Exception e) {
        log.error("保存设备日志到IoTDB报错={}", e);
      }

      // 处理存储策略配置
      String storePolicyConfiguration = ioTProduct.getStorePolicyConfiguration();
      try {
        if (StrUtil.isNotBlank(storePolicyConfiguration)
            || MessageType.EVENT.equals(upRequest.getMessageType())) {
          LogStorePolicyDTO productLogStorePolicy =
              iotProductDeviceService.getProductLogStorePolicy(ioTProduct.getProductKey());
          saveLogStorePolicy(productLogStorePolicy, upRequest, ioTProduct);
        }
      } catch (Exception e) {
        log.error("IoTDB保存设备属性扩展日志报错={}", e);
      }
    }
  }

  @Override
  public void saveDeviceLog(
      IoTDeviceLog ioTDeviceLog, IoTDeviceDTO ioTDeviceDTO, IoTProduct ioTProduct) {
    /** 产品数据存储策略，不为空则保存日志 */
    if (StrUtil.isNotBlank(ioTProduct.getStorePolicy())) {
      try {
        saveDeviceLogToIoTDB(ioTDeviceLog, null);
        log.info("IoTDB插入设备日志成功，deviceId={}", ioTDeviceDTO.getDeviceId());
      } catch (Exception e) {
        log.error("保存设备日志到IoTDB报错={}", e);
      }
    }
  }

  /** 根据存储策略保存元数据日志 支持属性和事件的详细信息存储 */
  private void saveLogStorePolicy(
      LogStorePolicyDTO logStorePolicyDTO, BaseUPRequest up, IoTProduct ioTProduct) {
    // 处理属性消息
    if (MessageType.PROPERTIES.equals(up.getMessageType())
        && up.getProperties() != null
        && CollectionUtil.isNotEmpty(logStorePolicyDTO.getProperties())) {
      up.getProperties()
          .forEach(
              (key, value) -> {
                if (logStorePolicyDTO.getProperties().containsKey(key)) {
                  try {
                    AbstractPropertyMetadata propertyOrNull =
                        getDeviceMetadata(ioTProduct.getMetadata()).getPropertyOrNull(key);
                    IoTDevicePropertiesBO ioTDevicePropertiesBO = new IoTDevicePropertiesBO();
                    ioTDevicePropertiesBO.withValue(propertyOrNull, value);
                    // 构建元数据并保存
                    String property = key;
                    String propertyName = ioTDevicePropertiesBO.getPropertyName();
                    String formatValue = ioTDevicePropertiesBO.getFormatValue();
                    String symbol = ioTDevicePropertiesBO.getSymbol();
                    String content = StrUtil.str(value, CharsetUtil.charset("UTF-8"));
                    long ts = up.getTime() != null ? up.getTime() : System.currentTimeMillis();
                    savePropertySeriesToIoTDB(
                        up, property, content, propertyName, formatValue, symbol, ts);
                  } catch (Exception e) {
                    log.error("IoTDB保存属性元数据失败: iotId={}, property={}", up.getIotId(), key, e);
                  }
                }
              });
    }

    // 处理事件消息
    if (MessageType.EVENT.equals(up.getMessageType())) {
      try {
        boolean allowedEvent =
            CollectionUtil.isNotEmpty(logStorePolicyDTO.getEvent())
                && logStorePolicyDTO.getEvent().containsKey(up.getEvent());

        if (!allowedEvent) {
          log.debug("跳过未配置事件写入: iotId={}, event={}", up.getIotId(), up.getEvent());
          return;
        }

        int maxStorage = logStorePolicyDTO.getEvent().get(up.getEvent()).getMaxStorage();
        long ts = up.getTime() != null ? up.getTime() : System.currentTimeMillis();
        saveEventSeriesToIoTDB(up, maxStorage, ts);
      } catch (Exception e) {
        log.error("IoTDB保存事件元数据失败: iotId={}, event={}", up.getIotId(), up.getEvent(), e);
      }
    }
  }

  /**
   * 保存属性元数据到IoTDB 路径: root.device.{productKey}.{deviceId}.property_metadata
   * 字段与MySQL一致：message_type,property,content,device_name,device_id,iot_id,product_key,create_time,ext1,ext2,ext3
   */
  private void savePropertyMetadataToIoTDB(
      BaseUPRequest up,
      String property,
      String content,
      String propertyName,
      String formatValue,
      String symbol,
      long timestamp)
      throws Exception {
    // 通过 executeWithRetry 统一处理，无需在此检查 session

    String metadataPath =
        buildDevicePath(up.getProductKey(), up.getDeviceId()) + ".property_metadata";

    // 与MySQL一致的字段
    List<String> measurements =
        List.of(
            "message_type",
            "property",
            "content",
            "device_name",
            "device_id",
            "iot_id",
            "product_key",
            "create_time",
            "ext1",
            "ext2",
            "ext3");

    // 显式指定数据类型（对齐 MySQL iot_device_log_metadata 表）
    List<org.apache.iotdb.tsfile.file.metadata.enums.TSDataType> types =
        List.of(
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType
                .TEXT, // message_type: varchar(20)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // property: varchar(32)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // content: text
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // device_name: varchar(64)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // device_id: varchar(64)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // iot_id: varchar(128)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // product_key: varchar(64)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT64, // create_time: datetime
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // ext1: varchar(255)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // ext2: varchar(655)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT); // ext3: varchar(255)

    List<Object> values =
        List.of(
            escapeValue(MessageType.PROPERTIES.name()),
            escapeValue(property),
            escapeValue(content),
            escapeValue(up.getDeviceName()),
            escapeValue(up.getDeviceId()),
            escapeValue(up.getIotId()),
            escapeValue(up.getProductKey()),
            timestamp, // INT64 不需要 String.valueOf
            escapeValue(propertyName),
            escapeValue(formatValue),
            escapeValue(symbol));

    try {
      executeWithRetry(
          () -> {
            Session currentSession = getSession();
            if (currentSession == null) {
              throw new IllegalStateException("IoTDB Session 不可用");
            }
            currentSession.insertRecord(metadataPath, timestamp, measurements, types, values);
            return null;
          },
          "insertRecord",
          metadataPath,
          up.getIotId());
      log.debug(
          "IoTDB插入属性元数据成功: path={}, property={}, iotId={}", metadataPath, property, up.getIotId());
    } catch (Exception e) {
      log.error(
          "IoTDB插入属性元数据失败: path={}, property={}, iotId={}",
          metadataPath,
          property,
          up.getIotId(),
          e);
      throw e;
    }
  }

  /** 保存事件元数据到IoTDB 路径: root.device.{productKey}.{deviceId}.event_metadata */
  private void saveEventMetadataToIoTDB(BaseUPRequest up, int maxStorage) throws Exception {
    saveEventMetadataToIoTDB(up, maxStorage, System.currentTimeMillis());
  }

  /** 保存事件元数据到IoTDB 路径: root.device.{productKey}.{deviceId}.event_metadata */
  private void saveEventMetadataToIoTDB(BaseUPRequest up, int maxStorage, long timestamp)
      throws Exception {
    // 通过 executeWithRetry 统一处理，无需在此检查 session

    String metadataPath = buildDevicePath(up.getProductKey(), up.getDeviceId()) + ".event_metadata";

    // 与MySQL保持一致的字段
    List<String> measurements =
        List.of(
            "message_type",
            "event",
            "content",
            "device_name",
            "device_id",
            "iot_id",
            "product_key",
            "create_time",
            "ext1",
            "max_storage");

    // 显式指定数据类型（对齐 MySQL iot_device_log_metadata 表）
    List<org.apache.iotdb.tsfile.file.metadata.enums.TSDataType> types =
        List.of(
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType
                .TEXT, // message_type: varchar(20)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // event: varchar(32)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // content: text
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // device_name: varchar(64)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // device_id: varchar(64)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // iot_id: varchar(128)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // product_key: varchar(64)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT64, // create_time: datetime
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // ext1: varchar(255)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT32); // max_storage: int

    List<Object> values =
        List.of(
            escapeValue(MessageType.EVENT.name()),
            escapeValue(up.getEvent()),
            escapeValue(up.getEventName()),
            escapeValue(up.getDeviceName()),
            escapeValue(up.getDeviceId()),
            escapeValue(up.getIotId()),
            escapeValue(up.getProductKey()),
            timestamp, // INT64 不需要 String.valueOf
            escapeValue(JSONUtil.toJsonStr(up.getData())),
            maxStorage); // INT32 不需要 String.valueOf

    try {
      executeWithRetry(
          () -> {
            Session currentSession = getSession();
            if (currentSession == null) {
              throw new IllegalStateException("IoTDB Session 不可用");
            }
            currentSession.insertRecord(metadataPath, timestamp, measurements, types, values);
            return null;
          },
          "insertRecord",
          metadataPath,
          up.getIotId());
      log.debug(
          "IoTDB插入事件元数据成功: path={}, event={}, iotId={}",
          metadataPath,
          up.getEvent(),
          up.getIotId());
    } catch (Exception e) {
      log.error(
          "IoTDB插入事件元数据失败: path={}, event={}, iotId={}",
          metadataPath,
          up.getEvent(),
          up.getIotId(),
          e);
      throw e;
    }
  }

  /**
   * 保存设备日志到IoTDB 数据路径结构: - 设备日志: root.device.{productKey}.{deviceId}.log - 元数据:
   * root.device.{productKey}.{deviceId}.metadata
   */
  private void saveDeviceLogToIoTDB(IoTDeviceLog ioTDeviceLog, BaseUPRequest upRequest)
      throws Exception {
    // 通过 executeWithRetry 统一处理，无需在此检查 session

    // 主日志路径 (对应iot_device_log_*)
    String devicePath =
        buildDevicePath(ioTDeviceLog.getProductKey(), ioTDeviceLog.getDeviceId()) + ".log";

    // 时间戳（毫秒）
    long timestamp =
        ioTDeviceLog
            .getCreateTime()
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli();

    // 字段定义（对齐 MySQL iot_device_log 表结构）
    List<String> measurements =
        List.of(
            "message_type",
            "content",
            "event",
            "command_id",
            "command_status",
            "point",
            "device_name");

    // 显式指定数据类型（重要：防止 IoTDB 自动推断错误）
    List<org.apache.iotdb.tsfile.file.metadata.enums.TSDataType> types =
        List.of(
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType
                .TEXT, // message_type: varchar(20)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // content: text
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // event: varchar(80)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // command_id: varchar(32)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT32, // command_status: tinyint
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT, // point: varchar(128)
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT // device_name: varchar(32)
        );

    // 字段值（需要根据类型转换）
    List<Object> values =
        List.of(
            escapeValue(ioTDeviceLog.getMessageType()),
            escapeValue(ioTDeviceLog.getContent()),
            escapeValue(ioTDeviceLog.getEvent()),
            escapeValue(ioTDeviceLog.getCommandId()),
            ioTDeviceLog.getCommandStatus() != null
                ? ioTDeviceLog.getCommandStatus()
                : 0, // INT32 不需要 String.valueOf
            escapeValue(ioTDeviceLog.getPoint()),
            escapeValue(ioTDeviceLog.getDeviceName()));

    try {
      executeWithRetry(
          () -> {
            Session currentSession = getSession();
            if (currentSession == null) {
              throw new IllegalStateException("IoTDB Session 不可用");
            }
            currentSession.insertRecord(devicePath, timestamp, measurements, types, values);
            return null;
          },
          "insertRecord",
          devicePath,
          ioTDeviceLog.getIotId());
      log.debug(
          "IoTDB插入设备日志成功: path={}, timestamp={}, productKey={}, deviceId={}",
          devicePath,
          timestamp,
          ioTDeviceLog.getProductKey(),
          ioTDeviceLog.getDeviceId());
    } catch (Exception e) {
      log.error(
          "IoTDB插入设备日志失败: path={}, productKey={}, deviceId={}, error={}",
          devicePath,
          ioTDeviceLog.getProductKey(),
          ioTDeviceLog.getDeviceId(),
          e.getMessage());
      throw e;
    }
  }

  /**
   * 按属性名拆分时间序列写入 路径: root.device.{productKey}.{deviceId}.property.{sanitizedProperty}
   * 精简字段：message_type,content,create_time,ext1,ext2,ext3
   */
  private void savePropertySeriesToIoTDB(
      BaseUPRequest up,
      String property,
      String content,
      String propertyName,
      String formatValue,
      String symbol,
      long timestamp)
      throws Exception {
    // 通过 executeWithRetry 统一处理，无需在此检查 session

    String seriesPath =
        buildDevicePath(up.getProductKey(), up.getDeviceId())
            + ".property."
            + sanitizePathNode(property);

    List<String> measurements =
        List.of(
            "message_type",
            "content",
            "create_time",
            "device_name",
            "device_id",
            "iot_id",
            "product_key",
            "ext1",
            "ext2",
            "ext3");

    List<org.apache.iotdb.tsfile.file.metadata.enums.TSDataType> types =
        List.of(
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT64,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT);

    List<Object> values =
        List.of(
            escapeValue(MessageType.PROPERTIES.name()),
            escapeValue(content),
            timestamp,
            escapeValue(up.getDeviceName()),
            escapeValue(up.getDeviceId()),
            escapeValue(up.getIotId()),
            escapeValue(up.getProductKey()),
            escapeValue(propertyName),
            escapeValue(formatValue),
            escapeValue(symbol));

    executeWithRetry(
        () -> {
          Session currentSession = getSession();
          if (currentSession == null) {
            throw new IllegalStateException("IoTDB Session 不可用");
          }
          currentSession.insertRecord(seriesPath, timestamp, measurements, types, values);
          return null;
        },
        "insertRecord",
        seriesPath,
        up.getIotId());

    log.debug(
        "IoTDB插入属性拆分序列成功: path={}, property={}, iotId={}", seriesPath, property, up.getIotId());
  }

  /**
   * 按事件名拆分时间序列写入 路径: root.device.{productKey}.{deviceId}.event.{sanitizedEvent}
   * 精简字段：message_type,content,create_time,ext1(maxStorage)
   */
  private void saveEventSeriesToIoTDB(BaseUPRequest up, int maxStorage, long timestamp)
      throws Exception {
    // 通过 executeWithRetry 统一处理，无需在此检查 session

    String seriesPath =
        buildDevicePath(up.getProductKey(), up.getDeviceId())
            + ".event."
            + sanitizePathNode(up.getEvent());

    List<String> measurements =
        List.of(
            "message_type",
            "content",
            "create_time",
            "device_name",
            "device_id",
            "iot_id",
            "product_key",
            "ext1");

    List<org.apache.iotdb.tsfile.file.metadata.enums.TSDataType> types =
        List.of(
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.INT64,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT,
            org.apache.iotdb.tsfile.file.metadata.enums.TSDataType.TEXT);

    List<Object> values =
        List.of(
            escapeValue(MessageType.EVENT.name()),
            escapeValue(up.getEventName()),
            timestamp,
            escapeValue(up.getDeviceName()),
            escapeValue(up.getDeviceId()),
            escapeValue(up.getIotId()),
            escapeValue(up.getProductKey()),
            String.valueOf(maxStorage));

    executeWithRetry(
        () -> {
          Session currentSession = getSession();
          if (currentSession == null) {
            throw new IllegalStateException("IoTDB Session 不可用");
          }
          currentSession.insertRecord(seriesPath, timestamp, measurements, types, values);
          return null;
        },
        "insertRecord",
        seriesPath,
        up.getIotId());

    log.debug(
        "IoTDB插入事件拆分序列成功: path={}, event={}, iotId={}", seriesPath, up.getEvent(), up.getIotId());
  }

  /** 转义字符串值，防止SQL注入 */
  private String escapeValue(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("'", "\\'");
  }

  /**
   * 从 iotId 中解析出 productKey 和 deviceId iotId 格式: productKey + deviceId (直接拼接，无分隔符) 或
   * productKey:deviceId (使用:分隔符)
   *
   * @param iotId 设备唯一ID
   * @param productKey 产品Key（可选，如果已知）
   * @return 数组 [productKey, deviceId]，解析失败返回 null
   */
  /**
   * 从 iotId 中解析出 productKey 和 deviceId iotId 格式: productKey + deviceId (直接拼接，无分隔符) 或
   * productKey:deviceId (使用:分隔符)
   *
   * @param iotId 设备唯一ID
   * @param productKey 产品Key（可选，如果已知）
   * @return 数组 [productKey, deviceId]，解析失败返回 null
   */
  private String[] parseIotId(String iotId, String productKey) {
    if (StrUtil.isBlank(iotId)) {
      return null;
    }

    // 如果包含冒号，使用分隔符解析 (TCP/UDP协议)
    if (iotId.contains(":")) {
      String[] parts = iotId.split(":", 2);
      if (parts.length == 2) {
        return parts; // [productKey, deviceId]
      }
    }

    // 如果已知 productKey，直接提取 deviceId
    if (StrUtil.isNotBlank(productKey) && iotId.startsWith(productKey)) {
      String deviceId = iotId.substring(productKey.length());
      if (StrUtil.isNotBlank(deviceId)) {
        return new String[] {productKey, deviceId};
      }
    }

    // 无法解析
    log.warn("无法从 iotId 解析出 productKey 和 deviceId: iotId={}, productKey={}", iotId, productKey);
    return null;
  }

  /**
   * 构建设备路径 路径结构: root.device.{productKey}.{deviceId} 这种结构自动实现了MySQL中按iot_id哈希分表的效果
   *
   * <p>IoTDB路径命名规范: 1. 必须以字母或下划线开头 2. 后续可包含字母、数字、下划线 3. 不能包含特殊字符
   */
  private String buildDevicePath(String productKey, String deviceId) {
    // 清理路径中的特殊字符（IoTDB路径命名规范）
    String cleanProductKey = sanitizePathNode(productKey);
    String cleanDeviceId = sanitizePathNode(deviceId);
    return storageGroup + "." + cleanProductKey + "." + cleanDeviceId;
  }

  /**
   * 清理路径节点，确保符合IoTDB命名规范
   *
   * @param node 原始节点名称
   * @return 清理后的合法节点名称
   */
  private String sanitizePathNode(String node) {
    if (node == null || node.isEmpty()) {
      return "device_unknown";
    }

    // 1. 替换特殊字符为下划线
    String cleaned = node.replaceAll("[^a-zA-Z0-9_]", "_");

    // 2. 如果以数字开头，添加前缀 "d_"
    if (cleaned.matches("^[0-9].*")) {
      cleaned = "d_" + cleaned;
    }

    // 3. 如果为空或只有下划线，使用默认值
    if (cleaned.isEmpty() || cleaned.matches("^_+$")) {
      cleaned = "device_" + Math.abs(node.hashCode());
    }

    return cleaned;
  }

  private String resolvePathNode(String path, String tag) {
    if (StrUtil.isBlank(path) || StrUtil.isBlank(tag)) {
      return "";
    }
    int idx = path.indexOf(tag);
    if (idx < 0) {
      return "";
    }
    String sub = path.substring(idx + tag.length());
    int dot = sub.indexOf('.');
    if (dot >= 0) {
      return sub.substring(0, dot);
    }
    return sub;
  }

  @Override
  public PageBean<IoTDeviceLogVO> pageList(LogQuery logQuery) {
    try {
      // 统一获取 session
      Session currentSession = getSession();
      if (currentSession == null) {
        log.warn("IoTDB session不可用且重连失败，返回空结果");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 构建查询路径，使用与写入相同的路径清理逻辑
      String queryPath = null;

      // 优先级 1: 如果同时有 productKey 和 deviceId，精确查询
      if (StrUtil.isNotBlank(logQuery.getProductKey())
          && StrUtil.isNotBlank(logQuery.getDeviceId())) {
        queryPath = buildDevicePath(logQuery.getProductKey(), logQuery.getDeviceId()) + ".log";
        log.debug("IoTDB使用精确路径查询: {}", queryPath);
      }
      // 优先级 2: 如果有 iotId，尝试解析
      else if (StrUtil.isNotBlank(logQuery.getIotId())) {
        String[] parsed = parseIotId(logQuery.getIotId(), logQuery.getProductKey());
        if (parsed != null && parsed.length == 2) {
          queryPath = buildDevicePath(parsed[0], parsed[1]) + ".log";
          log.debug("IoTDB从 iotId 解析路径: iotId={}, 解析后={}", logQuery.getIotId(), queryPath);
        } else {
          log.warn("IoTDB无法解析 iotId: {}", logQuery.getIotId());
          return new PageBean<>(
              new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
        }
      }
      // 优先级 3: 如果只有 productKey，模糊查询
      else if (StrUtil.isNotBlank(logQuery.getProductKey())) {
        String cleanProductKey = sanitizePathNode(logQuery.getProductKey());
        queryPath = storageGroup + "." + cleanProductKey + ".*.log";
        log.debug("IoTDB使用产品模糊查询: {}", queryPath);
      }
      // 无法构建查询路径
      else {
        log.warn("IoTDB查询参数不足，需要 productKey+deviceId 或 iotId");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 构建 SQL
      // 注意: IoTDB 会自动返回 time 列，不需要在 SELECT 中显式指定
      // 注意: IoTDB 不支持 WHERE 1=1，必须有实际的过滤条件或不加 WHERE
      StringBuilder sql = new StringBuilder();
      sql.append(
              "SELECT message_type, content, event, command_id, command_status, point, device_name FROM ")
          .append(queryPath);

      // 动态构建 WHERE 子句
      boolean hasWhere = false;
      if (StrUtil.isNotBlank(logQuery.getMessageType())) {
        sql.append(hasWhere ? " AND" : " WHERE");
        sql.append(" message_type='").append(logQuery.getMessageType()).append("'");
        hasWhere = true;
      }
      if (StrUtil.isNotBlank(logQuery.getEvent())) {
        sql.append(hasWhere ? " AND" : " WHERE");
        sql.append(" event='").append(logQuery.getEvent()).append("'");
        hasWhere = true;
      }

      // 添加时间范围查询条件
      if (logQuery.getBeginCreateTime() != null) {
        sql.append(hasWhere ? " AND" : " WHERE");
        sql.append(" time >= ").append(toMillisTimestamp(logQuery.getBeginCreateTime()));
        hasWhere = true;
      }
      if (logQuery.getEndCreateTime() != null) {
        sql.append(hasWhere ? " AND" : " WHERE");
        sql.append(" time <= ").append(toMillisTimestamp(logQuery.getEndCreateTime()));
      }

      sql.append(" ORDER BY time DESC LIMIT ")
          .append(logQuery.getPageSize())
          .append(" OFFSET ")
          .append((logQuery.getPageNum() - 1) * logQuery.getPageSize());

      log.debug("IoTDB查询SQL: {}", sql);

      // 执行查询
      List<IoTDeviceLogVO> resultList = new ArrayList<>();
      long total = 0;

      try {
        // 1. 先查询总数（不带分页）
        String countSql = buildCountSql(queryPath, logQuery);
        log.debug("IoTDB统计SQL: {}", countSql);

        try {
          // 确保 session 可用
          Session countSession = getSession();
          if (countSession == null) {
            log.warn("IoTDB session不可用，跳过统计查询");
          } else {
            var countDataSet = countSession.executeQueryStatement(countSql);
            if (countDataSet.hasNext()) {
              RowRecord countRecord = countDataSet.next();
              if (!countRecord.getFields().isEmpty()) {
                Field countField = countRecord.getFields().get(0);
                String countValue = getFieldValue(countField);
                total = countValue != null ? Long.parseLong(countValue) : 0;
              }
            }
            countDataSet.closeOperationHandle();
          }
        } catch (Exception e) {
          log.warn("IoTDB统计查询失败，使用结果集大小: {}", e.getMessage());
          // 如果是连接错误，标记 session 无效
          if (isReconnectNeeded(
              e instanceof Exception ? (Exception) e : new RuntimeException(e.getMessage()))) {
            invalidateSession();
          }
        }

        // 2. 查询数据
        Session querySession = getSession();
        if (querySession == null) {
          log.warn("IoTDB session不可用，跳过数据查询");
        } else {
          var dataSet = querySession.executeQueryStatement(sql.toString());

          while (dataSet.hasNext()) {
            RowRecord record = dataSet.next();
            IoTDeviceLogVO vo = new IoTDeviceLogVO();

            // 设置时间戳（IoTDB 自动返回）
            vo.setId(record.getTimestamp());
            // 转换为 LocalDateTime
            vo.setCreateTime(
                java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(record.getTimestamp()),
                    java.time.ZoneId.systemDefault()));

            // 解析字段值（注意：IoTDB返回的字段顺序与SELECT中的顺序一致）
            List<Field> fields = record.getFields();
            if (fields.size() >= 7) {
              vo.setMessageType(getFieldValue(fields.get(0)));
              vo.setContent(getFieldValue(fields.get(1)));
              vo.setEvent(getFieldValue(fields.get(2)));
              vo.setCommandId(getFieldValue(fields.get(3)));
              vo.setCommandStatus(parseIntValue(fields.get(4)));
              vo.setPoint(getFieldValue(fields.get(5)));
              vo.setDeviceName(getFieldValue(fields.get(6)));
            }

            // 设置其他字段
            vo.setProductKey(logQuery.getProductKey());
            vo.setDeviceId(logQuery.getDeviceId());
            vo.setIotId(logQuery.getIotId());

            resultList.add(vo);
          }

          dataSet.closeOperationHandle();
        }

        // 如果没有统计到总数，使用结果集大小
        if (total == 0 && !resultList.isEmpty()) {
          total = resultList.size();
        }

        log.debug("IoTDB查询成功，返回 {} 条记录，总数 {}", resultList.size(), total);

      } catch (IoTDBConnectionException | StatementExecutionException e) {
        log.error("IoTDB执行查询失败: sql={}", sql, e);
        // 连接错误时标记 session 无效
        if (isReconnectNeeded(e)) {
          invalidateSession();
        }
      }

      return new PageBean<>(resultList, total, logQuery.getPageSize(), logQuery.getPageNum());

    } catch (Exception e) {
      log.error("IoTDB查询设备日志失败", e);
      return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
    }
  }

  @Override
  public IoTDeviceLogVO queryById(LogQuery logQuery) {
    try {
      // 统一获取 session
      Session currentSession = getSession();
      if (currentSession == null) {
        log.warn("IoTDB session不可用且重连失败，返回null");
        return null;
      }

      // 构建查询路径
      String queryPath;
      if (StrUtil.isNotBlank(logQuery.getProductKey())
          && StrUtil.isNotBlank(logQuery.getDeviceId())) {
        queryPath = buildDevicePath(logQuery.getProductKey(), logQuery.getDeviceId()) + ".log";
      } else {
        log.warn("IoTDB查询日志详情参数不足");
        return null;
      }

      String sql =
          "SELECT message_type, content, event, command_id, command_status, point, device_name FROM "
              + queryPath
              + " WHERE time="
              + logQuery.getId()
              + " LIMIT 1";

      log.debug("IoTDB查询日志详情SQL: {}", sql);

      try {
        // 确保 session 可用
        Session detailSession = getSession();
        if (detailSession == null) {
          return null;
        }
        var dataSet = detailSession.executeQueryStatement(sql);

        if (dataSet.hasNext()) {
          RowRecord record = dataSet.next();
          IoTDeviceLogVO vo = new IoTDeviceLogVO();

          // 设置时间戳
          vo.setId(record.getTimestamp());
          vo.setCreateTime(
              java.time.LocalDateTime.ofInstant(
                  java.time.Instant.ofEpochMilli(record.getTimestamp()),
                  java.time.ZoneId.systemDefault()));

          // 解析字段值
          List<Field> fields = record.getFields();
          if (fields.size() >= 7) {
            vo.setMessageType(getFieldValue(fields.get(0)));
            vo.setContent(getFieldValue(fields.get(1)));
            vo.setEvent(getFieldValue(fields.get(2)));
            vo.setCommandId(getFieldValue(fields.get(3)));
            vo.setCommandStatus(parseIntValue(fields.get(4)));
            vo.setPoint(getFieldValue(fields.get(5)));
            vo.setDeviceName(getFieldValue(fields.get(6)));
          }

          // 设置其他字段
          vo.setProductKey(logQuery.getProductKey());
          vo.setDeviceId(logQuery.getDeviceId());
          vo.setIotId(logQuery.getIotId());

          dataSet.closeOperationHandle();
          return vo;
        }

        dataSet.closeOperationHandle();
        return null;

      } catch (IoTDBConnectionException | StatementExecutionException e) {
        log.error("IoTDB执行日志详情查询失败: sql={}", sql, e);
        // 连接错误时标记 session 无效
        if (isReconnectNeeded(e)) {
          invalidateSession();
        }
        return null;
      }

    } catch (Exception e) {
      log.error("IoTDB查询设备日志详情失败", e);
      return null;
    }
  }

  @Override
  public PageBean<IoTDeviceEvents> queryEventTotal(String productKey, String iotId) {
    List<IoTDeviceEvents> list = selectDevEvents(productKey);
    for (IoTDeviceEvents devEvent : list) {
      try {
        // 统一获取 session
        Session currentSession = getSession();
        if (currentSession == null) {
          log.warn("IoTDB session不可用且重连失败，跳过事件统计");
          devEvent.setQty("0");
          devEvent.setTime("");
          continue;
        }

        // 从 iotId 解析出 deviceId
        String[] parsed = parseIotId(iotId, productKey);
        if (parsed == null || parsed.length != 2) {
          log.warn("IoTDB无法解析 iotId 查询事件统计: iotId={}, productKey={}", iotId, productKey);
          continue;
        }

        // 使用buildDevicePath构建路径查询事件元数据
        String devicePath = buildDevicePath(parsed[0], parsed[1]) + ".event_metadata";

        // IoTDB 不支持混合聚合查询，需要分两次查询
        // 1. 查询事件数量
        String countSql =
            "SELECT COUNT(content) FROM " + devicePath + " WHERE event='" + devEvent.getId() + "'";

        log.debug("IoTDB查询事件数量SQL: {}", countSql);

        try {
          // 确保 session 可用
          Session eventCountSession = getSession();
          if (eventCountSession == null) {
            devEvent.setQty("0");
            devEvent.setTime("");
            continue;
          }
          // 查询数量
          var countDataSet = eventCountSession.executeQueryStatement(countSql);
          long count = 0;

          if (countDataSet.hasNext()) {
            RowRecord countRecord = countDataSet.next();
            List<Field> countFields = countRecord.getFields();
            if (!countFields.isEmpty()) {
              String countValue = getFieldValue(countFields.get(0));
              if (countValue != null) {
                count = Long.parseLong(countValue);
              }
            }
          }
          countDataSet.closeOperationHandle();

          devEvent.setQty(count >= 100 ? "99+" : String.valueOf(count));

          // 2. 查询最后时间（只有当有数据时才查询）
          if (count > 0) {
            String timeSql =
                "SELECT content FROM "
                    + devicePath
                    + " WHERE event='"
                    + devEvent.getId()
                    + "'"
                    + " ORDER BY time DESC LIMIT 1";

            log.debug("IoTDB查询事件最后时间SQL: {}", timeSql);

            Session eventTimeSession = getSession();
            if (eventTimeSession == null) {
              devEvent.setTime("");
              continue;
            }
            var timeDataSet = eventTimeSession.executeQueryStatement(timeSql);
            if (timeDataSet.hasNext()) {
              RowRecord timeRecord = timeDataSet.next();
              // 时间戳在 record.getTimestamp() 中
              long timestamp = timeRecord.getTimestamp();
              devEvent.setTime(java.time.Instant.ofEpochMilli(timestamp).toString());
            } else {
              devEvent.setTime("");
            }
            timeDataSet.closeOperationHandle();
          } else {
            devEvent.setTime("");
          }

        } catch (IoTDBConnectionException | StatementExecutionException e) {
          log.warn("IoTDB查询事件统计失败", e);
          devEvent.setQty("0");
          devEvent.setTime("");
          // 连接错误时标记 session 无效
          if (isReconnectNeeded(e)) {
            invalidateSession();
          }
        }

      } catch (Exception e) {
        log.error("IoTDB查询事件统计失败, productKey={}, iotId={}", productKey, iotId, e);
        devEvent.setQty("0");
        devEvent.setTime("");
      }
    }
    return new PageBean<>(list, 100L, 1, 100);
  }

  @Override
  public PageBean<IoTDeviceLogMetadataVO> queryLogMeta(LogQuery logQuery) {
    try {
      // 统一获取 session
      Session currentSession = getSession();
      if (currentSession == null) {
        log.warn("IoTDB session不可用且重连失败，返回空结果");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 构建查询路径 - 查询属性和事件的元数据
      List<String> queryPaths = new ArrayList<>();
      String basePath = null;

      // 优先级 1: 如果同时有 productKey 和 deviceId
      if (StrUtil.isNotBlank(logQuery.getProductKey())
          && StrUtil.isNotBlank(logQuery.getDeviceId())) {
        basePath = buildDevicePath(logQuery.getProductKey(), logQuery.getDeviceId());
        log.debug("IoTDB使用精确路径查询元数据: {}", basePath);
      }
      // 优先级 2: 如果有 iotId，尝试解析
      else if (StrUtil.isNotBlank(logQuery.getIotId())) {
        String[] parsed = parseIotId(logQuery.getIotId(), logQuery.getProductKey());
        if (parsed != null && parsed.length == 2) {
          basePath = buildDevicePath(parsed[0], parsed[1]);
          log.debug("IoTDB从 iotId 解析元数据路径: iotId={}, 解析后={}", logQuery.getIotId(), basePath);
        } else {
          log.warn("IoTDB无法解析 iotId: {}", logQuery.getIotId());
          return new PageBean<>(
              new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
        }
      }
      // 优先级 3: 如果只有 productKey
      else if (StrUtil.isNotBlank(logQuery.getProductKey())) {
        String cleanProductKey = sanitizePathNode(logQuery.getProductKey());
        String productBase = storageGroup + "." + cleanProductKey + ".*";
        boolean wantProperty =
            MessageType.PROPERTIES.name().equalsIgnoreCase(logQuery.getMessageType())
                || StrUtil.isNotBlank(logQuery.getProperty());
        boolean wantEvent =
            MessageType.EVENT.name().equalsIgnoreCase(logQuery.getMessageType())
                || StrUtil.isNotBlank(logQuery.getEvent());

        if (!wantProperty && !wantEvent) {
          queryPaths.add(productBase + ".property.*");
          queryPaths.add(productBase + ".event.*");
        } else {
          if (wantProperty) {
            queryPaths.add(productBase + ".property.*");
          }
          if (wantEvent) {
            queryPaths.add(productBase + ".event.*");
          }
        }
        log.debug("IoTDB使用产品模糊查询元数据");
      } else {
        log.warn("IoTDB查询元数据参数不足");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 如果有基础路径，添加属性或事件序列路径（按参数/类型选择）
      if (basePath != null && queryPaths.isEmpty()) {
        boolean wantProperty =
            MessageType.PROPERTIES.name().equalsIgnoreCase(logQuery.getMessageType())
                || StrUtil.isNotBlank(logQuery.getProperty());
        boolean wantEvent =
            MessageType.EVENT.name().equalsIgnoreCase(logQuery.getMessageType())
                || StrUtil.isNotBlank(logQuery.getEvent());

        if (!wantProperty && !wantEvent) {
          // 未指定类型与具体名时，两类都查询
          queryPaths.add(basePath + ".property.*");
          queryPaths.add(basePath + ".event.*");
        } else {
          if (wantProperty) {
            if (StrUtil.isNotBlank(logQuery.getProperty())) {
              queryPaths.add(basePath + ".property." + sanitizePathNode(logQuery.getProperty()));
            } else {
              queryPaths.add(basePath + ".property.*");
            }
          }
          if (wantEvent) {
            if (StrUtil.isNotBlank(logQuery.getEvent())) {
              queryPaths.add(basePath + ".event." + sanitizePathNode(logQuery.getEvent()));
            } else {
              queryPaths.add(basePath + ".event.*");
            }
          }
        }
      }

      List<IoTDeviceLogMetadataVO> resultList = new ArrayList<>();
      long total = 0;

      // 执行对每个路径的查询
      for (String queryPath : queryPaths) {
        StringBuilder sql = new StringBuilder();
        // 改造后字段精简：属性6列，事件4列
        if (queryPath.contains(".property.")) {
          sql.append(
                  "SELECT message_type, content, create_time, device_name, device_id, iot_id, product_key, ext1, ext2, ext3 FROM ")
              .append(queryPath);
        } else if (queryPath.contains(".event.")) {
          sql.append(
                  "SELECT message_type, content, create_time, device_name, device_id, iot_id, product_key, ext1 FROM ")
              .append(queryPath);
        } else {
          continue; // 跳过不是元数据的路径
        }

        // 仅保留时间范围过滤
        boolean hasWhere = false;

        // 添加时间范围查询条件
        if (logQuery.getBeginCreateTime() != null) {
          sql.append(hasWhere ? " AND" : " WHERE");
          sql.append(" time >= ").append(toMillisTimestamp(logQuery.getBeginCreateTime()));
          hasWhere = true;
        }
        if (logQuery.getEndCreateTime() != null) {
          sql.append(hasWhere ? " AND" : " WHERE");
          sql.append(" time <= ").append(toMillisTimestamp(logQuery.getEndCreateTime()));
        }

        // 先查询总数（仅按时间过滤）
        String countSql = buildMetadataCountSql(queryPath, logQuery);
        log.debug("IoTDB查询元数据总数SQL: {}", countSql);

        try {
          var countDataSet = session.executeQueryStatement(countSql);
          if (countDataSet.hasNext()) {
            RowRecord countRecord = countDataSet.next();
            if (!countRecord.getFields().isEmpty()) {
              Field countField = countRecord.getFields().get(0);
              String countValue = getFieldValue(countField);
              total += countValue != null ? Long.parseLong(countValue) : 0;
            }
          }
          countDataSet.closeOperationHandle();
        } catch (Exception e) {
          log.warn("IoTDB元数据统计查询失败: {}", e.getMessage());
        }

        sql.append(" ORDER BY time DESC LIMIT ")
            .append(logQuery.getPageSize())
            .append(" OFFSET ")
            .append((logQuery.getPageNum() - 1) * logQuery.getPageSize());

        log.debug("IoTDB查询元数据SQL: {}", sql);

        // 执行查询
        try {
          // 确保 session 可用
          Session metaSession = getSession();
          if (metaSession == null) {
            log.warn("IoTDB session不可用，跳过元数据查询");
            continue;
          }
          var dataSet = metaSession.executeQueryStatement(sql.toString());

          while (dataSet.hasNext()) {
            RowRecord record = dataSet.next();
            IoTDeviceLogMetadataVO vo = new IoTDeviceLogMetadataVO();

            // 设置时间戳
            vo.setCreateTime(
                java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(record.getTimestamp()),
                    java.time.ZoneId.systemDefault()));

            // 解析字段值——属性6列，事件4列
            List<Field> fields = record.getFields();
            if (queryPath.contains(".property.")) {
              if (fields.size() >= 10) {
                vo.setMessageType(getFieldValue(fields.get(0)));
                vo.setContent(getFieldValue(fields.get(1)));
                vo.setDeviceName(getFieldValue(fields.get(3)));
                vo.setDeviceId(getFieldValue(fields.get(4)));
                vo.setIotId(getFieldValue(fields.get(5)));
                vo.setProductKey(getFieldValue(fields.get(6)));
                vo.setExt1(getFieldValue(fields.get(7)));
                vo.setExt2(getFieldValue(fields.get(8)));
                vo.setExt3(getFieldValue(fields.get(9)));
              }
              String prop =
                  StrUtil.isNotBlank(logQuery.getProperty())
                      ? logQuery.getProperty()
                      : resolvePathNode(queryPath, ".property.");
              vo.setProperty(prop);
            } else if (queryPath.contains(".event.")) {
              if (fields.size() >= 8) {
                vo.setMessageType(getFieldValue(fields.get(0)));
                vo.setContent(getFieldValue(fields.get(1)));
                vo.setDeviceName(getFieldValue(fields.get(3)));
                vo.setDeviceId(getFieldValue(fields.get(4)));
                vo.setIotId(getFieldValue(fields.get(5)));
                vo.setProductKey(getFieldValue(fields.get(6)));
                vo.setExt1(getFieldValue(fields.get(7)));
              }
              String evt =
                  StrUtil.isNotBlank(logQuery.getEvent())
                      ? logQuery.getEvent()
                      : resolvePathNode(queryPath, ".event.");
              vo.setEvent(evt);
            }

            resultList.add(vo);
          }

          dataSet.closeOperationHandle();
          log.debug("IoTDB查询元数据成功，返回 {} 条记录", resultList.size());

        } catch (IoTDBConnectionException | StatementExecutionException e) {
          log.error("IoTDB执行元数据查询失败: sql={}", sql, e);
          // 连接错误时标记 session 无效
          if (isReconnectNeeded(e)) {
            invalidateSession();
          }
        }
      }

      // 如果没有统计到总数，使用结果集大小
      if (total == 0 && !resultList.isEmpty()) {
        total = resultList.size();
      }

      return new PageBean<>(resultList, total, logQuery.getPageSize(), logQuery.getPageNum());

    } catch (Exception e) {
      log.error("IoTDB查询设备元数据失败", e);
      return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
    }
  }

  @Override
  public String getPolicy() {
    return storePolicy;
  }


  /** 根据 iotId 构建查询路径 路径格式: root.device.{productKey}.{deviceId}.log 如果无法解析，使用通配符查询 */
  private String buildQueryPathFromIotId(String iotId) {
    if (StrUtil.isBlank(iotId)) {
      // 如果没有 iotId，使用通配符查询所有设备
      return storageGroup + ".*.*.log";
    }

    // 尝试解析 iotId（如果包含冒号分隔符）
    String[] parsed = parseIotId(iotId, null);
    if (parsed != null && parsed.length == 2) {
      return buildDevicePath(parsed[0], parsed[1]) + ".log";
    }

    // 无法解析，使用通配符查询并通过 WHERE 条件过滤
    return storageGroup + ".*.*.log";
  }

  /** 从IoTDB查询设备日志 */
  private List<IoTDeviceLog> queryDeviceLogsFromIoTDB(String sql) {
    List<IoTDeviceLog> logs = new ArrayList<>();

    Session session = null;
    try {
      session = getSession(); // 获取IoTDB会话

      // 执行查询
      SessionDataSet dataSet = session.executeQueryStatement(sql);
      List<String> columnNames = dataSet.getColumnNames();

      // 处理结果集
      while (dataSet.hasNext()) {
        RowRecord record = dataSet.next();
        IoTDeviceLog deviceLog = new IoTDeviceLog();

        // 设置时间戳（从RowRecord直接获取）
        long timestamp = record.getTimestamp();
        deviceLog.setId(timestamp);
        deviceLog.setCreateTime(
            LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));

        // 从记录中提取字段并设置到IoTDeviceLog对象中
        // 注意：columnNames包含"Time"在索引0，但fields不包含时间戳值
        // 所以columnNames和fields的索引有偏移
        List<Field> fields = record.getFields();
        int fieldIndex = 0;
        for (int i = 0; i < columnNames.size() && fieldIndex < fields.size(); i++) {
          String fullColumnName = columnNames.get(i);

          // IoTDB返回的列名是完整路径，如: root.device.xxx.log.command_status
          // 需要提取最后一部分作为字段名
          String columnName = extractFieldName(fullColumnName);

          // 跳过Time列，因为fields中不包含时间戳值
          if ("time".equalsIgnoreCase(columnName)) {
            continue;
          }

          Field field = fields.get(fieldIndex);
          fieldIndex++;
          String value = getFieldValue(field);

          switch (columnName.toLowerCase()) {
            case "iot_id":
              deviceLog.setIotId(value);
              break;
            case "device_id":
              deviceLog.setDeviceId(value);
              break;
            case "product_key":
              deviceLog.setProductKey(value);
              break;
            case "device_name":
              deviceLog.setDeviceName(value);
              break;
            case "message_type":
              deviceLog.setMessageType(value);
              break;
            case "command_id":
              deviceLog.setCommandId(value);
              break;
            case "command_status":
              if (value != null && !value.isEmpty()) {
                try {
                  // 兼容 "0.0" 这种格式
                  Double doubleValue = Double.parseDouble(value);
                  deviceLog.setCommandStatus(doubleValue.intValue());
                } catch (NumberFormatException e) {
                  log.warn("IoTDB解析command_status失败: {}", value);
                }
              }
              break;
            case "event":
              deviceLog.setEvent(value);
              break;
            case "content":
              deviceLog.setContent(value);
              break;
            case "point":
              deviceLog.setPoint(value);
              break;
          }
        }

        logs.add(deviceLog);
      }

      dataSet.close();
    } catch (Exception e) {
      log.error("IoTDB查询设备日志异常: {}", e.getMessage(), e);
    } finally {
      // 注意：这里不要关闭session，因为它是共享的
    }

    return logs;
  }

  @Override
  public JSONObject configMetadata() {
    JSONObject config = new JSONObject();
    config.put("host", host);
    config.put("port", port);
    config.put("username", username);
    config.put("storageGroup", storageGroup);
    return config;
  }

  /** 从 Field 中获取字符串值 */
  private String getFieldValue(Field field) {
    if (field == null || field.getDataType() == null) {
      return null;
    }

    try {
      Object value = field.getObjectValue(field.getDataType());
      return value != null ? value.toString() : null;
    } catch (Exception e) {
      log.warn("IoTDB解析字段值失败", e);
      return null;
    }
  }

  /** 从IoTDB完整列名中提取字段名 例如: root.device.xxx.log.command_status -> command_status */
  private String extractFieldName(String fullColumnName) {
    if (fullColumnName == null || fullColumnName.isEmpty()) {
      return "";
    }
    int lastDot = fullColumnName.lastIndexOf('.');
    if (lastDot >= 0 && lastDot < fullColumnName.length() - 1) {
      return fullColumnName.substring(lastDot + 1);
    }
    return fullColumnName;
  }

  /** 从 Field 中获取整型值 注意: IoTDB 可能返回浮点数格式（如 "0.0"），需要先转换为 Double 再取整 */
  private Integer parseIntValue(Field field) {
    String value = getFieldValue(field);
    if (value == null || value.isEmpty()) {
      return null;
    }

    try {
      // 先尝试转为 Double（兼容 "0.0" 这种格式）
      Double doubleValue = Double.parseDouble(value);
      return doubleValue.intValue();
    } catch (NumberFormatException e) {
      log.warn("IoTDB解析整型失败: {}", value);
      return null;
    }
  }

  /** 构建统计SQL */
  private String buildCountSql(String queryPath, LogQuery logQuery) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT COUNT(*) FROM ").append(queryPath);

    // 动态构建 WHERE 子句
    boolean hasWhere = false;
    if (StrUtil.isNotBlank(logQuery.getMessageType())) {
      sql.append(hasWhere ? " AND" : " WHERE");
      sql.append(" message_type='").append(logQuery.getMessageType()).append("'");
      hasWhere = true;
    }
    if (StrUtil.isNotBlank(logQuery.getEvent())) {
      sql.append(hasWhere ? " AND" : " WHERE");
      sql.append(" event='").append(logQuery.getEvent()).append("'");
      hasWhere = true;
    }

    // 添加时间范围查询条件
    if (logQuery.getBeginCreateTime() != null) {
      sql.append(hasWhere ? " AND" : " WHERE");
      sql.append(" time >= ").append(toMillisTimestamp(logQuery.getBeginCreateTime()));
      hasWhere = true;
    }
    if (logQuery.getEndCreateTime() != null) {
      sql.append(hasWhere ? " AND" : " WHERE");
      sql.append(" time <= ").append(toMillisTimestamp(logQuery.getEndCreateTime()));
    }

    return sql.toString();
  }

  /** 构建元数据统计SQL */
  private String buildMetadataCountSql(String queryPath, LogQuery logQuery) {
    StringBuilder sql = new StringBuilder();
    sql.append("SELECT COUNT(*) FROM ").append(queryPath);

    boolean hasWhere = false;

    // 添加时间范围查询条件
    if (logQuery.getBeginCreateTime() != null) {
      sql.append(hasWhere ? " AND" : " WHERE");
      sql.append(" time >= ").append(toMillisTimestamp(logQuery.getBeginCreateTime()));
      hasWhere = true;
    }
    if (logQuery.getEndCreateTime() != null) {
      sql.append(hasWhere ? " AND" : " WHERE");
      sql.append(" time <= ").append(toMillisTimestamp(logQuery.getEndCreateTime()));
    }

    return sql.toString();
  }
}

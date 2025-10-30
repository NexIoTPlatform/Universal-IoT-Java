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
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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

  private Session session;

  @PostConstruct
  public void initSession() {
    try {
      session = new Session.Builder().host(host).port(port).username(username).password(password).build();
      session.open();

      // 创建存储组
      try {
        session.setStorageGroup(storageGroup);
        log.info("创建存储组成功: {}", storageGroup);
      } catch (Exception e) {
        // 存储组可能已存在，忽略错误
        log.debug("存储组可能已存在: {}", storageGroup);
      }

      // 创建时间序列（类似MySQL的表结构）
      createTimeseries();

      log.info("初始化IoTDB连接成功，host={}, port={}", host, port);
    } catch (Exception e) {
      log.error("初始化IoTDB连接失败", e);
    }
  }

  /**
   * 创建时间序列（动态创建，类似自动建表）
   * IoTDB会在首次插入时自动创建，此方法用于预定义数据类型和编码
   */
  private void createTimeseries() {
    try {
      // 注意: IoTDB支持动态创建时间序列，这里仅做示例
      // 实际使用时可以省略此步骤，让IoTDB自动推断类型
      String[] measurements = {"message_type", "content", "event", "command_id", "command_status", "point"};
      log.info("时间序列定义已准备，将在数据插入时自动创建");
    } catch (Exception e) {
      log.warn("预创建时间序列失败（不影响使用）: {}", e.getMessage());
    }
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
      IoTDeviceDTO ioTDeviceDTO = iotDeviceService.selectDevInstanceBO(upRequest.getIotId());
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
        log.info("IoTDB插入设备日志成功，iotId={}", ioTDeviceDTO.getIotId());
      } catch (Exception e) {
        log.error("保存设备日志到IoTDB报错={}", e);
      }
    }
  }

  /**
   * 根据存储策略保存元数据日志
   * 支持属性和事件的详细信息存储
   */
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

                    savePropertyMetadataToIoTDB(
                        up, property, content, propertyName, formatValue, symbol);
                  } catch (Exception e) {
                    log.error(
                        "IoTDB保存属性元数据失败: iotId={}, property={}", up.getIotId(), key, e);
                  }
                }
              });
    }

    // 处理事件消息
    if (MessageType.EVENT.equals(up.getMessageType())) {
      try {
        int maxStorage = 10;
        if (CollectionUtil.isNotEmpty(logStorePolicyDTO.getEvent())
            && logStorePolicyDTO.getEvent().containsKey(up.getEvent())) {
          maxStorage = logStorePolicyDTO.getEvent().get(up.getEvent()).getMaxStorage();
        }

        saveEventMetadataToIoTDB(up, maxStorage);
      } catch (Exception e) {
        log.error("IoTDB保存事件元数据失败: iotId={}, event={}", up.getIotId(), up.getEvent(), e);
      }
    }
  }

  /**
   * 保存属性元数据到IoTDB
   * 路径: root.device.{productKey}.{deviceId}.property_metadata
   * 字段与MySQL一致：message_type,property,content,device_name,device_id,iot_id,product_key,create_time,ext1,ext2,ext3
   */
  private void savePropertyMetadataToIoTDB(
      BaseUPRequest up,
      String property,
      String content,
      String propertyName,
      String formatValue,
      String symbol)
      throws Exception {
    if (session == null) {
      throw new RuntimeException("IoTDB session未初始化");
    }

    String metadataPath =
        buildDevicePath(up.getProductKey(), up.getDeviceId()) + ".property_metadata";
    long timestamp = System.currentTimeMillis();

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
    List<String> values =
        List.of(
            escapeValue(MessageType.PROPERTIES.name()),
            escapeValue(property),
            escapeValue(content),
            escapeValue(up.getDeviceName()),
            escapeValue(up.getDeviceId()),
            escapeValue(up.getIotId()),
            escapeValue(up.getProductKey()),
            String.valueOf(System.currentTimeMillis()),
            escapeValue(propertyName),
            escapeValue(formatValue),
            escapeValue(symbol));

    try {
      session.insertRecord(metadataPath, timestamp, measurements, values);
      log.debug(
          "IoTDB插入属性元数据成功: path={}, property={}, iotId={}",
          metadataPath,
          property,
          up.getIotId());
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

  /**
   * 保存事件元数据到IoTDB
   * 路径: root.device.{productKey}.{deviceId}.event_metadata
   */
  private void saveEventMetadataToIoTDB(BaseUPRequest up, int maxStorage) throws Exception {
    if (session == null) {
      throw new RuntimeException("IoTDB session未初始化");
    }

    String metadataPath = buildDevicePath(up.getProductKey(), up.getDeviceId()) + ".event_metadata";
    long timestamp = System.currentTimeMillis();

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
    List<String> values =
        List.of(
            escapeValue(MessageType.EVENT.name()),
            escapeValue(up.getEvent()),
            escapeValue(up.getEventName()),
            escapeValue(up.getDeviceName()),
            escapeValue(up.getDeviceId()),
            escapeValue(up.getIotId()),
            escapeValue(up.getProductKey()),
            String.valueOf(System.currentTimeMillis()),
            escapeValue(JSONUtil.toJsonStr(up.getData())),
            String.valueOf(maxStorage));

    try {
      session.insertRecord(metadataPath, timestamp, measurements, values);
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
   * 保存设备日志到IoTDB
   * 数据路径结构:
   * - 设备日志: root.device.{productKey}.{deviceId}.log
   * - 元数据: root.device.{productKey}.{deviceId}.metadata
   */
  private void saveDeviceLogToIoTDB(IoTDeviceLog ioTDeviceLog, BaseUPRequest upRequest) throws Exception {
    if (session == null) {
      throw new RuntimeException("IoTDB session未初始化");
    }

    // 主日志路径 (对应iot_device_log_*)
    String devicePath = buildDevicePath(ioTDeviceLog.getProductKey(), ioTDeviceLog.getDeviceId()) + ".log";

    // 时间戳（毫秒）
    long timestamp = ioTDeviceLog.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

    // 使用List方式批量插入（推荐方式，性能更好）
    List<String> measurements = List.of("message_type", "content", "event", "command_id", "command_status", "point", "device_name");
    List<String> values = List.of(
        escapeValue(ioTDeviceLog.getMessageType()),
        escapeValue(ioTDeviceLog.getContent()),
        escapeValue(ioTDeviceLog.getEvent()),
        escapeValue(ioTDeviceLog.getCommandId()),
        String.valueOf(ioTDeviceLog.getCommandStatus() != null ? ioTDeviceLog.getCommandStatus() : 0),
        escapeValue(ioTDeviceLog.getPoint()),
        escapeValue(ioTDeviceLog.getDeviceName())
    );

    try {
      session.insertRecord(devicePath, timestamp, measurements, values);
      log.debug("IoTDB插入设备日志成功: path={}, timestamp={}, productKey={}, deviceId={}",
          devicePath, timestamp, ioTDeviceLog.getProductKey(), ioTDeviceLog.getDeviceId());
    } catch (Exception e) {
      log.error("IoTDB插入设备日志失败: path={}, productKey={}, deviceId={}, error={}",
          devicePath, ioTDeviceLog.getProductKey(), ioTDeviceLog.getDeviceId(), e.getMessage());
      throw e;
    }
  }

  /**
   * 保存元数据到IoTDB
   * 路径: root.device.{productKey}.{deviceId}.metadata
   * 注意: IoTDeviceLog暂无property字段,这里预留接口供未来扩展
   */
  private void saveMetadataToIoTDB(IoTDeviceLog ioTDeviceLog, String property) throws Exception {
    if (session == null) {
      throw new RuntimeException("IoTDB session未初始化");
    }

    String metadataPath = buildDevicePath(ioTDeviceLog.getProductKey(), ioTDeviceLog.getDeviceId()) + ".metadata";
    long timestamp = ioTDeviceLog.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();

    List<String> measurements = List.of("message_type", "property", "event", "content", "device_name", "iot_id", "product_key");
    List<String> values = List.of(
        escapeValue(ioTDeviceLog.getMessageType()),
        escapeValue(property),  // 从参数传入
        escapeValue(ioTDeviceLog.getEvent()),
        escapeValue(ioTDeviceLog.getContent()),
        escapeValue(ioTDeviceLog.getDeviceName()),
        escapeValue(ioTDeviceLog.getIotId()),
        escapeValue(ioTDeviceLog.getProductKey())
    );

    session.insertRecord(metadataPath, timestamp, measurements, values);
  }

  /**
   * 转义字符串值，防止SQL注入
   */
  private String escapeValue(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\").replace("'", "\\'");
  }

  /**
   * 从 iotId 中解析出 productKey 和 deviceId
   * iotId 格式: productKey + deviceId (直接拼接，无分隔符)
   * 或 productKey:deviceId (使用:分隔符)
   *
   * @param iotId 设备唯一ID
   * @param productKey 产品Key（可选，如果已知）
   * @return 数组 [productKey, deviceId]，解析失败返回 null
   */
  /**
   * 从 iotId 中解析出 productKey 和 deviceId
   * iotId 格式: productKey + deviceId (直接拼接，无分隔符)
   * 或 productKey:deviceId (使用:分隔符)
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
        return parts;  // [productKey, deviceId]
      }
    }

    // 如果已知 productKey，直接提取 deviceId
    if (StrUtil.isNotBlank(productKey) && iotId.startsWith(productKey)) {
      String deviceId = iotId.substring(productKey.length());
      if (StrUtil.isNotBlank(deviceId)) {
        return new String[]{productKey, deviceId};
      }
    }

    // 无法解析
    log.warn("无法从 iotId 解析出 productKey 和 deviceId: iotId={}, productKey={}", iotId, productKey);
    return null;
  }

  /**
   * 构建设备路径
   * 路径结构: root.device.{productKey}.{deviceId}
   * 这种结构自动实现了MySQL中按iot_id哈希分表的效果
   *
   * IoTDB路径命名规范:
   * 1. 必须以字母或下划线开头
   * 2. 后续可包含字母、数字、下划线
   * 3. 不能包含特殊字符
   */
  private String buildDevicePath(String productKey, String deviceId) {
    // 清理路径中的特殊字符（IoTDB路径命名规范）
    String cleanProductKey = sanitizePathNode(productKey);
    String cleanDeviceId = sanitizePathNode(deviceId);
    return storageGroup + "." + cleanProductKey + "." + cleanDeviceId;
  }

  /**
   * 清理路径节点，确保符合IoTDB命名规范
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

  @Override
  public PageBean<IoTDeviceLogVO> pageList(LogQuery logQuery) {
    try {
      if (session == null) {
        log.warn("IoTDB session未初始化");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 构建查询路径，使用与写入相同的路径清理逻辑
      String queryPath = null;

      // 优先级 1: 如果同时有 productKey 和 deviceId，精确查询
      if (StrUtil.isNotBlank(logQuery.getProductKey()) && StrUtil.isNotBlank(logQuery.getDeviceId())) {
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
          return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
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
      sql.append("SELECT message_type, content, event, command_id, command_status, point, device_name FROM ")
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

      sql.append(" ORDER BY time DESC LIMIT ").append(logQuery.getPageSize())
          .append(" OFFSET ").append((logQuery.getPageNum() - 1) * logQuery.getPageSize());

      log.debug("IoTDB查询SQL: {}", sql);

      // 执行查询
      List<IoTDeviceLogVO> resultList = new ArrayList<>();
      long total = 0;

      try {
        // 1. 先查询总数（不带分页）
        String countSql = buildCountSql(queryPath, logQuery);
        log.debug("IoTDB统计SQL: {}", countSql);

        try {
          var countDataSet = session.executeQueryStatement(countSql);
          if (countDataSet.hasNext()) {
            RowRecord countRecord = countDataSet.next();
            if (!countRecord.getFields().isEmpty()) {
              Field countField = countRecord.getFields().get(0);
              String countValue = getFieldValue(countField);
              total = countValue != null ? Long.parseLong(countValue) : 0;
            }
          }
          countDataSet.closeOperationHandle();
        } catch (Exception e) {
          log.warn("IoTDB统计查询失败，使用结果集大小: {}", e.getMessage());
        }

        // 2. 查询数据
        var dataSet = session.executeQueryStatement(sql.toString());

        while (dataSet.hasNext()) {
          RowRecord record = dataSet.next();
          IoTDeviceLogVO vo = new IoTDeviceLogVO();

          // 设置时间戳（IoTDB 自动返回）
          vo.setId(record.getTimestamp());
          // 转换为 LocalDateTime
          vo.setCreateTime(java.time.LocalDateTime.ofInstant(
              java.time.Instant.ofEpochMilli(record.getTimestamp()),
              java.time.ZoneId.systemDefault()
          ));

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

        // 如果没有统计到总数，使用结果集大小
        if (total == 0 && !resultList.isEmpty()) {
          total = resultList.size();
        }

        log.debug("IoTDB查询成功，返回 {} 条记录，总数 {}", resultList.size(), total);

      } catch (IoTDBConnectionException | StatementExecutionException e) {
        log.error("IoTDB执行查询失败: sql={}", sql, e);
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
      if (session == null) {
        log.warn("IoTDB session未初始化");
        return null;
      }

      // 构建查询路径
      String queryPath;
      if (StrUtil.isNotBlank(logQuery.getProductKey()) && StrUtil.isNotBlank(logQuery.getDeviceId())) {
        queryPath = buildDevicePath(logQuery.getProductKey(), logQuery.getDeviceId()) + ".log";
      } else {
        log.warn("IoTDB查询日志详情参数不足");
        return null;
      }

      String sql = "SELECT message_type, content, event, command_id, command_status, point, device_name FROM "
          + queryPath + " WHERE time=" + logQuery.getId() + " LIMIT 1";

      log.debug("IoTDB查询日志详情SQL: {}", sql);

      try {
        var dataSet = session.executeQueryStatement(sql);

        if (dataSet.hasNext()) {
          RowRecord record = dataSet.next();
          IoTDeviceLogVO vo = new IoTDeviceLogVO();

          // 设置时间戳
          vo.setId(record.getTimestamp());
          vo.setCreateTime(java.time.LocalDateTime.ofInstant(
              java.time.Instant.ofEpochMilli(record.getTimestamp()),
              java.time.ZoneId.systemDefault()
          ));

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
        if (session == null) {
          log.warn("IoTDB session未初始化");
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
        String sql = "SELECT COUNT(*) as qty, MAX(time) as last_time FROM " + devicePath +
            " WHERE event='" + devEvent.getId() + "'";

        log.debug("IoTDB查询事件统计SQL: {}", sql);

        try {
          var dataSet = session.executeQueryStatement(sql);

          if (dataSet.hasNext()) {
            RowRecord record = dataSet.next();
            List<Field> fields = record.getFields();

            if (fields.size() >= 2) {
              // 获取事件数量
              String qtyValue = getFieldValue(fields.get(0));
              if (qtyValue != null) {
                Long count = Long.parseLong(qtyValue);
                devEvent.setQty(count >= 100 ? "99+" : String.valueOf(count));
              } else {
                devEvent.setQty("0");
              }

              // 获取最后更新时间
              String timeValue = getFieldValue(fields.get(1));
              devEvent.setTime(timeValue != null ? timeValue : "");
            }
          } else {
            devEvent.setQty("0");
            devEvent.setTime("");
          }

          dataSet.closeOperationHandle();

        } catch (IoTDBConnectionException | StatementExecutionException e) {
          log.warn("IoTDB查询事件统计失败: sql={}", sql, e);
          devEvent.setQty("0");
          devEvent.setTime("");
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
      if (session == null) {
        log.warn("IoTDB session未初始化");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 构建查询路径 - 查询属性和事件的元数据
      List<String> queryPaths = new ArrayList<>();
      String basePath = null;

      // 优先级 1: 如果同时有 productKey 和 deviceId
      if (StrUtil.isNotBlank(logQuery.getProductKey()) && StrUtil.isNotBlank(logQuery.getDeviceId())) {
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
          return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
        }
      }
      // 优先级 3: 如果只有 productKey
      else if (StrUtil.isNotBlank(logQuery.getProductKey())) {
        String cleanProductKey = sanitizePathNode(logQuery.getProductKey());
        basePath = storageGroup + "." + cleanProductKey + ".*.property_metadata";
        queryPaths.add(basePath);
        basePath = storageGroup + "." + cleanProductKey + ".*.event_metadata";
        queryPaths.add(basePath);
        log.debug("IoTDB使用产品模糊查询元数据");
      }
      else {
        log.warn("IoTDB查询元数据参数不足");
        return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
      }

      // 如果有基础路径，添加属性和事件元数据路径
      if (basePath != null && queryPaths.isEmpty()) {
        queryPaths.add(basePath + ".property_metadata");
        queryPaths.add(basePath + ".event_metadata");
      }

      List<IoTDeviceLogMetadataVO> resultList = new ArrayList<>();

      // 执行对每个路径的查询
      for (String queryPath : queryPaths) {
        StringBuilder sql = new StringBuilder();
        // 不同路径查询不同字段：属性查11个，事件查9个
        if (queryPath.contains(".property_metadata")) {
          sql.append("SELECT message_type, property, content, device_name, device_id, iot_id, product_key, create_time, ext1, ext2, ext3 FROM ")
              .append(queryPath);
        } else if (queryPath.contains(".event_metadata")) {
          sql.append("SELECT message_type, event, content, device_name, device_id, iot_id, product_key, create_time, ext1 FROM ")
              .append(queryPath);
        } else {
          continue; // 跳过不是元数据的路径
        }

        // 动态构建 WHERE 子句
        boolean hasWhere = false;
        if (StrUtil.isNotBlank(logQuery.getProperty())) {
          sql.append(hasWhere ? " AND" : " WHERE");
          sql.append(" property='").append(logQuery.getProperty()).append("'");
          hasWhere = true;
        }
        if (StrUtil.isNotBlank(logQuery.getEvent())) {
          sql.append(hasWhere ? " AND" : " WHERE");
          sql.append(" event='").append(logQuery.getEvent()).append("'");
          hasWhere = true;
        }

        sql.append(" ORDER BY time DESC LIMIT ").append(logQuery.getPageSize())
            .append(" OFFSET ").append((logQuery.getPageNum() - 1) * logQuery.getPageSize());

        log.debug("IoTDB查询元数据SQL: {}", sql);

        // 执行查询
        try {
          var dataSet = session.executeQueryStatement(sql.toString());

          while (dataSet.hasNext()) {
            RowRecord record = dataSet.next();
            IoTDeviceLogMetadataVO vo = new IoTDeviceLogMetadataVO();

            // 设置时间戳
            vo.setCreateTime(java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(record.getTimestamp()),
                java.time.ZoneId.systemDefault()
            ));

            // 解析字段值——属性查11个字段，事件查9个字段
            List<Field> fields = record.getFields();
            if (queryPath.contains(".property_metadata")) {
              // 属性元数据: message_type, property, content, device_name, device_id, iot_id, product_key, create_time, ext1(propertyName), ext2(formatValue), ext3(symbol)
              if (fields.size() >= 11) {
                vo.setMessageType(getFieldValue(fields.get(0)));
                vo.setProperty(getFieldValue(fields.get(1)));
                vo.setContent(getFieldValue(fields.get(2)));
                vo.setDeviceName(getFieldValue(fields.get(3)));
                vo.setDeviceId(getFieldValue(fields.get(4)));
                vo.setIotId(getFieldValue(fields.get(5)));
                vo.setProductKey(getFieldValue(fields.get(6)));
                // fields.get(7) = create_time，已在vo.setCreateTime()中设置
                vo.setExt1(getFieldValue(fields.get(8)));   // propertyName
                vo.setExt2(getFieldValue(fields.get(9)));   // formatValue
                vo.setExt3(getFieldValue(fields.get(10)));  // symbol
              }
            } else if (queryPath.contains(".event_metadata")) {
              // 事件元数据: message_type, event, content, device_name, device_id, iot_id, product_key, create_time, ext1(JSONData)
              if (fields.size() >= 9) {
                vo.setMessageType(getFieldValue(fields.get(0)));
                vo.setEvent(getFieldValue(fields.get(1)));
                vo.setContent(getFieldValue(fields.get(2)));
                vo.setDeviceName(getFieldValue(fields.get(3)));
                vo.setDeviceId(getFieldValue(fields.get(4)));
                vo.setIotId(getFieldValue(fields.get(5)));
                vo.setProductKey(getFieldValue(fields.get(6)));
                // fields.get(7) = create_time，已在vo.setCreateTime()中设置
                vo.setExt1(getFieldValue(fields.get(8)));   // JSON数据
              }
            }

            resultList.add(vo);
          }

          dataSet.closeOperationHandle();
          log.debug("IoTDB查询元数据成功，返回 {} 条记录", resultList.size());

        } catch (IoTDBConnectionException | StatementExecutionException e) {
          log.error("IoTDB执行元数据查询失败: sql={}", sql, e);
        }
      }

      return new PageBean<>(resultList, (long) resultList.size(), logQuery.getPageSize(), logQuery.getPageNum());

    } catch (Exception e) {
      log.error("IoTDB查询设备元数据失败", e);
      return new PageBean<>(new ArrayList<>(), 0L, logQuery.getPageSize(), logQuery.getPageNum());
    }
  }

  @Override
  public String getPolicy() {
    return storePolicy;
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

  /**
   * 从 Field 中获取字符串值
   */
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

  /**
   * 从 Field 中获取整型值
   * 注意: IoTDB 可能返回浮点数格式（如 "0.0"），需要先转换为 Double 再取整
   */
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

  /**
   * 构建统计SQL
   */
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

    return sql.toString();
  }
}
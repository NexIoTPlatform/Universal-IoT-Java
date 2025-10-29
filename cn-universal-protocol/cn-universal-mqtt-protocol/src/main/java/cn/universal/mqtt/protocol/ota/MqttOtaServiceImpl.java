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

package cn.universal.mqtt.protocol.ota;

import cn.universal.dm.device.service.ota.api.OtaService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MQTT协议OTA服务实现
 *
 * <p>基于MQTT协议实现的OTA（Over-The-Air）升级服务
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/20
 */
@Slf4j(topic = "mqtt")
@Service("mqttOtaService")
@RequiredArgsConstructor
public class MqttOtaServiceImpl implements OtaService {

  /** 存储设备OTA状态 */
  private final Map<String, Map<String, Object>> deviceOtaStatus = new ConcurrentHashMap<>();

  /** 存储固件包信息 */
  private final Map<String, Map<String, Object>> firmwareRepository = new ConcurrentHashMap<>();

  /** 任务ID生成器 */
  private final AtomicLong taskIdGenerator = new AtomicLong(System.currentTimeMillis());

  /** 统计信息 */
  private final Map<String, OtaStatistics> statisticsMap = new ConcurrentHashMap<>();

  @Override
  public String getServiceType() {
    return "MQTT";
  }

  @Override
  public String getServiceDescription() {
    return "基于MQTT协议的OTA升级服务，支持设备固件远程升级";
  }

  @Override
  public boolean supportsProduct(String productKey) {
    // MQTT OTA服务支持所有产品
    return productKey != null && !productKey.trim().isEmpty();
  }

  @Override
  public boolean isServiceAvailable() {
    // 简化实现，实际应该检查MQTT服务状态
    return true;
  }

  @Override
  public OtaServiceResult handleFirmwareReport(Map<String, Object> reportData) {
    try {
      log.info("处理设备固件信息上报: {}", reportData);

      String productKey = (String) reportData.get("productKey");
      String deviceId = (String) reportData.get("deviceId");
      String currentVersion = (String) reportData.get("currentVersion");

      if (productKey == null || deviceId == null) {
        log.warn("设备信息不完整: productKey={}, deviceId={}", productKey, deviceId);
        return OtaServiceResult.PROTOCOL_ERROR;
      }

      // 更新设备状态
      String deviceKey = productKey + ":" + deviceId;
      Map<String, Object> status =
          deviceOtaStatus.computeIfAbsent(deviceKey, k -> new ConcurrentHashMap<>());
      status.put("productKey", productKey);
      status.put("deviceId", deviceId);
      status.put("currentVersion", currentVersion);
      status.put("lastReportTime", LocalDateTime.now());
      status.put("reportType", "FIRMWARE_INFO");

      // 更新统计信息
      updateStatistics(productKey, "firmwareReports", 1);

      return OtaServiceResult.SUCCESS;
    } catch (Exception e) {
      log.error("处理固件信息上报失败", e);
      return OtaServiceResult.PROTOCOL_ERROR;
    }
  }

  @Override
  public OtaServiceResult handleUpgradeProgress(Map<String, Object> progressData) {
    try {
      log.info("处理设备升级进度上报: {}", progressData);

      String productKey = (String) progressData.get("productKey");
      String deviceId = (String) progressData.get("deviceId");
      String taskId = (String) progressData.get("taskId");
      Integer progress = (Integer) progressData.get("progress");

      if (productKey == null || deviceId == null || taskId == null) {
        log.warn("进度上报信息不完整");
        return OtaServiceResult.PROTOCOL_ERROR;
      }

      // 更新设备状态
      String deviceKey = productKey + ":" + deviceId;
      Map<String, Object> status =
          deviceOtaStatus.computeIfAbsent(deviceKey, k -> new ConcurrentHashMap<>());
      status.put("currentTaskId", taskId);
      status.put("progress", progress);
      status.put("lastProgressTime", LocalDateTime.now());
      status.put("upgradeStatus", "IN_PROGRESS");

      // 更新统计信息
      updateStatistics(productKey, "progressReports", 1);

      return OtaServiceResult.SUCCESS;
    } catch (Exception e) {
      log.error("处理升级进度上报失败", e);
      return OtaServiceResult.PROTOCOL_ERROR;
    }
  }

  @Override
  public OtaServiceResult handleUpgradeResult(Map<String, Object> resultData) {
    try {
      log.info("处理设备升级结果上报: {}", resultData);

      String productKey = (String) resultData.get("productKey");
      String deviceId = (String) resultData.get("deviceId");
      String taskId = (String) resultData.get("taskId");
      String upgradeStatus = (String) resultData.get("upgradeStatus");
      String targetVersion = (String) resultData.get("targetVersion");

      if (productKey == null || deviceId == null || taskId == null || upgradeStatus == null) {
        log.warn("升级结果信息不完整");
        return OtaServiceResult.PROTOCOL_ERROR;
      }

      // 更新设备状态
      String deviceKey = productKey + ":" + deviceId;
      Map<String, Object> status =
          deviceOtaStatus.computeIfAbsent(deviceKey, k -> new ConcurrentHashMap<>());
      status.put("currentTaskId", taskId);
      status.put("upgradeStatus", upgradeStatus);
      status.put("lastResultTime", LocalDateTime.now());

      if ("SUCCESS".equals(upgradeStatus) && targetVersion != null) {
        status.put("currentVersion", targetVersion);
        status.put("lastSuccessfulUpgrade", LocalDateTime.now());
        updateStatistics(productKey, "successfulUpgrades", 1);
      } else if ("FAILED".equals(upgradeStatus)) {
        status.put("lastFailedUpgrade", LocalDateTime.now());
        status.put("lastErrorCode", resultData.get("errorCode"));
        status.put("lastErrorMessage", resultData.get("errorMessage"));
        updateStatistics(productKey, "failedUpgrades", 1);
      }

      // 更新统计信息
      updateStatistics(productKey, "resultReports", 1);

      return OtaServiceResult.SUCCESS;
    } catch (Exception e) {
      log.error("处理升级结果上报失败", e);
      return OtaServiceResult.PROTOCOL_ERROR;
    }
  }

  @Override
  public OtaServiceResult sendUpgradeCommand(Map<String, Object> updateData) {
    try {
      log.info("发送OTA升级命令: {}", updateData);

      String productKey = (String) updateData.get("productKey");
      String deviceId = (String) updateData.get("deviceId");
      String targetVersion = (String) updateData.get("targetVersion");

      if (productKey == null || deviceId == null || targetVersion == null) {
        log.warn("升级命令信息不完整");
        return OtaServiceResult.PROTOCOL_ERROR;
      }

      // 生成任务ID
      String taskId = generateTaskId();
      updateData.put("taskId", taskId);
      updateData.put("commandTime", LocalDateTime.now());

      // 检查设备是否在线（简化实现）
      String deviceKey = productKey + ":" + deviceId;
      Map<String, Object> status = deviceOtaStatus.get(deviceKey);
      if (status == null) {
        log.warn("设备状态未知，可能离线: {}", deviceKey);
        return OtaServiceResult.DEVICE_OFFLINE;
      }

      // 构建MQTT主题（简化实现）
      String topic = String.format("$ota/update/%s/%s", productKey, deviceId);

      // 模拟MQTT消息发送（实际应该调用MQTT客户端）
      log.info("发送MQTT升级命令到主题: {}, 消息: {}", topic, updateData);

      // 更新设备状态
      status.put("currentTaskId", taskId);
      status.put("targetVersion", targetVersion);
      status.put("upgradeStatus", "COMMAND_SENT");
      status.put("lastCommandTime", LocalDateTime.now());

      // 更新统计信息
      updateStatistics(productKey, "commandsSent", 1);
      updateStatistics(productKey, "activeUpgrades", 1);

      return OtaServiceResult.SUCCESS;
    } catch (Exception e) {
      log.error("发送升级命令失败", e);
      return OtaServiceResult.NETWORK_ERROR;
    }
  }

  @Override
  public OtaServiceResult cancelUpgrade(String productKey, String deviceId, String taskId) {
    try {
      log.info("取消OTA升级: productKey={}, deviceId={}, taskId={}", productKey, deviceId, taskId);

      String deviceKey = productKey + ":" + deviceId;
      Map<String, Object> status = deviceOtaStatus.get(deviceKey);

      if (status == null) {
        return OtaServiceResult.DEVICE_OFFLINE;
      }

      String currentTaskId = (String) status.get("currentTaskId");
      if (!taskId.equals(currentTaskId)) {
        log.warn("任务ID不匹配: 当前任务={}, 要取消的任务={}", currentTaskId, taskId);
        return OtaServiceResult.VERSION_CONFLICT;
      }

      // 构建取消命令
      Map<String, Object> cancelCommand = new HashMap<>();
      cancelCommand.put("taskId", taskId);
      cancelCommand.put("commandType", "CANCEL_UPGRADE");
      cancelCommand.put("timestamp", System.currentTimeMillis());

      // 构建MQTT主题
      String topic = String.format("$ota/update/%s/%s", productKey, deviceId);

      // 模拟发送取消命令
      log.info("发送MQTT取消命令到主题: {}, 消息: {}", topic, cancelCommand);

      // 更新设备状态
      status.put("upgradeStatus", "CANCELLED");
      status.put("lastCancelTime", LocalDateTime.now());

      // 更新统计信息
      updateStatistics(productKey, "cancelledUpgrades", 1);
      updateStatistics(productKey, "activeUpgrades", -1);

      return OtaServiceResult.SUCCESS;
    } catch (Exception e) {
      log.error("取消升级失败", e);
      return OtaServiceResult.NETWORK_ERROR;
    }
  }

  @Override
  public Map<String, Object> queryUpgradeStatus(String productKey, String deviceId, String taskId) {
    Map<String, Object> result = new HashMap<>();

    try {
      String deviceKey = productKey + ":" + deviceId;
      Map<String, Object> status = deviceOtaStatus.get(deviceKey);

      if (status == null) {
        result.put("success", false);
        result.put("message", "设备状态不存在");
        return result;
      }

      String currentTaskId = (String) status.get("currentTaskId");
      if (taskId != null && !taskId.equals(currentTaskId)) {
        result.put("success", false);
        result.put("message", "任务ID不匹配");
        return result;
      }

      result.put("success", true);
      result.put("taskId", currentTaskId);
      result.put("upgradeStatus", status.get("upgradeStatus"));
      result.put("progress", status.get("progress"));
      result.put("currentVersion", status.get("currentVersion"));
      result.put("targetVersion", status.get("targetVersion"));
      result.put("lastReportTime", status.get("lastReportTime"));

      return result;
    } catch (Exception e) {
      log.error("查询升级状态失败", e);
      result.put("success", false);
      result.put("message", "查询失败: " + e.getMessage());
      return result;
    }
  }

  @Override
  public List<Map<String, Object>> getAvailablePackages(String productKey) {
    List<Map<String, Object>> packages = new ArrayList<>();

    for (Map<String, Object> firmware : firmwareRepository.values()) {
      String fwProductKey = (String) firmware.get("productKey");
      if (productKey == null || productKey.equals(fwProductKey)) {
        packages.add(new HashMap<>(firmware));
      }
    }

    // 按版本排序（简化实现）
    packages.sort(
        (a, b) -> {
          String versionA = (String) a.get("firmwareVersion");
          String versionB = (String) b.get("firmwareVersion");
          return versionB.compareTo(versionA); // 降序
        });

    return packages;
  }

  @Override
  public Map<String, Object> getPackageInfo(String productKey, String packageVersion) {
    for (Map<String, Object> firmware : firmwareRepository.values()) {
      String fwProductKey = (String) firmware.get("productKey");
      String fwVersion = (String) firmware.get("firmwareVersion");

      if ((productKey == null || productKey.equals(fwProductKey))
          && packageVersion.equals(fwVersion)) {
        return new HashMap<>(firmware);
      }
    }

    return null;
  }

  @Override
  public boolean validatePackage(Map<String, Object> packageInfo) {
    // 基本验证
    return packageInfo != null
        && packageInfo.containsKey("firmwareName")
        && packageInfo.containsKey("firmwareVersion")
        && packageInfo.containsKey("downloadUrl")
        && packageInfo.containsKey("fileSize")
        && packageInfo.containsKey("checksum");
  }

  @Override
  public OtaStatistics getStatistics(String productKey) {
    return statisticsMap.computeIfAbsent(
        productKey != null ? productKey : "GLOBAL", k -> new OtaStatistics());
  }

  @Override
  public int getActiveUpgradeCount(String productKey) {
    if (productKey == null) {
      return (int)
          deviceOtaStatus.values().stream()
              .filter(
                  status ->
                      "IN_PROGRESS".equals(status.get("upgradeStatus"))
                          || "COMMAND_SENT".equals(status.get("upgradeStatus")))
              .count();
    } else {
      return (int)
          deviceOtaStatus.values().stream()
              .filter(
                  status ->
                      productKey.equals(status.get("productKey"))
                          && ("IN_PROGRESS".equals(status.get("upgradeStatus"))
                              || "COMMAND_SENT".equals(status.get("upgradeStatus"))))
              .count();
    }
  }

  @Override
  public double getUpgradeSuccessRate(String productKey) {
    OtaStatistics stats = getStatistics(productKey);
    return stats.getSuccessRate();
  }

  /** 生成任务ID */
  private String generateTaskId() {
    return "MQTT_OTA_" + taskIdGenerator.incrementAndGet();
  }

  /** 更新统计信息 */
  private void updateStatistics(String productKey, String metric, long value) {
    String statsKey = productKey != null ? productKey : "GLOBAL";
    OtaStatistics stats = statisticsMap.computeIfAbsent(statsKey, k -> new OtaStatistics());

    switch (metric) {
      case "firmwareReports":
      case "progressReports":
      case "resultReports":
      case "commandsSent":
        stats.setTotalUpgrades(stats.getTotalUpgrades() + value);
        break;
      case "successfulUpgrades":
        stats.setSuccessfulUpgrades(stats.getSuccessfulUpgrades() + value);
        break;
      case "failedUpgrades":
        stats.setFailedUpgrades(stats.getFailedUpgrades() + value);
        break;
      case "activeUpgrades":
        stats.setActiveUpgrades(Math.max(0, stats.getActiveUpgrades() + value));
        break;
      case "cancelledUpgrades":
        // 取消的升级不影响成功/失败统计
        break;
    }

    stats.setLastUpgradeTime(LocalDateTime.now().toString());
  }
}

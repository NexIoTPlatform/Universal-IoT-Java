package cn.universal.dm.device.service;

import cn.universal.dm.device.service.push.processor.PushStatisticsProcessor;
import cn.universal.persistence.entity.IoTDashboardStatistics;
import cn.universal.persistence.mapper.IoTDashboardStatisticsMapper;
import cn.universal.persistence.mapper.IoTDeviceMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 仪表盘服务
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Slf4j
@Service
public class DashboardService {

  @Autowired private IoTDashboardStatisticsMapper dashboardStatisticsMapper;

  @Autowired private StringRedisTemplate redisTemplate;

  @Autowired private SystemMonitorService systemMonitorService;

  @Autowired private PushStatisticsProcessor pushStatisticsProcessor;

  @Autowired private IoTDeviceMapper ioTDeviceMapper;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * 记录统计数据
   *
   * @param statistics 统计数据
   */
  public void recordStatistics(IoTDashboardStatistics statistics) {
    try {
      // 先查找是否存在记录
      IoTDashboardStatistics existing =
          dashboardStatisticsMapper.selectByUniqueKey(
              statistics.getStatDate(),
              statistics.getProductKey(),
              statistics.getChannel(),
              statistics.getMetricType());

      if (existing == null) {
        // 不存在则插入新记录
        statistics.setCreateTime(LocalDateTime.now());
        statistics.setUpdateTime(LocalDateTime.now());
        dashboardStatisticsMapper.insert(statistics);
        log.debug("[仪表盘统计] 插入新记录: {}", statistics);
      } else {
        // 存在则更新记录
        dashboardStatisticsMapper.updateByUniqueKey(
            statistics.getStatDate(),
            statistics.getProductKey(),
            statistics.getChannel(),
            statistics.getMetricType(),
            statistics.getMetricValue(),
            LocalDateTime.now());
        log.debug("[仪表盘统计] 更新现有记录: {}", statistics);
      }
    } catch (Exception e) {
      log.error("[仪表盘统计] 记录统计数据失败: {}", statistics, e);
    }
  }

  /**
   * 批量记录统计数据
   *
   * @param statisticsList 统计数据列表
   */
  public void batchRecordStatistics(List<IoTDashboardStatistics> statisticsList) {
    if (statisticsList == null || statisticsList.isEmpty()) {
      return;
    }

    for (IoTDashboardStatistics statistics : statisticsList) {
      recordStatistics(statistics);
    }
  }

  /** 调试方法：查看数据库中的统计记录 */
  public List<IoTDashboardStatistics> debugStatistics(LocalDate date) {
    log.info("[仪表盘调试] 查询日期: {}", date);

    // 查询所有记录
    List<IoTDashboardStatistics> allRecords =
        dashboardStatisticsMapper.selectByDateAndMetric(date, null, null, null);

    log.info("[仪表盘调试] 找到 {} 条记录", allRecords.size());

    for (IoTDashboardStatistics record : allRecords) {
      log.info(
          "[仪表盘调试] 记录: date={}, product={}, channel={}, type={}, value={}",
          record.getStatDate(),
          record.getProductKey(),
          record.getChannel(),
          record.getMetricType(),
          record.getMetricValue());
    }

    return allRecords;
  }

  /** 获取实时数据 */
  public Map<String, Object> getRealtimeData() {
    return getTodayOverview(); // 实时数据就是今日概览
  }

  /** 获取图表数据 */
  public Map<String, Object> getChartData(LocalDate date, String type, String channel) {
    Map<String, Object> chartData = new HashMap<>();

    try {
      String dateKey = date.format(DATE_FORMATTER);

      switch (type) {
        case "device_trend":
          chartData = getDeviceTrendData(dateKey);
          break;
        case "message_trend":
          chartData = getMessageTrendData(dateKey, channel);
          break;
        case "channel_distribution":
          chartData = getChannelDistributionData(dateKey);
          break;
        case "performance_metrics":
          chartData = getPerformanceMetricsData(dateKey);
          break;
        default:
          chartData.put("error", "不支持的图表类型: " + type);
      }
    } catch (Exception e) {
      log.error("[仪表盘] 获取图表数据失败，类型: {}", type, e);
      chartData.put("error", "获取图表数据失败");
    }

    return chartData;
  }

  /** 获取系统监控数据 */
  public Map<String, Object> getSystemMonitorData() {
    Map<String, Object> monitorData = new HashMap<>();

    try {
      // 获取系统监控数据
      Map<String, Object> systemMetrics = systemMonitorService.getSystemMetrics();

      // 获取系统信息
      Map<String, Object> systemInfo = systemMonitorService.getSystemInfo();

      monitorData.put("systemMetrics", systemMetrics);
      monitorData.put("systemInfo", systemInfo);
      monitorData.put("isLinuxSystem", systemMonitorService.isLinuxSystem());
      monitorData.put("timestamp", System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[仪表盘] 获取系统监控数据失败", e);
      monitorData.put("error", "获取系统监控数据失败");
    }

    return monitorData;
  }

  /** 获取统计管理器状态 */
  public Map<String, Object> getStatisticsManagerStatus() {
    return pushStatisticsProcessor.getManagerStatus();
  }

  /** 强制刷新统计到Redis */
  public void forceFlushStatistics() {
    pushStatisticsProcessor.forceFlushToRedis();
  }

  /** 获取系统监控服务 */
  public SystemMonitorService getSystemMonitorService() {
    return systemMonitorService;
  }

  /** 获取今日概览数据 */
  public Map<String, Object> getTodayOverview() {
    Map<String, Object> overview = new HashMap<>();

    try {
      LocalDate today = LocalDate.now();
      String dateKey = today.format(DATE_FORMATTER);

      // 设备统计
      Map<String, Object> deviceStats = getDeviceStats(dateKey);
      overview.put("deviceStats", deviceStats);

      // 消息统计
      Map<String, Object> messageStats = getMessageStats(dateKey);
      overview.put("messageStats", messageStats);

      // 性能统计
      Map<String, Object> performanceStats = getPerformanceStats(dateKey);
      overview.put("performanceStats", performanceStats);

      // 渠道统计
      Map<String, Object> channelStats = getChannelStats(dateKey);
      overview.put("channelStats", channelStats);

      overview.put("timestamp", System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[仪表盘] 获取今日概览数据失败", e);
      overview.put("error", "获取今日概览数据失败");
    }

    return overview;
  }

  /** 获取今日概览数据（带用户权限过滤） */
  public Map<String, Object> getTodayOverviewWithUserFilter(String unionId, boolean isAdmin) {
    Map<String, Object> overview = new HashMap<>();

    try {
      LocalDate today = LocalDate.now();
      String dateKey = today.format(DATE_FORMATTER);

      // 设备统计（根据用户权限过滤）
      Map<String, Object> deviceStats = getDeviceStatsWithUserFilter(dateKey, unionId, isAdmin);
      overview.put("deviceStats", deviceStats);

      // 消息统计（根据用户权限过滤）
      Map<String, Object> messageStats = getMessageStatsWithUserFilter(dateKey, unionId, isAdmin);
      overview.put("messageStats", messageStats);

      // 性能统计
      Map<String, Object> performanceStats = getPerformanceStats(dateKey);
      overview.put("performanceStats", performanceStats);

      // 渠道统计（根据用户权限过滤）
      Map<String, Object> channelStats = getChannelStatsWithUserFilter(dateKey, unionId, isAdmin);
      overview.put("channelStats", channelStats);

      overview.put("timestamp", System.currentTimeMillis());

    } catch (Exception e) {
      log.error("[仪表盘] 获取今日概览数据失败", e);
      overview.put("error", "获取今日概览数据失败");
    }

    return overview;
  }

  /** 从数据库获取设备统计数据（带用户权限过滤） */
  public Map<String, Object> getDeviceStatsFromDB(String unionId, boolean isAdmin) {
    Map<String, Object> deviceStats = new HashMap<>();

    try {
      long totalDevices = 0;
      long onlineDevices = 0;

      if (isAdmin) {
        // 管理员：查询所有设备
        totalDevices = ioTDeviceMapper.countAllDevices();
        onlineDevices = ioTDeviceMapper.countOnlineDevices();
      } else {
        // 普通用户：只查询自己的设备
        totalDevices = ioTDeviceMapper.countDevicesByCreator(unionId);
        onlineDevices = ioTDeviceMapper.countOnlineDevicesByCreator(unionId);
      }

      long offlineDevices = totalDevices - onlineDevices;
      double onlineRate = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0.0;

      deviceStats.put("totalDevices", totalDevices);
      deviceStats.put("onlineDevices", onlineDevices);
      deviceStats.put("offlineDevices", offlineDevices);
      deviceStats.put("onlineRate", onlineRate);

      log.debug(
          "[仪表盘] 设备统计: 总数={}, 在线={}, 离线={}, 在线率={}%",
          totalDevices, onlineDevices, offlineDevices, onlineRate);

    } catch (Exception e) {
      log.error("[仪表盘] 获取设备统计数据失败", e);
      deviceStats.put("error", "获取设备统计数据失败");
    }

    return deviceStats;
  }

  // ==================== 私有方法 ====================

  /** 获取设备统计数据（带用户权限过滤） */
  private Map<String, Object> getDeviceStatsWithUserFilter(
      String dateKey, String unionId, boolean isAdmin) {
    Map<String, Object> deviceStats = new HashMap<>();

    try {
      long totalDevices = 0;
      long onlineDevices = 0;

      if (isAdmin) {
        // 管理员：查询所有设备
        totalDevices = ioTDeviceMapper.countAllDevices();
        onlineDevices = ioTDeviceMapper.countOnlineDevices();
      } else {
        // 普通用户：只查询自己的设备
        totalDevices = ioTDeviceMapper.countDevicesByCreator(unionId);
        onlineDevices = ioTDeviceMapper.countOnlineDevicesByCreator(unionId);
      }

      long offlineDevices = totalDevices - onlineDevices;
      double onlineRate = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0.0;

      deviceStats.put("totalDevices", totalDevices);
      deviceStats.put("onlineDevices", onlineDevices);
      deviceStats.put("offlineDevices", offlineDevices);
      deviceStats.put("onlineRate", onlineRate);

      log.debug(
          "[仪表盘] 设备统计(用户过滤): 总数={}, 在线={}, 离线={}, 在线率={}%",
          totalDevices, onlineDevices, offlineDevices, onlineRate);

    } catch (Exception e) {
      log.error("[仪表盘] 获取设备统计数据失败", e);
      deviceStats.put("totalDevices", 0L);
      deviceStats.put("onlineDevices", 0L);
      deviceStats.put("offlineDevices", 0L);
      deviceStats.put("onlineRate", 0.0);
    }

    return deviceStats;
  }

  /** 获取消息统计数据（带用户权限过滤） */
  private Map<String, Object> getMessageStatsWithUserFilter(
      String dateKey, String unionId, boolean isAdmin) {
    Map<String, Object> messageStats = new HashMap<>();

    try {
      if (isAdmin) {
        // 管理员：获取所有消息统计
        return getMessageStats(dateKey);
      } else {
        // 普通用户：获取自己的消息统计
        // 这里需要根据用户权限过滤，暂时返回空数据
        messageStats.put("totalMessages", 0L);
        messageStats.put("successMessages", 0L);
        messageStats.put("failedMessages", 0L);
        messageStats.put("successRate", 0.0);
        messageStats.put("avgPerHour", 0.0);
      }
    } catch (Exception e) {
      log.error("[仪表盘] 获取消息统计数据失败", e);
    }

    return messageStats;
  }

  /** 获取渠道统计数据（带用户权限过滤） */
  private Map<String, Object> getChannelStatsWithUserFilter(
      String dateKey, String unionId, boolean isAdmin) {
    Map<String, Object> channelStats = new HashMap<>();

    try {
      if (isAdmin) {
        // 管理员：获取所有渠道统计
        return getChannelStats(dateKey);
      } else {
        // 普通用户：获取自己的渠道统计
        // 这里需要根据用户权限过滤，暂时返回空数据
        channelStats.put("channels", new HashMap<>());
      }
    } catch (Exception e) {
      log.error("[仪表盘] 获取渠道统计数据失败", e);
    }

    return channelStats;
  }

  /** 获取设备统计数据 */
  private Map<String, Object> getDeviceStats(String dateKey) {
    Map<String, Object> deviceStats = new HashMap<>();

    try {
      // 直接从数据库查询设备总数
      long totalDevices = ioTDeviceMapper.countAllDevices();

      // 直接从数据库查询在线设备数
      long onlineDevices = ioTDeviceMapper.countOnlineDevices();

      long offlineDevices = totalDevices - onlineDevices;
      double onlineRate = totalDevices > 0 ? (double) onlineDevices / totalDevices * 100 : 0.0;

      deviceStats.put("totalDevices", totalDevices);
      deviceStats.put("onlineDevices", onlineDevices);
      deviceStats.put("offlineDevices", offlineDevices);
      deviceStats.put("onlineRate", onlineRate);

      log.debug(
          "[仪表盘] 设备统计: 总数={}, 在线={}, 离线={}, 在线率={}%",
          totalDevices, onlineDevices, offlineDevices, onlineRate);

    } catch (Exception e) {
      log.error("[仪表盘] 获取设备统计数据失败", e);
      deviceStats.put("totalDevices", 0L);
      deviceStats.put("onlineDevices", 0L);
      deviceStats.put("offlineDevices", 0L);
      deviceStats.put("onlineRate", 0.0);
    }

    return deviceStats;
  }

  /** 获取消息统计数据 */
  private Map<String, Object> getMessageStats(String dateKey) {
    Map<String, Object> messageStats = new HashMap<>();

    try {
      // 消息总数
      String totalKey = String.format("dashboard:metric:%s:all:all:all:message_total", dateKey);
      String totalValue = redisTemplate.opsForValue().get(totalKey);
      long totalMessages = totalValue != null ? Long.valueOf(totalValue) : 0L;

      // 成功消息数
      String successKey = String.format("dashboard:metric:%s:all:all:all:message_success", dateKey);
      String successValue = redisTemplate.opsForValue().get(successKey);
      long successMessages = successValue != null ? Long.valueOf(successValue) : 0L;

      // 失败消息数
      String failedKey = String.format("dashboard:metric:%s:all:all:all:message_failed", dateKey);
      String failedValue = redisTemplate.opsForValue().get(failedKey);
      long failedMessages = failedValue != null ? Long.valueOf(failedValue) : 0L;

      messageStats.put("totalMessages", totalMessages);
      messageStats.put("successMessages", successMessages);
      messageStats.put("failedMessages", failedMessages);
      messageStats.put(
          "successRate", totalMessages > 0 ? (double) successMessages / totalMessages * 100 : 0.0);
      messageStats.put("avgPerHour", totalMessages / 24.0); // 假设24小时

    } catch (Exception e) {
      log.error("[仪表盘] 获取消息统计数据失败", e);
    }

    return messageStats;
  }

  /** 获取性能统计数据 */
  private Map<String, Object> getPerformanceStats(String dateKey) {
    Map<String, Object> performanceStats = new HashMap<>();

    try {
      // 平均响应时间
      String responseTimeKey =
          String.format("dashboard:metric:%s:all:all:all:response_time_total", dateKey);
      String responseCountKey =
          String.format("dashboard:metric:%s:all:all:all:response_count", dateKey);

      String totalTime = redisTemplate.opsForValue().get(responseTimeKey);
      String count = redisTemplate.opsForValue().get(responseCountKey);

      double avgResponseTime = 0.0;
      if (totalTime != null && count != null) {
        long total = Long.valueOf(totalTime);
        long responseCount = Long.valueOf(count);
        avgResponseTime = responseCount > 0 ? (double) total / responseCount : 0.0;
      }

      performanceStats.put("avgResponseTime", avgResponseTime);
      performanceStats.put("messageProcessingSpeed", getMessageProcessingSpeed(dateKey));
      performanceStats.put("systemThroughput", getSystemThroughput());
      performanceStats.put("connectionStability", getConnectionStability(dateKey));

    } catch (Exception e) {
      log.error("[仪表盘] 获取性能统计数据失败", e);
    }

    return performanceStats;
  }

  /** 获取渠道统计数据 */
  private Map<String, Object> getChannelStats(String dateKey) {
    Map<String, Object> channelStats = new HashMap<>();

    try {
      String[] channels = {"HTTP", "MQTT", "TCP", "Kafka"};
      Map<String, Long> channelData = new HashMap<>();

      for (String channel : channels) {
        String channelKey =
            String.format("dashboard:metric:%s:all:%s:all:message_total", dateKey, channel);
        String value = redisTemplate.opsForValue().get(channelKey);
        long count = value != null ? Long.valueOf(value) : 0L;
        channelData.put(channel, count);
      }

      channelStats.put("channels", channelData);

    } catch (Exception e) {
      log.error("[仪表盘] 获取渠道统计数据失败", e);
    }

    return channelStats;
  }

  /** 获取消息处理速度（消息/秒） */
  private double getMessageProcessingSpeed(String dateKey) {
    try {
      String totalKey = String.format("dashboard:metric:%s:all:all:all:message_total", dateKey);
      String value = redisTemplate.opsForValue().get(totalKey);
      long totalMessages = value != null ? Long.valueOf(value) : 0L;
      return totalMessages > 0 ? (double) totalMessages / 86400 : 0.0; // 86400秒/天
    } catch (Exception e) {
      log.error("[仪表盘] 获取消息处理速度失败", e);
    }
    return 0.0;
  }

  /** 获取系统吞吐量（MB/s） */
  private double getSystemThroughput() {
    // 这里可以集成系统监控数据
    // 暂时返回模拟数据
    return Math.random() * 10.0 + 1.0; // 1.0-11.0 MB/s
  }

  /** 获取连接稳定性（%） */
  private double getConnectionStability(String dateKey) {
    try {
      String totalKey = String.format("dashboard:metric:%s:all:all:all:device_total", dateKey);
      String onlineKey = String.format("dashboard:metric:%s:all:all:all:device_online", dateKey);

      String totalValue = redisTemplate.opsForValue().get(totalKey);
      String onlineValue = redisTemplate.opsForValue().get(onlineKey);

      if (totalValue != null && onlineValue != null) {
        long total = Long.valueOf(totalValue);
        long online = Long.valueOf(onlineValue);
        return total > 0 ? (double) online / total * 100 : 0.0;
      }
    } catch (Exception e) {
      log.error("[仪表盘] 获取连接稳定性失败", e);
    }
    return 0.0;
  }

  /** 获取设备趋势数据 */
  private Map<String, Object> getDeviceTrendData(String dateKey) {
    Map<String, Object> data = new HashMap<>();
    // 这里可以实现24小时设备在线趋势数据
    // 暂时返回模拟数据
    data.put("title", "设备在线趋势");
    data.put("series", new Object[0]); // 实际应该返回24小时数据
    return data;
  }

  /** 获取消息趋势数据 */
  private Map<String, Object> getMessageTrendData(String dateKey, String channel) {
    Map<String, Object> data = new HashMap<>();
    // 这里可以实现24小时消息趋势数据
    data.put("title", "消息量趋势");
    data.put("series", new Object[0]); // 实际应该返回24小时数据
    return data;
  }

  /** 获取渠道分布数据 */
  private Map<String, Object> getChannelDistributionData(String dateKey) {
    Map<String, Object> data = new HashMap<>();
    data.put("title", "渠道消息分布");
    data.put("series", new Object[0]); // 实际应该返回渠道分布数据
    return data;
  }

  /** 获取性能指标数据 */
  private Map<String, Object> getPerformanceMetricsData(String dateKey) {
    Map<String, Object> data = new HashMap<>();
    data.put("title", "系统性能指标");
    data.put("series", new Object[0]); // 实际应该返回性能指标数据
    return data;
  }
}

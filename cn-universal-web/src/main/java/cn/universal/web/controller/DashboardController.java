package cn.universal.web.controller;

import cn.universal.dm.device.service.DashboardService;
import cn.universal.dm.device.service.DashboardStatisticsTask;
import cn.universal.security.BaseController;
import cn.universal.security.utils.SecurityUtils;
import java.time.LocalDate;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器 根据仪表盘界面功能设计接口
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {

  @Autowired private DashboardService dashboardService;

  @Autowired private DashboardStatisticsTask dashboardStatisticsTask;

  /**
   * 获取仪表盘首页数据 包含：设备总数、在线设备、今日消息、成功率等概览数据 对应图片中的4个顶部概览卡片
   *
   * @return 仪表盘首页数据
   */
  @GetMapping("/overview")
  public Map<String, Object> getDashboardOverview() {
    log.info("[仪表盘] 获取首页概览数据");

    // 获取当前用户信息
    String unionId = SecurityUtils.getUnionId();
    boolean isAdmin = isAdmin();

    return dashboardService.getTodayOverviewWithUserFilter(unionId, isAdmin);
  }

  /**
   * 获取设备消息分类图表数据 对应图片中的"设备消息分类"图表 支持：今日、本周、本月时间范围
   *
   * @param timeRange 时间范围：today(今日)、week(本周)、month(本月)
   * @return 设备消息分类图表数据
   */
  @GetMapping("/message-classification")
  public Map<String, Object> getMessageClassification(
      @RequestParam(value = "timeRange", defaultValue = "today") String timeRange) {

    log.info("[仪表盘] 获取设备消息分类数据，时间范围: {}", timeRange);
    LocalDate date = LocalDate.now();

    // 消息统计默认返回管理员数据
    return dashboardService.getChartData(date, "message_classification", timeRange);
  }

  /**
   * 获取系统性能监控数据 对应图片中的"系统性能监控"部分 包含：CPU使用率、内存使用率、磁盘使用率、网络流量 支持：5秒、10秒、30秒刷新间隔
   *
   * @param refreshInterval 刷新间隔：5、10、30（秒）
   * @return 系统性能监控数据
   */
  @GetMapping("/system-monitor")
  public Map<String, Object> getSystemMonitor(
      @RequestParam(value = "refreshInterval", defaultValue = "5") Integer refreshInterval) {

    log.info("[仪表盘] 获取系统性能监控数据，刷新间隔: {}秒", refreshInterval);
    return dashboardService.getSystemMonitorData();
  }

  /**
   * 获取实时统计数据 用于实时更新仪表盘数据
   *
   * @return 实时统计数据
   */
  @GetMapping("/realtime")
  public Map<String, Object> getRealtimeData() {
    log.info("[仪表盘] 获取实时数据");

    // 获取当前用户信息
    String unionId = SecurityUtils.getUnionId();
    boolean isAdmin = isAdmin();

    return dashboardService.getTodayOverviewWithUserFilter(unionId, isAdmin);
  }

  /**
   * 检查统计数据状态
   *
   * @return 统计数据状态
   */
  @GetMapping("/statistics-status")
  public Map<String, Object> getStatisticsStatus() {
    log.info("[仪表盘] 获取统计数据状态");
    return dashboardService.getStatisticsManagerStatus();
  }

  /**
   * 强制刷新统计数据到Redis
   *
   * @return 刷新结果
   */
  @PostMapping("/flush-statistics")
  public Map<String, Object> flushStatistics() {
    log.info("[仪表盘] 强制刷新统计数据到Redis");
    dashboardService.forceFlushStatistics();
    return Map.of("success", true, "message", "统计数据已强制刷新到Redis");
  }

  /**
   * 手动刷新统计数据
   *
   * @return 刷新结果
   */
  @PostMapping("/statistics/refresh")
  public Map<String, Object> refreshStatistics() {
    log.info("[仪表盘] 手动刷新统计数据");
    dashboardStatisticsTask.manualRefreshStatistics();
    return Map.of("success", true, "message", "统计数据刷新完成");
  }

  /**
   * 检查统计数据是否存在
   *
   * @param date 日期（可选，默认为今天）
   * @return 检查结果
   */
  @GetMapping("/statistics/check")
  public Map<String, Object> checkStatistics(@RequestParam(required = false) String date) {
    LocalDate checkDate;
    if (date != null && !date.isEmpty()) {
      checkDate = LocalDate.parse(date);
    } else {
      checkDate = LocalDate.now();
    }

    log.info("[仪表盘] 检查统计数据是否存在，日期: {}", checkDate);
    boolean exists = dashboardStatisticsTask.checkStatisticsExists(checkDate);
    return Map.of("date", checkDate.toString(), "exists", exists);
  }

  /**
   * 清理重复的统计数据
   *
   * @param date 日期（可选，默认为今天）
   * @return 清理结果
   */
  @PostMapping("/statistics/cleanup")
  public Map<String, Object> cleanupDuplicateStatistics(
      @RequestParam(required = false) String date) {
    LocalDate cleanupDate;
    if (date != null && !date.isEmpty()) {
      cleanupDate = LocalDate.parse(date);
    } else {
      cleanupDate = LocalDate.now();
    }

    log.info("[仪表盘] 清理重复统计数据，日期: {}", cleanupDate);
    dashboardStatisticsTask.cleanupDuplicateStatistics(cleanupDate);
    return Map.of("success", true, "message", "重复统计数据清理完成", "date", cleanupDate.toString());
  }

  /**
   * 获取系统监控调试信息 用于排查系统监控问题
   *
   * @return 调试信息
   */
  @GetMapping("/system-debug")
  public Map<String, Object> getSystemDebugInfo() {
    log.info("[仪表盘] 获取系统监控调试信息");
    return dashboardService.getSystemMonitorService().getDebugInfo();
  }

  /**
   * 获取图表数据 支持多种图表类型
   *
   * @param date 统计日期
   * @param type 图表类型：device_trend(设备趋势)、message_trend(消息趋势)、
   *     channel_distribution(渠道分布)、performance_metrics(性能指标)
   * @param channel 渠道（可选）
   * @return 图表数据
   */
  @GetMapping("/chart-data")
  public Map<String, Object> getChartData(
      @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")
          LocalDate date,
      @RequestParam("type") String type,
      @RequestParam(value = "channel", required = false) String channel) {

    if (date == null) {
      date = LocalDate.now();
    }

    log.info("[仪表盘] 获取图表数据，类型: {}, 日期: {}, 渠道: {}", type, date, channel);

    // 根据图表类型决定是否使用用户权限过滤
    if ("device_trend".equals(type)) {
      // 设备趋势图使用用户权限过滤
      String unionId = SecurityUtils.getUnionId();
      boolean isAdmin = isAdmin();
      return dashboardService.getDeviceStatsFromDB(unionId, isAdmin);
    } else {
      // 其他图表默认返回管理员数据
      return dashboardService.getChartData(date, type, channel);
    }
  }
}

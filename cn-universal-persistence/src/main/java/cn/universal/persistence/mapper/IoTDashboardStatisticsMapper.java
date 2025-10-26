package cn.universal.persistence.mapper;

import cn.universal.persistence.entity.IoTDashboardStatistics;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.BaseMapper;

/**
 * 仪表盘统计Mapper接口
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Mapper
public interface IoTDashboardStatisticsMapper extends BaseMapper<IoTDashboardStatistics> {

  /**
   * 根据日期和指标类型查询统计数据
   *
   * @param statDate 统计日期
   * @param metricType 指标类型
   * @param productKey 产品Key
   * @param channel 渠道
   * @return 统计数据列表
   */
  List<IoTDashboardStatistics> selectByDateAndMetric(
      @Param("statDate") LocalDate statDate,
      @Param("metricType") String metricType,
      @Param("productKey") String productKey,
      @Param("channel") String channel);

  /**
   * 查询指定日期范围的统计数据
   *
   * @param startDate 开始日期
   * @param endDate 结束日期
   * @param metricType 指标类型
   * @param productKey 产品Key
   * @param channel 渠道
   * @return 统计数据列表
   */
  List<IoTDashboardStatistics> selectByDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("metricType") String metricType,
      @Param("productKey") String productKey,
      @Param("channel") String channel);

  /**
   * 根据唯一键查询统计数据
   *
   * @param statDate 统计日期
   * @param productKey 产品Key
   * @param channel 渠道
   * @param metricType 指标类型
   * @return 统计数据，如果不存在返回null
   */
  IoTDashboardStatistics selectByUniqueKey(
      @Param("statDate") LocalDate statDate,
      @Param("productKey") String productKey,
      @Param("channel") String channel,
      @Param("metricType") String metricType);

  /**
   * 根据唯一键更新统计数据
   *
   * @param statDate 统计日期
   * @param productKey 产品Key
   * @param channel 渠道
   * @param metricType 指标类型
   * @param metricValue 指标值
   * @param updateTime 更新时间
   * @return 影响行数
   */
  int updateByUniqueKey(
      @Param("statDate") LocalDate statDate,
      @Param("productKey") String productKey,
      @Param("channel") String channel,
      @Param("metricType") String metricType,
      @Param("metricValue") Long metricValue,
      @Param("updateTime") LocalDateTime updateTime);

  /**
   * 插入统计数据
   *
   * @param statistics 统计数据
   * @return 影响行数
   */
  int insert(IoTDashboardStatistics statistics);

  /**
   * 删除指定日期的所有统计数据
   *
   * @param statDate 统计日期
   * @return 删除的记录数
   */
  int deleteByDate(@Param("statDate") LocalDate statDate);
}

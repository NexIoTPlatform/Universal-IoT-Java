package cn.universal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tk.mybatis.mapper.annotation.KeySql;

/**
 * 仪表盘统计实体
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/9
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "iot_dashboard_statistics")
public class IoTDashboardStatistics implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @KeySql(useGeneratedKeys = true)
  private Long id;

  /** 统计日期 */
  @Column(name = "stat_date")
  private LocalDate statDate;

  /** 产品Key，NULL表示全产品 */
  @Column(name = "product_key")
  private String productKey;

  /** 推送渠道，NULL表示全渠道 */
  @Column(name = "channel")
  private String channel;

  /** 指标类型 */
  @Column(name = "metric_type")
  private String metricType;

  /** 指标值 */
  @Column(name = "metric_value")
  private Long metricValue;

  /** 创建时间 */
  @Column(name = "create_time")
  private LocalDateTime createTime;

  /** 更新时间 */
  @Column(name = "update_time")
  private LocalDateTime updateTime;

  /** 指标类型枚举 */
  public enum MetricType {
    DEVICE_TOTAL("device_total", "设备总数"),
    DEVICE_ONLINE("device_online", "在线设备数"),
    MESSAGE_TOTAL("message_total", "消息总数"),
    MESSAGE_SUCCESS("message_success", "成功消息数"),
    MESSAGE_FAILED("message_failed", "失败消息数"),
    MESSAGE_RETRY("message_retry", "重试消息数"),
    MESSAGE_PUSH("message_push", "推送消息数");

    private final String code;
    private final String description;

    MetricType(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }
}

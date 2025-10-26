package cn.universal.persistence.entity.bo;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

/**
 * 上行推送配置BO
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/7/12 10:47
 */
@Data
@Builder
public class UPPushBO implements Serializable {

  private static final long serialVersionUID = 1L;

  // HTTP 推送配置
  private HttpPushConfig http;

  // MQTT 推送配置
  private MqttPushConfig mqtt;

  // Kafka 推送配置
  private KafkaPushConfig kafka;

  // RocketMQ 推送配置
  private RocketMQPushConfig rocketMQ;

  // 统计配置
  private StatisticsConfig statistics;

  // 重试配置
  private RetryConfig retry;

  /** HTTP 推送配置 */
  @Data
  @Builder
  public static class HttpPushConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private String url;
    private boolean enable;
    private String header;
    private String secret;
    private boolean support;
  }

  /** MQTT 推送配置 */
  @Data
  @Builder
  public static class MqttPushConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private String url;
    private String topic;
    private boolean enable;
    private boolean support;
    private String password;
    private String username;
  }

  /** Kafka 推送配置 */
  @Data
  @Builder
  public static class KafkaPushConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean enable;
    private boolean support;
  }

  /** RocketMQ 推送配置 */
  @Data
  @Builder
  public static class RocketMQPushConfig implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean enable;
    private boolean support;
  }

  /** 统计配置 */
  @Data
  @Builder
  public static class StatisticsConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否启用统计 */
    private boolean enable = true;

    /** 统计数据保留时间（天） */
    private Integer retentionDays = 30;

    /** 异步统计 */
    private boolean async = true;
  }

  /** 重试配置 */
  @Data
  @Builder
  public static class RetryConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否启用重试 */
    private boolean enable = true;

    /** 最大重试次数 */
    private Integer maxCount = 3;

    /** 重试间隔（分钟） */
    private Integer intervalMinutes = 5;

    /** 重试队列保留时间（天） */
    private Integer queueRetentionDays = 1;

    /** 失败记录保留时间（天） */
    private Integer failedRetentionDays = 7;
  }
}

package cn.universal.rocketmq;

import cn.universal.core.config.InstanceIdProvider;
import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RocketMQ过滤策略管理器 自动检测SQL92是否开启，并选择相应的过滤方案
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/27
 */
@Component
@Slf4j
public class RocketMQFilterStrategy {

  @Autowired private RocketMQTemplate rocketMQTemplate;

  @Autowired private InstanceIdProvider instanceIdProvider;

  private final AtomicBoolean sql92Enabled = new AtomicBoolean(false);
  private final AtomicBoolean initialized = new AtomicBoolean(false);

  @PostConstruct
  public void init() {
    detectSql92Support();
  }

  /** 检测SQL92是否支持 */
  private void detectSql92Support() {
    try {
      // 尝试发送一个测试消息来检测SQL92支持
      // 如果SQL92不支持，会在启动时抛出异常
      log.info("正在检测RocketMQ SQL92支持...");

      // 这里可以通过检查RocketMQ的配置或者尝试创建SQL92消费者来检测
      // 由于检测比较复杂，我们提供一个配置开关
      String sql92EnabledConfig = System.getProperty("rocketmq.sql92.enabled", "false");
      boolean enabled = Boolean.parseBoolean(sql92EnabledConfig);

      if (enabled) {
        sql92Enabled.set(true);
        log.info("SQL92过滤已启用");
      } else {
        sql92Enabled.set(false);
        log.info("SQL92过滤未启用，将使用Tag过滤");
      }

    } catch (Exception e) {
      log.warn("SQL92检测失败，默认使用Tag过滤: {}", e.getMessage());
      sql92Enabled.set(false);
    } finally {
      initialized.set(true);
    }
  }

  /**
   * 获取当前过滤策略
   *
   * @return 过滤策略
   */
  public FilterStrategy getCurrentStrategy() {
    if (!initialized.get()) {
      detectSql92Support();
    }

    if (sql92Enabled.get()) {
      return FilterStrategy.SQL92;
    } else {
      return FilterStrategy.TAG;
    }
  }

  /**
   * 获取SQL92过滤表达式
   *
   * @return SQL92过滤表达式
   */
  public String getSql92Expression() {
    return "sourceId != '" + instanceIdProvider.getInstanceId() + "'";
  }

  /**
   * 获取Tag过滤表达式
   *
   * @return Tag过滤表达式
   */
  public String getTagExpression() {
    return "!instance-" + instanceIdProvider.getInstanceId();
  }

  /**
   * 获取当前实例的Tag
   *
   * @return 实例Tag
   */
  public String getInstanceTag() {
    return "instance-" + instanceIdProvider.getInstanceId();
  }

  /**
   * 获取SelectorType
   *
   * @return SelectorType
   */
  public SelectorType getSelectorType() {
    return sql92Enabled.get() ? SelectorType.SQL92 : SelectorType.TAG;
  }

  /**
   * 获取SelectorExpression
   *
   * @return SelectorExpression
   */
  public String getSelectorExpression() {
    return sql92Enabled.get() ? getSql92Expression() : getTagExpression();
  }

  /**
   * 手动设置SQL92启用状态
   *
   * @param enabled 是否启用
   */
  public void setSql92Enabled(boolean enabled) {
    sql92Enabled.set(enabled);
    log.info("手动设置SQL92过滤: {}", enabled ? "启用" : "禁用");
  }

  /**
   * 检查SQL92是否启用
   *
   * @return true表示启用
   */
  public boolean isSql92Enabled() {
    return sql92Enabled.get();
  }

  /**
   * 获取过滤策略信息
   *
   * @return 策略信息
   */
  public String getStrategyInfo() {
    FilterStrategy strategy = getCurrentStrategy();
    return String.format(
        "当前过滤策略: %s, 实例ID: %s", strategy.name(), instanceIdProvider.getSimpleInstanceId());
  }

  /** 过滤策略枚举 */
  public enum FilterStrategy {
    SQL92("SQL92过滤"),
    TAG("Tag过滤");

    private final String description;

    FilterStrategy(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}

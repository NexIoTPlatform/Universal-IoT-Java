package cn.universal.rocketmq;

import cn.universal.core.config.InstanceIdProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * RocketMQ自动配置 在应用启动时自动检测和配置过滤策略
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/27
 */
@Configuration
@Slf4j
public class RocketMQAutoConfig {

  @Autowired private RocketMQFilterStrategy filterStrategy;

  @Autowired private InstanceIdProvider instanceIdProvider;

  /** 应用启动完成后自动检测SQL92状态 */
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    log.info("=== RocketMQ过滤策略自动配置 ===");

    try {
      // 获取当前策略
      RocketMQFilterStrategy.FilterStrategy strategy = filterStrategy.getCurrentStrategy();
      log.info("当前过滤策略: {}", strategy.getDescription());
      log.info("实例ID: {}", instanceIdProvider.getInstanceId());

      // 输出配置信息
      if (strategy == RocketMQFilterStrategy.FilterStrategy.SQL92) {
        log.info("SQL92过滤表达式: {}", filterStrategy.getSql92Expression());
        log.info("✅ SQL92过滤已启用，将使用高性能的SQL92过滤");
      } else {
        log.info("Tag过滤表达式: {}", filterStrategy.getTagExpression());
        log.info("✅ Tag过滤已启用，将使用兼容性更好的Tag过滤");
      }

      log.info("=== 过滤策略配置完成 ===");

    } catch (Exception e) {
      log.error("过滤策略配置失败: {}", e.getMessage(), e);
      // 设置默认策略为Tag过滤
      filterStrategy.setSql92Enabled(false);
      log.info("已设置默认策略为Tag过滤");
    }
  }

  /** 手动切换过滤策略的方法 可以通过JMX或其他方式调用 */
  public void switchToSql92() {
    filterStrategy.setSql92Enabled(true);
    log.info("已切换到SQL92过滤策略");
  }

  public void switchToTag() {
    filterStrategy.setSql92Enabled(false);
    log.info("已切换到Tag过滤策略");
  }

  /** 获取当前策略信息 */
  public String getCurrentStrategyInfo() {
    return filterStrategy.getStrategyInfo();
  }
}

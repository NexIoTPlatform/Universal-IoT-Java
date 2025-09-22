# 推送系统架构说明

## 概述

本推送系统基于现有的`UPProcessorManager`
架构实现，支持多种推送渠道（HTTP、MQTT、Kafka、RocketMQ），具备统计、重试等完整功能，同时保持架构的一致性和可扩展性。

## 核心组件

### 1. IoTPushResult - 推送结果实体

```java
@Data
@Builder
public class IoTPushResult {
    private boolean ok;                    // 推送是否成功
    private ThirdPlatform platform;        // 第三方平台类型
    private String productKey;             // 产品Key
    private String deviceId;               // 设备ID
    private String channel;                // 推送渠道
    private String messageContent;         // 推送消息内容
    private LocalDateTime pushTime;        // 推送时间
    private Long responseTime;             // 响应时间(毫秒)
    private String errorMessage;           // 错误信息
    private String errorCode;              // 错误代码
    private Integer retryCount;            // 重试次数
    private Integer maxRetryCount;         // 最大重试次数
    private PushStatus status;             // 推送状态
    private String configId;               // 推送配置ID
    private String requestId;              // 请求ID
}
```

### 2. UPPushBO - 推送配置扩展

- **功能**: 扩展现有的`UPPushBO`，添加统计和重试配置
- **新增配置**:
    - `StatisticsConfig`: 统计配置（启用状态、保留时间、异步处理）
    - `RetryConfig`: 重试配置（启用状态、最大次数、间隔时间）

### 3. PushStatisticsProcessor - 推送统计处理器

- **架构**: 基于`UPProcessor`接口实现
- **功能**: 统计每天不同渠道、不同产品的推送数据
- **统计维度**:
    - 总体统计（成功/失败/总数/响应时间）
    - 渠道统计（按HTTP、MQTT等渠道）
    - 产品统计（按产品Key）
    - 平台统计（按第三方平台）
- **存储**: Redis Hash结构，支持TTL自动过期
- **执行时机**: 推送后处理（`afterPush`方法）

### 4. PushRetryProcessor - 推送重试处理器

- **架构**: 基于`UPProcessor`接口实现
- **功能**: 处理推送失败后的重试机制
- **特性**:
    - 可配置最大重试次数（默认3次）
    - 定时重试任务（每5分钟执行）
    - 手动重试功能
    - 重试队列管理
    - 自动清理过期重试记录
- **执行时机**: 推送后处理（`afterPush`方法）

### 5. PushStrategyManager - 推送策略管理器（升级版）

- **功能**: 统一管理所有推送策略，集成统计和重试功能
- **特性**:
    - 支持多种推送渠道（HTTP、MQTT、Kafka、RocketMQ）
    - 集成`UPProcessorManager`架构
    - 异步处理推送结果
    - 支持批量推送
    - 自动执行推送前/后处理器

## 架构优势

### 1. 基于现有架构

- ✅ 复用`UPProcessorManager`的处理器机制
- ✅ 扩展现有的`UPPushBO`配置
- ✅ 保持架构一致性
- ✅ 减少新增节点

### 2. 插件化设计

- ✅ 统计和重试功能作为处理器插件
- ✅ 支持优先级排序
- ✅ 易于扩展新功能
- ✅ 支持条件过滤

### 3. 配置驱动

- ✅ 通过`UPPushBO`统一配置
- ✅ 支持各渠道独立配置
- ✅ 统计和重试参数可配置
- ✅ 运行时动态调整

## 使用示例

### 基本推送

```java

@Autowired
private PushStrategyManager pushStrategyManager;

// 执行推送
List<IoTPushResult> results = pushStrategyManager.executePush(request, config);

// 批量推送
List<IoTPushResult> batchResults = pushStrategyManager.executeBatchPush(requests, config);
```

### 获取统计信息

```java
// 获取今日推送统计
PushStatisticsProcessor.PushStatistics stats =
    pushStatisticsProcessor.getStatistics(LocalDate.now());

// 获取渠道统计
PushStatisticsProcessor.ChannelStatistics channelStats =
    pushStatisticsProcessor.getChannelStatistics(LocalDate.now(), "HTTP");
```

### 重试功能

```java
// 手动重试
pushRetryProcessor.manualRetry("deviceId","HTTP","productKey");

// 获取重试次数
Integer retryCount = pushRetryProcessor.getRetryCount("deviceId", "HTTP");

// 重置重试计数
pushRetryProcessor.

resetRetryCount("deviceId","HTTP");
```

## 配置说明

### UPPushBO 配置示例

```java
UPPushBO config = UPPushBO.builder()
    .http(HttpPushConfig.builder()
        .url("http://example.com/push")
        .enable(true)
        .support(true)
        .build())
    .mqtt(MqttPushConfig.builder()
        .url("tcp://localhost:1883")
        .topic("platform/push")
        .enable(true)
        .support(true)
        .build())
    .statistics(UPPushBO.StatisticsConfig.builder()
        .enable(true)
        .retentionDays(30)
        .async(true)
        .build())
    .retry(UPPushBO.RetryConfig.builder()
        .enable(true)
        .maxCount(3)
        .intervalMinutes(5)
        .build())
    .build();
```

## Redis Key 结构

### 统计相关

- `push:stats:2025-01-09` - 总体统计
- `push:channel:2025-01-09:HTTP` - 渠道统计
- `push:product:2025-01-09:productKey` - 产品统计
- `push:platform:2025-01-09:ctwing` - 平台统计

### 重试相关

- `push:retry:queue:deviceId:channel` - 重试队列
- `push:retry:count:deviceId:channel` - 重试计数
- `push:failed:2025-01-09` - 失败推送记录

## 处理器执行流程

### 推送前处理

1. `UPProcessorManager.executeBeforePush()` 执行所有处理器
2. 按优先级排序执行
3. 支持条件过滤（`supports`方法）

### 推送执行

1. 根据配置执行各渠道推送
2. 记录推送结果到`IoTPushResult`
3. 异步处理推送结果

### 推送后处理

1. `UPProcessorManager.executeAfterPush()` 执行所有处理器
2. `PushStatisticsProcessor` 记录统计信息
3. `PushRetryProcessor` 处理失败重试

## 定时任务

### 重试任务

- **频率**: 每5分钟执行一次
- **功能**: 自动重试失败的推送
- **实现**: `PushRetryProcessor.retryFailedPushes()`

### 清理任务

- **重试清理**: 每天凌晨2点执行
- **功能**: 清理过期的重试记录和监控数据
- **实现**: `PushRetryProcessor.cleanupExpiredRetryRecords()`

## 扩展指南

### 添加新的统计维度

1. 在`PushStatisticsProcessor`中添加新的统计方法
2. 定义相应的统计数据结构
3. 在`afterPush`方法中调用统计方法

### 添加新的重试策略

1. 在`PushRetryProcessor`中实现新的重试逻辑
2. 配置重试参数到`UPPushBO.RetryConfig`
3. 在定时任务中调用重试方法

### 添加新的处理器

1. 实现`UPProcessor`接口
2. 配置优先级（`getOrder`方法）
3. 实现条件过滤（`supports`方法）
4. 在`afterPush`方法中实现业务逻辑

## 性能优化

### 异步处理

- 推送结果统计采用异步处理
- 重试逻辑采用异步处理
- 减少对主流程的影响

### Redis优化

- 使用Hash结构存储统计数据
- 设置合理的TTL自动过期
- 定期清理过期数据

### 批量操作

- 支持批量推送
- 批量统计处理
- 减少网络开销

## 监控指标

### 业务指标

- 推送成功率
- 平均响应时间
- 失败推送数量
- 重试次数

### 系统指标

- Redis连接状态
- 处理器执行时间
- 队列长度
- 内存使用情况

## 最佳实践

1. **配置优化**: 根据实际业务需求调整重试次数和间隔
2. **监控告警**: 设置合理的告警阈值，及时发现问题
3. **数据清理**: 定期清理过期数据，避免Redis内存占用过高
4. **性能调优**: 根据推送量调整异步处理参数
5. **故障演练**: 定期进行故障演练，验证系统容错能力

## 架构对比

### 原有架构

- 独立的推送策略管理器
- 分散的统计和重试逻辑
- 配置管理不统一

### 新架构优势

- ✅ 基于现有`UPProcessorManager`架构
- ✅ 统一的配置管理（`UPPushBO`）
- ✅ 插件化的统计和重试功能
- ✅ 更好的可扩展性和维护性
- ✅ 减少代码重复和架构复杂度 
# MQTTTopicManager 测试文档

## 概述

本目录包含了 `MQTTTopicManager` 类的完整测试用例，包括单元测试、集成测试、性能测试和边界条件测试。

## 文件结构

```
topic/
├── MQTTTopicManagerTest.java          # 主要测试类
├── MQTTTopicManagerTestData.java      # 测试数据类
├── MQTTTopicManagerTestRunner.java    # 测试运行器
├── MqttTopicPatternTest.java          # 主题模式测试
└── README.md                          # 本文档
```

## 测试类说明

### 1. MQTTTopicManagerTest.java

主要的测试类，包含以下测试类别：

#### 订阅主题管理测试

- `testGetSubscriptionTopics_ThingModel()` - 测试物模型主题获取
- `testGetSubscriptionTopics_SystemLevel()` - 测试系统级主题获取
- `testGetSubscriptionTopics_Passthrough()` - 测试透传主题获取
- `testGetAllSubscriptionTopics()` - 测试所有订阅主题获取

#### 主题类型解析测试

- `testParseTopicType_*()` - 测试各种主题类型的解析
- `testParseTopicType_CachePerformance()` - 测试缓存性能

#### 主题信息提取测试

- `testExtractTopicInfo_*()` - 测试主题信息提取
- `testTopicInfoBuilder()` - 测试 TopicInfo 构建器
- `testTopicInfoToString()` - 测试 TopicInfo 字符串表示

#### 主题分类匹配测试

- `testMatchCategory_*()` - 测试主题分类匹配

#### 产品Key和设备ID提取测试

- `testExtractProductKeyFromTopic_*()` - 测试产品Key提取
- `testExtractDeviceIdFromTopic_*()` - 测试设备ID提取

#### 配置解析测试

- `testParseSubscribeTopicsFromConfig_*()` - 测试配置解析

#### 边界条件和异常处理测试

- `testParseTopicType_WithSpecialCharacters()` - 测试特殊字符
- `testParseSubscribeTopicsFromConfig_WithException()` - 测试异常处理

#### 性能测试

- `testParseTopicType_Performance()` - 测试解析性能
- `testExtractTopicInfo_Performance()` - 测试信息提取性能

#### 集成测试

- `testFullWorkflow_*()` - 测试完整工作流程

### 2. MQTTTopicManagerTestData.java

测试数据类，提供各种测试场景的数据：

#### 基础测试数据

- 有效主题数据（物模型、透传、系统级）
- 无效主题数据（null、空、格式错误）
- 特殊字符主题数据（中文、数字、混合字符）
- 历史格式主题数据

#### 配置数据

- 标准主题配置JSON
- 扩展主题配置JSON
- 空配置和无效配置
- 各种分隔符的配置Map

#### 预期结果数据

- 主题类型映射
- 主题分类映射
- 产品Key映射
- 设备ID映射

#### 测试用例数据

- 性能测试数据（1000个主题）
- 边界测试数据
- 压力测试数据（10000个主题）

#### 辅助方法

- `validateTopicInfo()` - 验证主题信息
- `validateInvalidTopicInfo()` - 验证无效主题信息
- `validateTopicConfig()` - 验证主题配置

### 3. MQTTTopicManagerTestRunner.java

测试运行器，按顺序执行所有测试用例：

#### 基础功能测试

1. 主题管理器初始化
2. 物模型主题解析
3. 透传主题解析
4. 系统级主题解析

#### 批量测试

5. 所有有效主题测试
6. 特殊字符主题测试
7. 历史格式主题测试

#### 配置解析测试

8. 配置解析测试
9. 不同分隔符测试

#### 异常处理和性能测试

10. 异常情况处理
11. 性能测试

#### 集成和边界测试

12. 完整工作流程测试
13. 边界条件测试
14. 测试总结报告

### 4. MqttTopicPatternTest.java

主题模式测试，验证正则表达式的正确性。

## 运行测试

### 运行所有测试

```bash
# 在项目根目录执行
mvn test -Dtest=MQTTTopicManagerTest

# 或者运行特定测试类
mvn test -Dtest=MQTTTopicManagerTestRunner
```

### 运行特定测试方法

```bash
# 运行单个测试方法
mvn test -Dtest=MQTTTopicManagerTest#testParseTopicType_ThingPropertyUp

# 运行多个测试方法
mvn test -Dtest=MQTTTopicManagerTest#testParseTopicType_ThingPropertyUp,testParseTopicType_ThingEventUp
```

### 运行测试运行器

```bash
# 运行完整的测试流程
mvn test -Dtest=MQTTTopicManagerTestRunner
```

## 测试数据说明

### 有效主题格式

#### 物模型主题

- `$thing/up/property/{productKey}/{deviceId}` - 属性上报
- `$thing/up/event/{productKey}/{deviceId}` - 事件上报
- `$thing/down/{productKey}/{deviceId}` - 下行控制

#### 透传主题

- `$thing/up/{productKey}/{deviceId}` - 透传上行
- `$thing/down/{productKey}/{deviceId}` - 透传下行

#### 系统级主题

- `$ota/report/{productKey}/{deviceId}` - OTA上报
- `$ota/update/{productKey}/{deviceId}` - OTA更新

### 特殊字符支持

测试包含以下特殊字符场景：

- 中文字符：`测试产品/测试设备`
- 数字字符：`12345/67890`
- 混合字符：`test-产品_123/device-设备.456`
- 连字符和下划线：`test-product_123/test-device.456`

### 历史格式兼容

支持以下历史格式：

- `productKey/deviceId`
- `prefix/productKey/deviceId`
- `productKey`
- `a/b/c/productKey/deviceId`

## 性能要求

测试包含以下性能验证：

- 1000个主题解析应在100ms内完成
- 平均每个主题解析时间应小于100μs
- 缓存机制应显著提升重复解析性能

## 测试覆盖率

测试覆盖了以下方面：

- ✅ 基础功能测试
- ✅ 批量主题测试
- ✅ 特殊字符处理
- ✅ 历史格式兼容
- ✅ 配置解析测试
- ✅ 异常处理测试
- ✅ 性能测试
- ✅ 集成测试
- ✅ 边界条件测试

## 注意事项

1. **Java版本兼容性**：测试代码兼容Java 8及以上版本
2. **依赖要求**：需要JUnit 5和Mockito依赖
3. **内存使用**：性能测试可能消耗较多内存，建议在测试环境中运行
4. **测试顺序**：使用`@Order`注解确保测试按正确顺序执行

## 扩展测试

如需添加新的测试用例：

1. 在`MQTTTopicManagerTestData.java`中添加测试数据
2. 在`MQTTTopicManagerTest.java`中添加对应的测试方法
3. 在`MQTTTopicManagerTestRunner.java`中添加集成测试
4. 更新本文档

## 故障排除

### 常见问题

1. **测试失败**：检查测试数据是否正确
2. **性能测试超时**：调整性能阈值或优化测试环境
3. **内存不足**：增加JVM堆内存大小

### 调试技巧

1. 使用`@DisplayName`注解提供清晰的测试描述
2. 在测试方法中添加详细的日志输出
3. 使用断言消息提供失败原因
4. 使用测试运行器查看完整的测试流程

## 贡献指南

1. 遵循现有的测试代码风格
2. 添加适当的注释和文档
3. 确保新测试用例通过所有检查
4. 更新相关文档 
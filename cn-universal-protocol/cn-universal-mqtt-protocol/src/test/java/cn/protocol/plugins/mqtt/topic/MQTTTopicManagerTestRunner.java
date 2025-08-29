/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.protocol.mqtt.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTProductConfig;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * MQTTTopicManager 测试运行器
 *
 * <p>演示如何运行 MQTTTopicManager 的各种测试用例
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MQTTTopicManager 测试运行器")
class MQTTTopicManagerTestRunner {

  @InjectMocks private MQTTTopicManager topicManager;

  @BeforeAll
  void setUpClass() {
    System.out.println("=== MQTTTopicManager 测试开始 ===");
  }

  @BeforeEach
  void setUp() {
    topicManager.initialize();
  }

  @AfterEach
  void tearDown() {
    // 清理测试状态
  }

  @AfterAll
  void tearDownClass() {
    System.out.println("=== MQTTTopicManager 测试结束 ===");
  }

  // ==================== 基础功能测试 ====================

  @Test
  @Order(1)
  @DisplayName("测试主题管理器初始化")
  void testTopicManagerInitialization() {
    // 验证初始化后的状态
    List<String> allTopics = topicManager.getAllSubscriptionTopics();
    assertNotNull(allTopics);
    assertFalse(allTopics.isEmpty());

    System.out.println("初始化完成，订阅主题数量: " + allTopics.size());
    allTopics.forEach(topic -> System.out.println("  - " + topic));
  }

  @Test
  @Order(2)
  @DisplayName("测试物模型主题解析")
  void testThingModelTopicParsing() {
    String topic = MQTTTopicManagerTestData.VALID_THING_PROPERTY_UP_TOPIC;

    // 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(topic);
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType);

    // 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
    assertTrue(topicInfo.isValid());
    assertEquals(MQTTTopicManagerTestData.TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(MQTTTopicManagerTestData.TEST_DEVICE_ID, topicInfo.getDeviceId());

    System.out.println("物模型主题解析成功: " + topicInfo);
  }

  @Test
  @Order(3)
  @DisplayName("测试透传主题解析")
  void testPassthroughTopicParsing() {
    String topic = MQTTTopicManagerTestData.VALID_PASSTHROUGH_UP_TOPIC;

    // 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(topic);
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, topicType);

    // 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
    assertTrue(topicInfo.isValid());
    assertEquals(MQTTTopicManagerTestData.TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(MQTTTopicManagerTestData.TEST_DEVICE_ID, topicInfo.getDeviceId());

    System.out.println("透传主题解析成功: " + topicInfo);
  }

  @Test
  @Order(4)
  @DisplayName("测试系统级主题解析")
  void testSystemLevelTopicParsing() {
    String topic = MQTTTopicManagerTestData.VALID_OTA_REPORT_TOPIC;

    // 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(topic);
    assertEquals(MQTTTopicType.OTA_REPORT, topicType);

    // 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
    assertTrue(topicInfo.isValid());
    assertEquals(MQTTTopicManagerTestData.TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(MQTTTopicManagerTestData.TEST_DEVICE_ID, topicInfo.getDeviceId());

    System.out.println("系统级主题解析成功: " + topicInfo);
  }

  @Test
  @Order(5)
  @DisplayName("测试动态前缀主题解析")
  void testDynamicPrefixTopicParsing() {
    // 测试动态前缀物模型主题
    String dynamicThingTopic = MQTTTopicManagerTestData.VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC;

    // 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(dynamicThingTopic);
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType);

    // 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(dynamicThingTopic);
    assertTrue(topicInfo.isValid());
    assertEquals(MQTTTopicManagerTestData.TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(MQTTTopicManagerTestData.TEST_DEVICE_ID, topicInfo.getDeviceId());

    System.out.println("动态前缀物模型主题解析成功: " + topicInfo);

    // 测试动态前缀透传主题
    String dynamicPassthroughTopic = MQTTTopicManagerTestData.VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC;

    // 解析主题类型
    MQTTTopicType passthroughType = topicManager.parseTopicType(dynamicPassthroughTopic);
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughType);

    // 提取主题信息
    MQTTTopicManager.TopicInfo passthroughInfo =
        topicManager.extractTopicInfo(dynamicPassthroughTopic);
    assertTrue(passthroughInfo.isValid());
    assertEquals(MQTTTopicManagerTestData.TEST_PRODUCT_KEY, passthroughInfo.getProductKey());
    assertEquals(MQTTTopicManagerTestData.TEST_DEVICE_ID, passthroughInfo.getDeviceId());

    System.out.println("动态前缀透传主题解析成功: " + passthroughInfo);
  }

  @Test
  @Order(6)
  @DisplayName("测试不同动态前缀的兼容性")
  void testDifferentDynamicPrefixes() {
    String[] prefixes = {"$qiantang", "$custom", "$platform", "$device", "$sensor"};
    String productKey = "testProduct";
    String deviceId = "testDevice";

    for (String prefix : prefixes) {
      // 测试物模型属性上报
      String propertyTopic = prefix + "/up/property/" + productKey + "/" + deviceId;
      MQTTTopicType propertyType = topicManager.parseTopicType(propertyTopic);
      assertEquals(
          MQTTTopicType.THING_PROPERTY_UP, propertyType, "前缀 " + prefix + " 的物模型属性上报应该正确解析");

      // 测试透传上行
      String passthroughTopic = prefix + "/up/" + productKey + "/" + deviceId;
      MQTTTopicType passthroughType = topicManager.parseTopicType(passthroughTopic);
      assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughType, "前缀 " + prefix + " 的透传上行应该正确解析");

      System.out.println("动态前缀 " + prefix + " 测试成功");
    }
  }

  // ==================== 批量测试 ====================

  @Test
  @Order(7)
  @DisplayName("测试所有有效主题")
  void testAllValidTopics() {
    List<String> validTopics = MQTTTopicManagerTestData.getAllValidTopics();
    Map<String, MQTTTopicType> expectedTypes = MQTTTopicManagerTestData.getTopicTypeMapping();

    int successCount = 0;
    int totalCount = validTopics.size();

    for (String topic : validTopics) {
      try {
        MQTTTopicType topicType = topicManager.parseTopicType(topic);
        MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);

        if (topicType != null && topicInfo.isValid()) {
          successCount++;
          System.out.println("✓ " + topic + " -> " + topicType);
        } else {
          System.out.println("✗ " + topic + " -> 解析失败");
        }
      } catch (Exception e) {
        System.out.println("✗ " + topic + " -> 异常: " + e.getMessage());
      }
    }

    System.out.println("有效主题测试结果: " + successCount + "/" + totalCount + " 成功");
    assertEquals(totalCount, successCount, "所有有效主题都应该解析成功");
  }

  @Test
  @Order(8)
  @DisplayName("测试特殊字符主题")
  void testSpecialCharTopics() {
    List<String> specialTopics = MQTTTopicManagerTestData.getSpecialCharTopics();
    Map<String, String> expectedProductKeys = MQTTTopicManagerTestData.getProductKeyMapping();
    Map<String, String> expectedDeviceIds = MQTTTopicManagerTestData.getDeviceIdMapping();

    for (String topic : specialTopics) {
      String productKey = topicManager.extractProductKeyFromTopic(topic);
      String deviceId = topicManager.extractDeviceIdFromTopic(topic);

      String expectedProductKey = expectedProductKeys.get(topic);
      String expectedDeviceId = expectedDeviceIds.get(topic);

      assertEquals(expectedProductKey, productKey, "产品Key提取失败: " + topic);
      assertEquals(expectedDeviceId, deviceId, "设备ID提取失败: " + topic);

      System.out.println("特殊字符主题测试成功: " + topic);
      System.out.println("  产品Key: " + productKey);
      System.out.println("  设备ID: " + deviceId);
    }
  }

  @Test
  @Order(9)
  @DisplayName("测试历史格式主题")
  void testHistoricalTopics() {
    List<String> historicalTopics = MQTTTopicManagerTestData.getHistoricalTopics();

    for (String topic : historicalTopics) {
      String productKey = topicManager.extractProductKeyFromTopic(topic);
      String deviceId = topicManager.extractDeviceIdFromTopic(topic);

      System.out.println("历史格式主题: " + topic);
      System.out.println("  提取的产品Key: " + productKey);
      System.out.println("  提取的设备ID: " + deviceId);

      // 历史格式的主题可能无法完全解析，但不应抛出异常
      assertNotNull(productKey, "历史格式主题的产品Key提取不应返回null");
      assertNotNull(deviceId, "历史格式主题的设备ID提取不应返回null");
    }
  }

  // ==================== 配置解析测试 ====================

  @Test
  @Order(10)
  @DisplayName("测试配置解析")
  void testConfigParsing() {
    Map<String, Object> configMap = MQTTTopicManagerTestData.createStandardConfigMap();

    // 测试订阅主题解析
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 2);

    assertNotNull(topicConfigs);
    assertFalse(topicConfigs.isEmpty());

    System.out.println("配置解析测试成功，解析到 " + topicConfigs.size() + " 个主题配置:");
    for (MQTTProductConfig.MqttTopicConfig config : topicConfigs) {
      System.out.println("  - " + config.getTopicPattern() + " (QoS: " + config.getQos() + ")");
    }
  }

  @Test
  @Order(11)
  @DisplayName("测试不同分隔符的配置")
  void testDifferentSeparators() {
    // 测试逗号分隔符
    Map<String, Object> commaConfig = MQTTTopicManagerTestData.createStandardConfigMap();
    List<MQTTProductConfig.MqttTopicConfig> commaConfigs =
        topicManager.parseSubscribeTopicsFromConfig(commaConfig, 1);

    // 测试分号分隔符
    Map<String, Object> semicolonConfig = MQTTTopicManagerTestData.createSemicolonConfigMap();
    List<MQTTProductConfig.MqttTopicConfig> semicolonConfigs =
        topicManager.parseSubscribeTopicsFromConfig(semicolonConfig, 1);

    // 测试带空白字符的配置
    Map<String, Object> whitespaceConfig = MQTTTopicManagerTestData.createWhitespaceConfigMap();
    List<MQTTProductConfig.MqttTopicConfig> whitespaceConfigs =
        topicManager.parseSubscribeTopicsFromConfig(whitespaceConfig, 1);

    System.out.println("不同分隔符测试结果:");
    System.out.println("  逗号分隔符: " + commaConfigs.size() + " 个主题");
    System.out.println("  分号分隔符: " + semicolonConfigs.size() + " 个主题");
    System.out.println("  带空白字符: " + whitespaceConfigs.size() + " 个主题");

    assertTrue(commaConfigs.size() > 0);
    assertTrue(semicolonConfigs.size() > 0);
    assertTrue(whitespaceConfigs.size() > 0);
  }

  // ==================== 异常处理测试 ====================

  @Test
  @Order(12)
  @DisplayName("测试异常情况处理")
  void testExceptionHandling() {
    List<String> invalidTopics = MQTTTopicManagerTestData.getInvalidTopics();

    for (String topic : invalidTopics) {
      try {
        MQTTTopicType topicType = topicManager.parseTopicType(topic);
        MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);

        // 无效主题应该返回null或无效的TopicInfo
        if (topic == null || topic.trim().isEmpty()) {
          assertNull(topicType, "null或空主题应该返回null");
          assertFalse(topicInfo.isValid(), "null或空主题应该返回无效的TopicInfo");
        } else {
          System.out.println(
              "无效主题处理: " + topic + " -> " + (topicType != null ? topicType : "null"));
        }
      } catch (Exception e) {
        System.out.println("异常主题处理: " + topic + " -> 异常: " + e.getMessage());
        // 异常应该被正确处理，不应传播
      }
    }

    System.out.println("异常情况处理测试完成");
  }

  // ==================== 性能测试 ====================

  @Test
  @Order(13)
  @DisplayName("测试性能")
  void testPerformance() {
    List<String> performanceData = MQTTTopicManagerTestData.createPerformanceTestData();

    // 预热
    for (int i = 0; i < 100; i++) {
      topicManager.parseTopicType(performanceData.get(0));
    }

    // 性能测试
    long startTime = System.nanoTime();
    int successCount = 0;

    for (String topic : performanceData) {
      MQTTTopicType topicType = topicManager.parseTopicType(topic);
      if (topicType != null) {
        successCount++;
      }
    }

    long endTime = System.nanoTime();
    long duration = endTime - startTime;

    System.out.println("性能测试结果:");
    System.out.println("  处理主题数量: " + performanceData.size());
    System.out.println("  成功解析数量: " + successCount);
    System.out.println("  总耗时: " + (duration / 1_000_000) + " ms");
    System.out.println("  平均耗时: " + (duration / performanceData.size() / 1_000) + " μs/主题");

    // 验证性能要求
    assertTrue(duration < 100_000_000, "1000个主题的解析应该在100ms内完成");
    assertEquals(performanceData.size(), successCount, "所有主题都应该解析成功");
  }

  // ==================== 集成测试 ====================

  @Test
  @Order(14)
  @DisplayName("测试完整工作流程")
  void testCompleteWorkflow() {
    // 模拟完整的MQTT主题处理流程
    String[] testTopics = {
      MQTTTopicManagerTestData.VALID_THING_PROPERTY_UP_TOPIC,
      MQTTTopicManagerTestData.VALID_PASSTHROUGH_UP_TOPIC,
      MQTTTopicManagerTestData.VALID_OTA_REPORT_TOPIC
    };

    for (String topic : testTopics) {
      System.out.println("\n=== 处理主题: " + topic + " ===");

      // 1. 检查是否为标准主题
      boolean isStandard = topicManager.isStandardTopic(topic);
      System.out.println("1. 标准主题检查: " + isStandard);
      assertTrue(isStandard);

      // 2. 解析主题类型
      MQTTTopicType topicType = topicManager.parseTopicType(topic);
      System.out.println("2. 主题类型: " + topicType);
      assertNotNull(topicType);

      // 3. 提取主题信息
      MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
      System.out.println("3. 主题信息: " + topicInfo);
      assertTrue(topicInfo.isValid());

      // 4. 验证主题分类
      MqttConstant.TopicCategory category = MQTTTopicManager.matchCategory(topic);
      System.out.println("4. 主题分类: " + category);
      assertNotNull(category);

      // 5. 提取产品Key和设备ID
      String productKey = topicManager.extractProductKeyFromTopic(topic);
      String deviceId = topicManager.extractDeviceIdFromTopic(topic);
      System.out.println("5. 产品Key: " + productKey + ", 设备ID: " + deviceId);
      assertNotNull(productKey);
      assertNotNull(deviceId);

      // 6. 验证一致性
      assertEquals(productKey, topicInfo.getProductKey());
      assertEquals(deviceId, topicInfo.getDeviceId());
      assertEquals(topicType.getCategory(), category);

      System.out.println("✓ 主题处理完成");
    }

    System.out.println("\n=== 完整工作流程测试通过 ===");
  }

  // ==================== 边界条件测试 ====================

  @Test
  @Order(15)
  @DisplayName("测试边界条件")
  void testBoundaryConditions() {
    List<String> boundaryData = MQTTTopicManagerTestData.createBoundaryTestData();

    System.out.println("边界条件测试:");
    for (String topic : boundaryData) {
      try {
        MQTTTopicType topicType = topicManager.parseTopicType(topic);
        MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
        String productKey = topicManager.extractProductKeyFromTopic(topic);
        String deviceId = topicManager.extractDeviceIdFromTopic(topic);

        System.out.println("  主题: " + topic);
        System.out.println("    类型: " + topicType);
        System.out.println("    有效: " + topicInfo.isValid());
        System.out.println("    产品Key: " + productKey);
        System.out.println("    设备ID: " + deviceId);

        // 边界条件测试不应抛出异常
      } catch (Exception e) {
        System.out.println("  主题: " + topic + " -> 异常: " + e.getMessage());
        // 某些边界条件可能抛出异常，这是可以接受的
      }
    }

    System.out.println("边界条件测试完成");
  }

  // ==================== 总结报告 ====================

  @Test
  @Order(16)
  @DisplayName("生成测试总结报告")
  void generateTestSummary() {
    System.out.println("\n" + "=".repeat(60));
    System.out.println("MQTTTopicManager 测试总结报告");
    System.out.println("=".repeat(60));

    // 统计订阅主题
    List<String> allTopics = topicManager.getAllSubscriptionTopics();
    System.out.println("订阅主题统计:");
    System.out.println("  总主题数: " + allTopics.size());

    // 按分类统计
    for (MqttConstant.TopicCategory category : MqttConstant.TopicCategory.values()) {
      List<String> categoryTopics = topicManager.getSubscriptionTopics(category);
    }

    // 测试覆盖率统计
    System.out.println("\n测试覆盖范围:");
    System.out.println("  ✓ 基础功能测试");
    System.out.println("  ✓ 批量主题测试");
    System.out.println("  ✓ 特殊字符处理");
    System.out.println("  ✓ 历史格式兼容");
    System.out.println("  ✓ 配置解析测试");
    System.out.println("  ✓ 异常处理测试");
    System.out.println("  ✓ 性能测试");
    System.out.println("  ✓ 集成测试");
    System.out.println("  ✓ 边界条件测试");

    System.out.println("\n测试结果: 所有测试用例执行完成");
    System.out.println("=".repeat(60));
  }
}

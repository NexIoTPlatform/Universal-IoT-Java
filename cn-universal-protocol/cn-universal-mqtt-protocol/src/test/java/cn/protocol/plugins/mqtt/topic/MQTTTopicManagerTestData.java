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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * MQTTTopicManager 测试数据类
 *
 * <p>提供测试所需的各种 mock 数据和测试用例
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
public class MQTTTopicManagerTestData {

  // ==================== 基础测试数据 ====================

  public static final String TEST_PRODUCT_KEY = "testProduct";
  public static final String TEST_DEVICE_ID = "testDevice";
  public static final String TEST_PRODUCT_KEY_2 = "testProduct2";
  public static final String TEST_DEVICE_ID_2 = "testDevice2";

  // ==================== 有效主题数据 ====================

  // 物模型主题（固定前缀）
  public static final String VALID_THING_PROPERTY_UP_TOPIC =
      "$thing/up/property/testProduct/testDevice";
  public static final String VALID_THING_EVENT_UP_TOPIC = "$thing/up/event/testProduct/testDevice";
  public static final String VALID_THING_DOWN_TOPIC = "$thing/down/testProduct/testDevice";

  // 物模型主题（动态前缀）
  public static final String VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC =
      "$qiantang/up/property/testProduct/testDevice";
  public static final String VALID_DYNAMIC_THING_EVENT_UP_TOPIC =
      "$qiantang/up/event/testProduct/testDevice";
  public static final String VALID_DYNAMIC_THING_DOWN_TOPIC =
      "$qiantang/down/testProduct/testDevice";

  // 透传主题（固定前缀）
  public static final String VALID_PASSTHROUGH_UP_TOPIC = "$thing/up/testProduct/testDevice";
  public static final String VALID_PASSTHROUGH_DOWN_TOPIC = "$thing/down/testProduct/testDevice";

  // 透传主题（动态前缀）
  public static final String VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC =
      "$qiantang/up/testProduct/testDevice";
  public static final String VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC =
      "$qiantang/down/testProduct/testDevice";

  // 系统级主题
  public static final String VALID_OTA_REPORT_TOPIC = "$ota/report/testProduct/testDevice";
  public static final String VALID_OTA_UPDATE_TOPIC = "$ota/update/testProduct/testDevice";

  // ==================== 无效主题数据 ====================

  public static final String INVALID_TOPIC = "invalid/topic/format";
  public static final String NULL_TOPIC = null;
  public static final String EMPTY_TOPIC = "";
  public static final String BLANK_TOPIC = "   ";
  public static final String MALFORMED_TOPIC = "$thing/up/property/";
  public static final String INCOMPLETE_TOPIC = "$thing/up/property/productKey";

  // ==================== 特殊字符主题数据 ====================

  public static final String SPECIAL_CHAR_TOPIC =
      "$thing/up/property/test-product_123/test-device.456";
  public static final String CHINESE_TOPIC = "$thing/up/property/测试产品/测试设备";
  public static final String NUMERIC_TOPIC = "$thing/up/property/12345/67890";
  public static final String MIXED_TOPIC = "$thing/up/property/test-产品_123/device-设备.456";

  // ==================== 历史格式主题数据 ====================

  public static final String HISTORICAL_TOPIC_1 = "productKey/deviceId";
  public static final String HISTORICAL_TOPIC_2 = "prefix/productKey/deviceId";
  public static final String HISTORICAL_TOPIC_3 = "productKey";
  public static final String HISTORICAL_TOPIC_4 = "a/b/c/productKey/deviceId";

  // ==================== 主题配置数据 ====================

  /** 创建标准主题配置JSON */
  public static String createStandardTopicConfigJson() {
    return "{\n"
        + "    \"thing-model\": {\n"
        + "        \"description\": \"物模型主题\",\n"
        + "        \"topics\": [\n"
        + "            \"$thing/up/property/+/+\",\n"
        + "            \"$thing/up/event/+/+\",\n"
        + "            \"$thing/down/+/+\"\n"
        + "        ]\n"
        + "    },\n"
        + "    \"system-level\": {\n"
        + "        \"description\": \"系统级主题\",\n"
        + "        \"topics\": [\n"
        + "            \"$ota/report/+/+\",\n"
        + "            \"$ota/update/+/+\"\n"
        + "        ]\n"
        + "    },\n"
        + "    \"passthrough\": {\n"
        + "        \"description\": \"透传主题\",\n"
        + "        \"topics\": [\n"
        + "            \"$thing/up/+/+\",\n"
        + "            \"$thing/down/+/+\"\n"
        + "        ]\n"
        + "    }\n"
        + "}";
  }

  /** 创建扩展主题配置JSON */
  public static String createExtendedTopicConfigJson() {
    return "{\n"
        + "    \"thing-model\": {\n"
        + "        \"description\": \"物模型主题\",\n"
        + "        \"topics\": [\n"
        + "            \"$thing/up/property/+/+\",\n"
        + "            \"$thing/up/event/+/+\",\n"
        + "            \"$thing/down/+/+\",\n"
        + "            \"$thing/up/third/+/+\"\n"
        + "        ]\n"
        + "    },\n"
        + "    \"system-level\": {\n"
        + "        \"description\": \"系统级主题\",\n"
        + "        \"topics\": [\n"
        + "            \"$ota/report/+/+\",\n"
        + "            \"$ota/update/+/+\",\n"
        + "            \"$ota/status/+/+\"\n"
        + "        ]\n"
        + "    },\n"
        + "    \"passthrough\": {\n"
        + "        \"description\": \"透传主题\",\n"
        + "        \"topics\": [\n"
        + "            \"$thing/up/+/+\",\n"
        + "            \"$thing/down/+/+\",\n"
        + "            \"$thing/raw/+/+\"\n"
        + "        ]\n"
        + "    }\n"
        + "}";
  }

  /** 创建空主题配置JSON */
  public static String createEmptyTopicConfigJson() {
    return "{\n"
        + "    \"thing-model\": {\n"
        + "        \"description\": \"物模型主题\",\n"
        + "        \"topics\": []\n"
        + "    },\n"
        + "    \"system-level\": {\n"
        + "        \"description\": \"系统级主题\",\n"
        + "        \"topics\": []\n"
        + "    },\n"
        + "    \"passthrough\": {\n"
        + "        \"description\": \"透传主题\",\n"
        + "        \"topics\": []\n"
        + "    }\n"
        + "}";
  }

  /** 创建无效主题配置JSON */
  public static String createInvalidTopicConfigJson() {
    return "{\n"
        + "    \"invalid\": \"json\",\n"
        + "    \"format\": {\n"
        + "        \"missing\": \"topics\"\n"
        + "    }\n"
        + "}";
  }

  // ==================== 配置Map数据 ====================

  /** 创建标准配置Map */
  public static Map<String, Object> createStandardConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "$thing/up/property/+/+,$thing/up/event/+/+,$ota/report/+/+");
    configMap.put("topics", "$thing/up/+/+,$thing/down/+/+");
    configMap.put("host", "tcp://localhost:1883");
    configMap.put("username", "testuser");
    configMap.put("password", "testpass");
    configMap.put("defaultQos", 1);
    return configMap;
  }

  /** 创建使用分号分隔符的配置Map */
  public static Map<String, Object> createSemicolonConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "$thing/up/property/+/+;$thing/up/event/+/+;$ota/report/+/+");
    configMap.put("topics", "$thing/up/+/+;$thing/down/+/+");
    return configMap;
  }

  /** 创建带空白字符的配置Map */
  public static Map<String, Object> createWhitespaceConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put(
        "subscribeTopics", " $thing/up/property/+/+ , $thing/up/event/+/+ , $ota/report/+/+ ");
    configMap.put("topics", " $thing/up/+/+ , $thing/down/+/+ ");
    return configMap;
  }

  /** 创建空配置Map */
  public static Map<String, Object> createEmptyConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "");
    configMap.put("topics", "");
    return configMap;
  }

  /** 创建null配置Map */
  public static Map<String, Object> createNullConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", null);
    configMap.put("topics", null);
    return configMap;
  }

  /** 创建异常配置Map */
  public static Map<String, Object> createExceptionConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", new Object()); // 非字符串类型
    configMap.put("topics", 123); // 数字类型
    return configMap;
  }

  // ==================== 主题列表数据 ====================

  /** 获取所有有效主题列表 */
  public static List<String> getAllValidTopics() {
    return Arrays.asList(
        // 固定前缀主题
        VALID_THING_PROPERTY_UP_TOPIC,
        VALID_THING_EVENT_UP_TOPIC,
        VALID_THING_DOWN_TOPIC,
        VALID_PASSTHROUGH_UP_TOPIC,
        VALID_PASSTHROUGH_DOWN_TOPIC,
        // 动态前缀主题
        VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC,
        VALID_DYNAMIC_THING_EVENT_UP_TOPIC,
        VALID_DYNAMIC_THING_DOWN_TOPIC,
        VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC,
        VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC,
        // 系统级主题
        VALID_OTA_REPORT_TOPIC,
        VALID_OTA_UPDATE_TOPIC);
  }

  /** 获取物模型主题列表 */
  public static List<String> getThingModelTopics() {
    return Arrays.asList(
        // 固定前缀
        VALID_THING_PROPERTY_UP_TOPIC,
        VALID_THING_EVENT_UP_TOPIC,
        VALID_THING_DOWN_TOPIC,
        // 动态前缀
        VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC,
        VALID_DYNAMIC_THING_EVENT_UP_TOPIC,
        VALID_DYNAMIC_THING_DOWN_TOPIC);
  }

  /** 获取透传主题列表 */
  public static List<String> getPassthroughTopics() {
    return Arrays.asList(
        // 固定前缀
        VALID_PASSTHROUGH_UP_TOPIC, VALID_PASSTHROUGH_DOWN_TOPIC,
        // 动态前缀
        VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC, VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC);
  }

  /** 获取系统级主题列表 */
  public static List<String> getSystemLevelTopics() {
    return Arrays.asList(VALID_OTA_REPORT_TOPIC, VALID_OTA_UPDATE_TOPIC);
  }

  /** 获取无效主题列表 */
  public static List<String> getInvalidTopics() {
    return Arrays.asList(
        INVALID_TOPIC, NULL_TOPIC, EMPTY_TOPIC, BLANK_TOPIC, MALFORMED_TOPIC, INCOMPLETE_TOPIC);
  }

  /** 获取特殊字符主题列表 */
  public static List<String> getSpecialCharTopics() {
    return Arrays.asList(SPECIAL_CHAR_TOPIC, CHINESE_TOPIC, NUMERIC_TOPIC, MIXED_TOPIC);
  }

  /** 获取历史格式主题列表 */
  public static List<String> getHistoricalTopics() {
    return Arrays.asList(
        HISTORICAL_TOPIC_1, HISTORICAL_TOPIC_2, HISTORICAL_TOPIC_3, HISTORICAL_TOPIC_4);
  }

  // ==================== 预期结果数据 ====================

  /** 获取主题类型映射 */
  public static Map<String, MQTTTopicType> getTopicTypeMapping() {
    Map<String, MQTTTopicType> mapping = new HashMap<>();
    // 固定前缀主题
    mapping.put(VALID_THING_PROPERTY_UP_TOPIC, MQTTTopicType.THING_PROPERTY_UP);
    mapping.put(VALID_THING_EVENT_UP_TOPIC, MQTTTopicType.THING_EVENT_UP);
    mapping.put(VALID_THING_DOWN_TOPIC, MQTTTopicType.THING_DOWN);
    mapping.put(VALID_PASSTHROUGH_UP_TOPIC, MQTTTopicType.PASSTHROUGH_UP);
    mapping.put(VALID_PASSTHROUGH_DOWN_TOPIC, MQTTTopicType.PASSTHROUGH_DOWN);
    // 动态前缀主题
    mapping.put(VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC, MQTTTopicType.THING_PROPERTY_UP);
    mapping.put(VALID_DYNAMIC_THING_EVENT_UP_TOPIC, MQTTTopicType.THING_EVENT_UP);
    mapping.put(VALID_DYNAMIC_THING_DOWN_TOPIC, MQTTTopicType.THING_DOWN);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC, MQTTTopicType.PASSTHROUGH_UP);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC, MQTTTopicType.PASSTHROUGH_DOWN);
    // 系统级主题
    mapping.put(VALID_OTA_REPORT_TOPIC, MQTTTopicType.OTA_REPORT);
    mapping.put(VALID_OTA_UPDATE_TOPIC, MQTTTopicType.OTA_UPDATE);
    return mapping;
  }

  /** 获取主题分类映射 */
  public static Map<String, MqttConstant.TopicCategory> getTopicCategoryMapping() {
    Map<String, MqttConstant.TopicCategory> mapping = new HashMap<>();
    // 固定前缀主题
    mapping.put(VALID_THING_PROPERTY_UP_TOPIC, MqttConstant.TopicCategory.THING_MODEL);
    mapping.put(VALID_THING_EVENT_UP_TOPIC, MqttConstant.TopicCategory.THING_MODEL);
    mapping.put(VALID_THING_DOWN_TOPIC, MqttConstant.TopicCategory.THING_MODEL);
    mapping.put(VALID_PASSTHROUGH_UP_TOPIC, MqttConstant.TopicCategory.PASSTHROUGH);
    mapping.put(VALID_PASSTHROUGH_DOWN_TOPIC, MqttConstant.TopicCategory.PASSTHROUGH);
    // 动态前缀主题
    mapping.put(VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC, MqttConstant.TopicCategory.THING_MODEL);
    mapping.put(VALID_DYNAMIC_THING_EVENT_UP_TOPIC, MqttConstant.TopicCategory.THING_MODEL);
    mapping.put(VALID_DYNAMIC_THING_DOWN_TOPIC, MqttConstant.TopicCategory.THING_MODEL);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC, MqttConstant.TopicCategory.PASSTHROUGH);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC, MqttConstant.TopicCategory.PASSTHROUGH);
    // 系统级主题
    mapping.put(VALID_OTA_REPORT_TOPIC, MqttConstant.TopicCategory.SYSTEM_LEVEL);
    mapping.put(VALID_OTA_UPDATE_TOPIC, MqttConstant.TopicCategory.SYSTEM_LEVEL);
    return mapping;
  }

  /** 获取产品Key映射 */
  public static Map<String, String> getProductKeyMapping() {
    Map<String, String> mapping = new HashMap<>();
    // 固定前缀主题
    mapping.put(VALID_THING_PROPERTY_UP_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_THING_EVENT_UP_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_THING_DOWN_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_PASSTHROUGH_UP_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_PASSTHROUGH_DOWN_TOPIC, TEST_PRODUCT_KEY);
    // 动态前缀主题
    mapping.put(VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_DYNAMIC_THING_EVENT_UP_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_DYNAMIC_THING_DOWN_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC, TEST_PRODUCT_KEY);
    // 系统级主题
    mapping.put(VALID_OTA_REPORT_TOPIC, TEST_PRODUCT_KEY);
    mapping.put(VALID_OTA_UPDATE_TOPIC, TEST_PRODUCT_KEY);
    // 特殊字符主题
    mapping.put(SPECIAL_CHAR_TOPIC, "test-product_123");
    mapping.put(CHINESE_TOPIC, "测试产品");
    mapping.put(NUMERIC_TOPIC, "12345");
    mapping.put(MIXED_TOPIC, "test-产品_123");
    return mapping;
  }

  /** 获取设备ID映射 */
  public static Map<String, String> getDeviceIdMapping() {
    Map<String, String> mapping = new HashMap<>();
    // 固定前缀主题
    mapping.put(VALID_THING_PROPERTY_UP_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_THING_EVENT_UP_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_THING_DOWN_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_PASSTHROUGH_UP_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_PASSTHROUGH_DOWN_TOPIC, TEST_DEVICE_ID);
    // 动态前缀主题
    mapping.put(VALID_DYNAMIC_THING_PROPERTY_UP_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_DYNAMIC_THING_EVENT_UP_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_DYNAMIC_THING_DOWN_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_UP_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_DYNAMIC_PASSTHROUGH_DOWN_TOPIC, TEST_DEVICE_ID);
    // 系统级主题
    mapping.put(VALID_OTA_REPORT_TOPIC, TEST_DEVICE_ID);
    mapping.put(VALID_OTA_UPDATE_TOPIC, TEST_DEVICE_ID);
    // 特殊字符主题
    mapping.put(SPECIAL_CHAR_TOPIC, "test-device.456");
    mapping.put(CHINESE_TOPIC, "测试设备");
    mapping.put(NUMERIC_TOPIC, "67890");
    mapping.put(MIXED_TOPIC, "device-设备.456");
    return mapping;
  }

  // ==================== 测试用例数据 ====================

  /** 创建性能测试数据 */
  public static List<String> createPerformanceTestData() {
    List<String> testData = new ArrayList<>();
    String[] prefixes = {"$thing", "$qiantang", "$custom", "$platform"};

    for (int i = 0; i < 1000; i++) {
      String prefix = prefixes[i % prefixes.length];
      testData.add(prefix + "/up/property/product" + i + "/device" + i);
    }
    return testData;
  }

  /** 创建边界测试数据 */
  public static List<String> createBoundaryTestData() {
    return Arrays.asList(
        // 固定前缀边界测试
        "$thing/up/property/a/b", // 最短有效主题
        "$thing/up/property/" + "a".repeat(100) + "/" + "b".repeat(100), // 长主题
        "$thing/up/property"
            + "/1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "/device", // 超长产品Key
        "$thing/up/property/product/" + "d".repeat(1000), // 超长设备ID
        "$thing/up/property/", // 空产品Key和设备ID
        "$thing/up/property//", // 空产品Key
        "$thing/up/property/product/", // 空设备ID
        "$thing/up/property//device", // 空产品Key
        // 动态前缀边界测试
        "$qiantang/up/property/a/b", // 动态前缀最短有效主题
        "$custom/up/property/" + "a".repeat(100) + "/" + "b".repeat(100), // 动态前缀长主题
        "$platform/up/property/", // 动态前缀空产品Key和设备ID
        "$device/up/property//", // 动态前缀空产品Key
        "$sensor/up/property/product/" // 动态前缀空设备ID
        );
  }

  /** 创建压力测试数据 */
  public static List<String> createStressTestData() {
    List<String> testData = new ArrayList<>();
    Random random = new Random(42); // 固定种子以确保可重复性
    String[] prefixes = {"$thing", "$qiantang", "$custom", "$platform", "$device", "$sensor"};

    for (int i = 0; i < 10000; i++) {
      String prefix = prefixes[random.nextInt(prefixes.length)];
      String productKey = "product" + random.nextInt(1000);
      String deviceId = "device" + random.nextInt(1000);
      String topicType = random.nextBoolean() ? "property" : "event";
      testData.add(prefix + "/up/" + topicType + "/" + productKey + "/" + deviceId);
    }
    return testData;
  }

  // ==================== 辅助方法 ====================

  /** 验证主题信息 */
  public static void validateTopicInfo(
      MQTTTopicManager.TopicInfo topicInfo,
      String expectedTopic,
      MQTTTopicType expectedType,
      MqttConstant.TopicCategory expectedCategory,
      String expectedProductKey,
      String expectedDeviceId,
      boolean expectedUpstream) {
    assertNotNull(topicInfo);
    assertEquals(expectedTopic, topicInfo.getOriginalTopic());
    assertEquals(expectedType, topicInfo.getTopicType());
    assertEquals(expectedCategory, topicInfo.getCategory());
    assertEquals(expectedProductKey, topicInfo.getProductKey());
    assertEquals(expectedDeviceId, topicInfo.getDeviceId());
    assertEquals(expectedUpstream, topicInfo.isUpstream());
    assertEquals(!expectedUpstream, topicInfo.isDownstream());
    assertTrue(topicInfo.isValid());
    assertEquals(expectedProductKey + ":" + expectedDeviceId, topicInfo.getDeviceUniqueId());
  }

  /** 验证无效主题信息 */
  public static void validateInvalidTopicInfo(
      MQTTTopicManager.TopicInfo topicInfo, String expectedTopic) {
    assertNotNull(topicInfo);
    assertEquals(expectedTopic, topicInfo.getOriginalTopic());
    assertNull(topicInfo.getTopicType());
    assertNull(topicInfo.getCategory());
    assertNull(topicInfo.getProductKey());
    assertNull(topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertFalse(topicInfo.isValid());
    assertNull(topicInfo.getDeviceUniqueId());
  }

  /** 验证主题配置 */
  public static void validateTopicConfig(
      MQTTProductConfig.MqttTopicConfig config,
      String expectedPattern,
      int expectedQos,
      boolean expectedEnabled) {
    assertNotNull(config);
    assertEquals(expectedPattern, config.getTopicPattern());
    assertEquals(expectedQos, config.getQos());
    assertEquals(expectedEnabled, config.isEnabled());
  }
}

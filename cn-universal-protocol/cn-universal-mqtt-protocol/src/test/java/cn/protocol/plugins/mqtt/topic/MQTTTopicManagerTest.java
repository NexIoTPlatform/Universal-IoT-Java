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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.entity.MQTTProductConfig;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQTTTopicManager 测试类
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@ExtendWith(MockitoExtension.class)
class MQTTTopicManagerTest {

  private static final Logger log = LoggerFactory.getLogger(MQTTTopicManagerTest.class);

  @InjectMocks private MQTTTopicManager topicManager;

  // Mock 数据
  private static final String VALID_THING_PROPERTY_TOPIC =
      "$thing/up/property/testProduct/testDevice";
  private static final String VALID_THING_EVENT_TOPIC = "$thing/up/event/testProduct/testDevice";
  private static final String VALID_THING_DOWN_TOPIC = "$thing/down/testProduct/testDevice";
  private static final String VALID_PASSTHROUGH_UP_TOPIC = "$thing/up/testProduct/testDevice";
  private static final String VALID_PASSTHROUGH_DOWN_TOPIC = "$thing/down/testProduct/testDevice";
  private static final String VALID_OTA_REPORT_TOPIC = "$ota/report/testProduct/testDevice";
  private static final String VALID_OTA_UPDATE_TOPIC = "$ota/update/testProduct/testDevice";
  private static final String INVALID_TOPIC = "invalid/topic/format";
  private static final String NULL_TOPIC = null;
  private static final String EMPTY_TOPIC = "";

  private static final String TEST_PRODUCT_KEY = "testProduct";
  private static final String TEST_DEVICE_ID = "testDevice";

  @BeforeEach
  void setUp() {
    // 手动调用初始化方法
    topicManager.initialize();
  }

  // ==================== 测试数据准备 ====================

  /** 创建测试用的主题配置JSON */
  private String createTestTopicConfigJson() {
    return """
           {
               "thing-model": {
                   "description": "物模型主题",
                   "topics": [
                       "$thing/up/property/+/+",
                       "$thing/up/event/+/+",
                       "$thing/down/+/+"
                   ]
               },
               "system-level": {
                   "description": "系统级主题",
                   "topics": [
                       "$ota/report/+/+",
                       "$ota/update/+/+"
                   ]
               },
               "passthrough": {
                   "description": "透传主题",
                   "topics": [
                       "$thing/up/+/+",
                       "$thing/down/+/+"
                   ]
               }
           }
           """;
  }

  /** 创建测试用的配置Map */
  private Map<String, Object> createTestConfigMap() {
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "$thing/up/property/+/+,$thing/up/event/+/+,$ota/report/+/+");
    configMap.put("topics", "$thing/up/+/+,$thing/down/+/+");
    return configMap;
  }

  // ==================== 订阅主题管理测试 ====================

  @Test
  void testGetSubscriptionTopics_ThingModel() {
    List<String> topics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.THING_MODEL);
    assertNotNull(topics);
    assertFalse(topics.isEmpty());
    log.info("物模型主题: {}", topics);
  }

  @Test
  void testGetSubscriptionTopics_SystemLevel() {
    List<String> topics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.SYSTEM_LEVEL);
    assertNotNull(topics);
    assertFalse(topics.isEmpty());
    log.info("系统级主题: {}", topics);
  }

  @Test
  void testGetSubscriptionTopics_Passthrough() {
    List<String> topics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.PASSTHROUGH);
    assertNotNull(topics);
    assertFalse(topics.isEmpty());
    log.info("透传主题: {}", topics);
  }

  @Test
  void testGetSubscriptionTopics_EmptyCategory() {
    // 执行
    List<String> topics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.THING_MODEL);

    // 验证
    assertNotNull(topics);
    // 返回不可修改的列表
    assertThrows(UnsupportedOperationException.class, () -> topics.add("test"));
  }

  @Test
  void testGetAllSubscriptionTopics() {
    List<String> allTopics = topicManager.getAllSubscriptionTopics();
    assertNotNull(allTopics);
    assertFalse(allTopics.isEmpty());

    List<String> thingModelTopics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.THING_MODEL);
    List<String> systemLevelTopics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.SYSTEM_LEVEL);
    List<String> passthroughTopics =
        topicManager.getSubscriptionTopics(MqttConstant.TopicCategory.PASSTHROUGH);

    assertEquals(
        allTopics.size(),
        thingModelTopics.size() + systemLevelTopics.size() + passthroughTopics.size());
    log.info("所有主题: {}", allTopics);
  }

  // ==================== 主题类型解析测试 ====================

  @Test
  void testParseTopicType_ThingPropertyUp() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_THING_PROPERTY_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType);
    assertTrue(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_ThingEventUp() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_THING_EVENT_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.THING_EVENT_UP, topicType);
    assertTrue(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_ThingDown() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_THING_DOWN_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.THING_DOWN, topicType);
    assertFalse(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_PassthroughUp() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_PASSTHROUGH_UP_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, topicType);
    assertTrue(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_PassthroughDown() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_PASSTHROUGH_DOWN_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.PASSTHROUGH_DOWN, topicType);
    assertFalse(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_OtaReport() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_OTA_REPORT_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.OTA_REPORT, topicType);
    assertTrue(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_OtaUpdate() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(VALID_OTA_UPDATE_TOPIC);

    // 验证
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.OTA_UPDATE, topicType);
    assertFalse(topicType.isUpstream());
  }

  @Test
  void testParseTopicType_InvalidTopic() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(INVALID_TOPIC);

    // 验证
    assertNull(topicType);
  }

  @Test
  void testParseTopicType_NullTopic() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(NULL_TOPIC);

    // 验证
    assertNull(topicType);
  }

  @Test
  void testParseTopicType_EmptyTopic() {
    // 执行
    MQTTTopicType topicType = topicManager.parseTopicType(EMPTY_TOPIC);

    // 验证
    assertNull(topicType);
  }

  @Test
  void testParseTopicType_CachePerformance() {
    // 第一次解析
    MQTTTopicType firstResult = topicManager.parseTopicType(VALID_THING_PROPERTY_TOPIC);
    assertNotNull(firstResult);

    // 第二次解析相同主题（应该使用缓存）
    MQTTTopicType secondResult = topicManager.parseTopicType(VALID_THING_PROPERTY_TOPIC);
    assertNotNull(secondResult);
    assertEquals(firstResult, secondResult);
  }

  // ==================== 标准主题检查测试 ====================

  @Test
  void testIsStandardTopic_ValidTopics() {
    // 验证所有有效主题
    assertTrue(topicManager.isStandardTopic(VALID_THING_PROPERTY_TOPIC));
    assertTrue(topicManager.isStandardTopic(VALID_THING_EVENT_TOPIC));
    assertTrue(topicManager.isStandardTopic(VALID_THING_DOWN_TOPIC));
    assertTrue(topicManager.isStandardTopic(VALID_PASSTHROUGH_UP_TOPIC));
    assertTrue(topicManager.isStandardTopic(VALID_PASSTHROUGH_DOWN_TOPIC));
    assertTrue(topicManager.isStandardTopic(VALID_OTA_REPORT_TOPIC));
    assertTrue(topicManager.isStandardTopic(VALID_OTA_UPDATE_TOPIC));
  }

  @Test
  void testIsStandardTopic_InvalidTopics() {
    // 验证无效主题
    assertFalse(topicManager.isStandardTopic(INVALID_TOPIC));
    assertFalse(topicManager.isStandardTopic(NULL_TOPIC));
    assertFalse(topicManager.isStandardTopic(EMPTY_TOPIC));
  }

  // ==================== 主题信息提取测试 ====================

  @Test
  void testExtractTopicInfo_ThingPropertyUp() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo =
        topicManager.extractTopicInfo(VALID_THING_PROPERTY_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_THING_PROPERTY_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.THING_MODEL, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertTrue(topicInfo.isUpstream());
    assertTrue(topicInfo.isValid());
    assertEquals(TEST_PRODUCT_KEY + ":" + TEST_DEVICE_ID, topicInfo.getDeviceUniqueId());
  }

  @Test
  void testExtractTopicInfo_ThingEventUp() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(VALID_THING_EVENT_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_THING_EVENT_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.THING_EVENT_UP, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.THING_MODEL, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertTrue(topicInfo.isUpstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractTopicInfo_ThingDown() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(VALID_THING_DOWN_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_THING_DOWN_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.THING_DOWN, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.THING_MODEL, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertTrue(topicInfo.isDownstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractTopicInfo_PassthroughUp() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo =
        topicManager.extractTopicInfo(VALID_PASSTHROUGH_UP_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_PASSTHROUGH_UP_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.PASSTHROUGH, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertTrue(topicInfo.isUpstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractTopicInfo_PassthroughDown() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo =
        topicManager.extractTopicInfo(VALID_PASSTHROUGH_DOWN_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_PASSTHROUGH_DOWN_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.PASSTHROUGH_DOWN, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.PASSTHROUGH, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertTrue(topicInfo.isDownstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractTopicInfo_OtaReport() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(VALID_OTA_REPORT_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_OTA_REPORT_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.OTA_REPORT, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.SYSTEM_LEVEL, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertTrue(topicInfo.isUpstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractTopicInfo_OtaUpdate() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(VALID_OTA_UPDATE_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_OTA_UPDATE_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.OTA_UPDATE, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.SYSTEM_LEVEL, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertTrue(topicInfo.isDownstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractTopicInfo_InvalidTopic() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(INVALID_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(INVALID_TOPIC, topicInfo.getOriginalTopic());
    assertNull(topicInfo.getTopicType());
    assertNull(topicInfo.getCategory());
    assertNull(topicInfo.getProductKey());
    assertNull(topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertFalse(topicInfo.isValid());
    assertNull(topicInfo.getDeviceUniqueId());
  }

  @Test
  void testExtractTopicInfo_NullTopic() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(NULL_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(NULL_TOPIC, topicInfo.getOriginalTopic());
    assertNull(topicInfo.getTopicType());
    assertNull(topicInfo.getCategory());
    assertNull(topicInfo.getProductKey());
    assertNull(topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertFalse(topicInfo.isValid());
  }

  // ==================== TopicInfo 构建器测试 ====================

  @Test
  void testTopicInfoBuilder() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo =
        MQTTTopicManager.TopicInfo.builder()
            .originalTopic(VALID_THING_PROPERTY_TOPIC)
            .topicType(MQTTTopicType.THING_PROPERTY_UP)
            .category(MqttConstant.TopicCategory.THING_MODEL)
            .productKey(TEST_PRODUCT_KEY)
            .deviceId(TEST_DEVICE_ID)
            .isUpstream(true)
            .isValid(true)
            .build();

    // 验证
    assertNotNull(topicInfo);
    assertEquals(VALID_THING_PROPERTY_TOPIC, topicInfo.getOriginalTopic());
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicInfo.getTopicType());
    assertEquals(MqttConstant.TopicCategory.THING_MODEL, topicInfo.getCategory());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());
    assertTrue(topicInfo.isUpstream());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testTopicInfoUnknown() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo = MQTTTopicManager.TopicInfo.unknown(INVALID_TOPIC);

    // 验证
    assertNotNull(topicInfo);
    assertEquals(INVALID_TOPIC, topicInfo.getOriginalTopic());
    assertNull(topicInfo.getTopicType());
    assertNull(topicInfo.getCategory());
    assertNull(topicInfo.getProductKey());
    assertNull(topicInfo.getDeviceId());
    assertFalse(topicInfo.isUpstream());
    assertFalse(topicInfo.isValid());
  }

  @Test
  void testTopicInfoToString() {
    // 执行
    MQTTTopicManager.TopicInfo topicInfo =
        topicManager.extractTopicInfo(VALID_THING_PROPERTY_TOPIC);
    String toString = topicInfo.toString();

    // 验证
    assertNotNull(toString);
    assertTrue(toString.contains("TopicInfo"));
    assertTrue(toString.contains(VALID_THING_PROPERTY_TOPIC));
    assertTrue(toString.contains(TEST_PRODUCT_KEY));
    assertTrue(toString.contains(TEST_DEVICE_ID));
  }

  // ==================== 主题分类匹配测试 ====================

  @Test
  void testMatchCategory_ThingModel() {
    // 验证物模型主题分类
    assertEquals(
        MqttConstant.TopicCategory.THING_MODEL,
        MQTTTopicManager.matchCategory(VALID_THING_PROPERTY_TOPIC));
    assertEquals(
        MqttConstant.TopicCategory.THING_MODEL,
        MQTTTopicManager.matchCategory(VALID_THING_EVENT_TOPIC));
    assertEquals(
        MqttConstant.TopicCategory.THING_MODEL,
        MQTTTopicManager.matchCategory(VALID_THING_DOWN_TOPIC));
  }

  @Test
  void testMatchCategory_Passthrough() {
    // 验证透传主题分类
    assertEquals(
        MqttConstant.TopicCategory.PASSTHROUGH,
        MQTTTopicManager.matchCategory(VALID_PASSTHROUGH_UP_TOPIC));
    assertEquals(
        MqttConstant.TopicCategory.PASSTHROUGH,
        MQTTTopicManager.matchCategory(VALID_PASSTHROUGH_DOWN_TOPIC));
  }

  @Test
  void testMatchCategory_SystemLevel() {
    // 验证系统级主题分类
    assertEquals(
        MqttConstant.TopicCategory.SYSTEM_LEVEL,
        MQTTTopicManager.matchCategory(VALID_OTA_REPORT_TOPIC));
    assertEquals(
        MqttConstant.TopicCategory.SYSTEM_LEVEL,
        MQTTTopicManager.matchCategory(VALID_OTA_UPDATE_TOPIC));
  }

  @Test
  void testMatchCategory_Unknown() {
    // 验证未知主题分类
    assertEquals(MqttConstant.TopicCategory.UNKNOWN, MQTTTopicManager.matchCategory(INVALID_TOPIC));
  }

  // ==================== 产品Key和设备ID提取测试 ====================

  @Test
  void testExtractProductKeyFromTopic_ValidTopics() {
    // 验证所有有效主题的产品Key提取
    assertEquals(
        TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_THING_PROPERTY_TOPIC));
    assertEquals(
        TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_THING_EVENT_TOPIC));
    assertEquals(TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_THING_DOWN_TOPIC));
    assertEquals(
        TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_PASSTHROUGH_UP_TOPIC));
    assertEquals(
        TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_PASSTHROUGH_DOWN_TOPIC));
    assertEquals(TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_OTA_REPORT_TOPIC));
    assertEquals(TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(VALID_OTA_UPDATE_TOPIC));
  }

  @Test
  void testExtractProductKeyFromTopic_InvalidTopic() {
    // 验证无效主题的产品Key提取
    assertNull(topicManager.extractProductKeyFromTopic(INVALID_TOPIC));
    assertNull(topicManager.extractProductKeyFromTopic(NULL_TOPIC));
    assertNull(topicManager.extractProductKeyFromTopic(EMPTY_TOPIC));
  }

  @Test
  void testExtractProductKeyFromTopic_HistoricalFormat() {
    // 验证历史格式的产品Key提取
    String historicalTopic = "productKey/deviceId";
    assertEquals("productKey", topicManager.extractProductKeyFromTopic(historicalTopic));

    String historicalTopic2 = "prefix/productKey/deviceId";
    assertEquals("productKey", topicManager.extractProductKeyFromTopic(historicalTopic2));
  }

  @Test
  void testExtractDeviceIdFromTopic_ValidTopics() {
    // 验证所有有效主题的设备ID提取
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_THING_PROPERTY_TOPIC));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_THING_EVENT_TOPIC));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_THING_DOWN_TOPIC));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_PASSTHROUGH_UP_TOPIC));
    assertEquals(
        TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_PASSTHROUGH_DOWN_TOPIC));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_OTA_REPORT_TOPIC));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(VALID_OTA_UPDATE_TOPIC));
  }

  @Test
  void testExtractDeviceIdFromTopic_InvalidTopic() {
    // 验证无效主题的设备ID提取
    assertEquals("unknown", topicManager.extractDeviceIdFromTopic(INVALID_TOPIC));
    assertEquals("unknown", topicManager.extractDeviceIdFromTopic(NULL_TOPIC));
    assertEquals("unknown", topicManager.extractDeviceIdFromTopic(EMPTY_TOPIC));
  }

  @Test
  void testExtractDeviceIdFromTopic_HistoricalFormat() {
    // 验证历史格式的设备ID提取
    String historicalTopic = "productKey/deviceId";
    assertEquals("deviceId", topicManager.extractDeviceIdFromTopic(historicalTopic));

    String historicalTopic2 = "prefix/productKey/deviceId";
    assertEquals("deviceId", topicManager.extractDeviceIdFromTopic(historicalTopic2));
  }

  // ==================== 配置解析测试 ====================

  @Test
  void testParseSubscribeTopicsFromConfig_WithThirdMQTTSubscribeTopics() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "$thing/up/property/+/+,$thing/up/event/+/+,$ota/report/+/+");

    // 执行
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 2);

    // 验证
    assertNotNull(topicConfigs);
    assertEquals(3, topicConfigs.size());

    // 验证第一个主题配置
    MQTTProductConfig.MqttTopicConfig firstConfig = topicConfigs.get(0);
    assertEquals("$thing/up/property/+/+", firstConfig.getTopicPattern());
    assertEquals(2, firstConfig.getQos());
    assertTrue(firstConfig.isEnabled());
  }

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_WithTopics() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("topics", "$thing/up/+/+,$thing/down/+/+");

    // 执行
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证
    assertNotNull(topicConfigs);
    assertEquals(2, topicConfigs.size());

    // 验证主题配置
    MQTTProductConfig.MqttTopicConfig firstConfig = topicConfigs.get(0);
    assertEquals("$thing/up/+/+", firstConfig.getTopicPattern());
    assertEquals(1, firstConfig.getQos());
    assertTrue(firstConfig.isEnabled());
  }

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_WithEmptyTopics() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "");

    // 执行
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证 - 应该使用默认主题
    assertNotNull(topicConfigs);
    assertFalse(topicConfigs.isEmpty());
    assertTrue(topicConfigs.stream().allMatch(config -> config.getQos() == 1));
  }

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_WithNullConfig() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();

    // 执行
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证 - 应该使用默认主题
    assertNotNull(topicConfigs);
    assertFalse(topicConfigs.isEmpty());
    assertTrue(topicConfigs.stream().allMatch(config -> config.getQos() == 1));
  }

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_DefaultQos() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "$thing/up/property/+/+");

    // 执行 - 不指定QoS，使用默认值1
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证
    assertNotNull(topicConfigs);
    assertEquals(1, topicConfigs.size());
    assertEquals(1, topicConfigs.get(0).getQos());
  }

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_WithSemicolonSeparator() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", "$thing/up/property/+/+;$thing/up/event/+/+");

    // 执行
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证
    assertNotNull(topicConfigs);
    assertEquals(2, topicConfigs.size());
    assertEquals("$thing/up/property/+/+", topicConfigs.get(0).getTopicPattern());
    assertEquals("$thing/up/event/+/+", topicConfigs.get(1).getTopicPattern());
  }

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_WithWhitespace() {
    // 准备测试数据
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", " $thing/up/property/+/+ , $thing/up/event/+/+ ");

    // 执行
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证 - 应该去除空白字符
    assertNotNull(topicConfigs);
    assertEquals(2, topicConfigs.size());
    assertEquals("$thing/up/property/+/+", topicConfigs.get(0).getTopicPattern());
    assertEquals("$thing/up/event/+/+", topicConfigs.get(1).getTopicPattern());
  }

  // ==================== 边界条件测试 ====================

  @Test
  void testParseTopicType_WithSpecialCharacters() {
    // 测试包含特殊字符的主题
    String specialTopic = "$thing/up/property/test-product_123/test-device.456";
    MQTTTopicType topicType = topicManager.parseTopicType(specialTopic);

    assertNotNull(topicType);
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType);
  }

  @Test
  void testExtractTopicInfo_WithSpecialCharacters() {
    // 测试包含特殊字符的主题信息提取
    String specialTopic = "$thing/up/property/test-product_123/test-device.456";
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(specialTopic);

    assertNotNull(topicInfo);
    assertEquals("test-product_123", topicInfo.getProductKey());
    assertEquals("test-device.456", topicInfo.getDeviceId());
    assertTrue(topicInfo.isValid());
  }

  @Test
  void testExtractProductKeyFromTopic_WithSpecialCharacters() {
    // 测试包含特殊字符的产品Key提取
    String specialTopic = "$thing/up/property/test-product_123/test-device.456";
    String productKey = topicManager.extractProductKeyFromTopic(specialTopic);

    assertEquals("test-product_123", productKey);
  }

  @Test
  void testExtractDeviceIdFromTopic_WithSpecialCharacters() {
    // 测试包含特殊字符的设备ID提取
    String specialTopic = "$thing/up/property/test-product_123/test-device.456";
    String deviceId = topicManager.extractDeviceIdFromTopic(specialTopic);

    assertEquals("test-device.456", deviceId);
  }

  // ==================== 异常处理测试 ====================

  @Test
  void testParseThirdMQTTSubscribeTopicsFromConfig_WithException() {
    // 准备会抛出异常的配置
    Map<String, Object> configMap = new HashMap<>();
    configMap.put("subscribeTopics", new Object()); // 非字符串类型

    // 执行 - 应该不会抛出异常，而是使用默认主题
    List<MQTTProductConfig.MqttTopicConfig> topicConfigs =
        topicManager.parseSubscribeTopicsFromConfig(configMap, 1);

    // 验证
    assertNotNull(topicConfigs);
    assertFalse(topicConfigs.isEmpty());
  }

  @Test
  void testExtractProductKeyFromTopic_WithException() {
    // 测试异常情况下的产品Key提取
    String malformedTopic = "$thing/up/property/"; // 格式不完整

    // 执行 - 应该不会抛出异常
    String productKey = topicManager.extractProductKeyFromTopic(malformedTopic);

    // 验证
    assertNull(productKey);
  }

  @Test
  void testExtractDeviceIdFromTopic_WithException() {
    // 测试异常情况下的设备ID提取
    String malformedTopic = "$thing/up/property/"; // 格式不完整

    // 执行 - 应该不会抛出异常
    String deviceId = topicManager.extractDeviceIdFromTopic(malformedTopic);

    // 验证
    assertEquals("unknown", deviceId);
  }

  // ==================== 性能测试 ====================

  @Test
  void testParseTopicType_Performance() {
    // 测试解析性能
    String topic = VALID_THING_PROPERTY_TOPIC;

    // 预热
    for (int i = 0; i < 100; i++) {
      topicManager.parseTopicType(topic);
    }

    // 性能测试
    long startTime = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
      topicManager.parseTopicType(topic);
    }
    long endTime = System.nanoTime();

    long duration = endTime - startTime;
    // 验证1000次解析应该在合理时间内完成（比如小于100ms）
    assertTrue(duration < 100_000_000); // 100ms in nanoseconds
  }

  @Test
  void testExtractTopicInfo_Performance() {
    // 测试主题信息提取性能
    String topic = VALID_THING_PROPERTY_TOPIC;

    // 预热
    for (int i = 0; i < 100; i++) {
      topicManager.extractTopicInfo(topic);
    }

    // 性能测试
    long startTime = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
      topicManager.extractTopicInfo(topic);
    }
    long endTime = System.nanoTime();

    long duration = endTime - startTime;
    // 验证1000次提取应该在合理时间内完成
    assertTrue(duration < 200_000_000); // 200ms in nanoseconds
  }

  // ==================== 集成测试 ====================

  @Test
  void testFullWorkflow_ThingModelTopic() {
    // 测试完整的物模型主题处理流程
    String topic = VALID_THING_PROPERTY_TOPIC;

    // 1. 检查是否为标准主题
    assertTrue(topicManager.isStandardTopic(topic));

    // 2. 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(topic);
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType);

    // 3. 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
    assertNotNull(topicInfo);
    assertTrue(topicInfo.isValid());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());

    // 4. 验证主题分类
    assertEquals(MqttConstant.TopicCategory.THING_MODEL, MQTTTopicManager.matchCategory(topic));

    // 5. 提取产品Key和设备ID
    assertEquals(TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(topic));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(topic));
  }

  @Test
  void testFullWorkflow_SystemLevelTopic() {
    // 测试完整的系统级主题处理流程
    String topic = VALID_OTA_REPORT_TOPIC;

    // 1. 检查是否为标准主题
    assertTrue(topicManager.isStandardTopic(topic));

    // 2. 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(topic);
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.OTA_REPORT, topicType);

    // 3. 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
    assertNotNull(topicInfo);
    assertTrue(topicInfo.isValid());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());

    // 4. 验证主题分类
    assertEquals(MqttConstant.TopicCategory.SYSTEM_LEVEL, MQTTTopicManager.matchCategory(topic));

    // 5. 提取产品Key和设备ID
    assertEquals(TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(topic));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(topic));
  }

  @Test
  void testFullWorkflow_PassthroughTopic() {
    // 测试完整的透传主题处理流程
    String topic = VALID_PASSTHROUGH_UP_TOPIC;

    // 1. 检查是否为标准主题
    assertTrue(topicManager.isStandardTopic(topic));

    // 2. 解析主题类型
    MQTTTopicType topicType = topicManager.parseTopicType(topic);
    assertNotNull(topicType);
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, topicType);

    // 3. 提取主题信息
    MQTTTopicManager.TopicInfo topicInfo = topicManager.extractTopicInfo(topic);
    assertNotNull(topicInfo);
    assertTrue(topicInfo.isValid());
    assertEquals(TEST_PRODUCT_KEY, topicInfo.getProductKey());
    assertEquals(TEST_DEVICE_ID, topicInfo.getDeviceId());

    // 4. 验证主题分类
    assertEquals(MqttConstant.TopicCategory.PASSTHROUGH, MQTTTopicManager.matchCategory(topic));

    // 5. 提取产品Key和设备ID
    assertEquals(TEST_PRODUCT_KEY, topicManager.extractProductKeyFromTopic(topic));
    assertEquals(TEST_DEVICE_ID, topicManager.extractDeviceIdFromTopic(topic));
  }

  @Test
  void test_topic_parse() {
    String topic = "$qiantang/up/property/681c0775c2dc427d0480ab5f/o10000184";
    System.out.println(
        MqttConstant.TopicCategory.THING_MODEL.equals(MQTTTopicManager.matchCategory(topic)));
    assertTrue(
        MqttConstant.TopicCategory.THING_MODEL.equals(MQTTTopicManager.matchCategory(topic)));
    String topics = "$qiantang/up/propertyx/681c0775c2dc427d0480ab5f/o10000184";
    assertFalse(
        MqttConstant.TopicCategory.THING_MODEL.equals(MQTTTopicManager.matchCategory(topics)));
  }
}

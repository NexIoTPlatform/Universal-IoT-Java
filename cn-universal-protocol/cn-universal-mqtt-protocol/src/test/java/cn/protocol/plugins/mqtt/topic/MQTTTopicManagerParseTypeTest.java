package cn.universal.protocol.mqtt.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.universal.mqtt.protocol.config.MqttConstant;
import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * MQTT主题管理器主题类型解析测试
 *
 * @version 2.0 @Author Aleo
 * @since 2025/1/20
 */
@DisplayName("MQTT主题管理器主题类型解析测试")
public class MQTTTopicManagerParseTypeTest {

  @Test
  @DisplayName("测试动态前缀物模型主题类型解析")
  public void testDynamicPrefixThingModelTopicTypeParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试动态前缀物模型属性上报
    String dynamicPropertyTopic = "$qiantang/up/property/product123/device456";
    MQTTTopicType topicType = manager.parseTopicType(dynamicPropertyTopic);

    assertNotNull(topicType, "动态前缀主题应该被正确解析");
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType, "应该解析为物模型属性上报类型");

    // 测试动态前缀物模型事件上报
    String dynamicEventTopic = "$qiantang/up/event/product123/device456";
    MQTTTopicType eventType = manager.parseTopicType(dynamicEventTopic);

    assertNotNull(eventType, "动态前缀事件主题应该被正确解析");
    assertEquals(MQTTTopicType.THING_EVENT_UP, eventType, "应该解析为物模型事件上报类型");

    // 测试动态前缀物模型下行
    String dynamicDownTopic = "$qiantang/down/product123/device456";
    MQTTTopicType downType = manager.parseTopicType(dynamicDownTopic);

    assertNotNull(downType, "动态前缀下行主题应该被正确解析");
    assertEquals(MQTTTopicType.THING_DOWN, downType, "应该解析为物模型下行类型");
  }

  @Test
  @DisplayName("测试动态前缀透传主题类型解析")
  public void testDynamicPrefixPassthroughTopicTypeParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试动态前缀透传上行
    String dynamicPassthroughUpTopic = "$qiantang/up/product123/device456";
    MQTTTopicType passthroughUpType = manager.parseTopicType(dynamicPassthroughUpTopic);

    assertNotNull(passthroughUpType, "动态前缀透传上行主题应该被正确解析");
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughUpType, "应该解析为透传上行类型");

    // 测试动态前缀透传下行
    String dynamicPassthroughDownTopic = "$qiantang/down/product123/device456";
    MQTTTopicType passthroughDownType = manager.parseTopicType(dynamicPassthroughDownTopic);

    assertNotNull(passthroughDownType, "动态前缀透传下行主题应该被正确解析");
    assertEquals(MQTTTopicType.PASSTHROUGH_DOWN, passthroughDownType, "应该解析为透传下行类型");
  }

  @Test
  @DisplayName("测试固定前缀主题类型解析（向后兼容）")
  public void testFixedPrefixTopicTypeParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试固定前缀物模型属性上报
    String fixedPropertyTopic = "$thing/up/property/product123/device456";
    MQTTTopicType topicType = manager.parseTopicType(fixedPropertyTopic);

    assertNotNull(topicType, "固定前缀主题应该被正确解析");
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType, "应该解析为物模型属性上报类型");

    // 测试固定前缀透传上行
    String fixedPassthroughTopic = "$thing/up/product123/device456";
    MQTTTopicType passthroughType = manager.parseTopicType(fixedPassthroughTopic);

    assertNotNull(passthroughType, "固定前缀透传主题应该被正确解析");
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughType, "应该解析为透传上行类型");
  }

  @Test
  @DisplayName("测试系统级主题类型解析")
  public void testSystemLevelTopicTypeParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试系统级OTA上报
    String otaReportTopic = "$ota/report/product123/device456";
    MQTTTopicType otaReportType = manager.parseTopicType(otaReportTopic);

    assertNotNull(otaReportType, "OTA上报主题应该被正确解析");
    assertEquals(MQTTTopicType.OTA_REPORT, otaReportType, "应该解析为OTA上报类型");

    // 测试系统级OTA更新
    String otaUpdateTopic = "$ota/update/product123/device456";
    MQTTTopicType otaUpdateType = manager.parseTopicType(otaUpdateTopic);

    assertNotNull(otaUpdateType, "OTA更新主题应该被正确解析");
    assertEquals(MQTTTopicType.OTA_UPDATE, otaUpdateType, "应该解析为OTA更新类型");
  }

  @Test
  @DisplayName("测试无效主题类型解析")
  public void testInvalidTopicTypeParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试无效主题
    String[] invalidTopics = {
      "$qiantang/invalid/property/product123/device456",
      "$qiantang/up/invalid/product123/device456",
      "invalid/up/property/product123/device456",
      "$qiantang/up/property/",
      "$qiantang/up/property/product123",
      "",
      null
    };

    for (String topic : invalidTopics) {
      MQTTTopicType topicType = manager.parseTopicType(topic);
      assertNull(topicType, "无效主题应该返回null: " + topic);
    }
  }

  @Test
  @DisplayName("测试不同动态前缀的兼容性")
  public void testDifferentDynamicPrefixes() {
    MQTTTopicManager manager = new MQTTTopicManager();
    String[] prefixes = {"$qiantang", "$custom", "$platform", "$device", "$sensor"};

    for (String prefix : prefixes) {
      // 测试物模型属性上报
      String propertyTopic = prefix + "/up/property/product123/device456";
      MQTTTopicType propertyType = manager.parseTopicType(propertyTopic);
      assertEquals(
          MQTTTopicType.THING_PROPERTY_UP, propertyType, "前缀 " + prefix + " 的物模型属性上报应该正确解析");

      // 测试透传上行
      String passthroughTopic = prefix + "/up/product123/device456";
      MQTTTopicType passthroughType = manager.parseTopicType(passthroughTopic);
      assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughType, "前缀 " + prefix + " 的透传上行应该正确解析");
    }
  }

  @Test
  @DisplayName("测试主题信息提取")
  public void testTopicInfoExtraction() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试动态前缀物模型属性上报信息提取
    String dynamicPropertyTopic = "$qiantang/up/property/product123/device456";
    MQTTTopicManager.TopicInfo info = manager.extractTopicInfo(dynamicPropertyTopic);

    assertNotNull(info, "主题信息应该被正确提取");
    assertEquals(MqttConstant.TopicCategory.THING_MODEL, info.getCategory());
    assertEquals("product123", info.getProductKey());
    assertEquals("device456", info.getDeviceId());
    assertTrue(info.isUpstream());
    assertTrue(info.isValid());

    // 测试动态前缀透传上行信息提取
    String dynamicPassthroughTopic = "$qiantang/up/product123/device456";
    MQTTTopicManager.TopicInfo passthroughInfo = manager.extractTopicInfo(dynamicPassthroughTopic);

    assertNotNull(passthroughInfo, "透传主题信息应该被正确提取");
    assertEquals(MqttConstant.TopicCategory.PASSTHROUGH, passthroughInfo.getCategory());
    assertEquals("product123", passthroughInfo.getProductKey());
    assertEquals("device456", passthroughInfo.getDeviceId());
    assertTrue(passthroughInfo.isUpstream());
    assertTrue(passthroughInfo.isValid());
  }
}

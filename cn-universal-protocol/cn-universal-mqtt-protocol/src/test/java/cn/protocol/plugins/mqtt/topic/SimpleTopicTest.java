package cn.universal.protocol.mqtt.topic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cn.universal.mqtt.protocol.topic.MQTTTopicManager;
import cn.universal.mqtt.protocol.topic.MQTTTopicType;
import org.junit.jupiter.api.Test;

/** 简单的主题解析测试 */
public class SimpleTopicTest {

  @Test
  public void testDynamicPrefixTopicParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试动态前缀物模型属性上报
    String topic = "$qiantang/up/property/product123/device456";
    MQTTTopicType topicType = manager.parseTopicType(topic);

    System.out.println("Topic: " + topic);
    System.out.println("Parsed Type: " + topicType);

    assertNotNull(topicType, "动态前缀主题应该被正确解析");
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType, "应该解析为物模型属性上报类型");

    // 测试动态前缀透传上行
    String passthroughTopic = "$qiantang/up/product123/device456";
    MQTTTopicType passthroughType = manager.parseTopicType(passthroughTopic);

    System.out.println("Passthrough Topic: " + passthroughTopic);
    System.out.println("Parsed Type: " + passthroughType);

    assertNotNull(passthroughType, "动态前缀透传主题应该被正确解析");
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughType, "应该解析为透传上行类型");
  }

  @Test
  public void testFixedPrefixTopicParsing() {
    MQTTTopicManager manager = new MQTTTopicManager();

    // 测试固定前缀物模型属性上报
    String topic = "$thing/up/property/product123/device456";
    MQTTTopicType topicType = manager.parseTopicType(topic);

    assertNotNull(topicType, "固定前缀主题应该被正确解析");
    assertEquals(MQTTTopicType.THING_PROPERTY_UP, topicType, "应该解析为物模型属性上报类型");

    // 测试固定前缀透传上行
    String passthroughTopic = "$thing/up/product123/device456";
    MQTTTopicType passthroughType = manager.parseTopicType(passthroughTopic);

    assertNotNull(passthroughType, "固定前缀透传主题应该被正确解析");
    assertEquals(MQTTTopicType.PASSTHROUGH_UP, passthroughType, "应该解析为透传上行类型");
  }

  @Test
  public void testSystemLevelTopicParsing() {
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
}

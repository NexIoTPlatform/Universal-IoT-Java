package cn.universal.protocol.mqtt.topic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.universal.mqtt.protocol.config.MqttConstant;
import org.junit.jupiter.api.Test;

/** MQTT主题模式测试 */
public class MqttTopicPatternTest {

  @Test
  public void testThingModelPatterns() {
    // 测试物模型属性上报模式
    assertTrue(
        MqttConstant.THING_PROPERTY_UP_PATTERN
            .matcher("$thing/up/property/product123/device456")
            .matches());
    assertTrue(
        MqttConstant.THING_PROPERTY_UP_PATTERN
            .matcher("$qiantang/up/property/product123/device456")
            .matches());
    assertTrue(
        MqttConstant.THING_PROPERTY_UP_PATTERN
            .matcher("$custom/up/property/product123/device456")
            .matches());

    // 测试物模型事件上报模式
    assertTrue(
        MqttConstant.THING_EVENT_UP_PATTERN
            .matcher("$thing/up/event/product123/device456")
            .matches());
    assertTrue(
        MqttConstant.THING_EVENT_UP_PATTERN
            .matcher("$qiantang/up/event/product123/device456")
            .matches());
    assertTrue(
        MqttConstant.THING_EVENT_UP_PATTERN
            .matcher("$custom/up/event/product123/device456")
            .matches());

    // 测试物模型下行模式
    assertTrue(
        MqttConstant.THING_DOWN_PATTERN.matcher("$thing/down/product123/device456").matches());
    assertTrue(
        MqttConstant.THING_DOWN_PATTERN.matcher("$qiantang/down/product123/device456").matches());
    assertTrue(
        MqttConstant.THING_DOWN_PATTERN.matcher("$custom/down/product123/device456").matches());
  }

  @Test
  public void testPassthroughPatterns() {
    // 测试透传上行模式
    assertTrue(
        MqttConstant.PASSTHROUGH_UP_PATTERN.matcher("$thing/up/product123/device456").matches());
    assertTrue(
        MqttConstant.PASSTHROUGH_UP_PATTERN.matcher("$qiantang/up/product123/device456").matches());
    assertTrue(
        MqttConstant.PASSTHROUGH_UP_PATTERN.matcher("$custom/up/product123/device456").matches());

    // 测试透传下行模式
    assertTrue(
        MqttConstant.PASSTHROUGH_DOWN_PATTERN
            .matcher("$thing/down/product123/device456")
            .matches());
    assertTrue(
        MqttConstant.PASSTHROUGH_DOWN_PATTERN
            .matcher("$qiantang/down/product123/device456")
            .matches());
    assertTrue(
        MqttConstant.PASSTHROUGH_DOWN_PATTERN
            .matcher("$custom/down/product123/device456")
            .matches());
  }

  @Test
  public void testSystemLevelPatterns() {
    // 测试系统级OTA上报模式
    assertTrue(
        MqttConstant.OTA_REPORT_PATTERN.matcher("$ota/report/product123/device456").matches());

    // 测试系统级OTA更新模式
    assertTrue(
        MqttConstant.OTA_UPDATE_PATTERN.matcher("$ota/update/product123/device456").matches());
  }

  @Test
  public void testInvalidPatterns() {
    // 测试无效的物模型模式
    assertFalse(
        MqttConstant.THING_PROPERTY_UP_PATTERN
            .matcher("$thing/up/invalid/product123/device456")
            .matches());
    assertFalse(MqttConstant.THING_PROPERTY_UP_PATTERN.matcher("$thing/up/property/").matches());
    assertFalse(
        MqttConstant.THING_PROPERTY_UP_PATTERN
            .matcher("invalid/up/property/product123/device456")
            .matches());

    // 测试无效的透传模式
    assertFalse(
        MqttConstant.PASSTHROUGH_UP_PATTERN
            .matcher("$thing/invalid/product123/device456")
            .matches());
    assertFalse(MqttConstant.PASSTHROUGH_UP_PATTERN.matcher("$thing/up/").matches());
    assertFalse(
        MqttConstant.PASSTHROUGH_UP_PATTERN.matcher("invalid/up/product123/device456").matches());

    // 测试无效的系统级模式
    assertFalse(
        MqttConstant.OTA_REPORT_PATTERN.matcher("$thing/report/product123/device456").matches());
    assertFalse(
        MqttConstant.OTA_REPORT_PATTERN.matcher("$ota/invalid/product123/device456").matches());
  }
}

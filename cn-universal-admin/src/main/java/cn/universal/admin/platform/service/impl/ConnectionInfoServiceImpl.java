package cn.universal.admin.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.universal.admin.config.ConnectionConfig;
import cn.universal.admin.platform.dto.ConnectionInfoDTO;
import cn.universal.admin.platform.service.ConnectionInfoService;
import cn.universal.persistence.entity.IoTProduct;
import cn.universal.persistence.entity.Network;
import cn.universal.persistence.mapper.IoTProductMapper;
import cn.universal.persistence.mapper.NetworkMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 连接信息服务实现 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionInfoServiceImpl implements ConnectionInfoService {

  private final IoTProductMapper productMapper;
  private final NetworkMapper networkMapper;
  private final ConnectionConfig connectionConfig;

  @Override
  public ConnectionInfoDTO getConnectionInfo(String productKey) {
    log.debug("获取产品连接信息: productKey={}", productKey);

    // 查询产品信息
    IoTProduct product = productMapper.getProductByProductKey(productKey);
    if (product == null) {
      throw new RuntimeException("产品不存在: " + productKey);
    }

    // 构建内置连接信息
    ConnectionInfoDTO.BuiltinConnection builtin = buildBuiltinConnection(product);

    // 构建网络组件连接信息
    ConnectionInfoDTO.NetworkConnection network = buildNetworkConnection(product);

    return ConnectionInfoDTO.builder().builtin(builtin).network(network).build();
  }

  @Override
  public ConnectionInfoDTO getMqttPasswordInfo(String productKey) {
    log.debug("获取产品MQTT密码信息: productKey={}", productKey);

    // 查询产品信息
    IoTProduct product = productMapper.getProductByProductKey(productKey);
    if (product == null) {
      throw new RuntimeException("产品不存在: " + productKey);
    }

    // 构建内置MQTT密码信息
    ConnectionInfoDTO.BuiltinConnection builtin = buildBuiltinMqttPassword(product);

    // 构建网络组件MQTT密码信息
    ConnectionInfoDTO.NetworkConnection network = buildNetworkMqttPassword(product);

    return ConnectionInfoDTO.builder().builtin(builtin).network(network).build();
  }

  /** 构建内置连接信息 */
  private ConnectionInfoDTO.BuiltinConnection buildBuiltinConnection(IoTProduct product) {
    String platform = product.getThirdPlatform();

    ConnectionInfoDTO.BuiltinConnection.BuiltinConnectionBuilder builder =
        ConnectionInfoDTO.BuiltinConnection.builder();

    switch (platform) {
      case "tcp":
        return builder
            .connectionType(connectionConfig.getTcpConnectionType())
            .host(connectionConfig.getTcpHost(product.getProductKey()))
            .port(connectionConfig.getTcpSniPort())
            .description(connectionConfig.getTcpDescription())
            .build();

      case "mqtt":
        ConnectionInfoDTO.BuiltinConnection builtinMqtt =
            builder
                .connectionType(connectionConfig.getMqttConnectionType())
                .host(connectionConfig.getMqttHost())
                .port(connectionConfig.extractPortFromHost(connectionConfig.getMqttHost()))
                .username(product.getProductKey())
                .password("点击查看密码")
                .description(connectionConfig.getMqttDescription())
                .topics(buildDefaultTopics(product.getProductKey()))
                .build();
        return builtinMqtt;

      case "udp":
        return builder
            .connectionType(connectionConfig.getUdpConnectionType())
            .host(connectionConfig.getTcpHost(product.getProductKey()))
            .port(connectionConfig.getUdpPort())
            .description(connectionConfig.getUdpDescription())
            .build();
      default:
        return builder
            .connectionType("请联系管理员")
            .host("请联系管理员")
            .port(0)
            .description("暂无")
            .build();
    }
  }

  /** 构建网络组件连接信息 */
  private ConnectionInfoDTO.NetworkConnection buildNetworkConnection(IoTProduct product) {
    if (StrUtil.isBlank(product.getNetworkUnionId())) {
      return ConnectionInfoDTO.NetworkConnection.builder()
          .enabled(false)
          .message("未绑定网络组件")
          .build();
    }

    // 查询网络组件
    List<Network> networks = networkMapper.selectByUnionId(product.getNetworkUnionId());
    if (networks.isEmpty()) {
      return ConnectionInfoDTO.NetworkConnection.builder()
          .enabled(false)
          .message("绑定的网络组件不存在")
          .build();
    }

    Network network = networks.get(0);
    JSONObject config = JSONUtil.parseObj(network.getConfiguration());

    ConnectionInfoDTO.NetworkConnection.NetworkConnectionBuilder builder =
        ConnectionInfoDTO.NetworkConnection.builder()
            .enabled(true)
            .name(network.getName())
            .type(network.getType())
            .state(network.getState())
            .host(config.getStr("host"))
            .port(config.getInt("port"))
            .username(config.getStr("username"))
            .password("点击查看密码");

    // 处理MQTT主题信息
    if ("mqtt".equals(product.getThirdPlatform())) {
      ConnectionInfoDTO.TopicInfo subscribeTopics =
          buildSubscribeTopics(config, product.getProductKey());
      builder.subscribeTopics(subscribeTopics);
    }

    return builder.build();
  }

  /** 构建内置MQTT密码信息 */
  private ConnectionInfoDTO.BuiltinConnection buildBuiltinMqttPassword(IoTProduct product) {
    if (!"mqtt".equals(product.getThirdPlatform())) {
      return null;
    }

    return ConnectionInfoDTO.BuiltinConnection.builder()
        .connectionType(connectionConfig.getMqttConnectionType())
        .host(connectionConfig.getMqttHost())
        .port(connectionConfig.extractPortFromHost(connectionConfig.getMqttHost()))
        .username(product.getProductKey())
        .password(product.getProductSecret())
        .description(connectionConfig.getMqttDescription())
        .build();
  }

  /** 构建网络组件MQTT密码信息 */
  private ConnectionInfoDTO.NetworkConnection buildNetworkMqttPassword(IoTProduct product) {
    if (StrUtil.isBlank(product.getNetworkUnionId())) {
      return ConnectionInfoDTO.NetworkConnection.builder()
          .enabled(false)
          .message("未绑定网络组件")
          .build();
    }

    List<Network> networks = networkMapper.selectByUnionId(product.getNetworkUnionId());
    if (networks.isEmpty()) {
      return ConnectionInfoDTO.NetworkConnection.builder()
          .enabled(false)
          .message("绑定的网络组件不存在")
          .build();
    }

    Network network = networks.get(0);
    JSONObject config = JSONUtil.parseObj(network.getConfiguration());

    return ConnectionInfoDTO.NetworkConnection.builder()
        .enabled(true)
        .name(network.getName())
        .type(network.getType())
        .host(config.getStr("host"))
        .port(config.getInt("port"))
        .username(config.getStr("username"))
        .password(config.getStr("password"))
        .build();
  }

  /** 构建默认主题信息 */
  private ConnectionInfoDTO.TopicInfo buildDefaultTopics(String productKey) {
    ConnectionInfoDTO.ThingTopics thingTopics =
        ConnectionInfoDTO.ThingTopics.builder()
            .propertyUp(replaceProductKey(connectionConfig.getMqttThingPropertyUp(), productKey))
            .eventUp(replaceProductKey(connectionConfig.getMqttThingEventUp(), productKey))
            .commandDown(replaceProductKey(connectionConfig.getMqttThingCommandDown(), productKey))
            .build();

    ConnectionInfoDTO.PassthroughTopics passthroughTopics =
        ConnectionInfoDTO.PassthroughTopics.builder()
            .dataUp(replaceProductKey(connectionConfig.getMqttPassthroughDataUp(), productKey))
            .commandDown(
                replaceProductKey(connectionConfig.getMqttPassthroughCommandDown(), productKey))
            .build();

    return ConnectionInfoDTO.TopicInfo.builder()
        .thingTopics(thingTopics)
        .passthroughTopics(passthroughTopics)
        .build();
  }

  /** 构建订阅主题信息 */
  private ConnectionInfoDTO.TopicInfo buildSubscribeTopics(JSONObject config, String productKey) {
    JSONObject subscribeTopics = config.getJSONObject("subscribeTopics");

    if (subscribeTopics == null || subscribeTopics.isEmpty()) {
      // 使用默认主题
      return buildDefaultTopics(productKey);
    }

    ConnectionInfoDTO.ThingTopics thingTopics = null;
    ConnectionInfoDTO.PassthroughTopics passthroughTopics = null;

    JSONObject thingTopicsConfig = subscribeTopics.getJSONObject("thingTopics");
    if (thingTopicsConfig != null) {
      thingTopics =
          ConnectionInfoDTO.ThingTopics.builder()
              .propertyUp(thingTopicsConfig.getStr("propertyUp"))
              .eventUp(thingTopicsConfig.getStr("eventUp"))
              .commandDown(thingTopicsConfig.getStr("commandDown"))
              .build();
    }

    JSONObject passthroughTopicsConfig = subscribeTopics.getJSONObject("passthroughTopics");
    if (passthroughTopicsConfig != null) {
      passthroughTopics =
          ConnectionInfoDTO.PassthroughTopics.builder()
              .dataUp(passthroughTopicsConfig.getStr("dataUp"))
              .commandDown(passthroughTopicsConfig.getStr("commandDown"))
              .build();
    }

    return ConnectionInfoDTO.TopicInfo.builder()
        .thingTopics(thingTopics)
        .passthroughTopics(passthroughTopics)
        .build();
  }

  /** 替换主题中的产品Key占位符 */
  private String replaceProductKey(String topic, String productKey) {
    if (StrUtil.isBlank(topic)) {
      return topic;
    }
    // 支持两种格式：{productKey} 和 ${productKey}
    return topic
        .replace("{productKey}", productKey)
        .replace("${productKey}", productKey)
        .replace("{deviceId}", "${deviceId}")
        .replace("${deviceId}", "${deviceId}");
  }
}

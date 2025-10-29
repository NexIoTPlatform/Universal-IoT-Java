package cn.universal.admin.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 连接信息数据传输对象 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionInfoDTO {

  /** 内置连接信息 */
  private BuiltinConnection builtin;

  /** 网络组件连接信息 */
  private NetworkConnection network;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class BuiltinConnection {
    /** 连接方式 */
    private String connectionType;

    /** 连接地址 */
    private String host;

    /** 端口 */
    private Integer port;

    /** 用户名 */
    private String username;

    /** 密码（默认不显示，需要点击查看） */
    private String password;

    /** 配置说明 */
    private String description;

    /** MQTT主题信息（仅MQTT类型有） */
    private TopicInfo topics;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class NetworkConnection {
    /** 是否启用网络组件 */
    private Boolean enabled;

    /** 组件名称 */
    private String name;

    /** 组件类型 */
    private String type;

    /** 连接地址 */
    private String host;

    /** 端口 */
    private Integer port;

    /** 用户名 */
    private String username;

    /** 密码（默认不显示，需要点击查看） */
    private String password;

    /** 运行状态 */
    private Boolean state;

    /** 订阅主题信息 */
    private TopicInfo subscribeTopics;

    /** 错误信息（当enabled为false时） */
    private String message;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class TopicInfo {
    /** 物模型主题 */
    private ThingTopics thingTopics;

    /** 透传主题 */
    private PassthroughTopics passthroughTopics;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ThingTopics {
    private String propertyUp;
    private String eventUp;
    private String commandDown;
  }

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PassthroughTopics {
    private String dataUp;
    private String commandDown;
  }
}

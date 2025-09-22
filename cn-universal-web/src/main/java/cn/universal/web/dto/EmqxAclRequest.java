package cn.universal.web.dto;

import lombok.Data;

/**
 * EMQX ACL 授权请求 DTO
 *
 * @version 1.0 @Author Aleo
 * @since 2025/1/20
 */
@Data
public class EmqxAclRequest {

  /** 用户名 */
  private String username;

  /** 主题 */
  private String topic;

  /** 操作类型：publish 或 subscribe */
  private String action;

  /** 客户端ID */
  private String clientid;

  /** IP地址 */
  private String ip_address;

  /** 协议版本 */
  private String proto_ver;

  /** 端口 */
  private Integer port;

  /** 挂载点 */
  private String mountpoint;
}

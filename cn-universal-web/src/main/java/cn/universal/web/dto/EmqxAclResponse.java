package cn.universal.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EMQX ACL 授权响应 DTO
 *
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/1/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmqxAclResponse {

  /** 授权结果：allow 或 deny */
  private String result;
}

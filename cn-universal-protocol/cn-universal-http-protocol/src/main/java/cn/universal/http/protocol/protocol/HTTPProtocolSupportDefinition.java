package cn.universal.http.protocol.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gitee.com/NexIoT
 * @version 1.0
 * @since 2025/8/9 21:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HTTPProtocolSupportDefinition {

  /** 原消息 */
  private String payload;

  /** ProductKey */
  private String productKey;

  /** 解析脚本 */
  private String script;
}

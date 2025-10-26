package cn.universal.http.protocol.enums;

import cn.universal.core.protocol.support.ProtocolCodecSupport.CodecMethod;

/**
 * HTTP编解码方法枚举
 *
 * <p>与统一的CodecMethod保持一致，便于统一管理
 *
 * @version 1.0 @Author gitee.com/NexIoT
 * @since 2025/01/20
 */
public enum HTTPCodecMethod {
  /** 解码 */
  decode,
  /** 编码 */
  encode,
  /** 预解码 */
  preDecode,
  /** 添加 */
  codecAdd,
  /** 删除 */
  codecDelete,
  /** 更新 */
  codecUpdate,
  /** 查询 */
  codecQuery,
  /** IoT到第三方 */
  iotToYour,
  /** 第三方到IoT */
  yourToIot,
  /** 编解码功能方法 */
  codecFunction,
  /** 编解码其他方法 */
  codecOther;

  /** 转换为统一的CodecMethod */
  public CodecMethod toUnifiedMethod() {
    return switch (this) {
      case decode -> CodecMethod.decode;
      case encode -> CodecMethod.encode;
      case preDecode -> CodecMethod.preDecode;
      case codecAdd -> CodecMethod.codecAdd;
      case codecDelete -> CodecMethod.codecDelete;
      case codecUpdate -> CodecMethod.codecUpdate;
      case codecQuery -> CodecMethod.codecQuery;
      case iotToYour -> CodecMethod.iotToYour;
      case yourToIot -> CodecMethod.yourToIot;
      case codecFunction -> CodecMethod.iotToYour; // 映射到iotToYour
      case codecOther -> CodecMethod.yourToIot; // 映射到yourToIot
    };
  }
}

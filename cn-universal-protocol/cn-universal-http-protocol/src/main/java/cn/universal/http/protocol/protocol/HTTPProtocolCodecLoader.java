package cn.universal.http.protocol.protocol;

import cn.universal.common.exception.CodecException;
import cn.universal.core.protocol.loader.IProtocolCodecLoader;
import cn.universal.core.protocol.support.ProtocolCodecSupport.CodecMethod;
import cn.universal.http.protocol.enums.HTTPCodecMethod;

/**
 * HTTP协议编解码加载器接口
 *
 * <p>继承通用协议加载器接口，同时保持HTTP特有的方法
 *
 * @version 1.0 @Author Aleo
 * @since 2025/01/20
 */
public interface HTTPProtocolCodecLoader extends IProtocolCodecLoader {

  /**
   * 加载HTTP协议编解码器
   *
   * @param definition HTTP协议支持定义
   * @param codecMethod HTTP编解码方法
   * @throws CodecException 编解码异常
   */
  default void load(HTTPProtocolSupportDefinition definition, HTTPCodecMethod codecMethod)
      throws CodecException {
    load(definition.getProductKey(), codecMethod.toUnifiedMethod());
  }

  /** HTTP编解码操作 - 添加 */
  default String codecAdd(String productKey, String payload) throws CodecException {
    return execute(productKey, payload, CodecMethod.codecAdd);
  }

  /** HTTP编解码操作 - 删除 */
  default String codecDelete(String productKey, String payload) throws CodecException {
    return execute(productKey, payload, CodecMethod.codecDelete);
  }

  /** HTTP编解码操作 - 更新 */
  default String codecUpdate(String productKey, String payload) throws CodecException {
    return execute(productKey, payload, CodecMethod.codecUpdate);
  }

  /** HTTP编解码操作 - 查询 */
  default String codecQuery(String productKey, String payload) throws CodecException {
    return execute(productKey, payload, CodecMethod.codecQuery);
  }

  /** HTTP编码操作 */
  default String encode(String productKey, String payload) throws CodecException {
    return execute(productKey, payload, CodecMethod.encode);
  }

  /** HTTP解码操作 */
  default String decode(String productKey, String payload) throws CodecException {
    return execute(productKey, payload, CodecMethod.decode);
  }
}

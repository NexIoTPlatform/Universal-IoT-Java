package cn.universal.core.service;

import cn.universal.core.message.UPRequest;

/**
 * @version 1.0 @Author Aleo
 * @since 2025/6/26 19:41
 */
public interface UniversalCodec extends Codec {

  /**
   * 预解码，用于TCP表示出哪个ProductKey
   *
   * @param productKey 产品Key
   * @param message 消息原文
   * @return 解码内容
   */
  default UPRequest preDecode(String productKey, String message) {
    return null;
  }

  /**
   * 上行消息转换->转换为阿里云、腾讯云、三方平台
   *
   * @param productKey 产品Key
   * @param message 消息原文
   * @return 消息原文
   */
  default String messageFormatUP(String productKey, String message) {
    return null;
  }
}

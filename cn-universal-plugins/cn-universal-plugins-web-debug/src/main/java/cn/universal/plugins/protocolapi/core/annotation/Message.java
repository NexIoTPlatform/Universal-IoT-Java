package cn.universal.plugins.protocolapi.core.annotation;

import cn.universal.plugins.protocolapi.core.config.MessageType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WebSocket 消息
 *
 * @author mxd
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {

  /**
   * @return 消息类型
   */
  MessageType value();
}

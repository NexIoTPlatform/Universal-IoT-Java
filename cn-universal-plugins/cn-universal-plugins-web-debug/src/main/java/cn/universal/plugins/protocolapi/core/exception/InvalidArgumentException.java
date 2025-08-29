package cn.universal.plugins.protocolapi.core.exception;

import cn.universal.plugins.protocolapi.core.model.JsonCode;

/**
 * 参数错误异常
 *
 * @author mxd
 */
public class InvalidArgumentException extends RuntimeException {

  private final transient JsonCode jsonCode;

  public InvalidArgumentException(JsonCode jsonCode) {
    super(jsonCode.getMessage());
    this.jsonCode = jsonCode;
  }

  public int getCode() {
    return jsonCode.getCode();
  }
}

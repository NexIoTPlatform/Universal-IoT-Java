package cn.universal.plugins.protocolapi.servlet;

import cn.universal.plugins.protocolapi.core.servlet.MagicCookie;
import jakarta.servlet.http.Cookie;

public class MagicJakartaCookie implements MagicCookie {

  private final Cookie cookie;

  public MagicJakartaCookie(Cookie cookie) {
    this.cookie = cookie;
  }

  @Override
  public String getName() {
    return cookie.getName();
  }

  @Override
  public String getValue() {
    return cookie.getValue();
  }

  @Override
  public <T> T getCookie() {
    return (T) cookie;
  }
}

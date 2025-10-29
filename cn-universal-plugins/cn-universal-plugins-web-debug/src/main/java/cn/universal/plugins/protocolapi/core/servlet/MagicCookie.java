package cn.universal.plugins.protocolapi.core.servlet;

public interface MagicCookie {

  String getName();

  String getValue();

  <T> T getCookie();
}

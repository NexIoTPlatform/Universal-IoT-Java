package cn.universal.plugins.protocolapi.servlet;

import cn.universal.plugins.protocolapi.core.config.MagicCorsFilter;
import cn.universal.plugins.protocolapi.core.interceptor.AuthorizationInterceptor;
import cn.universal.plugins.protocolapi.core.interceptor.MagicWebRequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class MagicJakartaWebRequestInterceptor extends MagicWebRequestInterceptor
    implements HandlerInterceptor {

  public MagicJakartaWebRequestInterceptor(
      MagicCorsFilter magicCorsFilter, AuthorizationInterceptor authorizationInterceptor) {
    super(magicCorsFilter, authorizationInterceptor);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    super.handle(
        handler,
        new MagicJakartaHttpServletRequest(request, null),
        new MagicJakartaHttpServletResponse(response));
    return true;
  }
}

package cn.universal.plugins.protocolapi.core.interceptor;

import cn.universal.plugins.protocolapi.core.annotation.Valid;
import cn.universal.plugins.protocolapi.core.config.Constants;
import cn.universal.plugins.protocolapi.core.config.MagicCorsFilter;
import cn.universal.plugins.protocolapi.core.exception.MagicLoginException;
import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletRequest;
import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletResponse;
import cn.universal.plugins.protocolapi.core.web.MagicController;
import org.springframework.web.method.HandlerMethod;

public abstract class MagicWebRequestInterceptor {

  private final MagicCorsFilter magicCorsFilter;

  private final AuthorizationInterceptor authorizationInterceptor;

  public MagicWebRequestInterceptor(
      MagicCorsFilter magicCorsFilter, AuthorizationInterceptor authorizationInterceptor) {
    this.magicCorsFilter = magicCorsFilter;
    this.authorizationInterceptor = authorizationInterceptor;
  }

  public void handle(
      Object handler, MagicHttpServletRequest request, MagicHttpServletResponse response)
      throws MagicLoginException {
    HandlerMethod handlerMethod;
    if (handler instanceof HandlerMethod) {
      handlerMethod = (HandlerMethod) handler;
      handler = handlerMethod.getBean();
      if (handler instanceof MagicController) {
        if (magicCorsFilter != null) {
          magicCorsFilter.process(request, response);
        }
        Valid valid = handlerMethod.getMethodAnnotation(Valid.class);
        boolean requiredLogin = authorizationInterceptor.requireLogin();
        boolean validRequiredLogin = (valid == null || valid.requireLogin());
        if (validRequiredLogin && requiredLogin) {
          request.setAttribute(
              Constants.ATTRIBUTE_MAGIC_USER,
              authorizationInterceptor.getUserByToken(
                  request.getHeader(Constants.MAGIC_TOKEN_HEADER)));
        }
        ((MagicController) handler).doValid(request, valid);
      }
    }
  }
}

package cn.universal.plugins.protocolapi.servlet;

import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletRequest;
import cn.universal.plugins.protocolapi.core.servlet.MagicHttpServletResponse;
import cn.universal.plugins.protocolapi.core.servlet.MagicRequestContextHolder;
import org.springframework.web.multipart.MultipartResolver;

public class MagicJakartaRequestContextHolder implements MagicRequestContextHolder {

  private final MultipartResolver multipartResolver;

  public MagicJakartaRequestContextHolder(MultipartResolver multipartResolver) {
    this.multipartResolver = multipartResolver;
  }

  @Override
  public MagicHttpServletRequest getRequest() {
    return convert(
        servletRequestAttributes ->
            new MagicJakartaHttpServletRequest(
                servletRequestAttributes.getRequest(), multipartResolver));
  }

  @Override
  public MagicHttpServletResponse getResponse() {
    return convert(
        servletRequestAttributes ->
            new MagicJakartaHttpServletResponse(servletRequestAttributes.getResponse()));
  }
}

package cn.universal.plugins.protocolapi.servlet;

import cn.universal.plugins.protocolapi.core.config.MagicCorsFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MagicJakartaCorsFilter extends MagicCorsFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    super.process(
        new MagicJakartaHttpServletRequest((HttpServletRequest) request, null),
        new MagicJakartaHttpServletResponse((HttpServletResponse) response));
    chain.doFilter(request, response);
  }
}

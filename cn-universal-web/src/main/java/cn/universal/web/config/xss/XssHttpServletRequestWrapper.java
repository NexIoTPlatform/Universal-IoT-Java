/*
 *
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.web.config.xss;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/** XSS过滤 @Author Chill */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

  /** 没被包装过的HttpServletRequest（特殊场景,需要自己过滤） */
  private final HttpServletRequest orgRequest;

  /** 缓存报文,支持多次读取流 */
  private byte[] body;

  /** html过滤 */
  private static final XssHtmlFilter HTML_FILTER = new XssHtmlFilter();

  public XssHttpServletRequestWrapper(HttpServletRequest request) {
    super(request);
    orgRequest = request;
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    if (super.getHeader(HttpHeaders.CONTENT_TYPE) == null) {
      return super.getInputStream();
    }

    if (super.getHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)) {
      return super.getInputStream();
    }

    if (body == null) {
      body = xssEncode(getRequestBody(super.getInputStream())).getBytes();
    }

    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);

    return new ServletInputStream() {

      @Override
      public int read() {
        return byteArrayInputStream.read();
      }

      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {}
    };
  }

  @Override
  public String getParameter(String name) {
    String value = super.getParameter(xssEncode(name));
    if (StrUtil.isNotBlank(value)) {
      value = xssEncode(value);
    }
    return value;
  }

  @Override
  public String[] getParameterValues(String name) {
    String[] parameters = super.getParameterValues(name);
    if (parameters == null || parameters.length == 0) {
      return null;
    }

    for (int i = 0; i < parameters.length; i++) {
      parameters[i] = xssEncode(parameters[i]);
    }
    return parameters;
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    Map<String, String[]> map = new LinkedHashMap<>();
    Map<String, String[]> parameters = super.getParameterMap();
    for (String key : parameters.keySet()) {
      String[] values = parameters.get(key);
      for (int i = 0; i < values.length; i++) {
        values[i] = xssEncode(values[i]);
      }
      map.put(key, values);
    }
    return map;
  }

  @Override
  public String getHeader(String name) {
    String value = super.getHeader(xssEncode(name));
    if (StrUtil.isNotBlank(value)) {
      value = xssEncode(value);
    }
    return value;
  }

  private String xssEncode(String input) {
    return HTML_FILTER.filter(input);
  }

  /**
   * 获取初始request
   *
   * @return HttpServletRequest
   */
  public HttpServletRequest getOrgRequest() {
    return orgRequest;
  }

  /**
   * 获取初始request
   *
   * @param request request
   * @return HttpServletRequest
   */
  public static HttpServletRequest getOrgRequest(HttpServletRequest request) {
    if (request instanceof XssHttpServletRequestWrapper) {
      return ((XssHttpServletRequestWrapper) request).getOrgRequest();
    }
    return request;
  }

  private String getRequestBody(ServletInputStream servletInputStream) {
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader =
          new BufferedReader(new InputStreamReader(servletInputStream, StandardCharsets.UTF_8));
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (servletInputStream != null) {
        try {
          servletInputStream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return sb.toString();
  }
}

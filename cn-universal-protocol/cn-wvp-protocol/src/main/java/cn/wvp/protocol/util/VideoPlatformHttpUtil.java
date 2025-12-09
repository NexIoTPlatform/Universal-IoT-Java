/*
 * Copyright (c) 2025, NexIoT. All Rights Reserved.
 */
package cn.wvp.protocol.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 视频平台HTTP请求工具类
 */
@Slf4j
public class VideoPlatformHttpUtil {

  private static final int DEFAULT_TIMEOUT = 10000; // 10秒超时

  /**
   * 执行GET请求并返回JSON结果（自动从filters提取token）
   *
   * @param baseUrl 基础URL
   * @param path 接口路径
   * @param filters 过滤参数（包含token）
   * @return JSON响应对象
   */
  public static JSONObject httpGetJson(
      String baseUrl, String path, Map<String, Object> filters) {
    return httpGetJson(baseUrl, path, filters, DEFAULT_TIMEOUT);
  }

  /**
   * 执行GET请求并返回JSON结果（支持自定义超时）
   *
   * @param baseUrl 基础URL
   * @param path 接口路径
   * @param filters 过滤参数（包含token和其他查询参数）
   * @param timeout 超时时间（毫秒）
   * @return JSON响应对象
   */
  public static JSONObject httpGetJson(
      String baseUrl,
      String path,
      Map<String, Object> filters,
      int timeout) {
    String url = buildUrl(baseUrl, path);
    HttpRequest request = HttpRequest.get(url).timeout(timeout);

    // 提取并设置token
    String token = extractToken(filters);
    if (token != null && !token.isEmpty()) {
      request.header("Access-Token", token);
    }

    // 添加查询参数（排除token和endpoint/auth等元数据）
    Map<String, Object> params = buildQueryParams(filters);
    if (!params.isEmpty()) {
      request.form(params);
    }

    try {
      log.info("HTTP GET请求: URL={}, params={}", url, filters);
      HttpResponse response = request.execute();
      String body = response.body();
      log.info("x`HTTP GET响应: status={}, body={}", response.getStatus(), body);

      if (body == null || body.isEmpty()) {
        return buildErrorResponse("响应体为空");
      }
      return JSONUtil.parseObj(body);
    } catch (Exception e) {
      log.error("HTTP GET请求失败: URL={}, error={}", url, e.getMessage(), e);
      return buildErrorResponse(e.getMessage());
    }
  }

  /**
   * 执行POST请求并返回JSON结果（自动从filters提取token）
   *
   * @param baseUrl 基础URL
   * @param path 接口路径
   * @param body 请求体（JSON）
   * @param filters 过滤参数（包含token）
   * @return JSON响应对象
   */
  public static JSONObject httpPostJson(
      String baseUrl, String path, JSONObject body, Map<String, Object> filters) {
    return httpPostJson(baseUrl, path, body, filters, DEFAULT_TIMEOUT);
  }

  /**
   * 执行POST请求并返回JSON结果（支持自定义超时）
   *
   * @param baseUrl 基础URL
   * @param path 接口路径
   * @param body 请求体（JSON）
   * @param filters 过滤参数（包含token）
   * @param timeout 超时时间（毫秒）
   * @return JSON响应对象
   */
  public static JSONObject httpPostJson(
      String baseUrl, String path, JSONObject body, Map<String, Object> filters, int timeout) {
    String url = buildUrl(baseUrl, path);
    HttpRequest request = HttpRequest.post(url).timeout(timeout);

    // 提取并设置token
    String token = extractToken(filters);
    if (token != null && !token.isEmpty()) {
      request.header("Access-Token", token);
    }

    // 设置请求体
    if (body != null) {
      request.body(body.toString()).contentType("application/json");
    }

    try {
      log.debug("HTTP POST请求: URL={}, body={}", url, body);
      HttpResponse response = request.execute();
      String responseBody = response.body();
      log.debug("HTTP POST响应: status={}, body={}", response.getStatus(), responseBody);

      if (responseBody == null || responseBody.isEmpty()) {
        return buildErrorResponse("响应体为空");
      }
      return JSONUtil.parseObj(responseBody);
    } catch (Exception e) {
      log.error("HTTP POST请求失败: URL={}, error={}", url, e.getMessage(), e);
      return buildErrorResponse(e.getMessage());
    }
  }

  /**
   * 构建完整URL
   */
  private static String buildUrl(String baseUrl, String path) {
    String url = baseUrl;
    if (!url.endsWith("/")) {
      url += "/";
    }
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return url + path;
  }

  /**
   * 构建错误响应
   */
  private static JSONObject buildErrorResponse(String errorMsg) {
    JSONObject error = new JSONObject();
    error.set("error", errorMsg);
    error.set("success", false);
    return error;
  }

  /**
   * 检查响应是否包含错误
   */
  public static boolean hasError(JSONObject response) {
    if (response == null) {
      return true;
    }
    // 检查error字段
    if (response.containsKey("error")) {
      return true;
    }
    // 检查code字段（非0表示错误）
    if (response.containsKey("code")) {
      Integer code = response.getInt("code");
      return code != null && code != 0;
    }
    // 检查success字段
    if (response.containsKey("success")) {
      Boolean success = response.getBool("success");
      return success == null || !success;
    }
    return false;
  }

  /**
   * 获取错误消息
   */
  public static String getErrorMessage(JSONObject response) {
    if (response == null) {
      return "响应为空";
    }
    if (response.containsKey("error")) {
      return response.getStr("error");
    }
    if (response.containsKey("msg")) {
      return response.getStr("msg");
    }
    if (response.containsKey("message")) {
      return response.getStr("message");
    }
    if (response.containsKey("errMsg")) {
      return response.getStr("errMsg");
    }
    return "未知错误";
  }

  /**
   * 从filters中提取token
   * 优先从token字段获取，其次从auth字段解析
   */
  private static String extractToken(Map<String, Object> filters) {
    if (filters == null) {
      return null;
    }
    
    // 优先使用token字段
    Object tokenObj = filters.get("token");
    if (tokenObj != null) {
      return String.valueOf(tokenObj);
    }
    
    // 兼容旧的auth字段（JSON格式）
    Object authObj = filters.get("auth");
    if (authObj != null) {
      try {
        String authStr = String.valueOf(authObj);
        if (authStr.startsWith("{")) {
          JSONObject authJson = JSONUtil.parseObj(authStr);
          if (authJson.containsKey("apiKey")) {
            return authJson.getStr("apiKey");
          }
          if (authJson.containsKey("token")) {
            return authJson.getStr("token");
          }
        } else {
          // 直接是token字符串
          return authStr;
        }
      } catch (Exception e) {
        log.warn("解析auth字段失败: {}", e.getMessage());
      }
    }
    
    return null;
  }

  /**
   * 构建查询参数（排除元数据字段）
   */
  private static Map<String, Object> buildQueryParams(Map<String, Object> filters) {
    Map<String, Object> params = new java.util.HashMap<>();
    if (filters == null) {
      return params;
    }
    
    // 排除的元数据字段
    java.util.Set<String> excludeKeys = new java.util.HashSet<>();
    excludeKeys.add("endpoint");
    excludeKeys.add("auth");
    excludeKeys.add("token");
    excludeKeys.add("baseUrl");
    excludeKeys.add("apiKey");
    
    for (Map.Entry<String, Object> entry : filters.entrySet()) {
      if (!excludeKeys.contains(entry.getKey()) && entry.getValue() != null) {
        params.put(entry.getKey(), entry.getValue());
      }
    }
    
    return params;
  }
}

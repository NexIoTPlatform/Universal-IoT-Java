/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 Aleo 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: Aleo
 * @Email: wo8335224@gmail.com
 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.rule.rulego.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * rulego配置类
 *
 * @author Aleo
 * @since 2025/09/01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rulego")
public class RulegoConfig {

  /** API配置 */
  private Api api = new Api();

  /** 同步配置 */
  private Sync sync = new Sync();

  @Data
  public static class Api {
    /** 基础URL */
    private String baseUrl = "http://localhost:9090";

    private String webUrl="http://rule.192886.xyz:81/#";
    
    /** 认证token */
    private String token = "";
  }

  @Data
  public static class Sync {
    /** 是否启用同步任务 */
    private boolean enabled = true;
    
    /** 同步间隔（毫秒） */
    private long interval = 300000;
    
    /** 是否启用日志清理 */
    private boolean logCleanupEnabled = true;
    
    /** 日志保留天数 */
    private int logRetentionDays = 30;
  }
}

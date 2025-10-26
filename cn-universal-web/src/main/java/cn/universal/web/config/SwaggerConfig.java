/*
 *
 * Copyright (c) 2025, IoT-Universal. All Rights Reserved.
 *
 * @Description: 本文件由 gitee.com/NexIoT 开发并拥有版权，未经授权严禁擅自商用、复制或传播。
 * @Author: gitee.com/NexIoT

 * @Wechat: outlookFil
 *
 *
 */

package cn.universal.web.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI 3 配置 */
@Configuration
@EnableKnife4j
public class SwaggerConfig {

  @Value("${swagger.enable:true}")
  private Boolean enable;

  @Bean
  public OpenAPI customOpenAPI() {
    // 全局安全配置
    SecurityScheme securityScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    return new OpenAPI()
        .info(
            new Info()
                .title("IoT Universal")
                .description("IoT Universal")
                .version("1")
                .contact(new Contact().name("univ").email("").url("")))
        .addSecurityItem(new SecurityRequirement().addList("Authorization"))
        .components(new Components().addSecuritySchemes("Authorization", securityScheme));
  }
}

package com.starrysky.lifemini.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class Knife4jConfig {
    // 1. 全局基础信息配置（这里定义 Title, Version 等）
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Life-Mini接口文档")
                        .version("1.0.0")
                        .description("基于 Knife4j 的接口文档"));
    }

    // 2. 管理端分组
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("1-管理端接口")
                // 注意：这里必须匹配你 Controller 上的路径
                .packagesToScan("com.starrysky.lifemini.controller.admin")
                .build();
    }

    // 3. 用户端分组
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2-用户端接口")
                .packagesToScan("com.starrysky.lifemini.controller.user")
                .build();
    }
}
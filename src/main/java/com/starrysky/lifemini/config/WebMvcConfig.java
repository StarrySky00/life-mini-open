package com.starrysky.lifemini.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final StatsInterceptor statsInterceptor;
    private final GlobalMuteInterceptor globalMuteInterceptor;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // 1. 创建专门给 Controller 用的线程池
        ThreadPoolTaskExecutor mvcExecutor = new ThreadPoolTaskExecutor();
        mvcExecutor.setCorePoolSize(10);
        mvcExecutor.setMaxPoolSize(100); // 支撑同时在线的对话连接数
        mvcExecutor.setQueueCapacity(200);
        mvcExecutor.setThreadNamePrefix("mvc-async-");
        mvcExecutor.initialize();

        // 2. 把这个池子交给 Spring MVC
        configurer.setTaskExecutor(mvcExecutor);

        // 3. 设置默认接口超时时间 (毫秒)，例如 30秒
        // AI 回复比较慢，建议设长一点，或者在 SseEmitter 构造时单独设
        configurer.setDefaultTimeout(30_000);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("添加数据统计拦截器。。。");
        registry.addInterceptor(globalMuteInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/**",
                        "/user/wx/login/{code}",
                        "/error"
                );
        registry.addInterceptor(statsInterceptor)
                .excludePathPatterns("/admin/**","/error");
        log.info("添加登录拦截器。。。");
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/error",
                        "/shop/*",
                        "/shop-category/**",
                        "/keyword-dict/**",
                        "/user/register",
                        "/user/login",
                        "/user/sendVerificationCode/**",
                        "/user/wx/**",
                        "/user/findPassword",

                        "/admin/login",
                        "/admin/register",
                        "/admin/sendVerificationCode");
    }
}
package com.starrysky.lifemini.config;

import cn.hutool.core.text.AntPathMatcher;
import com.starrysky.lifemini.common.constant.CacheConstant;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 全站禁言/熔断 拦截器
 */
@Slf4j
@Component
public class GlobalMuteInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final List<String> PATH_LIST = List.of(
            "/chat/chat/*",
            "/comment/photo",
            "/comment/delete/*",
            "/comment/add",
            "/user/update/avatar",
            "/user/update"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private void sendResponse(HttpServletResponse response,int code,String message) throws IOException {
        // 3. JSON 格式的报错信息
        response.setContentType("application/json;charset=utf-8");
        // JSON 结构要和项目里的 Result 对象字段保持一致
        response.getWriter().write(String.format("{\"code\": %d, \"message\": \"%s\", \"data\": null}",code,message));
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 去 Redis 里看一眼开关状态
        String muteStatus = stringRedisTemplate.opsForValue().get(CacheConstant.MUTE_KEY);
        // 1. kill，拦截所有
        if ("kill".equalsIgnoreCase(muteStatus)) {
            log.warn("全站熔断，拦截所有请求");
            sendResponse(response,2,"小程序维护中，暂时无法访问");
            return false;
        }
        String uri = request.getRequestURI();
        // 3. on，只拦截指定的写操作
        if ("ON".equalsIgnoreCase(muteStatus)) {
            // 遍历咱们的黑名单列表
            for (String pattern : PATH_LIST) {
                // 使用 AntPathMatcher 判断当前请求 URI 是否命中了黑名单规则
                if (PATH_MATCHER.match(pattern, uri)) {
                    log.warn("触发禁言熔断，拦截写请求: {}", uri);
                    sendResponse(response, 1, "系统维护中，暂时关闭发布和上传功能");
                    return false;
                }
            }
        }
        // 正常情况，放行
        return true;
    }
}
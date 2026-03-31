package com.starrysky.lifemini.config;

import com.starrysky.lifemini.common.constant.CacheConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatsInterceptor implements HandlerInterceptor {
    public static final String pattern = "yyyy-MM-dd";
    public static final String IP_ = "IP_";
    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 直接放行静态资源请求
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        // 放行跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 放行接口文档请求
        String uri = request.getRequestURI();
        if (uri.startsWith("/doc.html")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/webjars")) {
            return true;
        }

        //1. 点击量+1
        String pvKey = CacheConstant.STATS_PV + LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
        String pvAllKey = CacheConstant.STATS_ALL_PV;
        stringRedisTemplate.opsForValue().increment(pvKey);//每日点击量
        stringRedisTemplate.expire(pvKey, 100, TimeUnit.DAYS);
        stringRedisTemplate.opsForValue().increment(pvAllKey);//总点击量

        //2. 访问量
        String ip = getClientIp(request);
        //3. 无ip直接放行
        if (ip == null) {
            return true;
        }
        String identifier = IP_ + ip;
        String uvKey = CacheConstant.STATS_UV + LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
        stringRedisTemplate.opsForHyperLogLog().add(uvKey, identifier);//每日访问量
        stringRedisTemplate.expire(uvKey, 100, TimeUnit.DAYS);//这里没办法，只能每次都设置了
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            // 本地测试时 getRemoteAddr() 可能是 0:0:0:0:0:0:0:1，转为 127.0.0.1
            if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
                ip = "127.0.0.1";
            }
        }

        // 多级代理时，X-Forwarded-For 可能是 "ip1, ip2, ip3"，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

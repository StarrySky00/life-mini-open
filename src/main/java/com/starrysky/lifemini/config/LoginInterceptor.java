package com.starrysky.lifemini.config;

import cn.hutool.json.JSONUtil;
import com.starrysky.lifemini.common.annotation.CheckRole;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.JwtClaimsConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.enums.RoleEnum;
import com.starrysky.lifemini.common.util.JWTUtil;
import com.starrysky.lifemini.common.util.ThreadLocalUtil;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.model.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        try {
            response.setStatus(status);
            response.setCharacterEncoding("UTF-8");//字符编码
            response.setContentType("application/json;charset=UTF-8");//响应Content-Type
            Result result = Result.error(status, message);
            response.getWriter().write(JSONUtil.toJsonStr(result));
        } catch (Exception e) {
            log.error("发送错误响应异常，message；{}", e.getMessage());
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 直接放行静态资源请求
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        //1. 放行跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        //2. 放行接口文档请求
        String uri = request.getRequestURI();
        if (uri.startsWith("/doc.html")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/webjars")) {
            return true;
        }
        //3. 需要登录的请求
        String token = request.getHeader("Authorization");
        if (token == null) {
            sendErrorResponse(response, 401, MessageConstant.NOT_LOGIN);//缺少令牌
            return false;
        }
        // 3.1 令牌是否过期
        try {
            Map<String, Object> claims = JWTUtil.parseJWT(token);
            Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
            String cacheToken = stringRedisTemplate.opsForValue().get(CacheConstant.TOKEN + userIdObj);
            if (!token.equals(cacheToken)) {
                throw new RuntimeException();
            }
            ThreadLocalUtil.setUserId(TypeConversionUtil.toLong(claims.get(JwtClaimsConstant.USER_ID)));
            // 3.2 仅登录即可访问
            CheckRole role = hm.getMethodAnnotation(CheckRole.class);
            if (role == null) {
                return true;
            }
            // 3.3 验权可访问
            RoleEnum roleEnum = role.value();
            String roleStr = (String) claims.get(JwtClaimsConstant.ROLE);
            if (roleEnum == RoleEnum.fromString(roleStr)) {
                return true;
            }
            sendErrorResponse(response, 403, MessageConstant.NO_PERMISSION); // 无权限访问
            return false;
        } catch (Exception e) {
            log.error("Token校验失败或异常:{}", e.getMessage());
            sendErrorResponse(response, 401, MessageConstant.SESSION_EXPIRED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.removeUserId();
    }
}

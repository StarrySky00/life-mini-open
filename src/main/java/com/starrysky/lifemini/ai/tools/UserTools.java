package com.starrysky.lifemini.ai.tools;

import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.model.entity.User;
import com.starrysky.lifemini.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author StarrySky
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserTools {

    private final IUserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    @Tool(description = "当用户要求推荐、或者询问'适合我'等与个人相关的服务时，调用此工具查询当前用户的个人描述、口味偏好和饮食禁忌等信息。")
    public String getUserProfile(ToolContext context) {
        log.debug("【AI.UserTools】查询用户个人信息工具被调用");
        // 从上下文中获取当前登录的 userId
        Long userId = (Long) context.getContext().get("userId");
        return userService.getUserProfile(userId);
    }
    /**
     * 获取用户当前坐标
     */
    @Tool(description = "当进行搜索，查询附近等需要用到用户坐标时，调用此工具查询用户坐标")
    public String getUserLocation(ToolContext context) {
        log.debug("【AI.UserTools】查询用户地理位置工具被调用");
        // 从上下文中获取当前登录的 userId
        Long userId = (Long) context.getContext().get("userId");
        return userService.getUserLocation(userId);
    }


}
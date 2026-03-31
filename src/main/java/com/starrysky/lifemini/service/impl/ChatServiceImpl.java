package com.starrysky.lifemini.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.starrysky.lifemini.ai.tools.ShopTools;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.constant.DataConstant;
import com.starrysky.lifemini.common.constant.MessageConstant;
import com.starrysky.lifemini.common.util.ThreadLocalUtil;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import com.starrysky.lifemini.mapper.ChatMessageMapper;
import com.starrysky.lifemini.model.dto.KeywordSimpleDTO;
import com.starrysky.lifemini.model.dto.ShopCategorySimpleDTO;
import com.starrysky.lifemini.model.entity.ChatMessage;
import com.starrysky.lifemini.model.result.RecordResult;
import com.starrysky.lifemini.model.result.Result;
import com.starrysky.lifemini.service.*;
import com.starrysky.lifemini.common.util.SensitiveWordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cache;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {
    private final IShopCategoryService shopCategoryService;
    private final IKeywordDictService keywordDictService;
    private final ChatClient chatClient;
    private final IWeChatService weChatService;
    private final ChatMessageMapper chatMessageMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final Executor cacheExecutor;
    private final IUserService userService;

    @Override
    public Flux<String> searchShops(String content, Long userId) {
        if (userId == null) {
            return Flux.just("请先登录！");
        }
        //检查用户当日语言违规次数是否到达上限
        String dataAndId = LocalDate.now().format(DateTimeFormatter.ofPattern(DataConstant.DATE_FORMAT)) + ":" + userId;
        if (!weChatService.checkContent(content)) {
            log.error("用户：【{}】  语言【{}】违规", userId, content);
            String warnKey = CacheConstant.CHAT_WARN_PRX + dataAndId;
            Long warnCount = stringRedisTemplate.opsForValue().increment(warnKey);
            stringRedisTemplate.expire(warnKey, 24, TimeUnit.HOURS);
            if (warnCount != null && warnCount > 3) {
                log.info("禁用用户{},强制下线", userId);
                //禁用当前用户，删除token强制下线
                userService.banUserAndForcedOffline(userId);
                return Flux.just("语言多次违规，账号已被封禁");
            }
            return Flux.just("请文明用语！");
        }
        //检查用户对话次数是否达到上限
        String limitKey = CacheConstant.CHAT_Limit_PRX + dataAndId;
        Long limit = stringRedisTemplate.opsForValue().increment(limitKey);
        stringRedisTemplate.expire(limitKey, 24, TimeUnit.HOURS);
        if (limit != null && limit > 20) {
            log.info("用户{}对话达到限制次数", userId);
            return Flux.just("今日可回复次数达到限制!");
        }
        log.info("【动态拼接systemPrompt】");
        CompletableFuture<List<ShopCategorySimpleDTO>> cateList = CompletableFuture.supplyAsync(shopCategoryService::queryShopCategorySimpleList, cacheExecutor);
        CompletableFuture<List<KeywordSimpleDTO>> keywordList = CompletableFuture.supplyAsync(keywordDictService::querySimpleKeywordList, cacheExecutor);
        CompletableFuture<List<Object>> location = CompletableFuture.supplyAsync(() -> {
            List<Object> loc = new ArrayList<>(List.of("未知", "未知"));
            String key = CacheConstant.USER_LOCATION + userId;
            List<Object> objects = stringRedisTemplate.opsForHash().multiGet(key, DataConstant.LOCATION);//x,y
            if (objects.size() >= 2 && objects.get(0) != null && objects.get(1) != null) {
                loc.add(0, TypeConversionUtil.ToDouble(objects.get(0)));
                loc.add(1, TypeConversionUtil.ToDouble(objects.get(1)));
            }
            return loc;
        }, cacheExecutor);
        //
        log.info("获取动态prompt内容，开始发送对话内容");
        return Mono.fromFuture(
                CompletableFuture.allOf(cateList, keywordList, location).thenApply(v -> {
                    // 此时三个任务都已经完成，可以直接 join 取值
                    List<ShopCategorySimpleDTO> categories = cateList.join();
                    List<KeywordSimpleDTO> keywords = keywordList.join();
                    List<Object> loc = location.join();
                    return new Object[]{categories, keywords, loc};
                })
        ).flatMapMany(result -> {
            List<Object> loc = (List<Object>) result[2];
            String systemPrompt = String.format(
                    DataConstant.DEFAULT_SYSTEM_PROMPT,
                    result[0],
                    result[1],
                    loc.get(0),
                    loc.get(1)
            );
            log.info("【ai开始执行】");
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(content)
                    .tools("searchShopsTool", "searchShopDetailsTool", "writeCommentTool")
                    .toolContext(Map.of("userId", userId))
                    .advisors(a -> a.param("chat_memory_conversation_id", userId))
                    .stream()
                    .content();
        });
    }

    /**
     * 查询对话记录
     *
     * @param conversationIdStr
     * @param lastId
     * @param pageSize
     * @return
     */
    @Override
    public RecordResult<ChatMessage> queryChatRecords(String conversationIdStr, Long lastId, Long pageSize) {
        //1. 获取查询参数
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            log.warn("用户未登录，无法查对话记录");
            return RecordResult.error(401);
        }
        Long conversationId = TypeConversionUtil.toLong(conversationIdStr);
        pageSize = Math.min(pageSize, 50);
        log.info("查询聊天记录，userId={},conversationId={},lastId={},pageSize={}", userId, conversationId, lastId, pageSize);
        //2. 查询对话记录
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        long pageCount = pageSize + 1;
        wrapper.eq(ChatMessage::getUserId, userId)
                .eq(ChatMessage::getConversationId, conversationId)
                .lt(lastId != null, ChatMessage::getId, lastId)
                .orderByDesc(ChatMessage::getId)
                .last("limit " + pageCount);
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(wrapper);
        if (CollUtil.isEmpty(chatMessages)) {
            log.info("无对话记录。");
            return RecordResult.success(false, null, Collections.emptyList());
        }
        //3. 判断是否还有更多记录
        boolean hasMore = false;
        if (chatMessages.size() > pageSize) {
            chatMessages = chatMessages.subList(0, chatMessages.size() - 1);
            hasMore = true;
        }
        Collections.reverse(chatMessages);//反转，把旧消息放在数组前面，新消息在后面
        lastId = chatMessages.get(0).getId();
        return RecordResult.success(hasMore, lastId, chatMessages);
    }

    /**
     * 获取用户坐标
     *
     * @param longitude
     * @param latitude
     * @return
     */
    @Override
    public Result saveUserLocation(Double longitude, Double latitude) {
        log.info("保存用户坐标信息");
        Long userId = ThreadLocalUtil.getUserId();
        if (userId == null) {
            return Result.error(401, MessageConstant.USER + MessageConstant.NOT_LOGIN);
        }
        if (latitude == null || longitude == null) {
            log.error("坐标信息错误：longitude={},latitude={}", longitude, latitude);
            return Result.error("坐标信息错误");
        }
        Map<String, String> location = new HashMap<>();
        location.put(CacheConstant.LONGITUDE, longitude.toString());
        location.put(CacheConstant.LATITUDE, latitude.toString());
        stringRedisTemplate.opsForHash().putAll(CacheConstant.USER_LOCATION + userId, location);
        stringRedisTemplate.expire(CacheConstant.USER_LOCATION + userId, 30, TimeUnit.MINUTES);
        return Result.success();
    }
}

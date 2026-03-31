package com.starrysky.lifemini.ai.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starrysky.lifemini.common.constant.CacheConstant;
import com.starrysky.lifemini.common.util.ThreadLocalUtil;
import com.starrysky.lifemini.model.event.ChatRecordEvent;
import com.starrysky.lifemini.model.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisChatMemory implements ChatMemory {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Redisson redisson;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private static final String memoryPrefix = "chat:memory:";
    private static final int MAX_LENGTH = 10;
    private static final int MAX_LIMIT = 20;

    //新增对话记录
    @Override
    public void add(String conversationId, List<Message> messages) {
        log.info("【执行add】");
        String key = memoryPrefix + conversationId;
        //把对话从右侧存入redisList（12小时过期）
        List<String> vosJson = messages.stream()
                .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                .map(m -> {
                    try {
                        return objectMapper.writeValueAsString(new MessageVO(m));
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();
        log.info("******************add内容*****************");
        log.info("add:" + vosJson);
        log.info("******************************************");
        RLock lock = redisson.getLock(CacheConstant.CHAT_LOCK_PRX + conversationId);
        try {
            boolean isLock = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (isLock) {
                //保证按顺序接收到发来的消息
                try {
                    stringRedisTemplate.opsForList().rightPushAll(key, vosJson);
                    stringRedisTemplate.opsForList().trim(key, -MAX_LIMIT, -1);
                    stringRedisTemplate.expire(key, 12, TimeUnit.HOURS);
                    //发送消息，异步保存对话记录
                    eventPublisher.publishEvent(new ChatRecordEvent(this, conversationId, messages));
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("获取会话锁失败，conversationId: {}", conversationId);
                throw new RuntimeException("系统繁忙，请稍后再试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("锁等待中断", e);
        }
    }

    //获取对话记录
    @Override
    public List<Message> get(String conversationId, int lastN) {
        log.info("【执行get】");
        int limit = Math.min(MAX_LENGTH, lastN);
        String key = memoryPrefix + conversationId;
        //从右侧取出最新的对话记录(最多十条
        List<String> vosJson = stringRedisTemplate.opsForList().range(key, -limit, -1);
        if (vosJson == null) {
            return Collections.emptyList();
        }
        List<Message> list = vosJson.stream().map(voJson -> {
                    try {
                        MessageVO vo = objectMapper.readValue(voJson, MessageVO.class);
                        return switch (vo.getRole().toLowerCase()) {
                            case "user" -> new UserMessage(vo.getText());
                            case "assistant" -> new AssistantMessage(vo.getText());
                            case "tool" -> {
                                var response = new ToolResponseMessage.ToolResponse(
                                        vo.getToolCallId(),
                                        vo.getToolName(),
                                        vo.getText()
                                );
                                yield new ToolResponseMessage(List.of(response), Collections.emptyMap());
                            }
                            default -> new UserMessage(vo.getText());
                        };
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        log.info("******************get结果*****************");
        log.info("get:" + list);
        log.info("******************************************");
        return list;
    }

    //清除对话记录
    @Override
    public void clear(String conversationId) {
        log.info("【执行clear】");
        String key = memoryPrefix + conversationId;
        stringRedisTemplate.delete(key);
    }
}

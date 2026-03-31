package com.starrysky.lifemini.listener;

import cn.hutool.core.collection.CollUtil;
import com.starrysky.lifemini.model.entity.ChatMessage;
import com.starrysky.lifemini.model.event.ChatRecordEvent;
import com.starrysky.lifemini.service.IChatMessageService;
import com.starrysky.lifemini.common.util.TypeConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ChatRecordListener {
    @Autowired
    private IChatMessageService chatMessageService;

    @Async("eventExecutor")
    @EventListener
    public void handleChatRecordEvent(ChatRecordEvent event) {
        log.info("【异步落库】开始处理会话: {}", event.getConversationId());
        try {
            Long conversationId = TypeConversionUtil.toLong(event.getConversationId());
            List<Message> messages = event.getMessages();
            if (CollUtil.isEmpty(messages)) {
                return;
            }
            Message message = messages.stream()
                    .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                    .findFirst()
                    .orElse(null);
            if(message==null){
                return;
            }
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setConversationId(conversationId);
            chatMessage.setUserId(conversationId);
            chatMessage.setRole(message.getMessageType().name().toLowerCase());
            chatMessage.setContent(message.getText());
            chatMessage.setCreateTime(LocalDateTime.now());
            chatMessageService.save(chatMessage);
        } catch (Exception e) {
            log.error("【异步落库】严重失败！请检查数据库状态或数据格式,conversionId={}", event.getConversationId(), e);
        }
    }
}

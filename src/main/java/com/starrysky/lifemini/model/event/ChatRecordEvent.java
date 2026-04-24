package com.starrysky.lifemini.model.event;

import org.springframework.ai.chat.messages.Message;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 用户聊天记录事件dto
 */
public class ChatRecordEvent extends ApplicationEvent {
    private final String conversationId;
    private final List<Message> messages;

    public ChatRecordEvent(Object source, String conversationId, List<Message> messages) {
        super(source);
        this.conversationId = conversationId;
        this.messages = messages;
    }

    public String getConversationId() { return conversationId; }
    public List<Message> getMessages() { return messages; }
}

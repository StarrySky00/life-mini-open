package com.starrysky.lifemini.model.event;

import org.springframework.ai.chat.messages.Message;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 用户聊天记录事件dto
 */
public record ChatRecordEvent (String conversationId, List<Message> messages){

}

package com.starrysky.lifemini.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;

/*
@NoArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;

    public MessageVO(Message message) {
        this.role = message.getMessageType().getValue();
        this.content = message.getContent();
    }
}*/
@Data
@NoArgsConstructor
public class MessageVO {
    private String role;
    private String text;
    private String toolCallId;
    private String toolName; // 新增：记录工具名称

    public MessageVO(Message message) {
        this.role = message.getMessageType().getValue();
        this.text = message.getText();

        // 针对 TOOL 类型消息，提取 ID 和 Name
        if (message instanceof ToolResponseMessage toolMsg) {
            if (!toolMsg.getResponses().isEmpty()) {
                var firstResponse = toolMsg.getResponses().get(0);
                this.toolCallId = firstResponse.id();   // 参数1
                this.toolName = firstResponse.name();   // 参数2
            }
        }
    }
}
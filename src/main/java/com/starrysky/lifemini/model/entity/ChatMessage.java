package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serial;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户对话记录表
 * </p>
 *
 * @author StarrySky
 * @since 2026-02-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_chat_message")
@Schema(name = "ChatMessage对象", description = "用户对话记录表")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "会话ID")
    private Long conversationId;

    @Schema(description = "角色: user/assistant")
    private String role;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "发送时间")
    private LocalDateTime createTime;
}

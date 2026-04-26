package com.starrysky.lifemini.ai.tools;

import com.starrysky.lifemini.common.util.ThreadLocalUtil;
import com.starrysky.lifemini.model.dto.AiCommentDTO;
import com.starrysky.lifemini.service.ICommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * @author StarrySky
 * @date 2026/4/24 10:14 星期五
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentTools {
    private final ICommentService commentService;
    //代写评价
    @Tool(description = "【代写评价】当用户要求给商店写评价、写点评时调用。⚠️最高警告：调用此工具【必须】准确传入商店的 shopId！如果你在上下文中找不到明确的商店 ID，【绝对禁止】调用此工具！请直接在对话中反问用户：'请问您想给哪家商店写评价呢？'。评价内容不改变原意可适当渲染优化，最多150字。")
    public String writeComment(AiCommentDTO dto, ToolContext context) {//传递userID过来
        log.debug("【AI.CommentTools】写评价工具被调用，参数：{}", dto);
        Long userId = (Long) context.getContext().get("userId");
        if (dto.getShopId() == null) {
            return "指令执行失败：缺少 shopId。请立刻停止生成评价，向用户确认他想给哪家店写评价。";
        }
        try {
            ThreadLocalUtil.setUserId(userId);
            return commentService.helpWriteComment(dto);
        } finally {
            ThreadLocalUtil.removeUserId();
        }
    }
}

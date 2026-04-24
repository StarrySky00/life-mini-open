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
    @Tool(description = "根据用户描述的内容，帮助用户给商店写评价(不改变原意可适当渲染优化，最多150字)")
    public String writeComment(AiCommentDTO dto, ToolContext context) {//传递userID过来
        log.debug("【AI.CommentTools】写评价工具被调用，参数：{}", dto);
        Long userId = (Long) context.getContext().get("userId");
        try {
            ThreadLocalUtil.setUserId(userId);
            return commentService.helpWriteComment(dto);
        } finally {
            ThreadLocalUtil.removeUserId();
        }
    }
}

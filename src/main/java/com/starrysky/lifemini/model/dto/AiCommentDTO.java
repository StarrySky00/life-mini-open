package com.starrysky.lifemini.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AiCommentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ToolParam(description = "必须传入！目标商店的全局唯一 ID。如果在之前的聊天中推荐过该商店，请从上下文中提取。", required = true)
    private Long shopId;

    @ToolParam(description = "评分 1.0-5.0")
    private BigDecimal score;

    @ToolParam(description = "评价的纯文本内容，字数在150以内,可适当渲染")
    private String content;

    @ToolParam(description = "评价时选择的商店评价关键词 id集合，最多选5个",required = false)
    private List<Integer> keywords;
}

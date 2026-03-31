package com.starrysky.lifemini.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AiCommentDTO {

    private static final long serialVersionUID = 1L;

    @ToolParam(description = "商店id")
    private Long shopId;

    @Schema(description = "评分 1.0-5.0")
    private BigDecimal score;

    @Schema(description = "评价的纯文本内容，字数在150以内")
    private String content;

    @Schema(description = "评价时选择的商店评价关键词 id集合，最多选5个")
    private List<Integer> keywords;
}

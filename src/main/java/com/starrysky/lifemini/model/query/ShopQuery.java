package com.starrysky.lifemini.model.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
public class ShopQuery {
    @ToolParam(description = "分类ID，1个")
    private Long categoryId;
    @ToolParam(description = "关键词ID列表，最多5个", required = false)
    private List<Long> keywordIds;
    @ToolParam(description = "用户经度", required = false)
    private Double longitude;
    @ToolParam(description = "用户纬度", required = false)
    private Double latitude;
    @ToolParam(description = "用户与商店的最大直线距离,单位km。", required = false)
    private Double distance;
    @ToolParam(description = "查询数量（最多五条）", required = false)
    private Integer limit;
}

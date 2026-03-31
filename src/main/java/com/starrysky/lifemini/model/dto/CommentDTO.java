package com.starrysky.lifemini.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
public class CommentDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "商家ID",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long shopId;

    @Schema(description = "评分 1.0-5.0【只有评价需要打分，回复无评分】",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal score;

    @Schema(description = "评价/回复的纯文本内容",requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "评价时选择的keyword（仅评价可选，最多选五个）")
    private List<Integer> keywords;

    @Schema(description = "评价附带图片")
    private String photoUrl;
}

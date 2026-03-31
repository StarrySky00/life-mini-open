package com.starrysky.lifemini.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommentAdminVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论主键ID")
    private Long id;

    @Schema(description = "关联商家ID")
    private Long shopId;

    @Schema(description = "发布者用户ID")
    private Long userId;

    @Schema(description = "发布者用户昵称")
    private String username;

    @Schema(description = "用户状态,1正常，0禁用")
    private Integer status;

    @Schema(description = "发布者用户头像")
    private String avatar;

    @Schema(description = "隐藏。默认1.  1：展示。 0：隐藏")
    private Integer hidden;

    @Schema(description = "评分 1.0-5.0【只有评价需要打分，回复无评分】")
    private BigDecimal score;

    @Schema(description = "评价/回复的纯文本内容")
    private String content;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    @Schema(description = "评价附带图片")
    private String photoUrl;
}

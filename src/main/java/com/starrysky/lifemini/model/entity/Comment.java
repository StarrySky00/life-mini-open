package com.starrysky.lifemini.model.entity;

import java.io.Serial;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
@TableName("tb_comment")
@Schema(description="评价回复表：0=评价(一级)，非0=回复(二级)，仅评价可绑定关键词")
public class Comment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "【逻辑外键】关联商家ID")
    private Long shopId;

    @Schema(description = "【逻辑外键】发布者用户ID")
    private Long userId;

    @Schema(description = "隐藏。默认1.  1：展示。 0：隐藏")
    private Integer hidden;

    @Schema(description = "评分 1.0-5.0【只有评价需要打分，回复无评分】")
    private BigDecimal score;

    @Schema(description = "评价/回复的纯文本内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeNum;

    @Schema(description = "踩数")
    private Integer dislikeNum;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;

    @Schema(description = "评价附带图片")
    private String photoUrl;


}

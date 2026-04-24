package com.starrysky.lifemini.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data

public class CommentKeywordRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论主键ID")
    public Long commentId;
    @Schema(description = "关键词")
    public String keyword;
}

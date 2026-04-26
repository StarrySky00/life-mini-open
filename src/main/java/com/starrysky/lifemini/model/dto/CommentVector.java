package com.starrysky.lifemini.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;



@Data

public class CommentVector implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "【逻辑外键】关联商家ID")
    private Long shopId;
    @Schema(description = "shop类型id")
    private Long shopCateId;

    @Schema(description = "隐藏。默认1.  1：展示。 0：隐藏")
    private Integer hidden;

    @Schema(description = "评价/回复的纯文本内容")
    private String content;
    
    @Schema(description = "商店名称")
    private String shopName;
    
    @Schema(description = "关键词")
    /**
     * 用, 分割关键词集合  e.g. "关键词1,关键词2,关键词3"   or  “无”
     *  
     */
    private String keyWordsStr;
}

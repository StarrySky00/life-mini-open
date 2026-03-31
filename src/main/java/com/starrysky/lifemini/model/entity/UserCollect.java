package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 用户收藏商家表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
@TableName("tb_user_collect")
@Schema(description="用户收藏商家表")
public class UserCollect implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "收藏主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "【逻辑外键】收藏的用户ID")
    private Long userId;

    @Schema(description = "【逻辑外键】被收藏的商家ID")
    private Long shopId;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}

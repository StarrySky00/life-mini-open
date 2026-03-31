package com.starrysky.lifemini.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 商家分类表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
public class ShopCategoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类主键ID")
    private Long id;

    @Schema(description = "分类名称(美食/药店/超市/快递站等)")
    private String categoryName;

    @Schema(description = "分类图标URL")
    private String icon;

    @Schema(description = "排序优先级，数值越小越靠前")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}

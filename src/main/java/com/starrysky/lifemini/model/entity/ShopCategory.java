package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 商家分类表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
@TableName("tb_shop_category")
@Schema(description="商家分类表")
public class ShopCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类主键ID")
    @TableId(value = "id", type = IdType.AUTO)
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

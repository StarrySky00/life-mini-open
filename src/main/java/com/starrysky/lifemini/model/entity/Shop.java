package com.starrysky.lifemini.model.entity;

import java.io.Serial;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * <p>
 * 商家信息表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
@TableName("tb_shop")
@Schema(description="商家信息表")
public class Shop implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(defaultValue = "商家主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商家名称")
    private String shopName;

    @Schema(description = "【逻辑外键】关联分类ID")
    private Long categoryId;

    @Schema(description = "商家详细地址")
    private String address;

    @Schema(description = "经度(位置搜索用)")
    private Double longitude;

    @Schema(description = "纬度(位置搜索用)")
    private Double latitude;

    @Schema(description = "商家综合评分 0.0-5.0")
    private BigDecimal score;

    @Schema(description = "商家联系电话")
    private String phone;

    @Schema(description = "商家封面图URL")
    private String imageUrl;

    @Schema(description = "状态 1=上架 0=下架")
    @Max(value = 1,message = "值仅可为0或1")
    @Min(value = 0,message = "值仅可为0或1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

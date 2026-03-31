package com.starrysky.lifemini.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShopSearchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(defaultValue = "商家主键ID")
    private Long id;

    @Schema(description = "商家名称")
    private String shopName;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "商家详细地址")
    private String address;

    @Schema(description = "评分")
    private Double score;

    @Schema(description = "直线距离")
    private Double distance;
}

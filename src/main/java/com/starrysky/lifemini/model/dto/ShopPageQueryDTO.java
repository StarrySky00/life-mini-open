package com.starrysky.lifemini.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.checkerframework.checker.units.qual.A;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopPageQueryDTO extends PageQueryDTO {
    @Schema(description = "分类id",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;
    @Schema(description = "最大距离（单位千米）（用户当前位置为圆心，距离为半径的范围内）")
    private Integer distance;
    @Schema(description = "商店名称",requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shopName;
}

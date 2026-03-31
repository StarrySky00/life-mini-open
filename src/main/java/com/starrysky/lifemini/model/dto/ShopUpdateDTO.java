package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 商家信息修改
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
public class ShopUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(defaultValue = "商家主键ID",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "id不能为null")
    private Long id;

    @Schema(description = "商家名称",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商店名称不能为空")
    private String shopName;

    @Schema(description = "【逻辑外键】关联分类ID",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类id不能为null")
    private Long categoryId;

    @Schema(description = "商家详细地址")
    private String address;

    @Schema(description = "经度(位置搜索用)")
    private Double longitude;

    @Schema(description = "纬度(位置搜索用)")
    private Double latitude;

    @Schema(description = "商家联系电话")
    @Phone()
    private String phone;
}

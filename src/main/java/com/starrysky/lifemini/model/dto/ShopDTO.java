package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Phone;
import com.starrysky.lifemini.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 商家信息表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
public class ShopDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Schema(defaultValue = "商家主键ID")
    private Long id;

    @NotBlank(message = MessageConstant.NOT_NULL)
    @Schema(description = "商家名称",requiredMode = Schema.RequiredMode.REQUIRED)
    private String shopName;

    @Schema(description = "【逻辑外键】关联分类ID",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = MessageConstant.NOT_NULL )
    private Long categoryId;

    @Schema(description = "商家详细地址",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = MessageConstant.NOT_NULL)
    private String address;

    @Schema(description = "经度(位置搜索用)")
    private Double longitude;

    @Schema(description = "纬度(位置搜索用)")
    private Double latitude;

    @Schema(description = "商家联系电话",requiredMode = Schema.RequiredMode.REQUIRED)
    @Phone
    @NotBlank
    private String phone;
}

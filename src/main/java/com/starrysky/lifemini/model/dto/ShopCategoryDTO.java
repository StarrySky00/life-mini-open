package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.constant.MessageConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 商家分类
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
public class ShopCategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分类名称(美食/药店/超市/快递站等)",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = MessageConstant.NOT_NULL)
    private String categoryName;

    @Schema(description = "分类图标URL")
    private String icon;

    @Schema(description = "排序优先级，数值越小越靠前")
    private Integer sort;
}

package com.starrysky.lifemini.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.Data;

@Data
public class PageQueryDTO {
    @Schema(description = "页数，默认1")
    private Integer pageNo=1;
    @Schema(description = "分页大小,默认10")
    private Integer pageSize=10;
}

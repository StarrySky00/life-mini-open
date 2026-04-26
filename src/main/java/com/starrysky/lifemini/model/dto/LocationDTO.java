package com.starrysky.lifemini.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LocationDTO {
    @Schema(description = "用户经度")
    private Double longitude;
    @Schema(description = "用户纬度")
    private Double latitude;
}
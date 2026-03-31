package com.starrysky.lifemini.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVisitsVO implements Serializable {

    @Schema(description ="日期，以逗号分隔，例如：2026-10-01,2026-10-02,2026-10-03" )
    private String dateList;

    @Schema(description = "每日的访问量，以逗号分隔，例如：20,21,10。")
    private String dayVisits;
}
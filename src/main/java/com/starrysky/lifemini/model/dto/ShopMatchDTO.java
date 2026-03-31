package com.starrysky.lifemini.model.dto;

import lombok.Data;

@Data
public class ShopMatchDTO {
    /**
     * 商店id
     */
    private Long id;
    /**
     * keyword匹配数量
     */
    private Integer matchKeywords;
    private Double score;
}

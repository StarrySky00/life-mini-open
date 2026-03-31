package com.starrysky.lifemini.model.dto;

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
public class ShopCategorySimpleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private String categoryName;

    @Override
    public String toString() {
        return id + ":" + categoryName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}

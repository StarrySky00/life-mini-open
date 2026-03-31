package com.starrysky.lifemini.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

// 分页返回结果对象
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    @Schema(description = "状态值")
    private Integer code;
    @Schema(description = "是否还有更多")
    private boolean hasMore = false;
    @Schema(description = "当前页数据集合")
    private List<T> items; //当前页数据集合

    public static <T> PageResult<T> success() {
        return new PageResult<>(200, false, Collections.emptyList());
    }

    public static <T> PageResult<T> success(boolean hasMore, List<T> items) {
        return new PageResult<>(200, hasMore, items);
    }
    public static <T> PageResult<T> error(int code) {
        return new PageResult<>(code, false, Collections.emptyList());
    }
}
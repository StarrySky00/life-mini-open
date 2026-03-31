package com.starrysky.lifemini.model.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordResult<T> {
    @Schema(description = "状态值")
    private Integer code;
    @Schema(description = "是否还有更多消息")
    private Boolean hasMore;
    @Schema(description = "本次查询最后一条消息id（用于下一次查询）")
    private Long lastId;
    @Schema(description = "本次查询的数据集合")
    private List<T> data;

    public static <T> RecordResult<T> success(boolean hasMore, Long lastId, List<T> data) {
        return new RecordResult<>(200, hasMore, lastId, data);
    }

    public static <T> RecordResult<T> error(Integer code) {
        return new RecordResult<>(code, false, null, Collections.emptyList());
    }
}

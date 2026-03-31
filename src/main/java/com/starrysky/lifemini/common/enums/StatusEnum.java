package com.starrysky.lifemini.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum StatusEnum {
    ENABLE(1, "正常"),
    DISABLE(0, "封禁");
    @EnumValue
    private final Integer id;
    private final String userStatus;

    StatusEnum(Integer id, String userStatus) {
        this.id = id;
        this.userStatus = userStatus;
    }
}

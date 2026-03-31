package com.starrysky.lifemini.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 文件上传状态枚举
 * 对应数据库 pending_uploads 表的 status 字段
 * <br/>
 * 默认0 ：待确认    1： 确认
 *
 */
@Getter
public enum FileUploadStatus {
    PENDING(0,"待确认"),
    CONFIRMED(1,"确认");
    @EnumValue
    private final Integer status;
    private final String description;

    FileUploadStatus(Integer status,String description) {
        this.status=status;
        this.description=description;
    }
}

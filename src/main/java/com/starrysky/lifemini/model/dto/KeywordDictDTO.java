package com.starrysky.lifemini.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class KeywordDictDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "关键词名称(如：性价比高、口味偏辣、上菜快)",requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    @Schema(description = "关键词分类 1=好评 2=差评 3=口味 4=通用",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "展示排序优先级，数值越小越靠前")
    private Integer sort;
}

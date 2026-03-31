package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 关键词字典表【预设所有可选关键词，统一管理】
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-16
 */
@Data
@TableName("tb_keyword_dict")
@Schema(description="关键词字典表【预设所有可选关键词，统一管理】")
public class KeywordDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关键词主键ID【核心字段，AI查询用】")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关键词名称(如：性价比高、口味偏辣、上菜快)",requiredMode = Schema.RequiredMode.REQUIRED)
    private String keyword;

    @Schema(description = "关键词分类 1=好评 2=差评 3=口味 4=通用",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "展示排序优先级，数值越小越靠前")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;


}

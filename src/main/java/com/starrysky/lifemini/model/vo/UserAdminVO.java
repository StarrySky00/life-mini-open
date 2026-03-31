package com.starrysky.lifemini.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserAdminVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号(登录/注册唯一标识)")
    private String phone;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "用户消费偏好(如：爱吃辣,预算50)")
    private String preferences;

    @Schema(description = "状态 0=正常 1=禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}

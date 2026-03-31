package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-15
 */
@Data
@TableName("tb_user")
@Schema(description="用户信息表")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "openId")
    private String openid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码(BCrypt加密存储)")
    private String password;

    @Schema(description = "手机号(登录/注册唯一标识)")
    private String phone;

    @Schema(description = "用户头像URL")
    private String avatar;

    @Schema(description = "用户消费偏好(如：爱吃辣,预算50)")
    private String preferences;

    @Schema(description = "状态 0=正常 1=禁用")
    @Min(value = 0,message = "状态只能是0或1")
    @Max(value = 1,message = "状态只能是0或1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;


}

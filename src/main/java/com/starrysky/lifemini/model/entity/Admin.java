package com.starrysky.lifemini.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 管理员信息表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_admin")
@Schema(name="Admin对象", description="管理员信息表")
public class Admin implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户主键ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码(BCrypt加密存储)")
    private String password;

    @Schema(description = "手机号(登录/注册唯一标识)")
    private String phone;

}

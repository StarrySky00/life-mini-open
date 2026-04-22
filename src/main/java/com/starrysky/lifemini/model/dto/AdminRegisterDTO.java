package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Password;
import com.starrysky.lifemini.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 用户信息表
 * </p>
 *
 * @author StarrySky
 * @since 2026-01-15
 */
@Data
public class AdminRegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码(BCrypt加密存储)",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码格式不正确")
    @Password(message = "密码由字母数字_与.构成，长度6-20")
    private String password;

    @Schema(description = "手机号(登录/注册唯一标识)",requiredMode = Schema.RequiredMode.REQUIRED)
    @Phone(message = "手机号格式不正确")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "验证码",requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}

package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Password;
import com.starrysky.lifemini.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserPasswordDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "手机号(登录/注册唯一标识)",requiredMode = Schema.RequiredMode.REQUIRED)
    @Phone(message = "手机号格式不正确")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "旧密码",requiredMode = Schema.RequiredMode.REQUIRED)
    @Password
    @NotBlank(message = "密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码",requiredMode = Schema.RequiredMode.REQUIRED)
    @Password
    @NotBlank(message = "密码不能为空")
    private String newPassword1;

    @Schema(description = "确认新密码",requiredMode = Schema.RequiredMode.REQUIRED)
    @Password
    @NotBlank(message = "密码不能为空")
    private String newPassword2;
}

package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class UserDeleteDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "密码(BCrypt加密存储)",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9._]{6,20}$",message = "密码必须由字母数字下划线.构成，长度6-20")
    private String password;

    @Schema(description = "手机号(登录/注册唯一标识)",requiredMode = Schema.RequiredMode.REQUIRED)
    @Phone(message = "手机号格式不正确")
    @NotBlank(message = "手机号不能为空")
    private String phone;

}

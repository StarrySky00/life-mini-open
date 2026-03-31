package com.starrysky.lifemini.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class UserInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String avatar;
}

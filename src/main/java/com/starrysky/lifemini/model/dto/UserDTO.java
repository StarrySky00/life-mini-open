package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户名")
    private String username;


    @Schema(description = "用户消费偏好(如：爱吃辣,预算50)")
    private String preferences;
}

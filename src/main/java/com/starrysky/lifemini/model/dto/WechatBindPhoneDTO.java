package com.starrysky.lifemini.model.dto;

import com.starrysky.lifemini.common.annotation.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WechatBindPhoneDTO {
    //
    @Schema(description ="对应前端获取的手机号 code" )
    @Phone
    @NotBlank
    private String phoneCode;
    // 用户的 openid
    @Schema(description ="用户的 openid" )
    private String openid;
}
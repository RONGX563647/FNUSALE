package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 验证码登录DTO
 */
@Data
@Schema(description = "验证码登录请求")
public class CaptchaLoginDTO implements Serializable {

    @Schema(description = "手机号或邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "验证码（6位数字）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String captcha;
}
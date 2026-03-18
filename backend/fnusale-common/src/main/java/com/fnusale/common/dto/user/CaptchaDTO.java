package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 发送验证码DTO
 */
@Data
@Schema(description = "发送验证码请求")
public class CaptchaDTO implements Serializable {

    @Schema(description = "手机号或邮箱地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "账号不能为空")
    private String account;

    @Schema(description = "验证码类型：REGISTER-注册，LOGIN-登录，RESET_PASSWORD-重置密码，BIND-绑定", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}
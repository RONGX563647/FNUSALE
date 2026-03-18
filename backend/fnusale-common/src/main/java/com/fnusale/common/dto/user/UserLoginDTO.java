package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录DTO
 */
@Data
@Schema(description = "用户登录请求")
public class UserLoginDTO implements Serializable {

    @Schema(description = "手机号（loginType为PHONE时必填）")
    private String phone;

    @Schema(description = "邮箱（loginType为EMAIL时必填）")
    private String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "登录类型（PHONE-手机号登录，EMAIL-邮箱登录）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"PHONE", "EMAIL"})
    @NotBlank(message = "登录类型不能为空")
    private String loginType;
}
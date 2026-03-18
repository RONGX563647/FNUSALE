package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册DTO
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterDTO implements Serializable {

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "手机号（registerType为PHONE时必填）")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "邮箱（registerType为EMAIL时必填）")
    @Pattern(regexp = "^$|^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$", message = "邮箱格式不正确")
    private String email;

    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "身份类型（STUDENT-学生，TEACHER-教职工）", allowableValues = {"STUDENT", "TEACHER"})
    private String identityType;

    @Schema(description = "注册类型（PHONE-手机号注册，EMAIL-邮箱注册）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"PHONE", "EMAIL"})
    @NotBlank(message = "注册类型不能为空")
    private String registerType;
}
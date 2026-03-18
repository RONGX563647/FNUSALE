package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员登录请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员登录请求")
public class AdminLoginDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", required = true)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;
}

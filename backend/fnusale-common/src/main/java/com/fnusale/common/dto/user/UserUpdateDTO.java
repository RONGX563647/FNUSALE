package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 用户更新DTO
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateDTO implements Serializable {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "定位权限状态（ALLOW-允许，DENY-拒绝）", allowableValues = {"ALLOW", "DENY"})
    private String locationPermission;
}
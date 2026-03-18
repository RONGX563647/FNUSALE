package com.fnusale.common.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
@Schema(description = "用户查询条件")
public class UserQueryDTO implements Serializable {

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "认证状态（UNAUTH/UNDER_REVIEW/AUTH_SUCCESS/AUTH_FAILED）")
    private String authStatus;

    @Schema(description = "身份类型（STUDENT/TEACHER）")
    private String identityType;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;
}
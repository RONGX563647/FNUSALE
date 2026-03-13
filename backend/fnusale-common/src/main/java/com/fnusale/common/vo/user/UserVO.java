package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户VO
 */
@Data
@Schema(description = "用户信息")
public class UserVO implements Serializable {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "学号/工号（脱敏）")
    private String studentTeacherId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号（脱敏）")
    private String phone;

    @Schema(description = "校园邮箱")
    private String campusEmail;

    @Schema(description = "身份类型")
    private String identityType;

    @Schema(description = "认证状态")
    private String authStatus;

    @Schema(description = "信誉分")
    private Integer creditScore;

    @Schema(description = "定位权限状态")
    private String locationPermission;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
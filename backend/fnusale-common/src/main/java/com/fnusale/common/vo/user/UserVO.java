package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
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

    @Schema(description = "身份类型（STUDENT-学生，TEACHER-教职工）", allowableValues = {"STUDENT", "TEACHER"})
    private String identityType;

    @Schema(description = "认证状态（UNAUTH-未认证，UNDER_REVIEW-审核中，AUTH_SUCCESS-认证成功，AUTH_FAILED-认证失败）", allowableValues = {"UNAUTH", "UNDER_REVIEW", "AUTH_SUCCESS", "AUTH_FAILED"})
    private String authStatus;

    @Schema(description = "认证审核备注")
    private String authResultRemark;

    @Schema(description = "信誉分")
    private Integer creditScore;

    @Schema(description = "定位权限状态（ALLOW-允许，DENY-拒绝）", allowableValues = {"ALLOW", "DENY"})
    private String locationPermission;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
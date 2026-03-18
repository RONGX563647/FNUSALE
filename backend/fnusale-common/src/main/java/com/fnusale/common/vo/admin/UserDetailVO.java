package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户详情VO（管理员视图）
 */
@Data
@Schema(description = "用户详情（管理员视图）")
public class UserDetailVO implements Serializable {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号（脱敏）")
    private String phone;

    @Schema(description = "学号/工号（脱敏）")
    private String studentTeacherId;

    @Schema(description = "校园邮箱")
    private String campusEmail;

    @Schema(description = "身份类型（STUDENT/TEACHER）")
    private String identityType;

    @Schema(description = "认证状态（UNAUTH/UNDER_REVIEW/AUTH_SUCCESS/AUTH_FAILED）")
    private String authStatus;

    @Schema(description = "认证审核备注")
    private String authResultRemark;

    @Schema(description = "校园卡图片URL")
    private String authImageUrl;

    @Schema(description = "信誉分")
    private Integer creditScore;

    @Schema(description = "头像地址")
    private String avatarUrl;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "注册时间")
    private LocalDateTime registerTime;
}
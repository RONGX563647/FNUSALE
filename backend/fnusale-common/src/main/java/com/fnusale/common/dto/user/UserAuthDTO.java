package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户认证DTO
 */
@Data
@Schema(description = "用户认证请求")
public class UserAuthDTO implements Serializable {

    @Schema(description = "学号/工号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2020010001")
    @NotBlank(message = "学号/工号不能为空")
    private String studentTeacherId;

    @Schema(description = "身份类型（STUDENT-学生，TEACHER-教职工）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"STUDENT", "TEACHER"})
    @NotBlank(message = "身份类型不能为空")
    private String identityType;

    @Schema(description = "校园卡/学生证审核图片地址（OSS）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "认证图片不能为空")
    private String authImageUrl;
}
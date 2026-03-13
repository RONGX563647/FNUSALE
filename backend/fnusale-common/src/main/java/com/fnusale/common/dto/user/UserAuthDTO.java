package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户认证DTO
 */
@Data
@Schema(description = "用户认证请求")
public class UserAuthDTO implements Serializable {

    @Schema(description = "学号/工号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentTeacherId;

    @Schema(description = "校园卡/学生证审核图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String authImageUrl;
}
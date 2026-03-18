package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 签到DTO
 */
@Data
@Schema(description = "签到请求")
public class SignDTO implements Serializable {

    @Schema(description = "签到日期（补签时使用，格式：yyyy-MM-dd）")
    private String signDate;
}
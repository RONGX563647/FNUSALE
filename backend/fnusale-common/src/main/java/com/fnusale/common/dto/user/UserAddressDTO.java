package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户地址DTO
 */
@Data
@Schema(description = "用户地址请求")
public class UserAddressDTO implements Serializable {

    @Schema(description = "地址类型（PICK_POINT-自提点，CUSTOM-自定义地址）", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"PICK_POINT", "CUSTOM"})
    @NotBlank(message = "地址类型不能为空")
    private String addressType;

    @Schema(description = "自提点ID（addressType为PICK_POINT时必填）")
    private Long pickPointId;

    @Schema(description = "自定义详细地址（addressType为CUSTOM时必填）")
    private String customAddress;

    @Schema(description = "地址经度")
    private String longitude;

    @Schema(description = "地址纬度")
    private String latitude;

    @Schema(description = "是否默认地址（0-否，1-是）", defaultValue = "0")
    private Integer isDefault = 0;
}
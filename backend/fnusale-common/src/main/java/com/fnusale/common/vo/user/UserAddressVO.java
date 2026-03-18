package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户地址VO
 */
@Data
@Schema(description = "用户地址信息")
public class UserAddressVO implements Serializable {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "地址类型（PICK_POINT-自提点，CUSTOM-自定义）")
    private String addressType;

    @Schema(description = "自提点ID（addressType为PICK_POINT时有值）")
    private Long pickPointId;

    @Schema(description = "自提点名称（addressType为PICK_POINT时有值）")
    private String pickPointName;

    @Schema(description = "自定义详细地址（addressType为CUSTOM时有值）")
    private String customAddress;

    @Schema(description = "地址经度")
    private BigDecimal longitude;

    @Schema(description = "地址纬度")
    private BigDecimal latitude;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
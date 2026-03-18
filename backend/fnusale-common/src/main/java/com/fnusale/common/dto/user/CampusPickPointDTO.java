package com.fnusale.common.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 校园自提点DTO
 */
@Data
@Schema(description = "校园自提点请求")
public class CampusPickPointDTO implements Serializable {

    @Schema(description = "自提点名称（如\"1号宿舍楼楼下\"\"图书馆驿站\"）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "自提点名称不能为空")
    private String pickPointName;

    @Schema(description = "所属校区（如\"东校区\"\"西校区\"）")
    private String campusArea;

    @Schema(description = "详细位置")
    private String detailAddress;

    @Schema(description = "经度")
    private String longitude;

    @Schema(description = "纬度")
    private String latitude;

    @Schema(description = "启用状态（0-禁用，1-启用）", defaultValue = "1")
    private Integer enableStatus = 1;
}
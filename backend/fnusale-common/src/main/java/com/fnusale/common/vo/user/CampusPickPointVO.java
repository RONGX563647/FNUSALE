package com.fnusale.common.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 校园自提点VO
 */
@Data
@Schema(description = "校园自提点信息")
public class CampusPickPointVO implements Serializable {

    @Schema(description = "自提点ID")
    private Long id;

    @Schema(description = "自提点名称")
    private String pickPointName;

    @Schema(description = "所属校区")
    private String campusArea;

    @Schema(description = "详细位置")
    private String detailAddress;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "启用状态")
    private Boolean enableStatus;

    @Schema(description = "距离（米），附近查询时返回")
    private Integer distance;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
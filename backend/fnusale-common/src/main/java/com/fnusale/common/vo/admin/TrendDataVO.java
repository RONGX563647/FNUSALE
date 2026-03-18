package com.fnusale.common.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 趋势数据VO
 */
@Data
@Schema(description = "趋势数据")
public class TrendDataVO implements Serializable {

    @Schema(description = "X轴日期数据")
    private List<String> xAxis;

    @Schema(description = "数据系列")
    private List<SeriesData> series;

    @Data
    @Schema(description = "数据系列")
    public static class SeriesData implements Serializable {
        @Schema(description = "系列名称")
        private String name;

        @Schema(description = "数据值列表")
        private List<Integer> data;
    }
}
package com.fnusale.common.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 快捷回复VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "快捷回复")
public class QuickReplyVO implements Serializable {

    @Schema(description = "快捷回复ID")
    private Long id;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "是否为系统预设")
    private Boolean isSystem;
}
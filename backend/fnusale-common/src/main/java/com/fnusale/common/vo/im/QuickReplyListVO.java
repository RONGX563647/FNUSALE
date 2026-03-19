package com.fnusale.common.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 快捷回复列表VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "快捷回复列表")
public class QuickReplyListVO implements Serializable {

    @Schema(description = "系统预设快捷回复")
    private List<QuickReplyVO> systemReplies;

    @Schema(description = "用户自定义快捷回复")
    private List<QuickReplyVO> userReplies;
}
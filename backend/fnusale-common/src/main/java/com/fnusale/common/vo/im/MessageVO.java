package com.fnusale.common.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消息详情")
public class MessageVO implements Serializable {

    @Schema(description = "消息ID")
    private Long messageId;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "发送者ID")
    private Long senderId;

    @Schema(description = "接收者ID")
    private Long receiverId;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "语音时长（秒）")
    private Integer duration;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "是否已读")
    private Boolean isRead;

    @Schema(description = "是否已撤回")
    private Boolean isRecalled;
}
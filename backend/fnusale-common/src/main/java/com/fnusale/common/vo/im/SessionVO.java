package com.fnusale.common.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话详情VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话详情")
public class SessionVO implements Serializable {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "对方用户ID")
    private Long targetUserId;

    @Schema(description = "对方用户名")
    private String targetUsername;

    @Schema(description = "对方头像")
    private String targetAvatarUrl;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品主图")
    private String productMainImage;

    @Schema(description = "商品价格")
    private java.math.BigDecimal productPrice;

    @Schema(description = "最后一条消息内容")
    private String lastMessageContent;

    @Schema(description = "最后一条消息时间")
    private LocalDateTime lastMessageTime;

    @Schema(description = "最后一条消息类型")
    private String lastMessageType;

    @Schema(description = "未读消息数")
    private Integer unreadCount;

    @Schema(description = "是否置顶")
    private Boolean isPinned;

    @Schema(description = "会话状态")
    private String sessionStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
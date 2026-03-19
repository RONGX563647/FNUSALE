package com.fnusale.admin.service;

import com.fnusale.common.event.ProductAuditEvent;
import com.fnusale.common.event.UserAuthAuditEvent;
import com.fnusale.common.event.UserBanEvent;

/**
 * Admin 事件发布服务
 */
public interface AdminEventPublisher {

    /**
     * 发布商品审核事件
     */
    void publishProductAuditEvent(ProductAuditEvent event);

    /**
     * 发布用户认证审核事件
     */
    void publishUserAuthAuditEvent(UserAuthAuditEvent event);

    /**
     * 发布用户封禁事件
     */
    void publishUserBanEvent(UserBanEvent event);
}
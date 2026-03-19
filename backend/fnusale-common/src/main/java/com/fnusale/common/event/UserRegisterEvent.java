package com.fnusale.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户注册事件
 * 用于异步处理注册后置操作：积分初始化、欢迎通知、新人优惠券等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号（可能为空）
     */
    private String phone;

    /**
     * 邮箱（可能为空）
     */
    private String email;

    /**
     * 身份类型（STUDENT/TEACHER）
     */
    private String identityType;

    /**
     * 注册来源（PHONE/EMAIL/CAPTCHA_LOGIN）
     */
    private String registerSource;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 事件ID（用于幂等性处理）
     */
    private String eventId;
}
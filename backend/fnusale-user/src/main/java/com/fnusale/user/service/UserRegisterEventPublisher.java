package com.fnusale.user.service;

import com.fnusale.common.event.UserRegisterEvent;

/**
 * 用户注册事件发布服务
 */
public interface UserRegisterEventPublisher {

    /**
     * 发布用户注册事件
     * @param event 注册事件
     */
    void publishRegisterEvent(UserRegisterEvent event);
}
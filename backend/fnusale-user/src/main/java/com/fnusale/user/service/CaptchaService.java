package com.fnusale.user.service;

import com.fnusale.common.dto.user.CaptchaDTO;
import com.fnusale.common.dto.user.CaptchaLoginDTO;
import com.fnusale.common.vo.user.LoginVO;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 发送验证码
     * @param dto 验证码请求
     */
    void sendCaptcha(CaptchaDTO dto);

    /**
     * 验证码登录
     * @param dto 验证码登录请求
     * @return 登录信息
     */
    LoginVO loginByCaptcha(CaptchaLoginDTO dto);

    /**
     * 验证验证码
     * @param account 账号（手机号或邮箱）
     * @param captcha 验证码
     * @param type 验证码类型
     * @return 是否验证成功
     */
    boolean verifyCaptcha(String account, String captcha, String type);
}
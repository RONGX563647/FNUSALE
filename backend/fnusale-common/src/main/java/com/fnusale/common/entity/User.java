package com.fnusale.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户基础表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学号/工号（唯一）
     */
    private String studentTeacherId;

    /**
     * 用户名（昵称）
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 校园邮箱
     */
    private String campusEmail;

    /**
     * 密码
     */
    private String password;

    /**
     * 身份类型（STUDENT/TEACHER）
     */
    private String identityType;

    /**
     * 认证状态（UNAUTH/UNDER_REVIEW/AUTH_SUCCESS/AUTH_FAILED）
     */
    private String authStatus;

    /**
     * 校园卡/学生证审核图片地址
     */
    private String authImageUrl;

    /**
     * 认证审核备注
     */
    private String authResultRemark;

    /**
     * 定位权限状态（ALLOW/DENY）
     */
    private String locationPermission;

    /**
     * 信誉分（默认100）
     */
    private Integer creditScore;
}
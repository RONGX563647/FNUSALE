package com.fnusale.admin.config;

/**
 * 管理员上下文工具类
 */
public class AdminContext {

    private static final ThreadLocal<Long> adminId = new ThreadLocal<>();

    /**
     * 设置管理员ID
     */
    public static void setAdminId(Long id) {
        adminId.set(id);
    }

    /**
     * 获取管理员ID
     */
    public static Long getAdminId() {
        return adminId.get();
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        adminId.remove();
    }
}
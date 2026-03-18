package com.fnusale.common.util;

/**
 * 用户上下文工具类
 * 用于在当前线程中存储和获取用户信息
 */
public class UserContext {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * 清除当前用户ID
     */
    public static void clearCurrentUserId() {
        currentUserId.remove();
    }

    /**
     * 设置当前用户角色
     */
    public static void setCurrentUserRole(String role) {
        currentUserRole.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }

    /**
     * 清除当前用户角色
     */
    public static void clearCurrentUserRole() {
        currentUserRole.remove();
    }

    /**
     * 清除所有用户上下文信息
     */
    public static void clear() {
        currentUserId.remove();
        currentUserRole.remove();
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isLoggedIn() {
        return currentUserId.get() != null;
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        String role = currentUserRole.get();
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    /**
     * 获取当前用户ID，如果未登录则返回null
     */
    public static Long getUserIdOrThrow() {
        Long userId = currentUserId.get();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }
}
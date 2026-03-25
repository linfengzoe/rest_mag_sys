package com.rest_mag_sys.common;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的ID
 */
public class BaseContext {

    private static final ThreadLocal<Long> currentIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRoleHolder = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * @param id 用户ID
     */
    public static void setCurrentId(Long id) {
        currentIdHolder.set(id);
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static Long getCurrentId() {
        return currentIdHolder.get();
    }

    /**
     * 设置当前用户角色
     * @param role 用户角色
     */
    public static void setCurrentRole(String role) {
        currentRoleHolder.set(role);
    }

    /**
     * 获取当前用户角色
     * @return 用户角色
     */
    public static String getCurrentRole() {
        return currentRoleHolder.get();
    }

    /**
     * 移除当前用户ID
     */
    public static void removeCurrentId() {
        currentIdHolder.remove();
        currentRoleHolder.remove();
    }
} 
package com.rest_mag_sys.common;

/**
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的ID
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * @param id 用户ID
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }

    /**
     * 移除当前用户ID
     */
    public static void removeCurrentId() {
        threadLocal.remove();
    }
} 
package com.rest_mag_sys.dto;

import lombok.Data;

/**
 * 顾客信息更新 DTO (后台管理使用)
 */
@Data
public class CustomerUpdateDTO {
    // 顾客表主键
    private Long id;

    // 用户表主键（t_user.id）
    private Long userId;

    // ========== 用户表字段 ==========
    private String username;
    private String email;
    private String birthday; // yyyy-MM-dd

    private String name;
    private String phone;
    private Integer sex; // 0-女 1-男

    // ========== 顾客表扩展字段 ==========
    private String address;
    private Integer points;
    private Integer memberLevel;
} 
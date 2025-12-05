package com.rest_mag_sys.dto;

import lombok.Data;

/**
 * 新增顾客 DTO
 */
@Data
public class AddCustomerDTO {
    // 用户表字段
    private String username;
    private String password;
    private String name;
    private String phone;
    private String email;
    private Integer sex; // 0-女 1-男
    private String birthday; // yyyy-MM-dd

    // 顾客表字段
    private String address;
    private Integer points; // 初始积分
    private Integer memberLevel; // 初始会员等级
} 
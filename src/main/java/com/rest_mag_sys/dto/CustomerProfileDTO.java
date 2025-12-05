package com.rest_mag_sys.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 顾客个人信息更新DTO
 */
@Data
public class CustomerProfileDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    // ========== 用户基本信息 ==========
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 性别：0-女，1-男
     */
    private Integer sex;
    
    /**
     * 生日
     */
    private String birthday;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 用户名
     */
    private String username;
    
    // ========== 顾客扩展信息 ==========
    /**
     * 地址（顾客可以自己更新）
     */
    private String address;
    
    // ========== 只读信息（系统管理） ==========
    /**
     * 积分（只读，通过消费获得）
     */
    private Integer points;
    
    /**
     * 会员等级（只读，根据积分自动计算）
     */
    private Integer memberLevel;
    
    /**
     * 注册时间（只读）
     */
    private LocalDateTime registerTime;
} 
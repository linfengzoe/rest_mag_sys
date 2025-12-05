package com.rest_mag_sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 员工个人信息更新DTO
 */
@Data
public class EmployeeProfileDTO {
    
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
    
    // ========== 员工扩展信息 ==========
    /**
     * 身份证号（员工可以自己更新）
     */
    private String idNumber;
    
    /**
     * 地址（员工可以自己更新）
     */
    private String address;
    
    // ========== 管理员才能修改的字段 ==========
    /**
     * 职位（只有管理员可以修改）
     */
    private String position;
    
    /**
     * 薪资（只有管理员可以修改）
     */
    private BigDecimal salary;
    
    /**
     * 入职日期（只有管理员可以修改）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime hireDate;

    /**
     * 自定义hireDate字段的setter方法，支持字符串格式的时间
     * @param hireDate 入职时间，可以是字符串或LocalDateTime
     */
    @JsonSetter("hireDate")
    public void setHireDateFromJson(Object hireDate) {
        if (hireDate == null) {
            this.hireDate = null;
            return;
        }
        
        if (hireDate instanceof LocalDateTime) {
            this.hireDate = (LocalDateTime) hireDate;
        } else if (hireDate instanceof String) {
            try {
                String timeStr = (String) hireDate;
                // 支持多种时间格式
                if (timeStr.contains("T")) {
                    // ISO格式：2024-01-01T00:00:00
                    this.hireDate = LocalDateTime.parse(timeStr);
                } else {
                    // 标准格式：2024-01-01 00:00:00
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    this.hireDate = LocalDateTime.parse(timeStr, formatter);
                }
            } catch (Exception e) {
                // 解析失败时忽略
                this.hireDate = null;
            }
        } else {
            this.hireDate = null;
        }
    }
} 
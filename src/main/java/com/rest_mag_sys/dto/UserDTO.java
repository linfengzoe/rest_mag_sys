package com.rest_mag_sys.dto;

import com.rest_mag_sys.entity.User;
import lombok.Data;

/**
 * 用户数据传输对象
 */
@Data
public class UserDTO extends User {

    /**
     * 确认密码
     */
    private String confirmPassword;

    /**
     * 验证码
     */
    private String code;
} 
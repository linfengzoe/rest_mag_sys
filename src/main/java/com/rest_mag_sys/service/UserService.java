package com.rest_mag_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.LoginDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.dto.UserDTO;
import com.rest_mag_sys.dto.EmployeeProfileDTO;
import com.rest_mag_sys.dto.CustomerProfileDTO;
import com.rest_mag_sys.entity.User;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    User getByUsername(String username);

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 用户信息
     */
    UserDTO login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param user 用户信息
     * @return 是否成功
     */
    boolean register(User user);
    
    /**
     * 用户注册
     * @param userDTO 用户信息
     * @return 是否成功
     */
    boolean register(UserDTO userDTO);

    /**
     * 分页查询用户列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 用户名或姓名
     * @param role 角色
     * @return 分页结果
     */
    R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name, String role);
    
    /**
     * 分页查询用户
     * @param pageQueryDTO 分页查询参数
     * @return 用户分页结果
     */
    Page<User> pageQuery(PageQueryDTO pageQueryDTO);
    
    /**
     * 更新用户信息
     * @param userDTO 用户信息
     * @return 是否成功
     */
    boolean updateUser(UserDTO userDTO);

    /**
     * 更新个人信息（包括相关表）
     * @param userDTO 用户数据传输对象
     * @return 是否成功
     */
    boolean updateProfile(UserDTO userDTO);

    /**
     * 更新员工完整个人信息
     * @param employeeProfileDTO 员工个人信息DTO
     * @param isAdmin 是否为管理员操作
     * @return 是否成功
     */
    boolean updateEmployeeProfile(EmployeeProfileDTO employeeProfileDTO, boolean isAdmin);

    /**
     * 更新顾客完整个人信息
     * @param customerProfileDTO 顾客个人信息DTO
     * @return 是否成功
     */
    boolean updateCustomerProfile(CustomerProfileDTO customerProfileDTO);
     
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 批量更新用户状态
     * @param ids 用户ID列表
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatusBatch(java.util.List<Long> ids, Integer status);
} 
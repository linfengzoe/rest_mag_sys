package com.rest_mag_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rest_mag_sys.common.CustomException;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.LoginDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.dto.UserDTO;
import com.rest_mag_sys.dto.EmployeeProfileDTO;
import com.rest_mag_sys.dto.CustomerProfileDTO;
import com.rest_mag_sys.entity.User;
import com.rest_mag_sys.entity.Customer;
import com.rest_mag_sys.entity.Employee;
import com.rest_mag_sys.mapper.UserMapper;
import com.rest_mag_sys.mapper.CustomerMapper;
import com.rest_mag_sys.mapper.EmployeeMapper;
import com.rest_mag_sys.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Pattern MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return this.getOne(queryWrapper);
    }

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 用户信息
     */
    @Override
    public UserDTO login(LoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        User user = this.getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new CustomException("用户名不存在");
        }

        // 2. 密码比对（兼容历史MD5，验证成功后自动升级为BCrypt）
        if (!matchesPassword(loginDTO.getPassword(), user)) {
            throw new CustomException("密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new CustomException("账号已禁用");
        }

        // 4. 将用户信息返回给前端
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setPassword(null); // 不返回密码

        return userDTO;
    }
    
    /**
     * 用户注册
     * @param user 用户信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean register(User user) {
        // 1. 检查用户名是否已存在
        User existUser = this.getByUsername(user.getUsername());
        if (existUser != null) {
            throw new CustomException("用户名已存在");
        }

        // 2. 密码加密（BCrypt）
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. 角色格式化处理，统一转换为小写
        if (user.getRole() != null) {
            user.setRole(user.getRole().toLowerCase());
        }

        // 4. 设置默认状态为启用
        user.setStatus(1);

        // 5. 保存用户
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new CustomException("用户注册失败");
        }

        // 6. 根据角色创建相应的扩展记录
        createExtendedRecord(user);

        return true;
    }

    /**
     * 用户注册（使用UserDTO）
     * @param userDTO 用户信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean register(UserDTO userDTO) {
        // 1. 检查用户名是否已存在
        User existUser = this.getByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new CustomException("用户名已存在");
        }

        // 2. 转换为User对象
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 3. 密码加密（BCrypt）
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 4. 角色格式化处理，统一转换为小写
        if (user.getRole() != null) {
            user.setRole(user.getRole().toLowerCase());
        }

        // 5. 设置默认状态为启用
        user.setStatus(1);

        // 6. 保存用户
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new CustomException("用户注册失败");
        }

        // 7. 根据角色创建相应的扩展记录
        createExtendedRecord(user);

        return true;
    }

    /**
     * 根据用户角色创建相应的扩展记录
     * @param user 用户对象
     */
    private void createExtendedRecord(User user) {
        String role = user.getRole();
        if (role == null) {
            return;
        }

        switch (role.toLowerCase()) {
            case "customer":
                createCustomerRecord(user);
                break;
            case "employee":
                createEmployeeRecord(user);
                break;
            case "admin":
                // 管理员通常也属于员工，创建员工记录
                createEmployeeRecord(user);
                break;
            default:
                log.warn("未知角色：{}", role);
        }
    }

    /**
     * 创建顾客记录
     * @param user 用户对象
     */
    private void createCustomerRecord(User user) {
        Customer customer = new Customer();
        customer.setUserId(user.getId());
        customer.setName(user.getName());
        customer.setPhone(user.getPhone());
        customer.setSex(user.getSex());
        customer.setMemberLevel(0); // 默认为普通用户
        customer.setPoints(0); // 默认积分为0
        customer.setRegisterTime(LocalDateTime.now());
        
        int insertResult = customerMapper.insert(customer);
        if (insertResult <= 0) {
            throw new CustomException("创建顾客记录失败");
        }
        log.info("为用户{}创建了顾客记录，ID: {}", user.getUsername(), customer.getId());
    }

    /**
     * 创建员工记录
     * @param user 用户对象
     */
    private void createEmployeeRecord(User user) {
        Employee employee = new Employee();
        employee.setUserId(user.getId());
        employee.setName(user.getName());
        employee.setPhone(user.getPhone());
        employee.setSex(user.getSex());
        // employee.setIdNumber(user.getIdNumber()); // 身份证号字段已删除
        employee.setHireDate(LocalDateTime.now());
        employee.setPosition("admin".equals(user.getRole()) ? "经理" : "员工");
        
        int insertResult = employeeMapper.insert(employee);
        if (insertResult <= 0) {
            throw new CustomException("创建员工记录失败");
        }
        log.info("为用户{}创建了员工记录，ID: {}", user.getUsername(), employee.getId());
    }

    /**
     * 分页查询用户列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 用户名或姓名
     * @param role 角色
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String name, String role) {
        // 创建分页对象
        Page<User> pageInfo = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        // 根据用户名或姓名模糊查询
        queryWrapper.like(StringUtils.isNotBlank(name), User::getUsername, name)
                .or()
                .like(StringUtils.isNotBlank(name), User::getName, name);
        // 根据角色精确查询
        queryWrapper.eq(StringUtils.isNotBlank(role), User::getRole, role);
        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreateTime);

        // 执行查询
        this.page(pageInfo, queryWrapper);

        // 处理查询结果，将密码置空
        List<UserDTO> userDTOList = pageInfo.getRecords().stream().map(user -> {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(user, userDTO);
            userDTO.setPassword(null);
            return userDTO;
        }).collect(Collectors.toList());

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("records", userDTOList);

        return R.success(result);
    }

    /**
     * 分页查询用户
     * @param pageQueryDTO 分页查询参数
     * @return 用户分页结果
     */
    @Override
    public Page<User> pageQuery(PageQueryDTO pageQueryDTO) {
        // 构造分页对象
        Page<User> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        
        // 构造查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageQueryDTO.getKeyword()), User::getUsername, pageQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(pageQueryDTO.getKeyword()), User::getName, pageQueryDTO.getKeyword());
        
        // 执行查询
        this.page(page, queryWrapper);
        
        // 处理密码，不返回给前端
        page.getRecords().forEach(user -> user.setPassword(null));
        
        return page;
    }
    
    /**
     * 更新用户信息
     * @param userDTO 用户数据传输对象
     * @return 是否成功
     */
    @Override
    public boolean updateUser(UserDTO userDTO) {
        // 1. 检查用户是否存在
        User existUser = this.getById(userDTO.getId());
        if (existUser == null) {
            throw new CustomException("用户不存在");
        }

        // 2. 转换为User对象
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 2.1 若请求中包含密码字段，则进行BCrypt加密后再存储
        if (StringUtils.isNotBlank(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // 3. 角色格式化处理
        if (user.getRole() != null) {
            user.setRole(user.getRole().toLowerCase());
        }

        // 4. 更新用户信息
        boolean updateResult = this.updateById(user);
        if (!updateResult) {
            throw new CustomException("更新用户失败");
        }

        // 5. 同时更新相关表
        updateRelatedTables(user);

        return true;
    }

    /**
     * 更新个人信息（包括相关表）
     * @param userDTO 用户数据传输对象
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateProfile(UserDTO userDTO) {
        // 1. 检查用户是否存在
        User existUser = this.getById(userDTO.getId());
        if (existUser == null) {
            throw new CustomException("用户不存在");
        }

        // 2. 转换为User对象
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(null); // 不允许通过此接口修改密码
        user.setRole(null); // 不允许通过此接口修改角色

        // 3. 更新用户信息
        boolean updateResult = this.updateById(user);
        if (!updateResult) {
            throw new CustomException("更新个人信息失败");
        }

        // 4. 同时更新相关表
        updateRelatedTables(user, existUser.getRole());

        return true;
    }

    /**
     * 更新相关表（customer或employee）
     * @param user 用户对象
     */
    private void updateRelatedTables(User user) {
        updateRelatedTables(user, user.getRole());
    }

    /**
     * 更新相关表（customer或employee）
     * @param user 用户对象
     * @param role 用户角色
     */
    private void updateRelatedTables(User user, String role) {
        if (role == null) {
            return;
        }

        switch (role.toLowerCase()) {
            case "customer":
                updateCustomerRecord(user);
                break;
            case "employee":
            case "admin":
                updateEmployeeRecord(user);
                break;
            default:
                log.warn("未知角色，无法更新相关表：{}", role);
        }
    }

    /**
     * 更新顾客记录
     * @param user 用户对象
     */
    private void updateCustomerRecord(User user) {
        // 查询现有的customer记录
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getUserId, user.getId());
        Customer existCustomer = customerMapper.selectOne(queryWrapper);

        if (existCustomer != null) {
            // 更新customer记录
            Customer customer = new Customer();
            customer.setId(existCustomer.getId());
            customer.setName(user.getName());
            customer.setPhone(user.getPhone());
            customer.setSex(user.getSex());
            
            int updateResult = customerMapper.updateById(customer);
            if (updateResult <= 0) {
                throw new CustomException("更新顾客信息失败");
            }
            log.info("更新顾客记录成功，用户ID: {}", user.getId());
        } else {
            // 如果没有customer记录，创建一个
            log.warn("用户{}没有对应的顾客记录，自动创建", user.getId());
            createCustomerRecord(user);
        }
    }

    /**
     * 更新员工记录
     * @param user 用户对象
     */
    private void updateEmployeeRecord(User user) {
        // 查询现有的employee记录
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUserId, user.getId());
        Employee existEmployee = employeeMapper.selectOne(queryWrapper);

        if (existEmployee != null) {
            // 更新employee记录
            Employee employee = new Employee();
            employee.setId(existEmployee.getId());
            employee.setName(user.getName());
            employee.setPhone(user.getPhone());
            employee.setSex(user.getSex());
            
            int updateResult = employeeMapper.updateById(employee);
            if (updateResult <= 0) {
                throw new CustomException("更新员工信息失败");
            }
            log.info("更新员工记录成功，用户ID: {}", user.getId());
        } else {
            // 如果没有employee记录，创建一个
            log.warn("用户{}没有对应的员工记录，自动创建", user.getId());
            createEmployeeRecord(user);
        }
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 检查用户是否存在
        User user = this.getById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }
        
        // 2. 检查旧密码是否正确（兼容历史MD5）
        if (!matchesPasswordWithoutUpgrade(oldPassword, user.getPassword())) {
            throw new CustomException("旧密码错误");
        }
        
        // 3. 更新密码（BCrypt）
        user.setPassword(passwordEncoder.encode(newPassword));
        
        return this.updateById(user);
    }

    private boolean matchesPassword(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return false;
        }
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }
        if (isLegacyMd5(storedPassword)) {
            String legacyMd5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
            if (storedPassword.equalsIgnoreCase(legacyMd5)) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                this.updateById(user);
                return true;
            }
        }
        return false;
    }

    private boolean matchesPasswordWithoutUpgrade(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }
        if (isLegacyMd5(storedPassword)) {
            String legacyMd5 = DigestUtils.md5DigestAsHex(rawPassword.getBytes());
            return storedPassword.equalsIgnoreCase(legacyMd5);
        }
        return false;
    }

    private boolean isLegacyMd5(String passwordHash) {
        return MD5_PATTERN.matcher(passwordHash).matches();
    }

    /**
     * 批量更新用户状态
     * @param ids 用户ID列表
     * @param status 状态
     * @return 是否成功
     */
    @Override
    public boolean updateStatusBatch(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 批量更新用户状态
        return ids.stream().allMatch(id -> {
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            return this.updateById(user);
        });
    }

    /**
     * 更新员工完整个人信息
     * @param employeeProfileDTO 员工个人信息DTO
     * @param isAdmin 是否为管理员操作
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateEmployeeProfile(EmployeeProfileDTO employeeProfileDTO, boolean isAdmin) {
        // 1. 检查用户是否存在
        User existUser = this.getById(employeeProfileDTO.getId());
        if (existUser == null) {
            throw new CustomException("用户不存在");
        }

        // 2. 更新用户基本信息 - 只更新非空字段
        User user = new User();
        user.setId(employeeProfileDTO.getId());
        
        if (employeeProfileDTO.getName() != null) {
            user.setName(employeeProfileDTO.getName());
        }
        if (employeeProfileDTO.getPhone() != null) {
            user.setPhone(employeeProfileDTO.getPhone());
        }
        if (employeeProfileDTO.getEmail() != null) {
            user.setEmail(employeeProfileDTO.getEmail());
        }
        if (employeeProfileDTO.getSex() != null) {
            user.setSex(employeeProfileDTO.getSex());
        }
        if (employeeProfileDTO.getBirthday() != null && !employeeProfileDTO.getBirthday().trim().isEmpty()) {
            user.setBirthday(employeeProfileDTO.getBirthday());
        }
        if (employeeProfileDTO.getAvatar() != null && !employeeProfileDTO.getAvatar().trim().isEmpty()) {
            user.setAvatar(employeeProfileDTO.getAvatar());
        }

        boolean updateUserResult = this.updateById(user);
        if (!updateUserResult) {
            throw new CustomException("更新用户基本信息失败");
        }

        // 3. 更新员工扩展信息
        updateEmployeeExtendedInfo(employeeProfileDTO, isAdmin);

        return true;
    }

    /**
     * 更新顾客完整个人信息
     * @param customerProfileDTO 顾客个人信息DTO
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateCustomerProfile(CustomerProfileDTO customerProfileDTO) {
        // 1. 检查用户是否存在
        User existUser = this.getById(customerProfileDTO.getId());
        if (existUser == null) {
            throw new CustomException("用户不存在");
        }

        // 2. 更新用户基本信息 - 只更新非空字段
        User user = new User();
        user.setId(customerProfileDTO.getId());
        
        if (customerProfileDTO.getName() != null) {
            user.setName(customerProfileDTO.getName());
        }
        if (customerProfileDTO.getPhone() != null) {
            user.setPhone(customerProfileDTO.getPhone());
        }
        if (customerProfileDTO.getEmail() != null) {
            user.setEmail(customerProfileDTO.getEmail());
        }
        if (customerProfileDTO.getSex() != null) {
            user.setSex(customerProfileDTO.getSex());
        }
        if (customerProfileDTO.getBirthday() != null && !customerProfileDTO.getBirthday().trim().isEmpty()) {
            user.setBirthday(customerProfileDTO.getBirthday());
        }
        if (customerProfileDTO.getAvatar() != null && !customerProfileDTO.getAvatar().trim().isEmpty()) {
            user.setAvatar(customerProfileDTO.getAvatar());
        }

        boolean updateUserResult = this.updateById(user);
        if (!updateUserResult) {
            throw new CustomException("更新用户基本信息失败");
        }

        // 3. 更新顾客扩展信息
        updateCustomerExtendedInfo(customerProfileDTO);

        return true;
    }

    /**
     * 更新员工扩展信息
     * @param employeeProfileDTO 员工信息DTO
     * @param isAdmin 是否为管理员操作
     */
    private void updateEmployeeExtendedInfo(EmployeeProfileDTO employeeProfileDTO, boolean isAdmin) {
        // 查询现有的employee记录
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUserId, employeeProfileDTO.getId());
        Employee existEmployee = employeeMapper.selectOne(queryWrapper);

        if (existEmployee == null) {
            log.warn("用户{}没有对应的员工记录，自动创建", employeeProfileDTO.getId());
            // 创建新的员工记录
            User user = this.getById(employeeProfileDTO.getId());
            createEmployeeRecord(user);
            // 重新查询
            existEmployee = employeeMapper.selectOne(queryWrapper);
        }

        // 更新员工信息 - 只更新非空字段
        Employee employee = new Employee();
        employee.setId(existEmployee.getId());
        
        // 基本信息更新
        if (employeeProfileDTO.getName() != null) {
            employee.setName(employeeProfileDTO.getName());
        }
        if (employeeProfileDTO.getPhone() != null) {
            employee.setPhone(employeeProfileDTO.getPhone());
        }
        if (employeeProfileDTO.getSex() != null) {
            employee.setSex(employeeProfileDTO.getSex());
        }
        
        // 员工可以自己更新的字段
        if (employeeProfileDTO.getIdNumber() != null && !employeeProfileDTO.getIdNumber().trim().isEmpty()) {
            employee.setIdNumber(employeeProfileDTO.getIdNumber());
        }
        if (employeeProfileDTO.getAddress() != null && !employeeProfileDTO.getAddress().trim().isEmpty()) {
            employee.setAddress(employeeProfileDTO.getAddress());
        }
        
        // 只有管理员可以更新的字段
        if (isAdmin) {
            if (employeeProfileDTO.getPosition() != null && !employeeProfileDTO.getPosition().trim().isEmpty()) {
                employee.setPosition(employeeProfileDTO.getPosition());
            }
            if (employeeProfileDTO.getSalary() != null) {
                employee.setSalary(employeeProfileDTO.getSalary());
            }
            if (employeeProfileDTO.getHireDate() != null) {
                employee.setHireDate(employeeProfileDTO.getHireDate());
            }
        }

        int updateResult = employeeMapper.updateById(employee);
        if (updateResult <= 0) {
            throw new CustomException("更新员工扩展信息失败");
        }
        log.info("更新员工扩展信息成功，用户ID: {}", employeeProfileDTO.getId());
    }

    /**
     * 更新顾客扩展信息
     * @param customerProfileDTO 顾客信息DTO
     */
    private void updateCustomerExtendedInfo(CustomerProfileDTO customerProfileDTO) {
        // 查询现有的customer记录
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getUserId, customerProfileDTO.getId());
        Customer existCustomer = customerMapper.selectOne(queryWrapper);

        if (existCustomer == null) {
            log.warn("用户{}没有对应的顾客记录，自动创建", customerProfileDTO.getId());
            // 创建新的顾客记录
            User user = this.getById(customerProfileDTO.getId());
            createCustomerRecord(user);
            // 重新查询
            existCustomer = customerMapper.selectOne(queryWrapper);
        }

        // 更新顾客信息 - 只更新非空字段
        Customer customer = new Customer();
        customer.setId(existCustomer.getId());
        
        // 基本信息更新
        if (customerProfileDTO.getName() != null) {
            customer.setName(customerProfileDTO.getName());
        }
        if (customerProfileDTO.getPhone() != null) {
            customer.setPhone(customerProfileDTO.getPhone());
        }
        if (customerProfileDTO.getSex() != null) {
            customer.setSex(customerProfileDTO.getSex());
        }
        if (customerProfileDTO.getAddress() != null && !customerProfileDTO.getAddress().trim().isEmpty()) {
            customer.setAddress(customerProfileDTO.getAddress());
        }
        
        // 积分、会员等级、注册时间由系统管理，不允许用户修改

        int updateResult = customerMapper.updateById(customer);
        if (updateResult <= 0) {
            throw new CustomException("更新顾客扩展信息失败");
        }
        log.info("更新顾客扩展信息成功，用户ID: {}", customerProfileDTO.getId());
    }
} 
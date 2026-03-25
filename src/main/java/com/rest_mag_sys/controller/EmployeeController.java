package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.common.RequireRoles;
import com.rest_mag_sys.dto.EmployeeProfileDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Employee;
import com.rest_mag_sys.entity.User;
import com.rest_mag_sys.mapper.EmployeeMapper;
import com.rest_mag_sys.mapper.UserMapper;
import com.rest_mag_sys.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询员工列表
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/list")
    @RequireRoles({"admin"})
    public R<Page<Map<String, Object>>> list(PageQueryDTO pageQueryDTO) {
        log.info("分页查询员工，参数：{}", pageQueryDTO);
        try {
            // 查询员工列表
            Page<Employee> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
            
            // 如果有姓名查询条件，需要先查询用户表
            LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(pageQueryDTO.getName())) {
                // 先查询用户表获取符合条件的用户ID
                LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
                userQueryWrapper.like(User::getName, pageQueryDTO.getName());
                userQueryWrapper.and(wrapper -> wrapper.eq(User::getRole, "employee").or().eq(User::getRole, "admin"));
                List<User> users = userMapper.selectList(userQueryWrapper);
                
                if (users.isEmpty()) {
                    // 如果没有找到符合条件的用户，返回空结果
                    Page<Map<String, Object>> resultPage = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
                    resultPage.setTotal(0);
                    resultPage.setRecords(new ArrayList<>());
                    return R.success(resultPage);
                }
                
                List<Long> userIds = users.stream().map(User::getId).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                queryWrapper.in(Employee::getUserId, userIds);
            }
            
            page = employeeMapper.selectPage(page, queryWrapper);
            
            // 组装返回数据，包含用户信息
            Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize());
            resultPage.setTotal(page.getTotal());
            
            List<Map<String, Object>> records = new ArrayList<>();
            for (Employee employee : page.getRecords()) {
                Map<String, Object> record = new HashMap<>();
                record.put("id", employee.getId());
                record.put("userId", employee.getUserId());
                record.put("name", employee.getName());
                record.put("phone", employee.getPhone());
                record.put("sex", employee.getSex());
                record.put("idNumber", employee.getIdNumber());
                record.put("hireDate", employee.getHireDate());
                record.put("position", employee.getPosition());
                record.put("salary", employee.getSalary());
                record.put("address", employee.getAddress());
                record.put("createTime", employee.getCreateTime());
                record.put("updateTime", employee.getUpdateTime());
                
                // 获取用户信息
                if (employee.getUserId() != null) {
                    User user = userMapper.selectById(employee.getUserId());
                    if (user != null) {
                        record.put("username", user.getUsername());
                        record.put("email", user.getEmail());
                        record.put("birthday", user.getBirthday());
                        record.put("avatar", user.getAvatar());
                    }
                }
                
                records.add(record);
            }
            
            resultPage.setRecords(records);
            return R.success(resultPage);
        } catch (Exception e) {
            log.error("查询员工列表异常", e);
            return R.error("查询员工列表失败");
        }
    }

    /**
     * 根据ID查询员工
     * @param id 员工ID
     * @return 员工信息
     */
    @GetMapping("/{id}")
    @RequireRoles({"admin"})
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据ID查询员工：{}", id);
        try {
            Employee employee = employeeMapper.selectById(id);
            return R.success(employee);
        } catch (Exception e) {
            log.error("查询员工详情异常", e);
            return R.error("查询员工详情失败");
        }
    }

    /**
     * 新增员工
     * @param employeeData 员工信息
     * @return 结果
     */
    @PostMapping
    @RequireRoles({"admin"})
    public R<String> save(@RequestBody Map<String, Object> employeeData) {
        log.info("新增员工：{}", employeeData);
        try {
            // 1. 创建用户
            User user = new User();
            user.setUsername((String) employeeData.get("username"));
            
            // 密码加密
            String password = (String) employeeData.get("password");
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            
            user.setName((String) employeeData.get("name"));
            user.setPhone((String) employeeData.get("phone"));
            user.setEmail((String) employeeData.get("email"));
            user.setSex((Integer) employeeData.get("sex"));
            if (employeeData.get("birthday") != null) {
                user.setBirthday((String) employeeData.get("birthday"));
            }
            user.setRole("employee");
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            userMapper.insert(user);
            
            // 2. 创建员工
            Employee employee = new Employee();
            employee.setUserId(user.getId());
            employee.setName((String) employeeData.get("name"));
            employee.setPhone((String) employeeData.get("phone"));
            employee.setSex((Integer) employeeData.get("sex"));
            employee.setIdNumber((String) employeeData.get("idNumber"));
            employee.setPosition((String) employeeData.get("position"));
            if (employeeData.get("salary") != null) {
                employee.setSalary(new BigDecimal(employeeData.get("salary").toString()));
            }
            if (employeeData.get("hireDate") != null) {
                String hireDateStr = (String) employeeData.get("hireDate");
                employee.setHireDate(parseDateTime(hireDateStr));
            }
            employee.setAddress((String) employeeData.get("address"));
            employee.setCreateTime(LocalDateTime.now());
            employee.setUpdateTime(LocalDateTime.now());
            
            employeeMapper.insert(employee);

            return R.success("新增成功");
        } catch (org.springframework.dao.DuplicateKeyException dupEx) {
            log.error("新增员工失败，用户名重复", dupEx);
            return R.error("用户名已存在，请更换");
        } catch (Exception e) {
            log.error("新增员工异常", e);
            return R.error("新增员工失败");
        }
    }

    /**
     * 修改员工
     * @param employeeData 员工信息
     * @return 结果
     */
    @PutMapping
    @RequireRoles({"admin"})
    public R<String> update(@RequestBody Map<String, Object> employeeData) {
        log.info("修改员工：{}", employeeData);
        try {
            Long employeeId = Long.valueOf(employeeData.get("id").toString());
            
            // 1. 获取员工信息
            Employee employee = employeeMapper.selectById(employeeId);
            if (employee == null) {
                return R.error("员工不存在");
            }
            
            // 2. 更新用户信息
            if (employee.getUserId() != null) {
                User user = userMapper.selectById(employee.getUserId());
                if (user != null) {
                    user.setName((String) employeeData.get("name"));
                    user.setPhone((String) employeeData.get("phone"));
                    user.setEmail((String) employeeData.get("email"));
                    user.setSex((Integer) employeeData.get("sex"));
                    if (employeeData.get("birthday") != null) {
                        user.setBirthday((String) employeeData.get("birthday"));
                    }
                    
                    // 如果有新密码，进行加密
                    String newPassword = (String) employeeData.get("password");
                    if (newPassword != null && !newPassword.trim().isEmpty()) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                    }
                    
                    user.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(user);
                }
            }
            
            // 3. 更新员工信息
            employee.setName((String) employeeData.get("name"));
            employee.setPhone((String) employeeData.get("phone"));
            employee.setSex((Integer) employeeData.get("sex"));
            employee.setIdNumber((String) employeeData.get("idNumber"));
            employee.setPosition((String) employeeData.get("position"));
            if (employeeData.get("salary") != null) {
                employee.setSalary(new BigDecimal(employeeData.get("salary").toString()));
            }
            if (employeeData.get("hireDate") != null) {
                String hireDateStr = (String) employeeData.get("hireDate");
                employee.setHireDate(parseDateTime(hireDateStr));
            }
            employee.setAddress((String) employeeData.get("address"));
            employee.setUpdateTime(LocalDateTime.now());
            
            employeeMapper.updateById(employee);
            
            return R.success("修改成功");
        } catch (Exception e) {
            log.error("修改员工异常", e);
            return R.error("修改员工失败：" + e.getMessage());
        }
    }

    /**
     * 删除员工
     * @param id 员工ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @RequireRoles({"admin"})
    public R<String> delete(@PathVariable Long id) {
        log.info("删除员工：{}", id);
        try {
            // 1. 获取员工信息
            Employee employee = employeeMapper.selectById(id);
            if (employee == null) {
                return R.error("员工不存在");
            }
            
            // 2. 删除员工
            employeeMapper.deleteById(id);
            
            // 3. 删除对应的用户
            if (employee.getUserId() != null) {
                userMapper.deleteById(employee.getUserId());
            }
            
            return R.success("删除成功");
        } catch (Exception e) {
            log.error("删除员工异常", e);
            return R.error("删除员工失败：" + e.getMessage());
        }
    }

    /**
     * 更新员工个人信息（完整版）
     * @param employeeProfileDTO 员工信息
     * @return 更新结果
     */
    @PutMapping("/profile")
    @RequireRoles({"employee", "admin"})
    public R<String> updateEmployeeProfile(@RequestBody EmployeeProfileDTO employeeProfileDTO) {
        try {
            Long userId = BaseContext.getCurrentId();
            if (!userId.equals(employeeProfileDTO.getId())) {
                return R.error("无权修改他人信息");
            }

            // 检查用户角色
            User user = userService.getById(userId);
            if (user == null) {
                return R.error("用户不存在");
            }

            if (!"employee".equals(user.getRole()) && !"admin".equals(user.getRole())) {
                return R.error("只有员工和管理员可以使用此接口");
            }

            // 判断是否为管理员
            boolean isAdmin = "admin".equals(user.getRole());

            boolean result = userService.updateEmployeeProfile(employeeProfileDTO, isAdmin);
            return result ? R.success("个人信息更新成功") : R.error("个人信息更新失败");
        } catch (Exception e) {
            log.error("更新员工个人信息失败", e);
            return R.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 管理员更新其他员工信息
     * @param employeeProfileDTO 员工信息
     * @return 更新结果
     */
    @PutMapping("/profile/admin")
    @RequireRoles({"admin"})
    public R<String> updateEmployeeByAdmin(@RequestBody EmployeeProfileDTO employeeProfileDTO) {
        try {
            Long currentUserId = BaseContext.getCurrentId();
            
            // 检查用户角色，只有管理员可以修改其他员工信息
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null) {
                return R.error("用户不存在");
            }

            if (!"admin".equals(currentUser.getRole())) {
                return R.error("只有管理员可以修改其他员工信息");
            }

            // 检查被修改的用户是否存在
            User targetUser = userService.getById(employeeProfileDTO.getId());
            if (targetUser == null) {
                return R.error("目标用户不存在");
            }

            if (!"employee".equals(targetUser.getRole()) && !"admin".equals(targetUser.getRole())) {
                return R.error("目标用户不是员工或管理员");
            }

            boolean result = userService.updateEmployeeProfile(employeeProfileDTO, true);
            return result ? R.success("员工信息更新成功") : R.error("员工信息更新失败");
        } catch (Exception e) {
            log.error("管理员更新员工信息失败", e);
            return R.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 获取员工完整信息（包含所有字段）
     * @return 员工完整信息
     */
    @GetMapping("/profile")
    @RequireRoles({"employee", "admin"})
    public R<EmployeeProfileDTO> getEmployeeProfile() {
        try {
            Long userId = BaseContext.getCurrentId();
            
            // 1. 获取用户信息
            User user = userService.getById(userId);
            if (user == null) {
                return R.error("用户不存在");
            }

            if (!"employee".equals(user.getRole()) && !"admin".equals(user.getRole())) {
                return R.error("只有员工和管理员可以使用此接口");
            }

            // 2. 获取员工信息
            LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Employee::getUserId, userId);
            Employee employee = employeeMapper.selectOne(queryWrapper);

            // 3. 组装返回信息
            EmployeeProfileDTO result = new EmployeeProfileDTO();
            result.setId(user.getId());
            result.setName(user.getName());
            result.setPhone(user.getPhone());
            result.setEmail(user.getEmail());
            result.setSex(user.getSex());
            result.setBirthday(user.getBirthday());
            result.setAvatar(user.getAvatar());
            result.setUsername(user.getUsername());

            if (employee != null) {
                result.setIdNumber(employee.getIdNumber());
                result.setAddress(employee.getAddress());
                result.setPosition(employee.getPosition());
                result.setSalary(employee.getSalary());
                result.setHireDate(employee.getHireDate());
            }

            return R.success(result);
        } catch (Exception e) {
            log.error("获取员工完整信息失败", e);
            return R.error("获取员工信息失败：" + e.getMessage());
        }
    }

    /**
     * 解析时间字符串，支持多种格式
     * @param dateTimeStr 时间字符串
     * @return LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        dateTimeStr = dateTimeStr.trim();
        
        try {
            // 尝试解析 "yyyy-MM-dd HH:mm:ss" 格式
            if (dateTimeStr.contains(" ")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return LocalDateTime.parse(dateTimeStr, formatter);
            }
            // 尝试解析 "yyyy-MM-dd" 格式
            else if (dateTimeStr.length() == 10) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(dateTimeStr, formatter).atStartOfDay();
            }
            // 默认尝试 ISO 格式
            else {
                return LocalDateTime.parse(dateTimeStr);
            }
        } catch (Exception e) {
            log.warn("无法解析时间字符串：{}", dateTimeStr, e);
            return null;
        }
    }
} 
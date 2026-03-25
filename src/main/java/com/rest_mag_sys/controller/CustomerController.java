package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.common.RequireRoles;
import com.rest_mag_sys.dto.CustomerProfileDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.dto.AddCustomerDTO;
import com.rest_mag_sys.dto.CustomerUpdateDTO;
import com.rest_mag_sys.entity.Customer;
import com.rest_mag_sys.entity.User;
import com.rest_mag_sys.mapper.CustomerMapper;
import com.rest_mag_sys.mapper.UserMapper;
import com.rest_mag_sys.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 顾客控制器
 */
@RestController
@RequestMapping("/customer")
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 分页查询顾客列表
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/list")
    @RequireRoles({"admin"})
    public R<Page<Map<String, Object>>> list(PageQueryDTO pageQueryDTO) {
        log.info("分页查询顾客，参数：{}", pageQueryDTO);
        Page<Customer> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(pageQueryDTO.getName())) {
            LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.like(User::getName, pageQueryDTO.getName());
            userQueryWrapper.eq(User::getRole, "customer");
            List<User> users = userMapper.selectList(userQueryWrapper);

            if (users.isEmpty()) {
                Page<Map<String, Object>> resultPage = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
                resultPage.setTotal(0);
                resultPage.setRecords(new ArrayList<>());
                return R.success(resultPage);
            }

            List<Long> userIds = users.stream().map(User::getId).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            queryWrapper.in(Customer::getUserId, userIds);
        }

        queryWrapper.orderByDesc(Customer::getCreateTime);
        page = customerMapper.selectPage(page, queryWrapper);

        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize());
        resultPage.setTotal(page.getTotal());

        List<Map<String, Object>> records = new ArrayList<>();
        for (Customer customer : page.getRecords()) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", customer.getId());
            record.put("userId", customer.getUserId());
            record.put("name", customer.getName());
            record.put("phone", customer.getPhone());
            record.put("sex", customer.getSex());
            record.put("address", customer.getAddress());
            record.put("points", customer.getPoints());
            record.put("memberLevel", customer.getMemberLevel());
            record.put("registerTime", customer.getRegisterTime());
            record.put("createTime", customer.getCreateTime());
            record.put("updateTime", customer.getUpdateTime());

            if (customer.getUserId() != null) {
                User user = userMapper.selectById(customer.getUserId());
                if (user != null) {
                    record.put("username", user.getUsername());
                    record.put("email", user.getEmail());
                    record.put("avatar", user.getAvatar());
                    record.put("status", user.getStatus());
                    record.put("birthday", user.getBirthday());
                }
            }

            records.add(record);
        }

        resultPage.setRecords(records);
        return R.success(resultPage);
    }

    /**
     * 分页查询顾客
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @RequireRoles({"admin"})
    public R<Page<Customer>> page(PageQueryDTO pageQueryDTO) {
        Page<Customer> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Customer::getCreateTime);

        Page<Customer> result = customerMapper.selectPage(page, queryWrapper);

        return R.success(result);
    }

    /**
     * 根据ID查询顾客
     * @param id 顾客ID
     * @return 顾客信息
     */
    @GetMapping("/{id}")
    @RequireRoles({"admin"})
    public R<Customer> getById(@PathVariable Long id) {
        Customer customer = customerMapper.selectById(id);
        return R.success(customer);
    }

    // 已删除原info接口，使用新的profile接口替代

    /**
     * 更新顾客个人信息（完整版）
     * @param customerProfileDTO 顾客信息
     * @return 更新结果
     */
    @PutMapping("/profile")
    @RequireRoles({"customer"})
    public R<String> updateCustomerProfile(@RequestBody CustomerProfileDTO customerProfileDTO) {
        Long userId = BaseContext.getCurrentId();
        if (!userId.equals(customerProfileDTO.getId())) {
            return R.error("无权修改他人信息");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return R.error("用户不存在");
        }

        if (!"customer".equals(user.getRole())) {
            return R.error("只有顾客可以使用此接口");
        }

        boolean result = userService.updateCustomerProfile(customerProfileDTO);
        return result ? R.success("个人信息更新成功") : R.error("个人信息更新失败");
    }

    /**
     * 获取顾客完整信息（包含所有字段）
     * @return 顾客完整信息
     */
    @GetMapping("/profile")
    @RequireRoles({"customer"})
    public R<CustomerProfileDTO> getCustomerProfile() {
        Long userId = BaseContext.getCurrentId();

        User user = userService.getById(userId);
        if (user == null) {
            return R.error("用户不存在");
        }

        if (!"customer".equals(user.getRole())) {
            return R.error("只有顾客可以使用此接口");
        }

        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getUserId, userId);
        Customer customer = customerMapper.selectOne(queryWrapper);

        CustomerProfileDTO result = new CustomerProfileDTO();
        result.setId(user.getId());
        result.setName(user.getName());
        result.setPhone(user.getPhone());
        result.setEmail(user.getEmail());
        result.setSex(user.getSex());
        result.setBirthday(user.getBirthday());
        result.setAvatar(user.getAvatar());
        result.setUsername(user.getUsername());

        if (customer != null) {
            result.setAddress(customer.getAddress());
            result.setPoints(customer.getPoints());
            result.setMemberLevel(customer.getMemberLevel());
            result.setRegisterTime(customer.getRegisterTime());
        }

        return R.success(result);
    }

    // 已删除原address接口，使用新的profile接口替代

    /**
     * 新增顾客
     * @param dto 顾客信息
     * @return 结果
     */
    @PostMapping
    @RequireRoles({"admin"})
    public R<String> save(@RequestBody AddCustomerDTO dto) {
        log.info("新增顾客：{}", dto);
        try {
            // 1. 创建用户
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setName(dto.getName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setSex(dto.getSex());
            user.setBirthday(dto.getBirthday());
            user.setRole("customer");
            user.setStatus(1);
            userMapper.insert(user);

            // 2. 创建顾客
            Customer customer = new Customer();
            customer.setUserId(user.getId());
            customer.setName(dto.getName());
            customer.setPhone(dto.getPhone());
            customer.setSex(dto.getSex());
            customer.setAddress(dto.getAddress());
            customer.setPoints(dto.getPoints() == null ? 0 : dto.getPoints());
            customer.setMemberLevel(dto.getMemberLevel() == null ? 0 : dto.getMemberLevel());
            customer.setRegisterTime(LocalDateTime.now());
            customerMapper.insert(customer);

            return R.success("新增顾客成功");
        } catch (org.springframework.dao.DuplicateKeyException dupEx) {
            log.error("新增顾客失败，用户名重复", dupEx);
            return R.error("用户名已存在，请更换");
        } catch (Exception e) {
            log.error("新增顾客失败", e);
            return R.error("新增顾客失败");
        }
    }

    /**
     * 修改顾客
     * @param dto 顾客信息
     * @return 结果
     */
    @PutMapping
    @RequireRoles({"admin"})
    public R<String> update(@RequestBody CustomerUpdateDTO dto) {
        log.info("修改顾客：{}", dto);

        if (dto.getId() != null) {
            Customer customer = new Customer();
            customer.setId(dto.getId());
            customer.setName(dto.getName());
            customer.setPhone(dto.getPhone());
            customer.setSex(dto.getSex());
            customer.setAddress(dto.getAddress());
            if (dto.getPoints() != null) {
                customer.setPoints(dto.getPoints());
            }
            if (dto.getMemberLevel() != null) {
                customer.setMemberLevel(dto.getMemberLevel());
            }
            customerMapper.updateById(customer);
        }

        if (dto.getUserId() != null) {
            User user = new User();
            user.setId(dto.getUserId());
            if (dto.getUsername() != null) {
                user.setUsername(dto.getUsername());
            }
            if (dto.getName() != null) {
                user.setName(dto.getName());
            }
            if (dto.getPhone() != null) {
                user.setPhone(dto.getPhone());
            }
            if (dto.getEmail() != null) {
                user.setEmail(dto.getEmail());
            }
            if (dto.getSex() != null) {
                user.setSex(dto.getSex());
            }
            if (dto.getBirthday() != null) {
                user.setBirthday(dto.getBirthday());
            }
            userMapper.updateById(user);
        }

        return R.success("修改成功");
    }

    /**
     * 删除顾客
     * @param id 顾客ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @RequireRoles({"admin"})
    public R<String> delete(@PathVariable Long id) {
        log.info("删除顾客：{}", id);
        customerMapper.deleteById(id);
        return R.success("删除成功");
    }

    /**
     * 更新顾客状态
     * @param customer 顾客信息
     * @return 结果
     */
    @PutMapping("/status")
    @RequireRoles({"admin"})
    public R<String> updateStatus(@RequestBody Customer customer) {
        log.info("更新顾客状态：{}", customer);
        customerMapper.updateById(customer);
        return R.success("状态更新成功");
    }
} 
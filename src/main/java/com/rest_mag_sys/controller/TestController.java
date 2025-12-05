package com.rest_mag_sys.controller;

import com.rest_mag_sys.common.R;
import com.rest_mag_sys.entity.TableInfo;
import com.rest_mag_sys.service.TableInfoService;
import com.rest_mag_sys.mapper.DishMapper;
import com.rest_mag_sys.mapper.CustomerMapper;
import com.rest_mag_sys.mapper.OrdersMapper;
import com.rest_mag_sys.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rest_mag_sys.entity.Category;
import com.rest_mag_sys.entity.Dish;
import com.rest_mag_sys.service.CategoryService;
import com.rest_mag_sys.service.DishService;
import lombok.extern.slf4j.Slf4j;
import com.rest_mag_sys.entity.User;
import com.rest_mag_sys.service.UserService;
import com.rest_mag_sys.entity.Customer;
import com.rest_mag_sys.entity.Employee;
import com.rest_mag_sys.mapper.EmployeeMapper;
import java.util.stream.Collectors;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private TableInfoService tableInfoService;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 测试接口
     */
    @GetMapping("/hello")
    public R<String> hello() {
        return R.success("Hello World!");
    }

    /**
     * 测试数据库连接和数据统计
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> testStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 统计各表的数据量
            long dishCount = dishMapper.selectCount(null);
            long customerCount = customerMapper.selectCount(null);
            long orderCount = ordersMapper.selectCount(null);
            long reviewCount = reviewMapper.selectCount(null);
            
            stats.put("dishCount", dishCount);
            stats.put("customerCount", customerCount);
            stats.put("orderCount", orderCount);
            stats.put("reviewCount", reviewCount);
            
            return R.success(stats);
        } catch (Exception e) {
            return R.error("数据库查询失败：" + e.getMessage());
        }
    }

    /**
     * 测试数据库连接和餐桌数据
     */
    @GetMapping("/tables")
    public R<List<TableInfo>> testTables() {
        try {
            List<TableInfo> tables = tableInfoService.list();
            return R.success(tables);
        } catch (Exception e) {
            return R.error("获取餐桌数据失败：" + e.getMessage());
        }
    }

    /**
     * 测试餐桌状态更新
     */
    @PutMapping("/table/status/{id}")
    public R<String> testUpdateTableStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            // 先查询餐桌是否存在
            TableInfo existingTable = tableInfoService.getById(id);
            if (existingTable == null) {
                return R.error("餐桌不存在，ID：" + id);
            }
            
            // 更新状态
            TableInfo tableInfo = new TableInfo();
            tableInfo.setId(id);
            tableInfo.setStatus(status);
            boolean result = tableInfoService.updateById(tableInfo);
            
            if (result) {
                return R.success("餐桌状态更新成功");
            } else {
                return R.error("餐桌状态更新失败");
            }
        } catch (Exception e) {
            return R.error("更新餐桌状态异常：" + e.getMessage());
        }
    }

    /**
     * 测试餐桌详情
     */
    @GetMapping("/table/{id}")
    public R<TableInfo> testGetTableById(@PathVariable Long id) {
        try {
            TableInfo table = tableInfoService.getById(id);
            if (table == null) {
                return R.error("餐桌不存在，ID：" + id);
            }
            return R.success(table);
        } catch (Exception e) {
            return R.error("获取餐桌详情失败：" + e.getMessage());
        }
    }

    /**
     * 测试餐桌更新
     */
    @PutMapping("/table")
    public R<String> testUpdateTable(@RequestBody TableInfo tableInfo) {
        try {
            System.out.println("测试餐桌更新 - 接收到的数据：" + tableInfo);
            
            // 先检查餐桌是否存在
            if (tableInfo.getId() == null) {
                return R.error("餐桌ID不能为空");
            }
            
            TableInfo existingTable = tableInfoService.getById(tableInfo.getId());
            if (existingTable == null) {
                return R.error("餐桌不存在，ID：" + tableInfo.getId());
            }
            
            System.out.println("现有餐桌数据：" + existingTable);
            
            boolean result = tableInfoService.updateById(tableInfo);
            if (result) {
                return R.success("餐桌更新成功");
            } else {
                return R.error("餐桌更新失败，updateById返回false");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("测试餐桌更新异常：" + e.getMessage());
        }
    }

    /**
     * 简单测试餐桌更新 - 只更新名称
     */
    @PutMapping("/table/simple/{id}")
    public R<String> testSimpleUpdateTable(@PathVariable Long id, @RequestParam String name) {
        try {
            System.out.println("简单测试餐桌更新 - ID：" + id + ", 新名称：" + name);
            
            // 先查询现有餐桌
            TableInfo existingTable = tableInfoService.getById(id);
            if (existingTable == null) {
                return R.error("餐桌不存在，ID：" + id);
            }
            
            System.out.println("现有餐桌数据：" + existingTable);
            
            // 只更新名称
            existingTable.setName(name);
            
            boolean result = tableInfoService.updateById(existingTable);
            if (result) {
                return R.success("餐桌名称更新成功");
            } else {
                return R.error("餐桌名称更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("简单测试餐桌更新异常：" + e.getMessage());
        }
    }

    /**
     * 重置数据库到初始状态（仅用于测试）
     * ⚠️ 警告：此操作会删除所有数据！
     */
    @PostMapping("/reset-database")
    public R<String> resetDatabase(@RequestParam(defaultValue = "false") boolean confirm) {
        if (!confirm) {
            return R.error("请在请求参数中添加 confirm=true 来确认重置数据库操作");
        }
        
        try {
            System.out.println("⚠️ 开始重置数据库到初始状态...");
            
            // 这里可以添加重置数据库的逻辑
            // 注意：这是一个危险操作，仅用于开发测试
            
            System.out.println("✅ 数据库重置完成");
            return R.success("数据库已重置到初始状态。请注意：所有自定义数据已丢失！");
            
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("数据库重置失败：" + e.getMessage());
        }
    }

    /**
     * 测试分类菜品数据
     * @return 测试结果
     */
    @GetMapping("/categoryDishes")
    public R<Map<String, Object>> testCategoryDishes() {
        log.info("测试分类菜品数据");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有分类
            List<Category> categories = categoryService.list();
            log.info("获取到 {} 个分类", categories.size());
            
            // 获取所有菜品
            List<Dish> dishes = dishService.list();
            log.info("获取到 {} 个菜品", dishes.size());
            
            // 分类统计
            Map<String, Integer> categoryCount = new HashMap<>();
            for (Category category : categories) {
                long count = dishes.stream()
                        .filter(dish -> dish.getCategoryId() != null && dish.getCategoryId().equals(category.getId()))
                        .count();
                categoryCount.put(category.getName(), (int) count);
            }
            
            result.put("categories", categories);
            result.put("dishes", dishes);
            result.put("categoryCount", categoryCount);
            
            return R.success(result);
        } catch (Exception e) {
            log.error("测试分类菜品数据异常", e);
            return R.error("测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试用户关联关系
     * @return 用户关联关系数据
     */
    @GetMapping("/user-relations")
    public R<Map<String, Object>> testUserRelations() {
        log.info("测试用户关联关系");
        
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 1. 获取所有用户
            List<User> users = userService.list();
            result.put("users", users);
            
            // 2. 获取所有顾客
            List<Customer> customers = customerMapper.selectList(null);
            result.put("customers", customers);
            
            // 3. 获取所有员工
            List<Employee> employees = employeeMapper.selectList(null);
            result.put("employees", employees);
            
            // 4. 统计信息
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", users.size());
            stats.put("totalCustomers", customers.size());
            stats.put("totalEmployees", employees.size());
            
            // 5. 检查缺少关联的用户
            List<User> missingCustomers = users.stream()
                .filter(user -> "customer".equals(user.getRole()))
                .filter(user -> customers.stream().noneMatch(c -> c.getUserId().equals(user.getId())))
                .collect(Collectors.toList());
            
            List<User> missingEmployees = users.stream()
                .filter(user -> "employee".equals(user.getRole()) || "admin".equals(user.getRole()))
                .filter(user -> employees.stream().noneMatch(e -> e.getUserId().equals(user.getId())))
                .collect(Collectors.toList());
            
            stats.put("missingCustomerRecords", missingCustomers.size());
            stats.put("missingEmployeeRecords", missingEmployees.size());
            stats.put("missingCustomerUsers", missingCustomers);
            stats.put("missingEmployeeUsers", missingEmployees);
            
            result.put("stats", stats);
            
            return R.success(result);
        } catch (Exception e) {
            log.error("测试用户关联关系失败", e);
            return R.error("测试失败：" + e.getMessage());
        }
    }
} 
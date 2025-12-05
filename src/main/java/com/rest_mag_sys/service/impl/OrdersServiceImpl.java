package com.rest_mag_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.CustomException;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.OrdersDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.*;
import com.rest_mag_sys.mapper.*;
import com.rest_mag_sys.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private TableInfoMapper tableInfoMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    /**
     * 分页查询订单
     * @param pageQueryDTO 分页查询参数
     * @return 订单分页结果
     */
    @Override
    public Page<OrdersDTO> pageQuery(PageQueryDTO pageQueryDTO) {
        try {
            // 构造分页对象
            Page<Orders> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
            
            // 构造查询条件
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            
            // 按订单ID或订单号(number)查询
            if (StringUtils.isNotBlank(pageQueryDTO.getKeyword())) {
                String kw = pageQueryDTO.getKeyword();
                queryWrapper.and(wrapper -> wrapper.like(Orders::getId, kw)
                        .or().like(Orders::getNumber, kw));
            }
            
            // 按时间范围查询
            if (StringUtils.isNotBlank(pageQueryDTO.getBeginTime()) && StringUtils.isNotBlank(pageQueryDTO.getEndTime())) {
                queryWrapper.between(Orders::getOrderTime, pageQueryDTO.getBeginTime(), pageQueryDTO.getEndTime());
            }
            
            // 按订单状态查询
            if (pageQueryDTO.getStatus() != null) {
                queryWrapper.eq(Orders::getStatus, pageQueryDTO.getStatus());
            }
            
            // 按时间降序排序
            queryWrapper.orderByDesc(Orders::getOrderTime);
            
            // 执行查询
            this.page(page, queryWrapper);
            
            // 构造返回结果
            Page<OrdersDTO> ordersDTOPage = new Page<>();
            BeanUtils.copyProperties(page, ordersDTOPage, "records");
            
            // 处理记录
            List<Orders> records = page.getRecords();
            List<OrdersDTO> list = records.stream().map(orders -> {
                OrdersDTO ordersDTO = new OrdersDTO();
                BeanUtils.copyProperties(orders, ordersDTO);
                
                // 设置顾客姓名
                if (orders.getCustomerId() != null) {
                    Customer customer = customerMapper.selectById(orders.getCustomerId());
                    if (customer != null) {
                        User user = userMapper.selectById(customer.getUserId());
                        if (user != null) {
                            ordersDTO.setCustomerName(user.getName());
                        }
                    }
                }
                
                // 设置餐桌名称
                if (orders.getTableId() != null) {
                    TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                    if (tableInfo != null) {
                        ordersDTO.setTableName(tableInfo.getName());
                    }
                }
                
                // 设置员工姓名
                if (orders.getEmployeeId() != null) {
                    Employee employee = employeeMapper.selectById(orders.getEmployeeId());
                    if (employee != null) {
                        User user = userMapper.selectById(employee.getUserId());
                        if (user != null) {
                            ordersDTO.setEmployeeName(user.getName());
                        }
                    }
                }
                
                return ordersDTO;
            }).collect(Collectors.toList());
            
            ordersDTOPage.setRecords(list);
            return ordersDTOPage;
        } catch (Exception e) {
            log.error("分页查询订单异常", e);
            throw e;
        }
    }

    /**
     * 根据ID查询订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @Override
    public OrdersDTO getOrderDetails(Long id) {
        log.info("查询订单详情，订单ID：{}", id);
        
        // 查询订单基本信息
        Orders orders = this.getById(id);
        log.info("查询结果：{}", orders);
        
        if (orders == null) {
            log.error("订单不存在，订单ID：{}", id);
            throw new CustomException("订单不存在");
        }
        
        // 构造返回结果
        OrdersDTO ordersDTO = new OrdersDTO();
        BeanUtils.copyProperties(orders, ordersDTO);
        
        // 查询订单详情
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);
        ordersDTO.setOrderDetails(orderDetails);
        
        // 设置顾客姓名
        if (orders.getCustomerId() != null) {
            Customer customer = customerMapper.selectById(orders.getCustomerId());
            if (customer != null) {
                User user = userMapper.selectById(customer.getUserId());
                if (user != null) {
                    ordersDTO.setCustomerName(user.getName());
                }
            }
        }
        
        // 设置餐桌名称
        if (orders.getTableId() != null) {
            TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
            if (tableInfo != null) {
                ordersDTO.setTableName(tableInfo.getName());
            }
        }
        
        // 设置员工姓名
        if (orders.getEmployeeId() != null) {
            Employee employee = employeeMapper.selectById(orders.getEmployeeId());
            if (employee != null) {
                User user = userMapper.selectById(employee.getUserId());
                if (user != null) {
                    ordersDTO.setEmployeeName(user.getName());
                }
            }
        }
        
        return ordersDTO;
    }

    /**
     * 提交订单
     * @param ordersDTO 订单信息
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean submit(OrdersDTO ordersDTO) {
        try {
            // 获取当前用户ID
            Long userId = BaseContext.getCurrentId();
            log.info("提交订单，当前用户ID：{}，订单信息：{}", userId, ordersDTO);
            
            if (userId == null) {
                log.error("提交订单失败，当前用户ID为null");
                throw new CustomException("用户认证失败");
            }
            
            // 查询顾客信息
            LambdaQueryWrapper<Customer> customerQueryWrapper = new LambdaQueryWrapper<>();
            customerQueryWrapper.eq(Customer::getUserId, userId);
            Customer customer = customerMapper.selectOne(customerQueryWrapper);
            log.info("根据用户ID查询顾客信息，用户ID：{}，查询结果：{}", userId, customer);
            
            if (customer == null) {
                log.warn("找不到对应的顾客记录，尝试创建新的顾客记录，用户ID：{}", userId);
                
                // 查询用户信息
                User user = userMapper.selectById(userId);
                if (user == null) {
                    log.error("用户不存在，用户ID：{}", userId);
                    throw new CustomException("用户不存在");
                }
                
                // 检查用户角色
                if (!"CUSTOMER".equals(user.getRole())) {
                    log.error("用户角色不是CUSTOMER，无法提交订单，用户ID：{}，角色：{}", userId, user.getRole());
                    throw new CustomException("只有顾客才能提交订单");
                }
                
                // 自动创建顾客记录
                customer = new Customer();
                customer.setUserId(userId);
                customer.setName(user.getName());
                customer.setPhone(user.getPhone());
                customer.setPoints(0);
                customer.setMemberLevel(0);
                customer.setRegisterTime(LocalDateTime.now());
                customerMapper.insert(customer);
                log.info("自动创建顾客记录成功，用户ID：{}，顾客ID：{}", userId, customer.getId());
            }
            
            log.info("找到顾客信息，顾客ID：{}，顾客姓名：{}", customer.getId(), customer.getName());
            
            // 判断餐桌是否可用
            if (ordersDTO.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(ordersDTO.getTableId());
                if (tableInfo == null) {
                    throw new CustomException("餐桌不存在");
                }
                if (tableInfo.getStatus() != 0) {
                    throw new CustomException("餐桌已被占用");
                }
                
                // 使用乐观锁更新餐桌状态，防止并发问题
                LambdaQueryWrapper<TableInfo> updateWrapper = new LambdaQueryWrapper<>();
                updateWrapper.eq(TableInfo::getId, ordersDTO.getTableId());
                updateWrapper.eq(TableInfo::getStatus, 0); // 确保当前状态为空闲
                
                TableInfo updateTable = new TableInfo();
                updateTable.setStatus(1); // 设为预订状态（客人下单但未接单）
                
                int updateCount = tableInfoMapper.update(updateTable, updateWrapper);
                if (updateCount == 0) {
                    throw new CustomException("餐桌已被占用");
                }
                
                log.info("餐桌状态已更新为预订状态，餐桌ID：{}", ordersDTO.getTableId());
            }
            
            // 构造订单对象
            Orders orders = new Orders();
            BeanUtils.copyProperties(ordersDTO, orders);
            
            // 生成唯一订单号
            String orderNumber = generateOrderNumber();
            orders.setNumber(orderNumber);
            
            orders.setCustomerId(customer.getId());
            orders.setStatus(0); // 待支付
            orders.setOrderTime(LocalDateTime.now());
            orders.setAmount(new BigDecimal(0)); // 初始化金额为0
            
            // 保存订单
            this.save(orders);
            
            // 处理前端传递的items数据，转换为orderDetails
            List<OrderDetail> orderDetails = ordersDTO.getOrderDetails();
            if ((orderDetails == null || orderDetails.isEmpty()) && ordersDTO.getItems() != null) {
                orderDetails = ordersDTO.getItems().stream().map(item -> {
                    OrderDetail detail = new OrderDetail();
                    detail.setDishId(Long.valueOf(item.get("dishId").toString()));
                    detail.setNumber(Integer.valueOf(item.get("quantity").toString()));
                    return detail;
                }).collect(Collectors.toList());
            }
            if (orderDetails != null && !orderDetails.isEmpty()) {
                // 计算订单总金额
                BigDecimal amount = new BigDecimal(0);
                
                for (OrderDetail orderDetail : orderDetails) {
                    // 查询菜品信息
                    Dish dish = dishMapper.selectById(orderDetail.getDishId());
                    if (dish == null) {
                        throw new CustomException("菜品不存在");
                    }
                    
                    // 设置订单详情信息
                    orderDetail.setOrderId(orders.getId());
                    orderDetail.setName(dish.getName()); // 设置菜品名称
                    orderDetail.setImage(dish.getImage()); // 设置菜品图片
                    orderDetail.setPrice(dish.getPrice());
                    orderDetail.setAmount(dish.getPrice().multiply(new BigDecimal(orderDetail.getNumber())));
                    
                    // 累加总金额
                    amount = amount.add(orderDetail.getAmount());
                    
                    // 保存订单详情
                    orderDetailMapper.insert(orderDetail);
                }
                
                // 更新订单总金额
                orders.setAmount(amount);
                this.updateById(orders);
            }
            
            return true;
        } catch (Exception e) {
            log.error("提交订单异常", e);
            throw e;
        }
    }

    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean payOrder(Long id, String paymentMethod) {
        try {
            log.info("支付订单，订单ID：{}，支付方式：{}", id, paymentMethod);
            
            // 查询订单
            Orders orders = this.getById(id);
            log.info("查询到的订单：{}", orders);
            
            if (orders == null) {
                log.error("支付失败，订单不存在，订单ID：{}", id);
                throw new CustomException("订单不存在");
            }

            // 检查订单状态
            if (orders.getStatus() != 0) {
                throw new CustomException("订单状态异常，无法支付");
            }

            // 更新订单状态
            orders.setStatus(1); // 已支付
            orders.setPaymentMethod(Integer.parseInt(paymentMethod));
            orders.setPaymentTime(LocalDateTime.now()); // 设置支付时间
            
            return this.updateById(orders);
        } catch (Exception e) {
            log.error("支付订单异常", e);
            throw e;
        }
    }

    /**
     * 完成订单
     * @param id 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean complete(Long id) {
        try {
            Orders orders = this.getById(id);
            if (orders == null) {
                throw new CustomException("订单不存在");
            }
            
            // 释放餐桌
            if (orders.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                if (tableInfo != null) {
                    tableInfo.setStatus(0); // 改为空闲状态
                    tableInfoMapper.updateById(tableInfo);
                }
            }
            
            orders.setStatus(3); // 已完成
            orders.setCompleteTime(LocalDateTime.now()); // 设置完成时间
            return this.updateById(orders);
        } catch (Exception e) {
            log.error("完成订单异常", e);
            throw e;
        }
    }

    /**
     * 取消订单
     * @param id 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean cancelOrder(Long id) {
        try {
            log.info("取消订单，订单ID：{}", id);
            
            // 查询订单
            Orders orders = this.getById(id);
            if (orders == null) {
                log.error("取消失败，订单不存在，订单ID：{}", id);
                throw new CustomException("订单不存在");
            }
            
            log.info("查询到的订单：ID={}, Status={}, Number={}", orders.getId(), orders.getStatus(), orders.getNumber());
            
            // 检查订单状态是否允许取消
            if (orders.getStatus() != 0 && orders.getStatus() != 1) {
                String statusText = getStatusText(orders.getStatus());
                log.error("取消失败，订单状态不允许取消，订单ID：{}，当前状态：{}", id, statusText);
                throw new CustomException("订单状态为" + statusText + "，无法取消");
            }
            
            // 释放餐桌
            if (orders.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                if (tableInfo != null) {
                    log.info("释放餐桌：{}", tableInfo.getName());
                    tableInfo.setStatus(0); // 改为空闲状态
                    tableInfoMapper.updateById(tableInfo);
                }
            }
            
            // 更新订单状态为已取消
            orders.setStatus(4); // 已取消
            boolean result = this.updateById(orders);
            
            if (result) {
                log.info("订单取消成功，订单ID：{}", id);
            } else {
                log.error("订单取消失败，数据库更新失败，订单ID：{}", id);
                throw new CustomException("数据库更新失败");
            }
            
            return result;
        } catch (CustomException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("取消订单异常，订单ID：{}", id, e);
            throw new CustomException("取消订单失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单状态文本
     * @param status 状态码
     * @return 状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知状态";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "制作中";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }

    /**
     * 根据用户ID查询订单
     * @param pageQueryDTO 分页查询参数
     * @param userId 用户ID
     * @return 订单分页结果
     */
    @Override
    public Page<OrdersDTO> userPageQuery(PageQueryDTO pageQueryDTO, Long userId) {
        try {
            log.info("用户分页查询订单，用户ID：{}，分页参数：{}", userId, pageQueryDTO);
            
            // 查询顾客信息
            LambdaQueryWrapper<Customer> customerQueryWrapper = new LambdaQueryWrapper<>();
            customerQueryWrapper.eq(Customer::getUserId, userId);
            Customer customer = customerMapper.selectOne(customerQueryWrapper);
            log.info("根据用户ID查询顾客信息，用户ID：{}，查询结果：{}", userId, customer);
            
            if (customer == null) {
                log.warn("找不到对应的顾客记录，尝试创建新的顾客记录，用户ID：{}", userId);
                
                // 查询用户信息
                User user = userMapper.selectById(userId);
                if (user == null) {
                    log.error("用户不存在，用户ID：{}", userId);
                    throw new CustomException("用户不存在");
                }
                
                // 检查用户角色（角色已统一为小写）
                if (!"customer".equals(user.getRole())) {
                    log.error("用户角色不是customer，无法查询订单，用户ID：{}，角色：{}", userId, user.getRole());
                    throw new CustomException("只有顾客才能查询订单");
                }
                
                // 自动创建顾客记录
                customer = new Customer();
                customer.setUserId(userId);
                customer.setName(user.getName());
                customer.setPhone(user.getPhone());
                customer.setPoints(0);
                customer.setMemberLevel(0);
                customer.setRegisterTime(LocalDateTime.now());
                customerMapper.insert(customer);
                log.info("自动创建顾客记录成功，用户ID：{}，顾客ID：{}", userId, customer.getId());
            }
            
            log.info("找到顾客信息，顾客ID：{}，顾客姓名：{}", customer.getId(), customer.getName());
            
            // 构造分页对象
            Page<Orders> page = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
            
            // 构造查询条件
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Orders::getCustomerId, customer.getId());
            
            // 添加状态查询条件
            if (pageQueryDTO.getStatus() != null) {
                queryWrapper.eq(Orders::getStatus, pageQueryDTO.getStatus());
            }
            
            // 添加时间范围查询条件
            if (StringUtils.isNotBlank(pageQueryDTO.getBeginTime())) {
                queryWrapper.ge(Orders::getOrderTime, pageQueryDTO.getBeginTime());
            }
            if (StringUtils.isNotBlank(pageQueryDTO.getEndTime())) {
                queryWrapper.le(Orders::getOrderTime, pageQueryDTO.getEndTime());
            }
            
            // 按时间降序排序
            queryWrapper.orderByDesc(Orders::getOrderTime);
            
            log.info("查询订单，顾客ID：{}", customer.getId());
            
            // 执行查询
            this.page(page, queryWrapper);
            
            log.info("查询到订单数量：{}", page.getRecords().size());
            
            // 构造返回结果
            Page<OrdersDTO> ordersDTOPage = new Page<>();
            BeanUtils.copyProperties(page, ordersDTOPage, "records");
            
            // 处理记录
            List<Orders> records = page.getRecords();
            List<OrdersDTO> list = records.stream().map(orders -> {
                OrdersDTO ordersDTO = new OrdersDTO();
                BeanUtils.copyProperties(orders, ordersDTO);
                
                // 查询餐桌信息
                if (orders.getTableId() != null) {
                    TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                    if (tableInfo != null) {
                        ordersDTO.setTableName(tableInfo.getName());
                    }
                }
                
                // 查询订单是否已评价
                LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
                reviewWrapper.eq(Review::getOrderId, orders.getId());
                Long reviewCount = reviewMapper.selectCount(reviewWrapper);
                ordersDTO.setReviewed(reviewCount != null && reviewCount > 0);
                
                // 查询订单明细
                LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
                detailQueryWrapper.eq(OrderDetail::getOrderId, orders.getId());
                List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailQueryWrapper);
                ordersDTO.setOrderDetails(orderDetails);
                
                return ordersDTO;
            }).collect(Collectors.toList());
            
            ordersDTOPage.setRecords(list);
            log.info("返回订单DTO数量：{}", list.size());
            return ordersDTOPage;
        } catch (Exception e) {
            log.error("用户分页查询订单异常，用户ID：{}", userId, e);
            throw e;
        }
    }

    /**
     * 接单（开始制作）
     * @param id 订单ID
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean accept(Long id) {
        try {
            log.info("接单处理开始，订单ID：{}", id);
            
            Orders orders = this.getById(id);
            if (orders == null) {
                throw new CustomException("订单不存在");
            }
            
            log.info("找到订单信息，订单ID：{}，当前状态：{}，餐桌ID：{}", 
                    orders.getId(), orders.getStatus(), orders.getTableId());
            
            // 检查订单状态是否允许接单
            if (orders.getStatus() != 1) { // 只有已支付状态才能接单
                throw new CustomException("订单状态不允许接单");
            }
            
            // 更新餐桌状态为就餐中
            if (orders.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                if (tableInfo != null) {
                    log.info("更新餐桌状态：{} 从 {} 到就餐中", tableInfo.getName(), 
                            tableInfo.getStatus() == 0 ? "空闲" : 
                            tableInfo.getStatus() == 1 ? "预订" : "就餐中");
                    
                    tableInfo.setStatus(2); // 设为就餐中状态
                    tableInfoMapper.updateById(tableInfo);
                    
                    log.info("餐桌状态已更新为就餐中，餐桌ID：{}，餐桌名称：{}", 
                            tableInfo.getId(), tableInfo.getName());
                } else {
                    log.warn("找不到对应的餐桌信息，餐桌ID：{}", orders.getTableId());
                }
            }
            
            // 更新订单状态为制作中
            orders.setStatus(2); // 制作中
            orders.setEmployeeId(BaseContext.getCurrentId());
            boolean result = this.updateById(orders);
            
            if (result) {
                log.info("接单成功，订单ID：{}，餐桌状态已更新为就餐中", id);
            } else {
                log.error("接单失败，订单更新失败，订单ID：{}", id);
            }
            
            return result;
        } catch (Exception e) {
            log.error("接单异常，订单ID：{}", id, e);
            throw e;
        }
    }

    /**
     * 拒单
     * @param id 订单ID
     * @param reason 拒单原因
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean reject(Long id, String reason) {
        try {
            log.info("拒单处理开始，订单ID：{}，拒单原因：{}", id, reason);
            
            Orders orders = this.getById(id);
            if (orders == null) {
                throw new CustomException("订单不存在");
            }
            
            log.info("找到订单信息，订单ID：{}，当前状态：{}，餐桌ID：{}", 
                    orders.getId(), orders.getStatus(), orders.getTableId());
            
            // 释放餐桌
            if (orders.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                if (tableInfo != null) {
                    log.info("释放餐桌：{} 从 {} 到空闲", tableInfo.getName(), 
                            tableInfo.getStatus() == 0 ? "空闲" : 
                            tableInfo.getStatus() == 1 ? "预订" : "就餐中");
                    
                    tableInfo.setStatus(0); // 改为空闲状态
                    tableInfoMapper.updateById(tableInfo);
                    
                    log.info("餐桌状态已更新为空闲，餐桌ID：{}，餐桌名称：{}", 
                            tableInfo.getId(), tableInfo.getName());
                } else {
                    log.warn("找不到对应的餐桌信息，餐桌ID：{}", orders.getTableId());
                }
            }
            
            // 更新订单状态为已取消
            orders.setStatus(4); // 已取消
            orders.setRemark(reason);
            boolean result = this.updateById(orders);
            
            if (result) {
                log.info("拒单成功，订单ID：{}，餐桌状态已更新为空闲", id);
            } else {
                log.error("拒单失败，订单更新失败，订单ID：{}", id);
            }
            
            return result;
        } catch (Exception e) {
            log.error("拒单异常，订单ID：{}", id, e);
            throw e;
        }
    }

    /**
     * 分页查询订单
     * @param page 页码
     * @param pageSize 每页记录数
     * @param number 订单号
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String number, String beginTime, String endTime) {
        // 创建分页对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        // 根据订单号模糊查询
        queryWrapper.like(StringUtils.isNotBlank(number), Orders::getNumber, number);
        // 根据时间范围查询
        if (StringUtils.isNotBlank(beginTime) && StringUtils.isNotBlank(endTime)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            queryWrapper.between(Orders::getOrderTime, 
                    LocalDateTime.parse(beginTime + " 00:00:00", formatter),
                    LocalDateTime.parse(endTime + " 23:59:59", formatter));
        }
        // 按下单时间降序排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        
        // 执行查询
        this.page(pageInfo, queryWrapper);
        
        // 处理查询结果，添加用户名和桌位名称
        List<OrdersDTO> ordersDTOList = pageInfo.getRecords().stream().map(orders -> {
            OrdersDTO ordersDTO = new OrdersDTO();
            BeanUtils.copyProperties(orders, ordersDTO);
            
            // 查询用户信息
            User user = userMapper.selectById(orders.getUserId());
            if (user != null) {
                ordersDTO.setUsername(user.getUsername());
            }
            
            // 查询桌位信息
            if (orders.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                if (tableInfo != null) {
                    ordersDTO.setTableName(tableInfo.getName());
                }
            }
            
            return ordersDTO;
        }).collect(Collectors.toList());
        
        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("records", ordersDTOList);
        
        return R.success(result);
    }

    /**
     * 查询用户订单
     * @param page 页码
     * @param pageSize 每页记录数
     * @param userId 用户ID
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getUserPageList(Integer page, Integer pageSize, Long userId) {
        // 创建分页对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        
        // 执行查询
        this.page(pageInfo, queryWrapper);
        
        // 处理查询结果，添加桌位名称和订单明细
        List<OrdersDTO> ordersDTOList = pageInfo.getRecords().stream().map(orders -> {
            OrdersDTO ordersDTO = new OrdersDTO();
            BeanUtils.copyProperties(orders, ordersDTO);
            
            // 查询桌位信息
            if (orders.getTableId() != null) {
                TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
                if (tableInfo != null) {
                    ordersDTO.setTableName(tableInfo.getName());
                }
            }
            
            // 查询订单明细
            LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
            detailQueryWrapper.eq(OrderDetail::getOrderId, orders.getId());
            List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailQueryWrapper);
            ordersDTO.setOrderDetails(orderDetails);
            
            return ordersDTO;
        }).collect(Collectors.toList());
        
        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("records", ordersDTOList);
        
        return R.success(result);
    }

    /**
     * 查询订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @Override
    public OrdersDTO getOrderDetail(Long id) {
        // 查询订单基本信息
        Orders orders = this.getById(id);
        if (orders == null) {
            throw new CustomException("订单不存在");
        }
        
        // 封装订单DTO
        OrdersDTO ordersDTO = new OrdersDTO();
        BeanUtils.copyProperties(orders, ordersDTO);
        
        // 查询用户信息
        User user = userMapper.selectById(orders.getUserId());
        if (user != null) {
            ordersDTO.setUsername(user.getUsername());
        }
        
        // 查询桌位信息
        if (orders.getTableId() != null) {
            TableInfo tableInfo = tableInfoMapper.selectById(orders.getTableId());
            if (tableInfo != null) {
                ordersDTO.setTableName(tableInfo.getName());
            }
        }
        
        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);
        ordersDTO.setOrderDetails(orderDetails);
        
        return ordersDTO;
    }

    /**
     * 再来一单
     * @param id 订单ID
     */
    @Override
    @Transactional
    public void againOrder(Long id) {
        // 获取当前用户ID
        Long userId = BaseContext.getCurrentId();
        
        // 查询原订单
        OrdersDTO ordersDTO = this.getOrderDetail(id);
        
        // 生成新订单号
        String orderNumber = generateOrderNumber();
        
        // 设置新订单信息
        Orders orders = new Orders();
        orders.setNumber(orderNumber);
        orders.setUserId(userId);
        orders.setStatus(1); // 待付款状态
        orders.setOrderTime(LocalDateTime.now());
        orders.setAmount(ordersDTO.getAmount());
        orders.setRemark(ordersDTO.getRemark());
        orders.setAddress(ordersDTO.getAddress());
        orders.setPhone(ordersDTO.getPhone());
        orders.setConsignee(ordersDTO.getConsignee());
        
        // 保存新订单
        this.save(orders);
        
        // 保存订单明细
        List<OrderDetail> orderDetails = ordersDTO.getOrderDetails();
        if (orderDetails != null && !orderDetails.isEmpty()) {
            orderDetails.forEach(detail -> {
                OrderDetail newDetail = new OrderDetail();
                newDetail.setOrderId(orders.getId());
                newDetail.setDishId(detail.getDishId());
                newDetail.setName(detail.getName());
                newDetail.setPrice(detail.getPrice());
                newDetail.setNumber(detail.getNumber());
                newDetail.setAmount(detail.getAmount());
                orderDetailMapper.insert(newDetail);
            });
        }
    }

    /**
     * 获取订单统计数据
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 统计各状态订单数量
            LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
            List<Orders> list = this.list(queryWrapper);
            
            log.info("获取到总订单数：{}", list.size());
            
            // 待支付 (status = 0)
            int waitingPayment = (int) list.stream().filter(order -> order.getStatus() == 0).count();
            // 待确认订单 - 已支付等待接单 (status = 1) 
            int pendingConfirm = (int) list.stream().filter(order -> order.getStatus() == 1).count();
            // 处理中订单 - 制作中 (status = 2)
            int processing = (int) list.stream().filter(order -> order.getStatus() == 2).count();
            // 已完成 (status = 3)
            int completed = (int) list.stream().filter(order -> order.getStatus() == 3).count();
            // 已取消 (status = 4)
            int cancelled = (int) list.stream().filter(order -> order.getStatus() == 4).count();
            
            result.put("waitingPayment", waitingPayment);      // 待支付
            result.put("pendingConfirm", pendingConfirm);      // 待确认
            result.put("processing", processing);              // 处理中
            result.put("completed", completed);                // 已完成
            result.put("cancelled", cancelled);                // 已取消
            result.put("total", list.size());                  // 总数
            
            log.info("订单统计结果：待支付={}, 待确认={}, 处理中={}, 已完成={}, 已取消={}, 总数={}", 
                    waitingPayment, pendingConfirm, processing, completed, cancelled, list.size());
            
            return result;
        } catch (Exception e) {
            log.error("获取订单统计数据异常", e);
            throw e;
        }
    }

    /**
     * 生成唯一订单号
     * @return 订单号
     */
    private String generateOrderNumber() {
        // 使用当前时间戳 + 随机数生成订单号
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStamp = LocalDateTime.now().format(formatter);
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timeStamp + randomNum;
    }


} 
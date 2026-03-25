package com.rest_mag_sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.dto.ReviewDTO;
import com.rest_mag_sys.entity.*;
import com.rest_mag_sys.mapper.*;
import com.rest_mag_sys.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价服务实现类
 */
@Service
@Slf4j
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 分页查询评论
     * @param pageQueryDTO 分页查询参数
     * @return 评论分页结果
     */
    @Override
    public Page<Review> pageQuery(PageQueryDTO pageQueryDTO) {
        // 构造分页对象
        Page<Review> pageInfo = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        
        // 构造条件查询对象
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasLength(pageQueryDTO.getKeyword())) {
            queryWrapper.like(Review::getContent, pageQueryDTO.getKeyword());
        }
        
        // 添加排序条件
        queryWrapper.orderByDesc(Review::getCreateTime);
        
        // 执行查询
        return page(pageInfo, queryWrapper);
    }

    /**
     * 分页查询评价列表（带关联信息）
     * @param pageQueryDTO 分页查询参数
     * @return 评价分页结果
     */
    @Override
    public Page<ReviewDTO> pageQueryWithDetails(PageQueryDTO pageQueryDTO) {
        log.info("分页查询评价列表，参数：{}", pageQueryDTO);
        
        // 构造分页对象
        Page<Review> pageInfo = new Page<>(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        
        // 构造条件查询对象
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasLength(pageQueryDTO.getKeyword())) {
            queryWrapper.like(Review::getContent, pageQueryDTO.getKeyword());
        }
        // 评分过滤
        if (pageQueryDTO.getRating() != null) {
            queryWrapper.eq(Review::getRating, pageQueryDTO.getRating());
        }
        // 时间范围过滤
        if (org.apache.commons.lang3.StringUtils.isNotBlank(pageQueryDTO.getBeginTime()) &&
            org.apache.commons.lang3.StringUtils.isNotBlank(pageQueryDTO.getEndTime())) {
            queryWrapper.between(Review::getCreateTime, pageQueryDTO.getBeginTime(), pageQueryDTO.getEndTime());
        }
        
        // 添加排序条件
        queryWrapper.orderByDesc(Review::getCreateTime);
        
        // 执行查询
        page(pageInfo, queryWrapper);
        
        // 转换为ReviewDTO
        Page<ReviewDTO> reviewDTOPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, reviewDTOPage, "records");
        
        // 处理记录，添加关联信息
        List<ReviewDTO> reviewDTOList = pageInfo.getRecords().stream().map(review -> {
            ReviewDTO reviewDTO = new ReviewDTO();
            BeanUtils.copyProperties(review, reviewDTO);
            
            // 设置回复时间
            reviewDTO.setReplyTime(review.getReplyTime());
            
            // 查询订单信息
            Orders orders = ordersMapper.selectById(review.getOrderId());
            if (orders != null) {
                reviewDTO.setOrderNumber(orders.getNumber());
                
                // 查询顾客信息
                if (orders.getCustomerId() != null) {
                    Customer customer = customerMapper.selectById(orders.getCustomerId());
                    if (customer != null) {
                        reviewDTO.setCustomerName(customer.getName());
                    }
                }
                
                // 如果没有顾客信息，尝试通过用户ID查询
                if (reviewDTO.getCustomerName() == null) {
                    User user = userMapper.selectById(review.getUserId());
                    if (user != null) {
                        reviewDTO.setCustomerName(user.getName());
                    }
                }
                
                // 查询菜品信息
                LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
                detailQueryWrapper.eq(OrderDetail::getOrderId, review.getOrderId());
                List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailQueryWrapper);
                
                if (!orderDetails.isEmpty()) {
                    // 获取所有菜品名称，用逗号分隔
                    String dishNames = orderDetails.stream()
                            .map(OrderDetail::getName)
                            .collect(Collectors.joining(", "));
                    reviewDTO.setDishName(dishNames);
                }
            }
            
            return reviewDTO;
        }).collect(Collectors.toList());
        
        reviewDTOPage.setRecords(reviewDTOList);
        
        log.info("查询到评价记录数量：{}", reviewDTOList.size());
        return reviewDTOPage;
    }

    /**
     * 根据订单ID查询评论
     * @param orderId 订单ID
     * @return 评论列表
     */
    @Override
    public List<Review> getByOrderId(Long orderId) {
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getOrderId, orderId);
        queryWrapper.orderByDesc(Review::getCreateTime);
        return list(queryWrapper);
    }

    /**
     * 分页查询评价列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param orderId 订单ID
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getPageList(Integer page, Integer pageSize, Long orderId) {
        // 构造分页对象
        Page<Review> pageInfo = new Page<>(page, pageSize);
        
        // 构造条件查询对象
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (orderId != null) {
            queryWrapper.eq(Review::getOrderId, orderId);
        }
        
        // 添加排序条件
        queryWrapper.orderByDesc(Review::getCreateTime);
        
        // 执行查询
        page(pageInfo, queryWrapper);
        
        // 构造返回结果
        Map<String, Object> map = new HashMap<>();
        map.put("total", pageInfo.getTotal());
        map.put("records", pageInfo.getRecords());
        
        return R.success(map);
    }

    /**
     * 查询用户评价列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param userId 用户ID
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getUserPageList(Integer page, Integer pageSize, Long userId) {
        return getUserPageList(page, pageSize, userId, null, null);
    }

    /**
     * 查询用户评价列表（支持时间范围查询）
     * @param page 页码
     * @param pageSize 每页记录数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @Override
    public R<Map<String, Object>> getUserPageList(Integer page, Integer pageSize, Long userId, String startDate, String endDate) {
        // 创建分页对象
        Page<Review> pageInfo = new Page<>(page, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Review> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Review::getUserId, userId);
        
        // 添加时间范围查询条件
        if (StringUtils.hasLength(startDate)) {
            queryWrapper.ge(Review::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasLength(endDate)) {
            queryWrapper.le(Review::getCreateTime, endDate + " 23:59:59");
        }
        
        queryWrapper.orderByDesc(Review::getCreateTime);

        // 执行查询
        this.page(pageInfo, queryWrapper);

        // 处理查询结果，添加订单信息
        List<ReviewDTO> reviewDTOList = pageInfo.getRecords().stream().map(review -> {
            ReviewDTO reviewDTO = new ReviewDTO();
            BeanUtils.copyProperties(review, reviewDTO);
            
            // 设置回复时间
            reviewDTO.setReplyTime(review.getReplyTime());
            
            // 查询订单信息
            Orders orders = ordersMapper.selectById(review.getOrderId());
            if (orders != null) {
                reviewDTO.setOrderNumber(orders.getNumber());
                reviewDTO.setOrderTime(orders.getOrderTime());
                
                // 查询菜品信息
                LambdaQueryWrapper<OrderDetail> detailQueryWrapper = new LambdaQueryWrapper<>();
                detailQueryWrapper.eq(OrderDetail::getOrderId, review.getOrderId());
                List<OrderDetail> orderDetails = orderDetailMapper.selectList(detailQueryWrapper);
                
                if (!orderDetails.isEmpty()) {
                    // 获取所有菜品名称，用逗号分隔
                    String dishNames = orderDetails.stream()
                            .map(OrderDetail::getName)
                            .collect(Collectors.joining(", "));
                    reviewDTO.setDishName(dishNames);
                }
            }
            
            return reviewDTO;
        }).collect(Collectors.toList());

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        result.put("records", reviewDTOList);

        return R.success(result);
    }
} 
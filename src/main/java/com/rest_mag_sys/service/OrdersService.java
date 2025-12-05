package com.rest_mag_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.OrdersDTO;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Orders;

import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrdersService extends IService<Orders> {

    /**
     * 分页查询订单
     * @param pageQueryDTO 分页查询参数
     * @return 订单分页结果
     */
    Page<OrdersDTO> pageQuery(PageQueryDTO pageQueryDTO);
    
    /**
     * 根据ID查询订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrdersDTO getOrderDetails(Long id);
    
    /**
     * 提交订单
     * @param ordersDTO 订单信息
     * @return 是否成功
     */
    boolean submit(OrdersDTO ordersDTO);
    
    /**
     * 支付订单
     * @param id 订单ID
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    boolean payOrder(Long id, String paymentMethod);
    
    /**
     * 完成订单
     * @param id 订单ID
     * @return 是否成功
     */
    boolean complete(Long id);
    
    /**
     * 取消订单
     * @param id 订单ID
     * @return 是否成功
     */
    boolean cancelOrder(Long id);
    
    /**
     * 根据用户ID分页查询订单
     * @param pageQueryDTO 分页查询参数
     * @param userId 用户ID
     * @return 订单分页结果
     */
    Page<OrdersDTO> userPageQuery(PageQueryDTO pageQueryDTO, Long userId);

    /**
     * 分页查询订单
     * @param page 页码
     * @param pageSize 每页记录数
     * @param number 订单号
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    R<Map<String, Object>> getPageList(Integer page, Integer pageSize, String number, String beginTime, String endTime);

    /**
     * 查询用户订单
     * @param page 页码
     * @param pageSize 每页记录数
     * @param userId 用户ID
     * @return 分页结果
     */
    R<Map<String, Object>> getUserPageList(Integer page, Integer pageSize, Long userId);

    /**
     * 查询订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrdersDTO getOrderDetail(Long id);

    /**
     * 再来一单
     * @param id 订单ID
     */
    void againOrder(Long id);

    /**
     * 获取订单统计数据
     * @return 统计数据
     */
    Map<String, Object> getStatistics();
    
    /**
     * 接单
     * @param id 订单ID
     * @return 是否成功
     */
    boolean accept(Long id);
    
        /**
     * 拒单
     * @param id 订单ID
     * @param reason 拒单原因
     * @return 是否成功
     */
    boolean reject(Long id, String reason);
} 
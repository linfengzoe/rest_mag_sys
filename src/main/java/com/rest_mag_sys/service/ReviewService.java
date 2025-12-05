package com.rest_mag_sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.entity.Review;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 */
public interface ReviewService extends IService<Review> {

    /**
     * 分页查询评论
     * @param pageQueryDTO 分页查询参数
     * @return 评论分页结果
     */
    Page<Review> pageQuery(PageQueryDTO pageQueryDTO);

    /**
     * 根据订单ID查询评论
     * @param orderId 订单ID
     * @return 评论列表
     */
    List<Review> getByOrderId(Long orderId);

    /**
     * 分页查询评论
     * @param page 页码
     * @param pageSize 每页记录数
     * @param orderId 订单ID
     * @return 分页结果
     */
    R<Map<String, Object>> getPageList(Integer page, Integer pageSize, Long orderId);

    /**
     * 查询用户评价列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param userId 用户ID
     * @return 分页结果
     */
    R<Map<String, Object>> getUserPageList(Integer page, Integer pageSize, Long userId);

    /**
     * 查询用户评价列表（支持时间范围查询）
     * @param page 页码
     * @param pageSize 每页记录数
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    R<Map<String, Object>> getUserPageList(Integer page, Integer pageSize, Long userId, String startDate, String endDate);
} 
package com.rest_mag_sys.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评价DTO
 */
@Data
public class ReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 下单时间
     */
    private LocalDateTime orderTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 顾客姓名
     */
    private String customerName;

    /**
     * 菜品名称（多个菜品用逗号分隔）
     */
    private String dishName;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 评分(1-5)
     */
    private Integer rating;

    /**
     * 回复内容
     */
    private String reply;

    /**
     * 回复时间
     */
    private LocalDateTime replyTime;

    /**
     * 状态：0-待回复，1-已回复
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 
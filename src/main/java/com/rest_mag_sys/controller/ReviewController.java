package com.rest_mag_sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rest_mag_sys.common.BaseContext;
import com.rest_mag_sys.common.R;
import com.rest_mag_sys.common.RequireRoles;
import com.rest_mag_sys.dto.PageQueryDTO;
import com.rest_mag_sys.dto.ReviewDTO;
import com.rest_mag_sys.entity.Review;
import com.rest_mag_sys.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评价控制器
 */
@RestController
@RequestMapping("/review")
@Slf4j
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 提交评价
     * @param review 评价信息
     * @return 结果
     */
    @PostMapping
    @RequireRoles({"customer"})
    public R<String> save(@RequestBody Review review) {
        log.info("提交评价：{}", review);
        Long userId = BaseContext.getCurrentId();
        review.setUserId(userId);
        boolean result = reviewService.save(review);
        return result ? R.success("评价成功") : R.error("评价失败");
    }

    /**
     * 分页查询
     * @param pageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @RequireRoles({"admin", "employee"})
    public R<Page<ReviewDTO>> page(PageQueryDTO pageQueryDTO) {
        log.info("分页查询评价，参数：{}", pageQueryDTO);
        Page<ReviewDTO> page = reviewService.pageQueryWithDetails(pageQueryDTO);
        return R.success(page);
    }

    /**
     * 查询用户评价
     * @param page 页码
     * @param pageSize 每页记录数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/userPage")
    @RequireRoles({"customer"})
    public R<Map<String, Object>> userPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("查询用户评价，页码：{}，每页记录数：{}，开始日期：{}，结束日期：{}", page, pageSize, startDate, endDate);
        Long userId = BaseContext.getCurrentId();
        return reviewService.getUserPageList(page, pageSize, userId, startDate, endDate);
    }

    /**
     * 根据ID查询
     * @param id 评价ID
     * @return 评价信息
     */
    @GetMapping("/{id}")
    @RequireRoles({"admin", "employee"})
    public R<Review> getById(@PathVariable Long id) {
        log.info("根据ID查询评价：{}", id);
        Review review = reviewService.getById(id);
        return R.success(review);
    }

    /**
     * 更新评价
     * @param review 评价信息
     * @return 结果
     */
    @PutMapping
    @RequireRoles({"customer"})
    public R<String> update(@RequestBody Review review) {
        log.info("更新评价：{}", review.getId());
        try {
            // 验证评价是否属于当前用户
            Long currentUserId = BaseContext.getCurrentId();
            Review existingReview = reviewService.getById(review.getId());
            if (existingReview == null) {
                return R.error("评价不存在");
            }
            if (!existingReview.getUserId().equals(currentUserId)) {
                return R.error("无权限修改此评价");
            }
            
            // 更新评价
            review.setUserId(currentUserId);
            boolean result = reviewService.updateById(review);
            return result ? R.success("更新成功") : R.error("更新失败");
        } catch (Exception e) {
            log.error("更新评价异常", e);
            return R.error("更新评价失败");
        }
    }

    /**
     * 回复评价
     * @param review 评价信息
     * @return 结果
     */
    @PutMapping("/reply")
    @RequireRoles({"admin", "employee"})
    public R<String> reply(@RequestBody Review review) {
        log.info("回复评价：{}", review.getId());
        try {
            // 检查评价是否存在
            Review existingReview = reviewService.getById(review.getId());
            if (existingReview == null) {
                return R.error("评价不存在");
            }
            
            // 只更新回复内容、回复时间和状态
            existingReview.setReply(review.getReply());
            existingReview.setReplyTime(java.time.LocalDateTime.now()); // 设置回复时间
            existingReview.setStatus(1); // 设置为已回复状态
            
            boolean result = reviewService.updateById(existingReview);
            return result ? R.success("回复成功") : R.error("回复失败");
        } catch (Exception e) {
            log.error("回复评价异常", e);
            return R.error("回复失败");
        }
    }

    /**
     * 删除评价
     * @param id 评价ID
     * @return 结果
     */
    @DeleteMapping("/{id}")
    @RequireRoles({"admin", "employee"})
    public R<String> delete(@PathVariable Long id) {
        log.info("删除评价：{}", id);
        boolean result = reviewService.removeById(id);
        return result ? R.success("删除成功") : R.error("删除失败");
    }

    /**
     * 根据订单ID查询评论
     * @param orderId 订单ID
     * @return 评论列表
     */
    @GetMapping("/order/{orderId}")
    @RequireRoles({"admin", "employee", "customer"})
    public R<List<Review>> getByOrderId(@PathVariable Long orderId) {
        log.info("根据订单ID查询评论，订单ID：{}", orderId);
        List<Review> list = reviewService.getByOrderId(orderId);
        return R.success(list);
    }

    /**
     * 查询评价列表（用于前端getReviewList API）
     * @param page 页码
     * @param pageSize 每页记录数
     * @param content 内容
     * @param rating 评分
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    @RequireRoles({"admin", "employee"})
    public R<Page<ReviewDTO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("查询评价列表，页码：{}，每页记录数：{}，内容：{}，评分：{}，开始日期：{}，结束日期：{}", page, pageSize, content, rating, startDate, endDate);
        PageQueryDTO pageQueryDTO = new PageQueryDTO();
        pageQueryDTO.setPage(page);
        pageQueryDTO.setPageSize(pageSize);
        pageQueryDTO.setKeyword(content);
        pageQueryDTO.setRating(rating);
        if (startDate != null) {
            pageQueryDTO.setBeginTime(startDate + " 00:00:00");
        }
        if (endDate != null) {
            pageQueryDTO.setEndTime(endDate + " 23:59:59");
        }
        Page<ReviewDTO> pageResult = reviewService.pageQueryWithDetails(pageQueryDTO);
        return R.success(pageResult);
    }
} 
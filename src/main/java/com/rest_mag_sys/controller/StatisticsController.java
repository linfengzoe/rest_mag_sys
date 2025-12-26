package com.rest_mag_sys.controller;

import com.rest_mag_sys.common.R;
import com.rest_mag_sys.dto.StatisticsAskRequest;
import com.rest_mag_sys.dto.StatisticsInterpretRequest;
import com.rest_mag_sys.service.StatisticsLlmService;
import com.rest_mag_sys.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器
 * 提供各种数据统计和分析功能
 */
@RestController
@RequestMapping("/statistics")
@Slf4j
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private StatisticsLlmService statisticsLlmService;

    /**
     * 获取订单统计数据
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @return 订单统计数据
     */
    @GetMapping("/orders")
    public R<List<Map<String, Object>>> getOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取订单统计数据，开始日期：{}，结束日期：{}", startDate, endDate);
        try {
            List<Map<String, Object>> statistics = statisticsService.getOrderStatistics(startDate, endDate);
            return R.success(statistics);
        } catch (Exception e) {
            log.error("获取订单统计数据失败", e);
            return R.error("获取订单统计数据失败");
        }
    }

    /**
     * 获取菜品销售排行榜
     * @param limit 返回条数，默认10
     * @param type 排序类型：quantity(销量)、sales(销售额)，默认quantity
     * @return 菜品销售排行榜
     */
    @GetMapping("/dishes/ranking")
    public R<List<Map<String, Object>>> getDishSalesRanking(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "quantity") String type) {
        log.info("获取菜品销售排行榜，条数：{}，排序类型：{}", limit, type);
        try {
            List<Map<String, Object>> ranking = statisticsService.getDishSalesRanking(limit, type);
            return R.success(ranking);
        } catch (Exception e) {
            log.error("获取菜品销售排行榜失败", e);
            return R.error("获取菜品销售排行榜失败");
        }
    }

    /**
     * 获取顾客行为分析数据
     * @param limit 返回条数，默认20
     * @return 顾客行为分析数据
     */
    @GetMapping("/customers/behavior")
    public R<List<Map<String, Object>>> getCustomerBehaviorAnalysis(
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取顾客行为分析数据，条数：{}", limit);
        try {
            List<Map<String, Object>> analysis = statisticsService.getCustomerBehaviorAnalysis(limit);
            return R.success(analysis);
        } catch (Exception e) {
            log.error("获取顾客行为分析数据失败", e);
            return R.error("获取顾客行为分析数据失败");
        }
    }

    /**
     * 获取顾客菜品偏好分析
     * @param customerId 顾客ID
     * @param limit 返回条数，默认5
     * @return 顾客菜品偏好数据
     */
    @GetMapping("/customers/{customerId}/preferences")
    public R<List<Map<String, Object>>> getCustomerDishPreferences(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "5") Integer limit) {
        log.info("获取顾客菜品偏好分析，顾客ID：{}，条数：{}", customerId, limit);
        try {
            List<Map<String, Object>> preferences = statisticsService.getCustomerDishPreferences(customerId, limit);
            return R.success(preferences);
        } catch (Exception e) {
            log.error("获取顾客菜品偏好分析失败", e);
            return R.error("获取顾客菜品偏好分析失败");
        }
    }

    /**
     * 获取员工绩效统计
     * @param limit 返回条数，默认20
     * @return 员工绩效统计数据
     */
    @GetMapping("/employees/performance")
    public R<List<Map<String, Object>>> getEmployeePerformance(
            @RequestParam(defaultValue = "20") Integer limit) {
        log.info("获取员工绩效统计，条数：{}", limit);
        try {
            List<Map<String, Object>> performance = statisticsService.getEmployeePerformance(limit);
            return R.success(performance);
        } catch (Exception e) {
            log.error("获取员工绩效统计失败", e);
            return R.error("获取员工绩效统计失败");
        }
    }

    /**
     * 获取时间段订单分布
     * @return 时间段订单分布数据
     */
    @GetMapping("/orders/hourly-distribution")
    public R<List<Map<String, Object>>> getHourlyOrderDistribution() {
        log.info("获取时间段订单分布");
        try {
            List<Map<String, Object>> distribution = statisticsService.getHourlyOrderDistribution();
            return R.success(distribution);
        } catch (Exception e) {
            log.error("获取时间段订单分布失败", e);
            return R.error("获取时间段订单分布失败");
        }
    }

    /**
     * 获取月度销售趋势
     * @param months 返回月数，默认12
     * @return 月度销售趋势数据
     */
    @GetMapping("/sales/monthly-trend")
    public R<List<Map<String, Object>>> getMonthlySalesTrend(
            @RequestParam(defaultValue = "12") Integer months) {
        log.info("获取月度销售趋势，月数：{}", months);
        try {
            List<Map<String, Object>> trend = statisticsService.getMonthlySalesTrend(months);
            return R.success(trend);
        } catch (Exception e) {
            log.error("获取月度销售趋势失败", e);
            return R.error("获取月度销售趋势失败");
        }
    }

    /**
     * 获取餐桌使用率统计
     * @return 餐桌使用率统计数据
     */
    @GetMapping("/tables/utilization")
    public R<List<Map<String, Object>>> getTableUtilizationStatistics() {
        log.info("获取餐桌使用率统计");
        try {
            List<Map<String, Object>> statistics = statisticsService.getTableUtilizationStatistics();
            return R.success(statistics);
        } catch (Exception e) {
            log.error("获取餐桌使用率统计失败", e);
            return R.error("获取餐桌使用率统计失败");
        }
    }

    /**
     * 获取综合经营仪表板数据
     * @return 综合仪表板数据
     */
    @GetMapping("/dashboard")
    public R<Map<String, Object>> getDashboardData() {
        log.info("获取综合经营仪表板数据");
        try {
            Map<String, Object> dashboard = statisticsService.getDashboardData();
            return R.success(dashboard);
        } catch (Exception e) {
            log.error("获取综合经营仪表板数据失败", e);
            return R.error("获取综合经营仪表板数据失败");
        }
    }

    /**
     * 获取销售汇总报表
     * @param period 时间周期：day, week, month, year
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售汇总报表
     */
    @GetMapping("/reports/sales-summary")
    public R<Map<String, Object>> getSalesSummaryReport(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("获取销售汇总报表，周期：{}，开始日期：{}，结束日期：{}", period, startDate, endDate);
        try {
            Map<String, Object> report = statisticsService.getSalesSummaryReport(period, startDate, endDate);
            return R.success(report);
        } catch (Exception e) {
            log.error("获取销售汇总报表失败", e);
            return R.error("获取销售汇总报表失败");
        }
    }

    /**
     * 获取菜品评论星级排行
     * @param limit 返回条数，默认10
     * @return 菜品评论星级排行
     */
    @GetMapping("/dishes/review-ranking")
    public R<List<Map<String, Object>>> getDishReviewRanking(
            @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取菜品评论星级排行，条数：{}", limit);
        try {
            List<Map<String, Object>> ranking = statisticsService.getDishReviewRanking(limit);
            return R.success(ranking);
        } catch (Exception e) {
            log.error("获取菜品评论星级排行失败", e);
            return R.error("获取菜品评论星级排行失败");
        }
    }

    /**
     * LLM解读统计报表
     * @param request 解读请求
     * @return 解读结果
     */
    @PostMapping("/llm/interpret")
    public R<Map<String, Object>> interpretReport(@RequestBody StatisticsInterpretRequest request) {
        log.info("LLM解读统计报表，类型：{}", request.getReportType());
        try {
            Map<String, Object> result = statisticsLlmService.interpretReport(request);
            return R.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("LLM解读参数错误: {}", e.getMessage());
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("LLM解读报表失败", e);
            return R.error("LLM解读报表失败");
        }
    }

    /**
     * LLM统计问答
     * @param request 问答请求
     * @return 回答结果
     */
    @PostMapping("/llm/ask")
    public R<Map<String, Object>> askQuestion(@Valid @RequestBody StatisticsAskRequest request) {
        log.info("LLM统计问答，问题：{}", request.getQuestion());
        try {
            Map<String, Object> result = statisticsLlmService.answerQuestion(request);
            return R.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("LLM问答参数错误: {}", e.getMessage());
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("LLM问答失败", e);
            return R.error("LLM问答失败");
        }
    }
} 

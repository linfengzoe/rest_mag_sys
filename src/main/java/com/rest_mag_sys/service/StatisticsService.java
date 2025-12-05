package com.rest_mag_sys.service;

import java.util.List;
import java.util.Map;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取订单统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单统计数据
     */
    List<Map<String, Object>> getOrderStatistics(String startDate, String endDate);

    /**
     * 获取菜品销售排行榜
     * @param limit 返回条数
     * @param type 排序类型
     * @return 菜品销售排行榜
     */
    List<Map<String, Object>> getDishSalesRanking(Integer limit, String type);

    /**
     * 获取顾客行为分析数据
     * @param limit 返回条数
     * @return 顾客行为分析数据
     */
    List<Map<String, Object>> getCustomerBehaviorAnalysis(Integer limit);

    /**
     * 获取顾客菜品偏好分析
     * @param customerId 顾客ID
     * @param limit 返回条数
     * @return 顾客菜品偏好数据
     */
    List<Map<String, Object>> getCustomerDishPreferences(Long customerId, Integer limit);

    /**
     * 获取员工绩效统计
     * @param limit 返回条数
     * @return 员工绩效统计数据
     */
    List<Map<String, Object>> getEmployeePerformance(Integer limit);

    /**
     * 获取菜品评价排行榜
     * @param limit 返回条数
     * @return 菜品评价排行榜
     */
    List<Map<String, Object>> getDishReviewRanking(Integer limit);

    /**
     * 获取时间段订单分布
     * @return 时间段订单分布数据
     */
    List<Map<String, Object>> getHourlyOrderDistribution();

    /**
     * 获取月度销售趋势
     * @param months 返回月数
     * @return 月度销售趋势数据
     */
    List<Map<String, Object>> getMonthlySalesTrend(Integer months);

    /**
     * 获取餐桌使用率统计
     * @return 餐桌使用率统计数据
     */
    List<Map<String, Object>> getTableUtilizationStatistics();

    /**
     * 获取综合经营仪表板数据
     * @return 综合仪表板数据
     */
    Map<String, Object> getDashboardData();

    /**
     * 获取销售汇总报表
     * @param period 时间周期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售汇总报表
     */
    Map<String, Object> getSalesSummaryReport(String period, String startDate, String endDate);
} 
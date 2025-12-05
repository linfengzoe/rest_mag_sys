package com.rest_mag_sys.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计分析Mapper接口
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 获取订单统计数据
     */
    @Select("SELECT * FROM v_daily_order_statistics WHERE order_date BETWEEN #{startDate} AND #{endDate} ORDER BY order_date DESC")
    List<Map<String, Object>> getOrderStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 获取菜品销售排行榜（按销量）
     */
    @Select("SELECT * FROM v_dish_sales_ranking ORDER BY total_quantity DESC LIMIT #{limit}")
    List<Map<String, Object>> getDishSalesRankingByQuantity(@Param("limit") Integer limit);

    /**
     * 获取菜品销售排行榜（按销售额）
     */
    @Select("SELECT * FROM v_dish_sales_ranking ORDER BY total_sales DESC LIMIT #{limit}")
    List<Map<String, Object>> getDishSalesRankingBySales(@Param("limit") Integer limit);

    /**
     * 获取顾客行为分析数据
     */
    @Select("SELECT * FROM v_customer_behavior_analysis ORDER BY total_consumption DESC LIMIT #{limit}")
    List<Map<String, Object>> getCustomerBehaviorAnalysis(@Param("limit") Integer limit);

    /**
     * 获取顾客菜品偏好分析
     */
    @Select("SELECT * FROM v_customer_dish_preference WHERE user_id = #{customerId} ORDER BY preference_rank LIMIT #{limit}")
    List<Map<String, Object>> getCustomerDishPreferences(@Param("customerId") Long customerId, @Param("limit") Integer limit);

    /**
     * 获取员工绩效统计
     */
    @Select("SELECT * FROM v_employee_performance ORDER BY total_sales DESC LIMIT #{limit}")
    List<Map<String, Object>> getEmployeePerformance(@Param("limit") Integer limit);



    /**
     * 获取时间段订单分布
     */
    @Select("SELECT * FROM v_hourly_order_distribution ORDER BY hour_of_day")
    List<Map<String, Object>> getHourlyOrderDistribution();

    /**
     * 获取月度销售趋势
     */
    @Select("SELECT * FROM v_monthly_sales_trend ORDER BY year DESC, month DESC LIMIT #{months}")
    List<Map<String, Object>> getMonthlySalesTrend(@Param("months") Integer months);

    /**
     * 获取餐桌使用率统计
     */
    @Select("SELECT * FROM v_table_utilization_statistics ORDER BY total_sales DESC")
    List<Map<String, Object>> getTableUtilizationStatistics();

    /**
     * 获取今日订单统计
     */
    @Select("SELECT * FROM v_daily_order_statistics WHERE order_date = CURDATE() LIMIT 1")
    Map<String, Object> getTodayOrderStatistics();

    /**
     * 获取本月订单汇总
     */
    @Select("SELECT SUM(total_sales) as month_sales, SUM(order_count) as month_orders " +
            "FROM v_daily_order_statistics " +
            "WHERE order_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')")
    Map<String, Object> getThisMonthSummary();

    /**
     * 获取订单状态统计
     */
    @Select("SELECT status, COUNT(*) as count FROM orders GROUP BY status")
    List<Map<String, Object>> getOrderStatusStatistics();

    /**
     * 获取最近7天趋势
     */
    @Select("SELECT * FROM v_daily_order_statistics WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ORDER BY order_date")
    List<Map<String, Object>> getWeekTrend();
} 
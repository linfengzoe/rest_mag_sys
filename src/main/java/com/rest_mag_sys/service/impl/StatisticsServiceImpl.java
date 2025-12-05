package com.rest_mag_sys.service.impl;

import com.rest_mag_sys.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统计分析服务实现类
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getOrderStatistics(String startDate, String endDate) {
        StringBuilder sql = new StringBuilder("SELECT * FROM v_daily_order_statistics WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND order_date >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND order_date <= ?");
            params.add(endDate);
        }

        sql.append(" ORDER BY order_date DESC LIMIT 30");

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getDishSalesRanking(Integer limit, String type) {
        String orderBy = "quantity".equals(type) ? "total_quantity DESC" : "total_sales DESC";
        String sql = "SELECT * FROM v_dish_sales_ranking ORDER BY " + orderBy + " LIMIT ?";
        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public List<Map<String, Object>> getCustomerBehaviorAnalysis(Integer limit) {
        String sql = "SELECT * FROM v_customer_behavior_analysis ORDER BY total_consumption DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public List<Map<String, Object>> getCustomerDishPreferences(Long customerId, Integer limit) {
        String sql = "SELECT * FROM v_customer_dish_preference WHERE user_id = ? ORDER BY preference_rank LIMIT ?";
        return jdbcTemplate.queryForList(sql, customerId, limit);
    }

    @Override
    public List<Map<String, Object>> getEmployeePerformance(Integer limit) {
        String sql = "SELECT * FROM v_employee_performance ORDER BY total_sales DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public List<Map<String, Object>> getDishReviewRanking(Integer limit) {
        String sql = "SELECT * FROM v_dish_review_ranking ORDER BY avg_rating DESC, review_count DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public List<Map<String, Object>> getHourlyOrderDistribution() {
        String sql = "SELECT * FROM v_hourly_order_distribution ORDER BY hour_of_day";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getMonthlySalesTrend(Integer months) {
        String sql = "SELECT * FROM v_monthly_sales_trend ORDER BY year DESC, month DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, months);
    }

    @Override
    public List<Map<String, Object>> getTableUtilizationStatistics() {
        String sql = "SELECT * FROM v_table_utilization_statistics ORDER BY total_sales DESC";
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        try {
            // 今日数据（仅统计已完成 status = 3）
            String todaySql = "SELECT COUNT(*) AS order_count, COALESCE(SUM(amount),0) AS total_sales " +
                              "FROM orders WHERE status = 3 AND DATE(create_time) = CURDATE()";
            Map<String, Object> todayData = jdbcTemplate.queryForMap(todaySql);
            dashboard.put("today", todayData);

            // 本月数据（仅统计已完成）
            String monthSql = "SELECT COUNT(*) AS month_orders, COALESCE(SUM(amount),0) AS month_sales " +
                              "FROM orders WHERE status = 3 AND DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')";
            Map<String, Object> monthData = jdbcTemplate.queryForMap(monthSql);
            dashboard.put("thisMonth", monthData);

            // 热销菜品 TOP 5
            List<Map<String, Object>> topDishes = getDishSalesRanking(5, "quantity");
            dashboard.put("topDishes", topDishes);

            // 优秀员工 TOP 5
            List<Map<String, Object>> topEmployees = getEmployeePerformance(5);
            dashboard.put("topEmployees", topEmployees);

            // 新增：统计员工数
            String employeeCountSql = "SELECT COUNT(*) FROM employee";
            Integer employeeCount = jdbcTemplate.queryForObject(employeeCountSql, Integer.class);
            dashboard.put("employeeCount", employeeCount);

            // 新增：统计顾客数
            String customerCountSql = "SELECT COUNT(*) FROM customer";
            Integer customerCount = jdbcTemplate.queryForObject(customerCountSql, Integer.class);
            dashboard.put("customerCount", customerCount);

            // 订单状态统计
            String orderStatusSql = "SELECT status, COUNT(*) as count FROM orders GROUP BY status";
            List<Map<String, Object>> orderStatus = jdbcTemplate.queryForList(orderStatusSql);
            dashboard.put("orderStatus", orderStatus);

            // 最近7天趋势
            String weekTrendSql = "SELECT * FROM v_daily_order_statistics WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ORDER BY order_date";
            List<Map<String, Object>> weekTrend = jdbcTemplate.queryForList(weekTrendSql);
            dashboard.put("weekTrend", weekTrend);

        } catch (Exception e) {
            log.error("获取仪表板数据失败", e);
            dashboard.put("error", "数据获取失败");
        }

        return dashboard;
    }

    @Override
    public Map<String, Object> getSalesSummaryReport(String period, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();

        try {
            String groupBy;
            String dateFormat;
            switch (period.toLowerCase()) {
                case "week":
                    groupBy = "YEARWEEK(o.create_time, 1)";
                    dateFormat = "CONCAT(YEAR(o.create_time), '-W', WEEK(o.create_time, 1))";
                    break;
                case "month":
                    groupBy = "YEAR(o.create_time), MONTH(o.create_time)";
                    dateFormat = "CONCAT(YEAR(o.create_time), '-', LPAD(MONTH(o.create_time), 2, '0'))";
                    break;
                case "year":
                    groupBy = "YEAR(o.create_time)";
                    dateFormat = "YEAR(o.create_time)";
                    break;
                default: // day
                    groupBy = "DATE(o.create_time)";
                    dateFormat = "DATE(o.create_time)";
            }

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ");
            sql.append(dateFormat).append(" as period, ");
            sql.append("COUNT(*) as order_count, ");
            sql.append("SUM(o.amount) as total_sales, ");
            sql.append("AVG(o.amount) as average_order_value, ");
            sql.append("COUNT(DISTINCT o.user_id) as unique_customers ");
            sql.append("FROM orders o WHERE o.status = 3");

            List<Object> params = new ArrayList<>();
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append(" AND DATE(o.create_time) >= ?");
                params.add(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append(" AND DATE(o.create_time) <= ?");
                params.add(endDate);
            }

            sql.append(" GROUP BY ").append(groupBy);
            sql.append(" ORDER BY ").append(groupBy).append(" DESC");
            sql.append(" LIMIT 50");

            List<Map<String, Object>> salesData = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            report.put("salesData", salesData);

            // 汇总统计
            String summarySql = "SELECT COUNT(*) as total_orders, SUM(amount) as total_sales, " +
                               "AVG(amount) as avg_order_value, COUNT(DISTINCT user_id) as total_customers " +
                               "FROM orders WHERE status = 3";
            List<Object> summaryParams = new ArrayList<>();
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                summarySql += " AND DATE(create_time) >= ?";
                summaryParams.add(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                summarySql += " AND DATE(create_time) <= ?";
                summaryParams.add(endDate);
            }

            List<Map<String, Object>> summary = jdbcTemplate.queryForList(summarySql, summaryParams.toArray());
            report.put("summary", summary.isEmpty() ? new HashMap<>() : summary.get(0));

            // 菜品销售统计
            String dishSql = "SELECT d.name, SUM(od.number) as quantity, SUM(od.amount) as sales " +
                            "FROM order_detail od " +
                            "INNER JOIN orders o ON od.order_id = o.id " +
                            "INNER JOIN dish d ON od.dish_id = d.id " +
                            "WHERE o.status = 3";
            
            List<Object> dishParams = new ArrayList<>();
            if (startDate != null && !startDate.trim().isEmpty()) {
                dishSql += " AND DATE(o.create_time) >= ?";
                dishParams.add(startDate);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                dishSql += " AND DATE(o.create_time) <= ?";
                dishParams.add(endDate);
            }
            
            dishSql += " GROUP BY d.id, d.name ORDER BY sales DESC LIMIT 10";
            List<Map<String, Object>> dishSales = jdbcTemplate.queryForList(dishSql, dishParams.toArray());
            report.put("topDishSales", dishSales);

        } catch (Exception e) {
            log.error("获取销售汇总报表失败", e);
            report.put("error", "报表生成失败");
        }

        return report;
    }
} 
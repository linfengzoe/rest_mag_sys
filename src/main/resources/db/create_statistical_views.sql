-- 统计分析数据库视图
-- 创建统计分析相关的数据库视图，用于提高查询性能和简化统计逻辑

-- 1. 订单统计视图 - 按日期统计订单数量、销售额、平均订单金额
CREATE OR REPLACE VIEW v_daily_order_statistics AS
SELECT 
    DATE(o.create_time) AS order_date,
    -- 仅统计已完成(status = 3)的订单
    SUM(CASE WHEN o.status = 3 THEN 1 ELSE 0 END) AS order_count,
    SUM(CASE WHEN o.status = 3 THEN o.amount ELSE 0 END) AS total_sales,
    -- 平均订单金额基于已完成订单
    CASE 
        WHEN SUM(CASE WHEN o.status = 3 THEN 1 ELSE 0 END) = 0 THEN 0
        ELSE ROUND(SUM(CASE WHEN o.status = 3 THEN o.amount ELSE 0 END) / SUM(CASE WHEN o.status = 3 THEN 1 ELSE 0 END), 2)
    END AS average_order_amount,
    COUNT(DISTINCT CASE WHEN o.status = 3 THEN o.user_id END) AS customer_count,
    -- 保留已完成/已取消统计字段
    SUM(CASE WHEN o.status = 3 THEN 1 ELSE 0 END) AS completed_orders,
    SUM(CASE WHEN o.status = 4 THEN 1 ELSE 0 END) AS cancelled_orders
FROM orders o
WHERE o.create_time IS NOT NULL
GROUP BY DATE(o.create_time)
ORDER BY order_date DESC;

-- 2. 菜品销售排行视图 - 统计各菜品的销售数量、销售额、排名
CREATE OR REPLACE VIEW v_dish_sales_ranking AS
SELECT 
    d.id as dish_id,
    d.name as dish_name,
    d.category_id,
    c.name as category_name,
    d.price as dish_price,
    COALESCE(sales.total_quantity, 0) as total_quantity,
    COALESCE(sales.total_sales, 0) as total_sales,
    COALESCE(sales.order_count, 0) as order_count,
    RANK() OVER (ORDER BY COALESCE(sales.total_quantity, 0) DESC) as quantity_rank,
    RANK() OVER (ORDER BY COALESCE(sales.total_sales, 0) DESC) as sales_rank
FROM dish d
LEFT JOIN category c ON d.category_id = c.id
LEFT JOIN (
    SELECT 
        od.dish_id,
        SUM(od.number) as total_quantity,
        SUM(od.amount) as total_sales,
        COUNT(DISTINCT od.order_id) as order_count
    FROM order_detail od
    INNER JOIN orders o ON od.order_id = o.id
    WHERE o.status = 3  -- 只统计已完成的订单
    GROUP BY od.dish_id
) sales ON d.id = sales.dish_id
ORDER BY total_quantity DESC;

-- 3. 顾客行为分析视图 - 统计顾客消费频率、平均消费金额
CREATE OR REPLACE VIEW v_customer_behavior_analysis AS
SELECT 
    u.id as user_id,
    u.name as customer_name,
    u.phone as customer_phone,
    c.member_level,
    c.points,
    COALESCE(behavior.order_count, 0) as order_count,
    COALESCE(behavior.total_consumption, 0) as total_consumption,
    COALESCE(behavior.average_consumption, 0) as average_consumption,
    behavior.first_order_date,
    behavior.last_order_date,
    DATEDIFF(CURDATE(), behavior.last_order_date) as days_since_last_order,
    CASE 
        WHEN behavior.order_count >= 20 THEN 'VIP客户'
        WHEN behavior.order_count >= 10 THEN '忠实客户'
        WHEN behavior.order_count >= 5 THEN '普通客户'
        WHEN behavior.order_count >= 1 THEN '新客户'
        ELSE '潜在客户'
    END as customer_level
FROM t_user u
LEFT JOIN customer c ON u.id = c.user_id
LEFT JOIN (
    SELECT 
        o.user_id,
        COUNT(*) as order_count,
        SUM(o.amount) as total_consumption,
        AVG(o.amount) as average_consumption,
        MIN(o.create_time) as first_order_date,
        MAX(o.create_time) as last_order_date
    FROM orders o
    WHERE o.status = 3  -- 只统计已完成的订单
    GROUP BY o.user_id
) behavior ON u.id = behavior.user_id
WHERE u.role = 'customer'
ORDER BY total_consumption DESC;

-- 4. 菜品偏好分析视图 - 分析顾客的菜品选择偏好
CREATE OR REPLACE VIEW v_customer_dish_preference AS
SELECT 
    u.id as user_id,
    u.name as customer_name,
    d.id as dish_id,
    d.name as dish_name,
    c.name as category_name,
    SUM(od.number) as total_ordered_quantity,
    COUNT(DISTINCT od.order_id) as order_frequency,
    SUM(od.amount) as total_spent_on_dish,
    AVG(od.amount) as average_spent_per_order,
    RANK() OVER (PARTITION BY u.id ORDER BY SUM(od.number) DESC) as preference_rank
FROM t_user u
INNER JOIN orders o ON u.id = o.user_id
INNER JOIN order_detail od ON o.id = od.order_id
INNER JOIN dish d ON od.dish_id = d.id
LEFT JOIN category c ON d.category_id = c.id
WHERE u.role = 'customer' AND o.status = 3
GROUP BY u.id, u.name, d.id, d.name, c.name
ORDER BY u.id, preference_rank;

-- 5. 员工绩效统计视图 - 统计员工处理订单数量、销售额
CREATE OR REPLACE VIEW v_employee_performance AS
SELECT 
    u.id as user_id,
    u.name as employee_name,
    e.position,
    e.hire_date,
    COALESCE(performance.handled_orders, 0) as handled_orders,
    COALESCE(performance.total_sales, 0) as total_sales,
    COALESCE(performance.average_order_value, 0) as average_order_value,
    performance.first_handled_date,
    performance.last_handled_date,
    CASE 
        WHEN performance.handled_orders >= 100 THEN '优秀'
        WHEN performance.handled_orders >= 50 THEN '良好'
        WHEN performance.handled_orders >= 20 THEN '一般'
        ELSE '待提高'
    END as performance_level
FROM t_user u
INNER JOIN employee e ON u.id = e.user_id
LEFT JOIN (
    SELECT 
        o.employee_id as employee_id,
        COUNT(*) as handled_orders,
        SUM(o.amount) as total_sales,
        AVG(o.amount) as average_order_value,
        MIN(o.update_time) as first_handled_date,
        MAX(o.update_time) as last_handled_date
    FROM orders o
    WHERE o.employee_id IS NOT NULL AND o.status = 3  -- 只统计已完成订单
    GROUP BY o.employee_id
) performance ON u.id = performance.employee_id
WHERE u.role IN ('employee', 'admin')
ORDER BY total_sales DESC;

-- 6. 时间段订单分布视图 - 分析不同时间段的订单分布
CREATE OR REPLACE VIEW v_hourly_order_distribution AS
SELECT 
    t.hour_of_day,
    COUNT(*) as order_count,
    SUM(t.amount) as total_sales,
    AVG(t.amount) as average_order_value,
    CASE 
        WHEN t.hour_of_day BETWEEN 6 AND 10 THEN '早餐时段'
        WHEN t.hour_of_day BETWEEN 11 AND 14 THEN '午餐时段'
        WHEN t.hour_of_day BETWEEN 17 AND 21 THEN '晚餐时段'
        ELSE '其他时段'
    END as time_period
FROM (
    SELECT HOUR(create_time) as hour_of_day, amount
    FROM orders
    WHERE status = 3 AND create_time IS NOT NULL
) t
GROUP BY t.hour_of_day
ORDER BY t.hour_of_day;

-- 7. 月度销售趋势视图 - 按月统计销售趋势
CREATE OR REPLACE VIEW v_monthly_sales_trend AS
SELECT 
    YEAR(create_time) as `year`,
    MONTH(create_time) as `month`,
    COUNT(*) as order_count,
    SUM(amount) as total_sales,
    AVG(amount) as average_order_value,
    COUNT(DISTINCT user_id) as unique_customers
FROM orders
WHERE status = 3 AND create_time IS NOT NULL
GROUP BY YEAR(create_time), MONTH(create_time)
ORDER BY `year` DESC, `month` DESC;

-- 8. 餐桌使用率统计视图
CREATE OR REPLACE VIEW v_table_utilization_statistics AS
SELECT 
    t.id as table_id,
    t.name as table_name,
    t.capacity as seats,
    COALESCE(tbl_usage.order_count, 0) as order_count,
    COALESCE(tbl_usage.total_sales, 0) as total_sales,
    COALESCE(tbl_usage.average_order_value, 0) as average_order_value,
    ROUND(COALESCE(tbl_usage.total_sales, 0) / t.capacity, 2) as sales_per_seat
FROM table_info t
LEFT JOIN (
    SELECT 
        table_id,
        COUNT(*) as order_count,
        SUM(amount) as total_sales,
        AVG(amount) as average_order_value
    FROM orders
    WHERE status = 3 AND table_id IS NOT NULL
    GROUP BY table_id
) tbl_usage ON t.id = tbl_usage.table_id
ORDER BY total_sales DESC;

-- 9. 菜品评论星级排行视图 - 统计每个菜品的平均评分、评论数
CREATE OR REPLACE VIEW v_dish_review_ranking AS
SELECT
    d.id as dish_id,
    d.name as dish_name,
    c.name as category_name,
    COALESCE(sub.avg_rating, 0) as avg_rating,
    COALESCE(sub.review_count, 0) as review_count
FROM
    dish d
LEFT JOIN (
    SELECT
        od.dish_id,
        AVG(r.rating) as avg_rating,
        COUNT(DISTINCT r.order_id) as review_count
    FROM
        review r
    JOIN
        order_detail od ON r.order_id = od.order_id
    WHERE
        r.rating IS NOT NULL
    GROUP BY
        od.dish_id
) sub ON d.id = sub.dish_id
LEFT JOIN
    category c ON d.category_id = c.id
ORDER BY
    avg_rating DESC, review_count DESC;


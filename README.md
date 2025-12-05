# 餐厅管理系统 (Rest Mag Sys)

> 一站式餐厅运营平台，涵盖点餐、后厨、收银、评价、统计分析等完整业务流程。后端基
> 于 **Spring Boot 2.7.5 + MyBatis-Plus + MySQL 8**，前端基于 **Vue 2 + Element
> UI**。

---

## 核心功能

| 模块        | 主要特性                                                                                                         |
| ----------- | ---------------------------------------------------------------------------------------------------------------- |
| 账户与权限  | JWT 单点登录、员工/顾客双端、基于角色的路由守卫                                                                  |
| 菜品 & 分类 | 菜品 CRUD、分类管理、菜品上下架、图片上传                                                                        |
| 餐桌管理    | 餐桌状态(空闲/占用/预订)、座位容量、实时更新                                                                     |
| 下单流程    | 顾客自助点餐 → 购物车 → 下单 → 接单 → 完成付款                                                               |
| 订单管理    | 订单列表、状态流转(待确认 → 制作中 → 完成)、订单明细                                                             |
| 评价系统    | 顾客评价、商家回复、评价状态控制                                                                                 |
| 统计分析    | 日/周/月销售趋势、菜品销量排行、顾客行为、员工绩效、餐桌使用率、时间段分布等 8 大分析视图，图表基于 ECharts 渲染 |
| 系统配置    | 全局异常处理、统一返回结果 `R<T>`、跨域/CORS、静态资源映射                                                       |

---

##  技术栈

### 后端

- Spring Boot **2.7.5**
- Spring Security + JWT
- MyBatis-Plus **3.5.2**
- MySQL **8.0.31**
- Druid 连接池、Lombok、SLF4J

### 前端

- Vue **2.6.x** + Vue-Router + Vuex
- Element UI **2.15.x**
- Axios **0.27.x**
- ECharts **5.5.x**（统计页折线/柱状/饼图）

---

## 快速启动

### 1. 克隆仓库

```bash
$ git clone https://github.com/yourname/rest_mag_sys.git
$ cd rest_mag_sys
```

### 2. 数据库准备

1. 创建数据库 `rest_mag_sys`，字符集 `utf8mb4`。
2. 执行脚本：
   - `src/main/resources/db/schema.sql` – 表结构
   - `src/main/resources/db/data.sql` – 初始化数据
   - `src/main/resources/db/create_statistical_views.sql` – 8 个统计视图



### 3. 启动后端

```bash
# Windows
mvnw spring-boot:run
# Mac / Linux
authority ./mvnw spring-boot:run
```

- 默认端口：**8081**
- Swagger 文档：http://localhost:8081/doc.html
- H2 Console：http://localhost:8081/h2-console

### 4. 启动前端

```bash
cd vue_ui
npm install   # 第一次运行或依赖变更时
npm run serve # 本地开发
```

- 默认端口：**8080**（代理接口到 8081）
- 构建产物：`npm run build` 输出到 `dist/`

> 如端口冲突，可在 `vue.config.js` / `application.properties` 调整。

---

##  统计分析视图一览

| 视图         | 描述                               | 对应 SQL 视图                    |
| ------------ | ---------------------------------- | -------------------------------- |
| 日订单统计   | 按日期汇总订单数量、销售额、完成率 | `v_daily_order_statistics`       |
| 菜品销售排行 | TOP N 菜品销量 / 销售额            | `v_dish_sales_ranking`           |
| 顾客行为分析 | 订单频次、消费金额、客户等级       | `v_customer_behavior_analysis`   |
| 顾客菜品偏好 | 每位顾客常点菜品排行榜             | `v_customer_dish_preference`     |
| 员工绩效统计 | 处理订单数、销售额、绩效等级       | `v_employee_performance`         |
| 时间段分布   | 早餐/午餐/晚餐等时段订单分布       | `v_hourly_order_distribution`    |
| 月度销售趋势 | 按月销售额、均价、客单量           | `v_monthly_sales_trend`          |
| 餐桌使用率   | 每桌销售额、使用率                 | `v_table_utilization_statistics` |

> 统计接口位于 `StatisticsController`，服务实现 `StatisticsServiceImpl` 通过
> `JdbcTemplate` 直接查询视图，前端 `Statistics.vue` 使用 ECharts 绘图。

---

## 目录结构（部分）

```
rest_mag_sys
 ├── src/main/java/com/rest_mag_sys
 │   ├── controller      # 控制器层
 │   ├── service         # 业务层接口
 │   ├── service/impl    # 业务实现
 │   ├── mapper          # MyBatis-Plus Mapper
 │   ├── entity          # 实体类
 │   ├── dto             # 数据传输对象
 │   └── common          # 公共工具/异常/返回封装
 ├── src/main/resources
 │   ├── db              # SQL 脚本
 │   └── mapper          # XML (如需)
 ├── vue_ui             # 前端源码
 │   └── src
 │       ├── views       # 页面 (含 statistics)
 │       ├── api         # Axios 封装
 │       └── utils       # 公共工具
```

---

## 接口快速预览

| Method | URL                          | 描述                                    |
| ------ | ---------------------------- | --------------------------------------- |
| GET    | `/orders/list`               | 分页查询订单                            |
| POST   | `/orders/submit`             | 顾客下单                                |
| GET    | `/statistics/dashboard`      | 经营仪表盘数据                          |
| GET    | `/statistics/orders`         | 日订单统计 (可传 `startDate`/`endDate`) |
| GET    | `/statistics/dishes/ranking` | 菜品销量排行                            |
| …      | …                            | 详见 Swagger                            |


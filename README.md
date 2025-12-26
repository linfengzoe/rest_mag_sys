# 餐厅管理系统 (Rest Mag Sys)

> 一站式餐厅运营平台，涵盖点餐、后厨、收银、评价、统计分析等完整业务流程。后端基于 **Spring Boot 2.7.5 + MyBatis-Plus + MySQL 8**，前端基于 **Vue 2 + Element UI**。

---

## 核心功能

### 🍽️ 餐厅管理
- **菜品分类管理**：灵活的菜品分类系统，支持多级分类
- **菜品信息管理**：详细的菜品信息，包括价格、图片、描述等
- **餐桌管理**：餐桌状态实时监控，支持预订和占用管理
- **员工管理**：员工信息、权限管理、工作状态跟踪

### 🛒 订单系统
- **在线点餐**：顾客可通过前端界面浏览菜单并下单
- **订单处理**：订单状态跟踪，从下单到完成的全程管理
- **订单详情**：详细的订单明细，包括菜品、数量、价格等
- **收银结算**：自动计算金额，支持多种支付方式

### 👥 用户管理
- **顾客管理**：顾客信息维护，订单历史记录
- **用户认证**：基于JWT的安全认证系统
- **权限控制**：不同角色的权限管理（管理员、员工、顾客）
- **安全保护**：Spring Security提供的全面安全防护

### ⭐ 评价系统
- **客户评价**：顾客可对服务和菜品进行评价
- **评价统计**：评价数据的收集和统计分析
- **服务改进**：基于评价反馈优化服务质量

### 📊 数据统计
- **销售统计**：菜品销售情况统计和报表
- **财务报表**：收入、支出等财务数据分析
- **运营分析**：餐厅运营数据的多维度分析

---

## 技术架构

### 后端技术栈

| 技术组件 | 版本 | 用途 |
|---------|------|------|
| Spring Boot | 2.7.5 | 后端框架 |
| MyBatis-Plus | 3.5.2 | 数据访问层 |
| MySQL | 8.0 | 主数据库 |
| Druid | 1.2.8 | 数据库连接池 |
| Redis | - | 缓存和会话管理 |
| Spring Security | - | 安全认证 |
| FastJSON | 2.0.14 | JSON处理 |
| JWT | 0.9.1 | Token认证 |
| Apache Commons Lang3 | 3.12.0 | 工具类库 |
| Lombok | - | 代码简化 |

### 前端技术栈

| 技术组件 | 版本 | 用途 |
|---------|------|------|
| Vue.js | 2.x | 前端框架 |
| Element UI | - | UI组件库 |
| Axios | - | HTTP客户端 |
| Vue Router | - | 前端路由 |

---

## 数据库设计

### 核心数据表

| 表名 | 用途 | 主要字段 |
|------|------|----------|
| `t_user` | 系统用户 | id, username, password, role, status |
| `category` | 菜品分类 | id, name, description, sort_order |
| `dish` | 菜品信息 | id, name, price, category_id, image, description |
| `table_info` | 餐桌信息 | id, table_number, capacity, status |
| `orders` | 订单主表 | id, table_id, customer_id, status, total_amount |
| `order_detail` | 订单明细 | id, order_id, dish_id, quantity, unit_price |
| `employee` | 员工信息 | id, name, position, phone, hire_date |
| `customer` | 顾客信息 | id, name, phone, email, address |
| `review` | 评价信息 | id, customer_id, order_id, rating, comment |

### 数据库连接配置

```properties
# 数据源配置
spring.datasource.url=jdbc:mysql://localhost:3306/rest_mag_sys?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=1193217659
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Druid连接池配置
spring.datasource.druid.initial-size=5
spring.datasource.druid.min-idle=5
spring.datasource.druid.max-active=20
spring.datasource.druid.max-wait=60000
spring.datasource.druid.time-between-eviction-runs-millis=60000
spring.datasource.druid.min-evictable-idle-time-millis=300000

# MyBatis-Plus配置
mybatis-plus.configuration.map-underscore-to-camel-case=true
mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml
```

---

## 项目结构

```
rest_mag_sys/
├── src/main/java/com/rest_mag_sys/
│   ├── RestMagSysApplication.java      # Spring Boot启动类
│   ├── config/                         # 配置类
│   │   ├── DruidConfig.java           # Druid配置
│   │   ├── MyBatisPlusConfig.java     # MyBatis-Plus配置
│   │   ├── SecurityConfig.java        # 安全配置
│   │   ├── WebMvcConfig.java          # Web配置
│   │   └── JacksonConfig.java         # JSON配置
│   ├── controller/                     # 控制器层
│   │   ├── CustomerController.java    # 顾客管理
│   │   ├── DishController.java        # 菜品管理
│   │   ├── EmployeeController.java    # 员工管理
│   │   ├── OrderController.java       # 订单管理
│   │   ├── TableController.java       # 餐桌管理
│   │   └── UserController.java        # 用户管理
│   ├── service/                        # 服务层
│   │   ├── impl/                      # 服务实现
│   │   └── *.java                     # 服务接口
│   ├── mapper/                         # 数据访问层
│   │   ├── *.java                     # Mapper接口
│   └── entity/                         # 实体类
│       ├── Customer.java              # 顾客实体
│       ├── Dish.java                  # 菜品实体
│       ├── Employee.java              # 员工实体
│       ├── Order.java                 # 订单实体
│       ├── TableInfo.java             # 餐桌实体
│       └── User.java                  # 用户实体
├── src/main/resources/
│   ├── application.properties          # 应用配置
│   ├── db/                            # 数据库脚本
│   │   ├── schema.sql                # 数据库结构
│   │   └── data.sql                  # 初始数据
│   └── mapper/                        # MyBatis XML文件
├── vue_ui/                             # 前端项目
│   ├── src/                          # 前端源码
│   ├── public/                       # 静态资源
│   └── package.json                  # 前端依赖
└── pom.xml                           # Maven配置
```

---

## 快速开始

### 环境要求

- **Java**: JDK 1.8+
- **Node.js**: 14.0+
- **MySQL**: 8.0+
- **Maven**: 3.6+
- **Redis**: 6.0+（可选）

### 数据库设置

1. 创建数据库：
```sql
CREATE DATABASE rest_mag_sys CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
# 在MySQL命令行中执行
source d:/rest_sys/rest_mag_sys/src/main/resources/db/schema.sql;
source d:/rest_sys/rest_mag_sys/src/main/resources/db/data.sql;
```

### 后端启动

1. 进入项目目录：
```bash
cd d:/rest_sys/rest_mag_sys
```

2. 启动后端服务：
```bash
mvn spring-boot:run
```

后端服务将在 **8081端口** 启动

### 前端启动

1. 进入前端目录：
```bash
cd d:/rest_sys/rest_mag_sys/vue_ui
```

2. 安装依赖：
```bash
npm install
```

3. 启动前端服务：
```bash
npm run serve
```

前端服务将在 **8082端口** 启动

### 访问应用

- **前端地址**: http://localhost:8082/
- **后端API**: http://localhost:8081/

---

## 默认账号

| 角色 | 用户名 | 密码 | 权限说明 |
|------|--------|------|----------|
| 管理员 | admin | 123456 | 系统管理、权限分配 |
| 员工 | employee1 | 123456 | 订单处理、菜品管理 |
| 顾客 | customer1/customer2 | 123456 | 在线点餐、订单查看 |

---

## API文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取用户信息

### 菜品管理
- `GET /api/dish` - 获取菜品列表
- `POST /api/dish` - 新增菜品
- `PUT /api/dish/{id}` - 更新菜品
- `DELETE /api/dish/{id}` - 删除菜品

### 订单管理
- `GET /api/orders` - 获取订单列表
- `POST /api/orders` - 创建订单
- `PUT /api/orders/{id}/status` - 更新订单状态

### 用户管理
- `GET /api/users` - 获取用户列表
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户信息

---

## 开发说明

### 开发环境配置

1. **修改数据库连接**
   - 编辑 `application.properties`
   - 修改数据库连接地址、用户名、密码

2. **配置热部署**
   - 确保 `spring-boot-devtools` 依赖已包含
   - 修改代码后自动重启应用

3. **日志配置**
   - 默认日志级别为INFO
   - 可在 `application.properties` 中调整日志级别

### 代码规范

1. **实体类命名**：使用驼峰命名法，表字段使用下划线
2. **Mapper接口**：继承BaseMapper<T>获得基础CRUD能力
3. **Service层**：面向接口编程，实现依赖注入
4. **Controller层**：负责HTTP请求处理，参数验证

### 安全配置

1. **JWT认证**：登录成功后生成JWT Token
2. **权限控制**：基于Spring Security的Role-Based Access Control
3. **数据验证**：使用Hibernate Validator进行参数校验
4. **SQL防注入**：MyBatis-Plus提供参数绑定保护

---

## 部署指南

### 生产环境部署

1. **打包应用**：
```bash
mvn clean package -DskipTests
```

2. **配置生产环境**：
   - 修改 `application.properties` 中的数据库连接
   - 配置Redis连接信息
   - 设置适当的JVM参数

3. **运行应用**：
```bash
java -jar target/rest_mag_sys-0.0.1-SNAPSHOT.jar
```

### Docker部署

```dockerfile
FROM openjdk:8-jre-alpine
COPY target/rest_mag_sys-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 常见问题

### Q: 数据库连接失败？
**A**: 检查MySQL服务是否启动，验证数据库连接信息是否正确

### Q: 前端无法访问后端API？
**A**: 确认后端服务正常运行，检查CORS配置是否正确

### Q: 登录后无法访问需要权限的接口？
**A**: 验证JWT Token是否正确，检查用户角色权限配置

### Q: 如何重置数据库？
**A**: 删除现有数据，重新执行 `schema.sql` 和 `data.sql` 脚本

---

## 版本历史

- **v1.0.0** - 初始版本，包含基础的CRUD功能
- **v1.1.0** - 添加JWT认证和权限控制
- **v1.2.0** - 完善订单流程和评价系统
- **v1.3.0** - 优化前端界面和用户体验

---

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 联系方式

如有问题或建议，请通过以下方式联系：

- 邮箱：linfengzoe@qq.com
- 项目地址：[GitHub Repository](https://github.com/linfengzoe/rest_mag_sys)

---

## 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [Vue.js](https://vuejs.org/)
- [Element UI](https://element.eleme.io/)
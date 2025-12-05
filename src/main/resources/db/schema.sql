-- 删除所有表（按外键依赖逆序删除）
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS order_detail;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS dish;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS table_info;
DROP TABLE IF EXISTS t_user;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(64) NOT NULL COMMENT '密码',
    name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    sex INT COMMENT '性别',
    birthday VARCHAR(20) COMMENT '生日',
    avatar VARCHAR(255) COMMENT '头像',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    status INT DEFAULT 1 COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态'
) COMMENT='菜品分类表';

-- 菜品表
CREATE TABLE IF NOT EXISTS dish (
    id BIGINT PRIMARY KEY COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '菜品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    image VARCHAR(255) COMMENT '图片',
    description VARCHAR(255) COMMENT '描述',
    status INT DEFAULT 1 COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (category_id) REFERENCES category(id)
) COMMENT='菜品表';

-- 桌位信息表
CREATE TABLE IF NOT EXISTS table_info (
    id BIGINT PRIMARY KEY COMMENT '主键',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '桌位名称',
    capacity INT NOT NULL COMMENT '座位数',
    status INT DEFAULT 1 COMMENT '状态',
    location VARCHAR(100) COMMENT '位置'
) COMMENT='餐桌信息表';

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY COMMENT '主键',
    number VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    status INT DEFAULT 1 COMMENT '订单状态',
    user_id BIGINT NOT NULL COMMENT '下单用户ID',
    customer_id BIGINT COMMENT '顾客ID',
    employee_id BIGINT COMMENT '处理员工ID',
    table_id BIGINT COMMENT '桌位ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
    remark VARCHAR(255) COMMENT '备注',
    address VARCHAR(255) COMMENT '送餐地址',
    phone VARCHAR(20) COMMENT '联系电话',
    consignee VARCHAR(50) COMMENT '收货人',
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    checkout_time TIMESTAMP COMMENT '结账时间',
    payment_time TIMESTAMP COMMENT '支付时间',
    payment_method INT COMMENT '支付方式',
    complete_time TIMESTAMP COMMENT '完成时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES t_user(id),
    FOREIGN KEY (table_id) REFERENCES table_info(id)
) COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT PRIMARY KEY COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    name VARCHAR(50) NOT NULL COMMENT '菜品名称',
    image VARCHAR(255) COMMENT '图片',
    price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    number INT NOT NULL COMMENT '数量',
    amount DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (dish_id) REFERENCES dish(id)
) COMMENT='订单明细表';

-- 评价表
CREATE TABLE IF NOT EXISTS review (
    id BIGINT PRIMARY KEY COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content VARCHAR(500) NOT NULL COMMENT '评价内容',
    rating INT NOT NULL COMMENT '评分',
    reply VARCHAR(500) COMMENT '回复内容',
    reply_time TIMESTAMP COMMENT '回复时间',
    status INT DEFAULT 1 COMMENT '状态',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES t_user(id)
) COMMENT='评价表';

-- 员工表
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    sex INT COMMENT '性别',
    id_number VARCHAR(20) COMMENT '身份证号',
    position VARCHAR(50) COMMENT '职位',
    hire_date DATE COMMENT '入职日期',
    salary DECIMAL(10, 2) COMMENT '薪资',
    address VARCHAR(255) COMMENT '地址',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES t_user(id)
) COMMENT='员工表';

-- 顾客表
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    points INT DEFAULT 0 COMMENT '积分',
    member_level INT DEFAULT 0 COMMENT '会员等级',
    name VARCHAR(50) COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    sex INT COMMENT '性别',
    address VARCHAR(255) COMMENT '地址',
    register_time TIMESTAMP COMMENT '注册时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES t_user(id)
) COMMENT='顾客表'; 
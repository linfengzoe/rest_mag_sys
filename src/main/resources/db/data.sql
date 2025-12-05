-- 数据初始化脚本，根据schema.sql表结构生成

-- 插入用户数据到t_user表
INSERT INTO `t_user` (`id`, `username`, `password`, `name`, `phone`, `email`, `sex`, `birthday`, `avatar`, `role`, `status`, `create_time`, `update_time`) VALUES
(1000001, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '13888888888', 'admin@example.com', 1, '1990-01-01', 'https://img.alicdn.com/imgextra/i1/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 'admin', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000002, 'employee1', 'e10adc3949ba59abbe56e057f20f883e', '员工1', '13999999999', 'employee1@example.com', 1, '1992-01-01', 'https://img.alicdn.com/imgextra/i2/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 'employee', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000003, 'customer1', 'e10adc3949ba59abbe56e057f20f883e', '顾客1', '13777777777', 'customer1@example.com', 1, '1995-01-01', 'https://img.alicdn.com/imgextra/i3/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 'customer', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000004, 'customer2', 'e10adc3949ba59abbe56e057f20f883e', '顾客2', '13666666666', 'customer2@example.com', 0, '1993-01-01', 'https://img.alicdn.com/imgextra/i4/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 'customer', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- 插入分类数据

INSERT INTO `category` (`id`, `name`, `sort`, `status`) VALUES
(1000001, '热菜', 1, 1),
(1000002, '凉菜', 2, 1),
(1000003, '主食', 3, 1),
(1000004, '饮品', 4, 1),
(1000005, '酒水', 5, 1);

-- 插入菜品数据
INSERT INTO `dish` (`id`, `name`, `category_id`, `price`, `image`, `description`, `status`, `create_time`, `update_time`) VALUES
(1000001, '宫保鸡丁', 1000001, 28.00, 'https://img.alicdn.com/imgextra/i1/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '川菜经典，鸡丁爽嫩，花生酥脆', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000002, '麻婆豆腐', 1000001, 18.00, 'https://img.alicdn.com/imgextra/i2/2206356636456/O1CN01t5qZZs1zvE8bWzGfk_!!2206356636456.jpg', '豆腐嫩滑，麻辣鲜香', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000003, '回锅肉', 1000001, 32.00, 'https://img.alicdn.com/imgextra/i3/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '四川名菜，肥而不腻', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000004, '口水鸡', 1000002, 22.00, 'https://img.alicdn.com/imgextra/i4/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '鸡肉嫩滑，口感层次丰富', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000005, '凉拌黄瓜', 1000002, 8.00, 'https://img.alicdn.com/imgextra/i5/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '清爽开胃，营养健康', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000006, '扬州炒饭', 1000003, 15.00, 'https://img.alicdn.com/imgextra/i6/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '粒粒分明，色香味俱全', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000007, '红烧牛肉面', 1000003, 25.00, 'https://img.alicdn.com/imgextra/i7/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '牛肉软烂，汤汁浓郁', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000008, '鲜榨橙汁', 1000004, 12.00, 'https://img.alicdn.com/imgextra/i8/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '新鲜橙子现榨，维C丰富', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000009, '柠檬蜂蜜茶', 1000004, 10.00, 'https://img.alicdn.com/imgextra/i9/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '清香甘甜，生津止渴', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000010, '青岛啤酒', 1000005, 8.00, 'https://img.alicdn.com/imgextra/i10/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', '经典啤酒，清香爽口', 1, '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- 插入桌位信息
INSERT INTO `table_info` (`id`, `name`, `capacity`, `status`, `location`, `create_time`, `update_time`) VALUES
(1000001, '1号桌', 4, 1, '大厅左侧', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000002, '2号桌', 6, 1, '大厅中央', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000003, '3号桌', 8, 1, '大厅右侧', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000004, '4号桌', 4, 1, '包厢A', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000005, '5号桌', 2, 1, '包厢B', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- 插入员工数据
INSERT INTO `employee` (`id`, `user_id`, `name`, `phone`, `sex`, `id_number`, `position`, `hire_date`, `salary`, `address`, `create_time`, `update_time`) VALUES
(1000001, 1000001, '管理员', '13888888888', 1, '110101199001010001', '经理', '2023-01-01', 8000.00, '北京市朝阳区管理员住址', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000002, 1000002, '员工1', '13999999999', 1, '110101199001010002', '服务员', '2023-06-01', 4000.00, '北京市海淀区员工住址', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- 插入顾客数据
INSERT INTO `customer` (`id`, `user_id`, `points`, `member_level`, `name`, `phone`, `sex`, `address`, `register_time`, `create_time`, `update_time`) VALUES
(1000001, 1000003, 100, 1, '顾客1', '13777777777', 1, '北京市朝阳区测试地址1', '2024-01-01 00:00:00', '2024-01-01 00:00:00', '2024-01-01 00:00:00'),
(1000002, 1000004, 200, 2, '顾客2', '13666666666', 0, '北京市海淀区测试地址2', '2024-01-01 00:00:00', '2024-01-01 00:00:00', '2024-01-01 00:00:00');

-- 插入订单数据（今日订单用于首页统计）
INSERT INTO `orders` (`id`, `number`, `status`, `user_id`, `customer_id`, `employee_id`, `table_id`, `amount`, `remark`, `address`, `phone`, `consignee`, `order_time`, `checkout_time`, `payment_time`, `payment_method`, `complete_time`, `create_time`, `update_time`) VALUES
(1000001, 'ORD20250104001', 5, 1000003, 1000001, 1000002, 1000001, 68.00, '不要辣', '北京市朝阳区测试地址1', '13777777777', '顾客1', NOW(), DATE_ADD(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 30 MINUTE), 1, DATE_ADD(NOW(), INTERVAL 60 MINUTE), NOW(), NOW()),
(1000002, 'ORD20250104002', 5, 1000004, 1000002, 1000002, 1000002, 45.00, '多放醋', '北京市海淀区测试地址2', '13666666666', '顾客2', DATE_ADD(NOW(), INTERVAL -1 HOUR), DATE_ADD(NOW(), INTERVAL -30 MINUTE), DATE_ADD(NOW(), INTERVAL -30 MINUTE), 2, NOW(), DATE_ADD(NOW(), INTERVAL -1 HOUR), NOW()),
(1000003, 'ORD20250104003', 3, 1000003, 1000001, 1000002, 1000003, 52.00, '少盐', '北京市朝阳区测试地址1', '13777777777', '顾客1', DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL -90 MINUTE), DATE_ADD(NOW(), INTERVAL -90 MINUTE), 1, NULL, DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL -90 MINUTE));

-- 插入订单明细数据
INSERT INTO `order_detail` (`id`, `order_id`, `dish_id`, `name`, `image`, `price`, `number`, `amount`, `create_time`, `update_time`) VALUES
(1000001, 1000001, 1000001, '宫保鸡丁', 'https://img.alicdn.com/imgextra/i1/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 28.00, 2, 56.00, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
(1000002, 1000001, 1000008, '鲜榨橙汁', 'https://img.alicdn.com/imgextra/i8/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 12.00, 1, 12.00, '2024-01-01 12:00:00', '2024-01-01 12:00:00'),
(1000003, 1000002, 1000004, '口水鸡', 'https://img.alicdn.com/imgextra/i4/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 22.00, 1, 22.00, '2024-01-01 13:00:00', '2024-01-01 13:00:00'),
(1000004, 1000002, 1000006, '扬州炒饭', 'https://img.alicdn.com/imgextra/i6/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 15.00, 1, 15.00, '2024-01-01 13:00:00', '2024-01-01 13:00:00'),
(1000005, 1000002, 1000005, '凉拌黄瓜', 'https://img.alicdn.com/imgextra/i5/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 8.00, 1, 8.00, '2024-01-01 13:00:00', '2024-01-01 13:00:00'),
(1000006, 1000003, 1000003, '回锅肉', 'https://img.alicdn.com/imgextra/i3/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 32.00, 1, 32.00, '2024-01-01 18:00:00', '2024-01-01 18:00:00'),
(1000007, 1000003, 1000007, '红烧牛肉面', 'https://img.alicdn.com/imgextra/i7/2206356636456/O1CN01YGsYFx1zvE8bWzGfk_!!2206356636456.jpg', 25.00, 1, 25.00, '2024-01-01 18:00:00', '2024-01-01 18:00:00');

-- 插入评价数据
INSERT INTO `review` (`id`, `order_id`, `user_id`, `content`, `rating`, `reply`, `status`, `create_time`, `update_time`) VALUES
(1000001, 1000001, 1000003, '宫保鸡丁做得很好，味道正宗！橙汁也很新鲜。', 5, '谢谢您的好评，我们会继续努力！', 1, '2024-01-01 13:30:00', '2024-01-01 13:30:00'),
(1000002, 1000002, 1000004, '口水鸡很下饭，扬州炒饭粒粒分明，推荐！', 5, '感谢您的支持，欢迎再次光临！', 1, '2024-01-01 14:30:00', '2024-01-01 14:30:00'),
(1000003, 1000003, 1000003, '回锅肉还不错，但是牛肉面稍微有点咸。', 4, '感谢您的反馈，我们会改进口味。', 1, '2024-01-01 19:00:00', '2024-01-01 19:00:00'); 
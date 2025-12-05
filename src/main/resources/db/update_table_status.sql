-- 更新餐桌状态脚本
-- 将所有餐桌状态改为空闲（除了正在被使用的餐桌）

-- 首先，将所有餐桌状态设为空闲
UPDATE table_info SET status = 0 WHERE status = 1;

-- 然后，根据正在进行的订单，将对应餐桌设为占用状态
UPDATE table_info 
SET status = 2 
WHERE id IN (
    SELECT DISTINCT table_id 
    FROM orders 
    WHERE table_id IS NOT NULL 
    AND status IN (0, 1, 2, 3) -- 待支付、已支付、已确认、制作中的订单
);

-- 餐桌状态说明：
-- 0: 空闲可用
-- 1: 启用（暂时不使用）
-- 2: 已被占用 

-- 更新脚本：为order_detail表添加image列
-- 如果列不存在则添加
ALTER TABLE order_detail ADD COLUMN IF NOT EXISTS image VARCHAR(255);

-- 为现有的订单明细记录填充图片信息（从菜品表获取）
UPDATE order_detail od 
SET image = (
    SELECT d.image 
    FROM dish d 
    WHERE d.id = od.dish_id
) 
WHERE od.image IS NULL; 
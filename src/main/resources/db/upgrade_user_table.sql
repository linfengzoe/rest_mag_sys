-- 数据库升级脚本：删除用户表中的身份证号和性别字符串字段
-- 执行此脚本前请备份数据库

-- 检查字段是否存在，如果存在则删除
-- 删除身份证号字段
SET @exist_id_number = (SELECT COUNT(*) FROM information_schema.columns 
    WHERE table_schema=DATABASE() AND table_name='t_user' AND column_name='id_number');
SET @sql_id_number = IF(@exist_id_number > 0, 'ALTER TABLE t_user DROP COLUMN id_number', 'SELECT 1');
PREPARE stmt_id_number FROM @sql_id_number;
EXECUTE stmt_id_number;
DEALLOCATE PREPARE stmt_id_number;

-- 删除性别字符串字段
SET @exist_gender = (SELECT COUNT(*) FROM information_schema.columns 
    WHERE table_schema=DATABASE() AND table_name='t_user' AND column_name='gender');
SET @sql_gender = IF(@exist_gender > 0, 'ALTER TABLE t_user DROP COLUMN gender', 'SELECT 1');
PREPARE stmt_gender FROM @sql_gender;
EXECUTE stmt_gender;
DEALLOCATE PREPARE stmt_gender;

-- 为employee表添加address字段（如果不存在）
SET @exist_employee_address = (SELECT COUNT(*) FROM information_schema.columns 
    WHERE table_schema=DATABASE() AND table_name='employee' AND column_name='address');
SET @sql_employee_address = IF(@exist_employee_address = 0, 
    'ALTER TABLE employee ADD COLUMN address VARCHAR(255) AFTER salary', 
    'SELECT 1');
PREPARE stmt_employee_address FROM @sql_employee_address;
EXECUTE stmt_employee_address;
DEALLOCATE PREPARE stmt_employee_address;

-- 检查升级结果
SELECT 
    'User表字段检查' as 检查项目,
    GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) as 字段列表
FROM information_schema.columns 
WHERE table_schema=DATABASE() AND table_name='t_user'
UNION ALL
SELECT 
    'Employee表字段检查' as 检查项目,
    GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) as 字段列表
FROM information_schema.columns 
WHERE table_schema=DATABASE() AND table_name='employee';

-- 输出升级完成信息
SELECT '数据库升级完成！' as 状态, NOW() as 升级时间; 
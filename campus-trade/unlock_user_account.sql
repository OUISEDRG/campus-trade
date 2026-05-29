-- ==========================================
-- 解封用户账号脚本
-- ==========================================

-- 1. 查看当前用户状态
SELECT id, username, name, status, 
       CASE WHEN status = 0 THEN '正常' WHEN status = 1 THEN '封禁' ELSE '未知' END as status_text
FROM `user` WHERE username = '2373762281';

-- 2. 解封账号 - 将状态改为0（正常）
UPDATE `user` SET `status` = 0 WHERE username = '2373762281';

-- 3. 验证解封结果
SELECT id, username, name, status, 
       CASE WHEN status = 0 THEN '正常' WHEN status = 1 THEN '封禁' ELSE '未知' END as status_text
FROM `user` WHERE username = '2373762281';

-- 可选：解封所有被封禁的账号
-- UPDATE `user` SET `status` = 0 WHERE `status` = 1;

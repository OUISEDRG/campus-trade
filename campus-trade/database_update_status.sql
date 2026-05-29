-- ==========================================
-- 账号封禁功能 - 数据库字段补充
-- ==========================================

-- 1. 检查并添加 status 字段（如果不存在）
ALTER TABLE `user` 
    ADD COLUMN IF NOT EXISTS `status` INT DEFAULT 0 COMMENT '账号状态：0-正常，1-封禁' AFTER `phone`;

-- 2. 为已存在的所有用户设置为正常状态（0）
UPDATE `user` SET `status` = 0 WHERE `status` IS NULL OR `status` NOT IN (0, 1);

-- 3. 验证修改结果
SELECT id, username, name, status, 
       CASE WHEN status = 0 THEN '正常' WHEN status = 1 THEN '封禁' ELSE '未知' END as status_text
FROM `user`;

-- ==========================================
-- 聊天消息表结构
-- ==========================================

CREATE TABLE IF NOT EXISTS `chat_messages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
    `status` INT DEFAULT 0 COMMENT '消息状态：0-未读，1-已读',
    `goods_id` BIGINT COMMENT '关联商品ID（可选）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_sender_id (`sender_id`),
    INDEX idx_receiver_id (`receiver_id`),
    INDEX idx_create_time (`create_time`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 测试数据
INSERT INTO `chat_messages` (`sender_id`, `receiver_id`, `content`, `status`, `goods_id`, `create_time`) VALUES
(1, 2, '你好，我想问一下这个商品还在吗？', 0, 1, NOW() - INTERVAL 2 HOUR),
(2, 1, '在的，请问有什么问题吗？', 1, 1, NOW() - INTERVAL 1 HOUR),
(1, 2, '价格能便宜一点吗？', 0, 1, NOW() - INTERVAL 30 MINUTE),
(3, 1, '学长好，我是新来的学生', 0, NULL, NOW() - INTERVAL 10 MINUTE);

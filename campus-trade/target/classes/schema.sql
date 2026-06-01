CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255) UNIQUE COMMENT '用户名，必须唯一',
    `password` VARCHAR(255),
    `name` VARCHAR(255),
    `avatar` VARCHAR(255),
    `email` VARCHAR(255),
    `college` VARCHAR(255),
    `student_id` VARCHAR(255),
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone Number',
    `role` INT DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status` INT DEFAULT 0 COMMENT '状态: 0-正常, 1-封禁',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `goods` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT COMMENT '发布者ID',
    `title` VARCHAR(255) COMMENT '商品标题',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10, 2) COMMENT '价格',
    `image_url` VARCHAR(500) COMMENT '图片URL',
    `status` INT DEFAULT 0 COMMENT '状态: 0-在售, 1-已售, 2-下架',
    `category_name` VARCHAR(100) COMMENT '分类名称',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已下架/删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
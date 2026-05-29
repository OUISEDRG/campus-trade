CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(255),
    `password` VARCHAR(255),
    `name` VARCHAR(255),
    `avatar` VARCHAR(255),
    `email` VARCHAR(255),
    `college` VARCHAR(255),
    `student_id` VARCHAR(255),
    `phone` VARCHAR(20) DEFAULT NULL COMMENT 'Phone Number'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
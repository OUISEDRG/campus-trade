ALTER TABLE `user` ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已删除';
ALTER TABLE `goods` ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除: 0-正常, 1-已下架/删除';
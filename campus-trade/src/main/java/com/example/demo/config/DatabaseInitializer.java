package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 创建砍价表
            createBargainTables();
            
            // 创建交换表
            createExchangeTables();
            
            // 创建消息相关表
            createMessageTables();
            
            // 添加缺失的列
            addMissingColumns();
            
            // 插入默认消息模板
            insertMessageTemplates();
            
            System.out.println("数据库表结构初始化完成");
        } catch (Exception e) {
            System.out.println("数据库表结构初始化警告: " + e.getMessage());
        }
    }

    private void createBargainTables() {
        // 创建 bargain 表
        String sql = "CREATE TABLE IF NOT EXISTS `bargain` (" +
                "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "`goods_id` BIGINT COMMENT '商品ID', " +
                "`user_id` BIGINT COMMENT '发起用户ID', " +
                "`original_price` DECIMAL(10,2) COMMENT '原价', " +
                "`current_price` DECIMAL(10,2) COMMENT '当前价格', " +
                "`target_price` DECIMAL(10,2) COMMENT '目标价格', " +
                "`status` INT DEFAULT 0 COMMENT '状态: 0-进行中, 1-已成功, 2-已取消', " +
                "`max_participants` INT DEFAULT 10 COMMENT '最大参与人数', " +
                "`current_participants` INT DEFAULT 0 COMMENT '当前参与人数', " +
                "`start_time` DATETIME COMMENT '开始时间', " +
                "`end_time` DATETIME COMMENT '结束时间', " +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "`is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(sql);
        
        // 创建 bargain_record 表
        sql = "CREATE TABLE IF NOT EXISTS `bargain_record` (" +
                "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "`bargain_id` BIGINT COMMENT '砍价活动ID', " +
                "`user_id` BIGINT COMMENT '参与用户ID', " +
                "`cut_amount` DECIMAL(10,2) COMMENT '砍价金额', " +
                "`after_price` DECIMAL(10,2) COMMENT '砍后价格', " +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(sql);
    }

    private void createExchangeTables() {
        String sql = "CREATE TABLE IF NOT EXISTS `exchange` (" +
                "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "`initiator_id` BIGINT COMMENT '发起用户ID', " +
                "`target_user_id` BIGINT COMMENT '目标用户ID', " +
                "`initiator_goods_id` BIGINT COMMENT '发起方商品ID', " +
                "`target_goods_id` BIGINT COMMENT '目标商品ID', " +
                "`status` INT DEFAULT 0 COMMENT '状态: 0-待确认, 1-已接受, 2-已拒绝, 3-已取消', " +
                "`message` TEXT COMMENT '留言信息', " +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "`respond_time` DATETIME COMMENT '响应时间'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(sql);
    }

    private void createMessageTables() {
        String sql = "CREATE TABLE IF NOT EXISTS `message_template` (" +
                "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "`template_code` VARCHAR(100) UNIQUE COMMENT '模板编码', " +
                "`template_name` VARCHAR(255) COMMENT '模板名称', " +
                "`title` VARCHAR(255) COMMENT '消息标题', " +
                "`content` TEXT COMMENT '消息内容(支持变量替换)', " +
                "`type` VARCHAR(50) COMMENT '消息类型', " +
                "`status` INT DEFAULT 0 COMMENT '状态: 0-启用, 1-禁用', " +
                "`variables` TEXT COMMENT '变量列表(JSON)', " +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(sql);
        
        sql = "CREATE TABLE IF NOT EXISTS `message_record` (" +
                "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "`user_id` BIGINT COMMENT '用户ID', " +
                "`template_code` VARCHAR(100) COMMENT '模板编码', " +
                "`title` VARCHAR(255) COMMENT '消息标题', " +
                "`content` TEXT COMMENT '消息内容', " +
                "`status` INT DEFAULT 0 COMMENT '状态: 0-未读, 1-已读', " +
                "`push_type` VARCHAR(50) COMMENT '推送类型', " +
                "`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "`read_time` DATETIME COMMENT '阅读时间'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(sql);
    }

    private void addMissingColumns() {
        // 添加 openid 到 user 表
        try {
            jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `openid` VARCHAR(100) UNIQUE COMMENT '微信OpenID' AFTER `status`");
        } catch (Exception e) {
            // 可能已经存在
        }
        
        // 添加 create_time 到 user 表
        try {
            jdbcTemplate.execute("ALTER TABLE `user` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `openid`");
        } catch (Exception e) {
            // 可能已经存在
        }
        
        // 添加 create_time 到 goods 表
        try {
            jdbcTemplate.execute("ALTER TABLE `goods` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `is_deleted`");
        } catch (Exception e) {
            // 可能已经存在
        }
        
        // 添加 update_time 到 orders 表
        try {
            jdbcTemplate.execute("ALTER TABLE `orders` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `status`");
        } catch (Exception e) {
            // 可能已经存在
        }
    }

    private void insertMessageTemplates() {
        String[] codes = {"ORDER_CREATE", "ORDER_STATUS", "BARGAIN_SUCCESS", "EXCHANGE_REQUEST", "EXCHANGE_RESPONSE"};
        String[] names = {"订单创建通知", "订单状态变更", "砍价成功", "交换请求", "交换响应"};
        String[] titles = {"新订单通知", "订单状态更新", "砍价成功通知", "物品交换请求", "交换请求已处理"};
        String[] contents = {
                "您有一个新订单，商品：${goodsName}，金额：${amount}元",
                "您的订单#${orderId}状态已变更为：${status}",
                "恭喜！您参与的砍价活动\"${goodsName}\"已成功，最终价格：${price}元",
                "用户${userName}向您发起了物品交换请求，请查看详情",
                "您发起的交换请求已被${userName}${action}"
        };
        String[] types = {"order", "order", "bargain", "exchange", "exchange"};
        String[] vars = {
                "[\"goodsName\", \"amount\"]",
                "[\"orderId\", \"status\"]",
                "[\"goodsName\", \"price\"]",
                "[\"userName\"]",
                "[\"userName\", \"action\"]"
        };
        
        for (int i = 0; i < codes.length; i++) {
            try {
                jdbcTemplate.execute(
                        "INSERT IGNORE INTO `message_template` " +
                                "(`template_code`, `template_name`, `title`, `content`, `type`, `status`, `variables`) " +
                                "VALUES ('" + codes[i] + "', '" + names[i] + "', '" + titles[i] + "', '" + contents[i] + "', '" + types[i] + "', 0, '" + vars[i] + "')"
                );
            } catch (Exception e) {
                // 可能已经存在
            }
        }
    }
}

-- ============================================
-- 校园二手交易平台 - 数据库索引优化
-- 请在 campus_trade 数据库中执行此脚本
-- ============================================

-- user 表：按用户名和状态联合查询（登录+封禁检查）
ALTER TABLE user ADD INDEX idx_username_status (username, status);

-- goods 表：多维度查询索引
ALTER TABLE goods ADD INDEX idx_user_id (user_id);
ALTER TABLE goods ADD INDEX idx_status (status);
ALTER TABLE goods ADD INDEX idx_category_name (category_name);
ALTER TABLE goods ADD INDEX idx_status_category (status, category_name);
ALTER TABLE goods ADD INDEX idx_status_price (status, price);
ALTER TABLE goods ADD INDEX idx_status_create_time (status, id);

-- orders 表：按买家和卖家查询订单、关联商品
ALTER TABLE orders ADD INDEX idx_buyer_id (buyer_id);
ALTER TABLE orders ADD INDEX idx_seller_id (seller_id);
ALTER TABLE orders ADD INDEX idx_goods_id (goods_id);
ALTER TABLE orders ADD INDEX idx_status (status);
ALTER TABLE orders ADD INDEX idx_buyer_status (buyer_id, status);
ALTER TABLE orders ADD INDEX idx_seller_status (seller_id, status);

-- favorite 表：用户收藏查询（已在建表时添加唯一索引）
-- uk_user_goods 已覆盖 (user_id, goods_id) 查询

-- review 表：被评价人查询
-- idx_target_user 和 idx_order_id 已在建表时添加

-- message 表（如果存在）：私信查询
-- 检查表是否存在，存在则添加索引
-- ALTER TABLE message ADD INDEX IF NOT EXISTS idx_conversation (from_user_id, to_user_id, create_time);
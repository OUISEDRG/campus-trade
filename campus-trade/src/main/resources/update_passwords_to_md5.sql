-- 更新用户密码为 MD5 加密格式
-- "123456" 的 MD5 值为: e10adc3949ba59abbe56e057f20f883e

-- 更新 admin 用户密码为 MD5("123456")
UPDATE `user` SET password = 'e10adc3949ba59abbe56e057f20f883e' WHERE username = 'admin';

-- 更新其他用户密码也为 MD5("123456")，以便测试
UPDATE `user` SET password = 'e10adc3949ba59abbe56e057f20f883e';

-- 也可以为特定用户设置不同密码，比如 testuser:
-- UPDATE `user` SET password = 'e10adc3949ba59abbe56e057f20f883e' WHERE username = 'testuser';

-- 验证更新结果
SELECT id, username, password, name FROM `user`;

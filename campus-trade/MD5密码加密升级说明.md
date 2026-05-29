# MD5 密码加密升级说明

## 📋 修改内容

### 1. 后端代码修改 (UserController.java)
- ✅ 引入了 `org.springframework.util.DigestUtils`
- ✅ 登录接口：对用户输入的密码进行 MD5 加密后再比对
- ✅ 注册接口：对用户设置的密码进行 MD5 加密后再保存

### 2. 创建了 SQL 脚本
- `update_passwords_to_md5.sql` - 用于更新现有用户密码

---

## 🔧 应用步骤

### 第一步：执行 SQL 脚本更新数据库

打开 MySQL 客户端（如 Navicat、MySQL Workbench 或命令行），执行以下 SQL：

```sql
-- 使用你的数据库
USE campus_trade;

-- 更新用户密码为 MD5 加密格式
UPDATE `user` SET password = 'e10adc3949ba59abbe56e057f20f883e' WHERE username = 'admin';
UPDATE `user` SET password = 'e10adc3949ba59abbe56e057f20f883e';

-- 验证更新结果
SELECT id, username, password, name FROM `user`;
```

**注意**：`e10adc3949ba59abbe56e057f20f883e` 是 `"123456"` 的 MD5 值。

---

### 第二步：重新编译并启动后端

#### 方式一：使用 Maven 命令
```bash
cd d:\重写版毕设1\campus-trade
mvn clean compile
mvn spring-boot:run
```

#### 方式二：使用 IDE
1. 在 IDE（如 IntelliJ IDEA）中打开项目
2. 重新构建项目 (Build -> Rebuild Project)
3. 重新启动 Spring Boot 应用

---

### 第三步：测试登录

现在可以使用以下账号密码登录：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| testuser | 123456 | 普通用户 |

---

## 📊 MD5 加密值参考表

| 明文密码 | MD5 密文 |
|---------|---------|
| 123456 | e10adc3949ba59abbe56e057f20f883e |
| 111111 | 96e79218965eb72c92a549dd5a330114 |
| admin | 21232f297a57a5a743894a0e4a801fc3 |
| password | 5f4dcc3b5aa765d61d8327deb882cf99 |

---

## ⚠️ 注意事项

1. **现有用户密码**：如果数据库中已有用户，需要执行 SQL 脚本更新他们的密码为 MD5 格式，否则他们无法登录！
2. **新注册用户**：新注册的用户密码会自动使用 MD5 加密存储
3. **安全性**：MD5 已不再是最安全的加密算法，生产环境建议使用 BCrypt/Argon2 等更安全的算法

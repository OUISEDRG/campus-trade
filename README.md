# 校园二手交易平台

## 项目简介

校园二手交易平台是一个专为在校大学生打造的二手物品交易平台，旨在为学生提供一个安全、便捷的二手物品交易环境。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0+
- **ORM**: MyBatis-Plus 3.5.5
- **WebSocket**: Spring WebSocket

### 前端
- **框架**: Vue 3 + Vite 5
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios

## 功能特性

### 用户模块
- 用户注册与登录
- 用户信息管理
- 用户状态管理

### 商品模块
- 商品发布与管理
- 商品搜索与筛选
- 商品详情展示

### 订单模块
- 订单创建与管理
- 订单状态追踪

### 评论模块
- 商品评论功能

### 聊天模块
- WebSocket实时聊天

### 管理模块
- 管理员后台
- 用户管理
- 数据统计

## 项目结构

```
campus-trade/                    # 后端项目
  ├── src/main/java/com/example/demo/
  │   ├── controller/           # 控制层
  │   ├── service/              # 服务层
  │   ├── mapper/               # 数据访问层
  │   ├── entity/               # 实体类
  │   ├── dto/                  # 数据传输对象
  │   ├── config/               # 配置类
  │   ├── exception/            # 异常处理
  │   └── interceptor/          # 拦截器
  └── src/main/resources/
      ├── mapper/               # MyBatis映射文件
      └── application.yml       # 应用配置

campus-trade-vue/               # 前端项目
  ├── src/
  │   ├── views/                # 页面组件
  │   ├── components/           # 公共组件
  │   ├── router/               # 路由配置
  │   ├── stores/               # 状态管理
  │   ├── api/                  # API接口
  │   └── utils/                # 工具函数
  └── dist/                     # 构建产物
```

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- MySQL 8.0+

### 后端启动

1. 配置数据库连接信息：
   ```yaml
   # campus-trade/src/main/resources/application.yml
   spring:
     datasource:
       username: root
       password: your_password
       url: jdbc:mysql://localhost:3306/campus_trade?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
   ```

2. 创建数据库：
   ```sql
   CREATE DATABASE campus_trade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. 启动后端服务：
   ```bash
   cd campus-trade
   mvn spring-boot:run
   ```

### 前端启动

1. 安装依赖：
   ```bash
   cd campus-trade-vue
   npm install
   ```

2. 启动开发服务器：
   ```bash
   npm run dev
   ```

3. 构建生产版本：
   ```bash
   npm run build
   ```

## 访问地址

- **前端页面**: http://localhost:5173
- **后端API**: http://localhost:8080

## API接口示例

### 用户登录
```
POST /api/user/login
Content-Type: application/json

{
  "username": "test",
  "password": "123456"
}
```

### 获取商品列表
```
GET /api/goods?page=1&size=10&keyword=book
```

## 开发规范

- 代码风格遵循阿里巴巴Java开发手册
- 提交信息格式: `[模块] 功能描述`
- 分支管理采用Git Flow模式

## 许可证

MIT License
# 微服务教学系统

基于Vue + Spring Cloud + Nacos + MySQL的微服务教学系统

## 系统架构

### 后端微服务
- **user-service** (端口8081): 用户认证和用户管理
- **course-service** (端口8082): 课程管理和选课
- **homework-service** (端口8083): 作业发布和提交
- **gateway** (端口8080): API网关，统一入口

### 前端
- Vue 3 + Element Plus
- 运行端口: 3000

### 基础设施
- Nacos: 服务注册与发现 (127.0.0.1:8848)
- MySQL: 真实数据库 (localhost:3306)

## 快速开始

### 1. 环境要求
- JDK 11+
- Maven 3.6+
- Node.js 14+
- MySQL 8.0+
- Nacos Server

### 2. 数据库初始化
```bash
mysql -u root -p123456 < sql/init.sql
```

### 3. 启动后端微服务
```bash
# 安装common模块
cd common
mvn clean install -DskipTests

# 启动各个微服务（每个命令在新的终端窗口）
cd user-service && mvn spring-boot:run
cd course-service && mvn spring-boot:run
cd homework-service && mvn spring-boot:run
cd gateway && mvn spring-boot:run
```

### 4. 启动前端
```bash
cd frontend
npm install
npm run dev
```

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| teacher1 | 123456 | 教师 |
| teacher2 | 123456 | 教师 |
| teacher3 | 123456 | 教师 |
| student1 | 123456 | 学生 |
| student2 | 123456 | 学生 |
| student3 | 123456 | 学生 |
| student4 | 123456 | 学生 |
| student5 | 123456 | 学生 |
| student6 | 123456 | 学生 |

## 功能模块

### 学生端
- 查看可选课程
- 选修课程
- 查看已选课程
- 查看课程章节、课件和公告
- 在线观看课程视频，支持播放进度记录和弹幕
- 查看和提交作业

### 教师端
- 查看教授的课程
- 管理课程章节、视频、课件和公告
- 发布作业
- 查看课程学生列表
- 查看作业提交情况

### 管理员端
- 用户管理
- 课程管理
- 数据统计

## 视频地址说明

课程视频支持两类地址：

- 视频文件直链：如 `.mp4`、`.webm`、`.ogg`、`.mov`，会使用站内原生播放器播放，并支持进度记录和弹幕。
- 视频平台或网页链接：如 B 站、YouTube、腾讯视频等页面链接，会优先尝试站内嵌入播放；如果平台禁止嵌入，页面会提供“新窗口打开”入口。

注意：普通网页链接能否站内播放取决于目标网站是否允许被 iframe 嵌入，前端无法绕过平台的安全限制。

## 技术栈

- **前端**: Vue 3, Vue Router, Axios, Element Plus
- **后端**: Spring Boot, Spring Cloud, Spring Data JPA
- **服务治理**: Alibaba Nacos
- **数据库**: MySQL 8.0
- **构建工具**: Maven

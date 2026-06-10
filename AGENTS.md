# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## 环境要求

- JDK 11+、Maven 3.6+、Node.js 14+、MySQL 8.0+、Nacos Server（127.0.0.1:8848）

## 构建与运行

### 数据库初始化

```bash
mysql -u root -p123456 < sql/init.sql
```

数据库表也会由 JPA `ddl-auto: update` 自动管理；`sql/init.sql` 是权威的表结构定义。

### 后端（有顺序要求 — `common` 模块必须先安装）

```bash
# 安装共享依赖库
cd common && mvn clean install -DskipTests

# 启动各微服务（需在独立终端窗口中运行，common 安装后顺序不限）
cd user-service && mvn spring-boot:run       # 端口 8081
cd course-service && mvn spring-boot:run      # 端口 8082
cd homework-service && mvn spring-boot:run    # 端口 8083
cd gateway && mvn spring-boot:run             # 端口 8080
```

也可在父目录下执行 `mvn spring-boot:run -pl <模块名>`。

### 前端

```bash
cd frontend && npm install && npm run dev     # 端口 3000
```

访问 `http://localhost:3000`。Vite 开发服务器将 `/api` 请求代理到网关 `http://localhost:8080`。

### 生产构建

```bash
mvn clean package -DskipTests   # 构建所有后端 JAR 包
cd frontend && npm run build    # 输出到 frontend/dist/
```

本项目**没有任何测试** — 所有模块均不存在 `src/test` 目录，也未声明任何测试依赖。

## 架构

### 微服务（共享数据库模式）

四个 Spring Boot 2.7 服务，共享一个 JPA 实体库（`common`）：

| 模块 | 端口 | 职责 |
|--------|------|---------|
| `gateway` | 8080 | Spring Cloud Gateway — 将 `/api/user/**` 路由到 user-service，`/api/course/**` 路由到 course-service，`/api/homework/**` 路由到 homework-service。转发时去除 `/api` 前缀。 |
| `user-service` | 8081 | 用户认证（登录/注册）及用户增删改查 |
| `course-service` | 8082 | 课程管理、选课（course_selections）、评论 |
| `homework-service` | 8083 | 作业发布与提交/批改 |

三个业务服务共享**同一个 MySQL 数据库**（`teaching_system`）。虽然外表上是微服务架构，但服务之间仅在代码层面做了逻辑划分 — 它们都指向同一个数据库，没有按服务做数据隔离。Nacos 用于服务注册，但实际并未使用服务间 RPC 调用；网关直接将所有前端请求路由到对应服务。

### 共享库（`common/`）

包含所有 JPA 实体（`com.teaching.common.entity`）和通用 `Result<T>` 响应包装类（`com.teaching.common.result`）。每个业务服务都依赖此 JAR，并通过 `@EntityScan` / `@EnableJpaRepositories` 引用其中的包。

### 服务模块结构

每个业务服务遵循相同的分层结构：
```
src/main/java/com/teaching/<名称>/
  <Service>Application.java    — @SpringBootApplication + @EnableDiscoveryClient
  controller/<Service>Controller.java
  service/<Service>Service.java
  repository/<Something>Repository.java   — Spring Data JPA 接口
src/main/resources/
  application.yml    — 服务端口、数据源配置（root/123456）、JPA 配置
  bootstrap.yml      — spring.application.name、Nacos 配置中心地址
```

### 前端

Vue 3 单页应用，使用 Element Plus 组件库。Axios 客户端（`src/api/index.js`）使用请求拦截器注入 Bearer 令牌，使用响应拦截器解包 `Result<T>` 封装 — API 响应格式为 `{ code: 200, message: "...", data: ... }`，拦截器提取 `.data`，使组件直接拿到实际数据。

路由：`/login`、`/student`、`/student/course/:courseId`、`/teacher`、`/teacher/course/:courseId`、`/admin`。

### 配置注意事项

- **密码以明文存储**（`123456`）在种子数据中。README 明确指出这不适合生产环境。
- **数据库凭据硬编码**（`root` / `123456`）在每个服务的 `application.yml` 中。
- **Nacos 地址硬编码**为 `127.0.0.1:8848`，命名空间 `public`，所有服务和网关均如此。
- **JPA `ddl-auto: update`** — Hibernate 在启动时会自动修改表结构，若与 `sql/init.sql` 不一致可能导致冲突。注意表结构变更可能来自这两种途径中的任何一个。

### 端口汇总

| 端口 | 组件 |
|------|-----------|
| 3000 | 前端（Vite 开发服务器） |
| 8080 | API 网关 |
| 8081 | user-service |
| 8082 | course-service |
| 8083 | homework-service |
| 8848 | Nacos（外部服务） |
| 3306 | MySQL（外部服务） |

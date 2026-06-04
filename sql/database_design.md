# 微服务教学系统 - 数据库设计文档

## 1. 数据库概述

本数据库设计用于支持微服务教学系统，包含用户管理、课程管理、选课管理、作业管理等核心功能模块。

**数据库名称**: `teaching_system`  
**字符集**: `utf8mb4`  
**排序规则**: `utf8mb4_unicode_ci`  
**存储引擎**: `InnoDB`

---

## 2. 实体关系图 (ERD)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────────┐
│     users       │       │    courses      │       │  course_selections │
├─────────────────┤       ├─────────────────┤       ├─────────────────────┤
│ id (PK)         │◄──────│ id (PK)         │──────►│ id (PK)             │
│ username (UK)   │       │ name            │       │ course_id (FK)      │
│ password        │       │ code (UK)       │       │ student_id (FK)     │
│ name            │       │ teacher_id (FK) │       │ status              │
│ role (ENUM)     │       │ teacher_name    │       │ grade               │
│ email           │       │ description     │       │ selected_at         │
│ phone           │       │ credit          │       │ dropped_at          │
│ department      │       │ max_students    │       └─────────────────────┘
│ status (ENUM)   │       │ current_students│
│ created_at      │       │ semester        │
│ updated_at      │       │ status (ENUM)   │
└─────────────────┘       │ created_at      │       ┌─────────────────┐
                          │ updated_at      │       │   homeworks     │
                          └─────────────────┘       ├─────────────────┤
                                                   │ id (PK)         │
                                                   │ title           │
                                                   │ course_id (FK)  │
                                                   │ course_name     │
                                                   │ description     │
                                                   │ max_score       │
                                                   │ deadline        │
                                                   │ status (ENUM)   │
                                                   │ created_at      │
                                                   │ updated_at      │
                                                   └────────┬────────┘
                                                            │
                                                            ▼
                                                   ┌──────────────────────┐
                                                   │ homework_submissions │
                                                   ├──────────────────────┤
                                                   │ id (PK)              │
                                                   │ homework_id (FK)     │
                                                   │ student_id (FK)      │
                                                   │ student_name         │
                                                   │ content              │
                                                   │ file_path            │
                                                   │ score                │
                                                   │ comment              │
                                                   │ status (ENUM)        │
                                                   │ submit_time          │
                                                   │ graded_at            │
                                                   └──────────────────────┘
```

---

## 3. 表结构详情

### 3.1 users 表 - 用户表

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 用户唯一标识 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | - | 用户名（登录账号） |
| password | VARCHAR(255) | NOT NULL | - | 密码（加密存储） |
| name | VARCHAR(50) | NOT NULL | - | 真实姓名 |
| role | ENUM | NOT NULL | student | 角色：student/teacher/admin |
| email | VARCHAR(100) | - | - | 邮箱地址 |
| phone | VARCHAR(20) | - | - | 手机号码 |
| department | VARCHAR(100) | - | - | 所属部门/院系 |
| status | ENUM | NOT NULL | active | 账号状态：active/inactive |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- `idx_username` (username)
- `idx_role` (role)
- `idx_status` (status)
- `idx_created_at` (created_at)

---

### 3.2 courses 表 - 课程表

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 课程唯一标识 |
| name | VARCHAR(100) | NOT NULL | - | 课程名称 |
| code | VARCHAR(50) | UNIQUE | - | 课程编号 |
| teacher_id | BIGINT | NOT NULL, FOREIGN KEY | - | 授课教师ID |
| teacher_name | VARCHAR(50) | - | - | 授课教师姓名 |
| description | TEXT | - | - | 课程描述 |
| credit | INT | - | 3 | 学分 |
| max_students | INT | - | 50 | 最大选课人数 |
| current_students | INT | - | 0 | 当前选课人数 |
| semester | VARCHAR(20) | - | - | 学期（如：2024-2025-1） |
| status | ENUM | NOT NULL | open | 课程状态：open/closed |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- `idx_teacher_id` (teacher_id)
- `idx_name` (name)
- `idx_semester` (semester)
- `idx_status` (status)

**外键约束**:
- `fk_course_teacher`: teacher_id → users(id) ON DELETE CASCADE

---

### 3.3 course_selections 表 - 选课记录表

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 选课记录唯一标识 |
| course_id | BIGINT | NOT NULL, FOREIGN KEY | - | 课程ID |
| student_id | BIGINT | NOT NULL, FOREIGN KEY | - | 学生ID |
| status | ENUM | NOT NULL | selected | 选课状态：selected/dropped |
| grade | DECIMAL(5,2) | - | - | 成绩 |
| selected_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 选课时间 |
| dropped_at | DATETIME | - | - | 退选时间 |

**唯一约束**:
- `uk_course_student` (course_id, student_id)

**索引**:
- `idx_student_id` (student_id)
- `idx_status` (status)

**外键约束**:
- `fk_selection_course`: course_id → courses(id) ON DELETE CASCADE
- `fk_selection_student`: student_id → users(id) ON DELETE CASCADE

---

### 3.4 homeworks 表 - 作业表

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 作业唯一标识 |
| title | VARCHAR(200) | NOT NULL | - | 作业标题 |
| course_id | BIGINT | NOT NULL, FOREIGN KEY | - | 所属课程ID |
| course_name | VARCHAR(100) | - | - | 所属课程名称 |
| description | TEXT | - | - | 作业描述/要求 |
| max_score | INT | - | 100 | 满分分数 |
| deadline | DATETIME | NOT NULL | - | 截止时间 |
| status | ENUM | NOT NULL | active | 作业状态：active/closed |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 创建时间 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 更新时间 |

**索引**:
- `idx_course_id` (course_id)
- `idx_deadline` (deadline)
- `idx_status` (status)

**外键约束**:
- `fk_homework_course`: course_id → courses(id) ON DELETE CASCADE

---

### 3.5 homework_submissions 表 - 作业提交表

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|--------|------|------|--------|------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 作业提交唯一标识 |
| homework_id | BIGINT | NOT NULL, FOREIGN KEY | - | 作业ID |
| student_id | BIGINT | NOT NULL, FOREIGN KEY | - | 学生ID |
| student_name | VARCHAR(50) | - | - | 学生姓名 |
| content | TEXT | NOT NULL | - | 作业内容 |
| file_path | VARCHAR(255) | - | - | 附件路径 |
| score | INT | - | - | 批改分数 |
| comment | TEXT | - | - | 批改评语 |
| status | ENUM | NOT NULL | submitted | 提交状态：submitted/graded |
| submit_time | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 提交时间 |
| graded_at | DATETIME | - | - | 批改时间 |

**唯一约束**:
- `uk_homework_student` (homework_id, student_id)

**索引**:
- `idx_student_id` (student_id)
- `idx_status` (status)
- `idx_submit_time` (submit_time)

**外键约束**:
- `fk_submission_homework`: homework_id → homeworks(id) ON DELETE CASCADE
- `fk_submission_student`: student_id → users(id) ON DELETE CASCADE

---

## 4. 数据字典

### 4.1 角色枚举 (Role)

| 值 | 说明 |
|----|------|
| student | 学生 |
| teacher | 教师 |
| admin | 管理员 |

### 4.2 用户状态枚举 (UserStatus)

| 值 | 说明 |
|----|------|
| active | 正常 |
| inactive | 禁用 |

### 4.3 课程状态枚举 (CourseStatus)

| 值 | 说明 |
|----|------|
| open | 开放选课 |
| closed | 关闭选课 |

### 4.4 选课状态枚举 (SelectionStatus)

| 值 | 说明 |
|----|------|
| selected | 已选 |
| dropped | 已退选 |

### 4.5 作业状态枚举 (HomeworkStatus)

| 值 | 说明 |
|----|------|
| active | 进行中 |
| closed | 已截止 |

### 4.6 提交状态枚举 (SubmissionStatus)

| 值 | 说明 |
|----|------|
| submitted | 已提交 |
| graded | 已批改 |

---

## 5. 业务规则

### 5.1 用户管理
- 用户名必须唯一
- 密码需要加密存储（实际生产环境）
- 角色分为学生、教师、管理员三种

### 5.2 课程管理
- 一门课程只能由一位教师授课
- 课程有最大选课人数限制
- 当前选课人数需要与选课记录表保持同步

### 5.3 选课管理
- 一个学生不能重复选择同一门课程
- 学生可以退选课程
- 成绩范围：0-100

### 5.4 作业管理
- 作业必须关联到具体课程
- 作业有截止时间限制
- 学生可以提交作业，教师可以批改

---

## 6. 测试数据说明

初始化脚本包含以下测试数据：

**用户**: 10个（1管理员 + 3教师 + 6学生）
**课程**: 6门（涵盖不同学期）
**选课记录**: 11条
**作业**: 7个
**作业提交**: 5条（含批改记录）

---

## 7. 数据库初始化

```bash
# 创建数据库并导入数据
mysql -u root -p123456 < sql/init.sql
```

---

## 8. 性能优化建议

1. **索引优化**: 已在各表建立必要索引，支持常用查询场景
2. **数据分区**: 对于大数据量场景，可按学期对课程表进行分区
3. **读写分离**: 生产环境建议配置主从复制，实现读写分离
4. **缓存策略**: 对课程信息、用户信息等热点数据进行缓存
5. **定期清理**: 定期清理过期的作业提交记录（如需）

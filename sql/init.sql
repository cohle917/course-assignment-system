DROP DATABASE IF EXISTS teaching_system;
CREATE DATABASE teaching_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE teaching_system;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（登录账号）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    role ENUM('student', 'teacher', 'admin') NOT NULL DEFAULT 'student' COMMENT '角色：学生/教师/管理员',
    email VARCHAR(100) COMMENT '邮箱地址',
    phone VARCHAR(20) COMMENT '手机号码',
    department VARCHAR(100) COMMENT '所属部门/院系',
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT '账号状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    code VARCHAR(50) UNIQUE COMMENT '课程编号',
    teacher_id BIGINT NOT NULL COMMENT '授课教师ID',
    teacher_name VARCHAR(50) COMMENT '授课教师姓名',
    description TEXT COMMENT '课程描述',
    category VARCHAR(50) COMMENT '课程分类',
    department VARCHAR(100) COMMENT '开课院系',
    cover_image VARCHAR(255) COMMENT '封面图URL',
    syllabus TEXT COMMENT '课程大纲',
    credit INT DEFAULT 3 COMMENT '学分',
    max_students INT DEFAULT 50 COMMENT '最大选课人数',
    current_students INT DEFAULT 0 COMMENT '当前选课人数',
    popularity INT DEFAULT 0 COMMENT '热度值',
    semester VARCHAR(20) COMMENT '学期（如：2024-2025-1）',
    status ENUM('open', 'closed') NOT NULL DEFAULT 'open' COMMENT '课程状态：开放/关闭',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_name (name),
    INDEX idx_category (category),
    INDEX idx_department (department),
    INDEX idx_popularity (popularity),
    INDEX idx_semester (semester),
    INDEX idx_status (status),
    CONSTRAINT fk_course_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE IF NOT EXISTS course_selections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选课记录ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    status ENUM('selected', 'dropped') NOT NULL DEFAULT 'selected' COMMENT '选课状态：已选/已退选',
    grade DECIMAL(5,2) COMMENT '成绩',
    selected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    dropped_at DATETIME COMMENT '退选时间',
    
    UNIQUE KEY uk_course_student (course_id, student_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status),
    CONSTRAINT fk_selection_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_selection_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录表';

CREATE TABLE IF NOT EXISTS course_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程评论ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    user_name VARCHAR(50) NOT NULL COMMENT '用户姓名',
    user_role VARCHAR(20) NOT NULL COMMENT '用户角色',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID',
    content TEXT NOT NULL COMMENT '评论内容',
    rating INT COMMENT '评分1-5',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_id (course_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    CONSTRAINT fk_comment_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES course_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评论表';

CREATE TABLE IF NOT EXISTS course_chapters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    title VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chapter_course_id (course_id),
    INDEX idx_chapter_parent_id (parent_id),
    INDEX idx_chapter_sort (course_id, sort_order),
    CONSTRAINT fk_chapter_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course chapters';

CREATE TABLE IF NOT EXISTS course_videos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    video_url VARCHAR(1000) NOT NULL,
    duration INT DEFAULT 0,
    sort_order INT DEFAULT 0,
    status ENUM('draft', 'published', 'hidden') NOT NULL DEFAULT 'draft',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_video_course_id (course_id),
    INDEX idx_video_chapter_id (chapter_id),
    INDEX idx_video_status (status),
    INDEX idx_video_sort (chapter_id, sort_order),
    CONSTRAINT fk_video_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_video_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course videos';

CREATE TABLE IF NOT EXISTS course_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    chapter_id BIGINT NULL,
    title VARCHAR(150) NOT NULL,
    file_type VARCHAR(50),
    resource_url VARCHAR(1000) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material_course_id (course_id),
    INDEX idx_material_chapter_id (chapter_id),
    INDEX idx_material_sort (course_id, sort_order),
    CONSTRAINT fk_material_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_material_chapter FOREIGN KEY (chapter_id) REFERENCES course_chapters(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course materials';

CREATE TABLE IF NOT EXISTS course_announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    published_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_announcement_course_id (course_id),
    INDEX idx_announcement_published_at (published_at),
    CONSTRAINT fk_announcement_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='course announcements';

CREATE TABLE IF NOT EXISTS video_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    last_position INT DEFAULT 0,
    duration INT DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_progress_student_video (student_id, video_id),
    INDEX idx_progress_video_id (video_id),
    CONSTRAINT fk_progress_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_video FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='video progress';

CREATE TABLE IF NOT EXISTS video_danmaku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    student_name VARCHAR(50),
    time_seconds INT DEFAULT 0,
    content VARCHAR(200) NOT NULL,
    color VARCHAR(20) DEFAULT '#ffffff',
    status ENUM('visible', 'hidden') NOT NULL DEFAULT 'visible',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_danmaku_video_time (video_id, time_seconds),
    INDEX idx_danmaku_course_id (course_id),
    CONSTRAINT fk_danmaku_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_danmaku_video FOREIGN KEY (video_id) REFERENCES course_videos(id) ON DELETE CASCADE,
    CONSTRAINT fk_danmaku_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='video danmaku';

CREATE TABLE IF NOT EXISTS homeworks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业ID',
    title VARCHAR(200) NOT NULL COMMENT '作业标题',
    course_id BIGINT NOT NULL COMMENT '所属课程ID',
    course_name VARCHAR(100) COMMENT '所属课程名称',
    description TEXT COMMENT '作业描述/要求',
    max_score INT DEFAULT 100 COMMENT '满分分数',
    deadline DATETIME NOT NULL COMMENT '截止时间',
    status ENUM('active', 'closed') NOT NULL DEFAULT 'active' COMMENT '作业状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_course_id (course_id),
    INDEX idx_deadline (deadline),
    INDEX idx_status (status),
    CONSTRAINT fk_homework_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业表';

CREATE TABLE IF NOT EXISTS homework_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '作业提交ID',
    homework_id BIGINT NOT NULL COMMENT '作业ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_name VARCHAR(50) COMMENT '学生姓名',
    content TEXT NOT NULL COMMENT '作业内容',
    file_path VARCHAR(255) COMMENT '附件路径',
    score INT COMMENT '批改分数',
    comment TEXT COMMENT '批改评语',
    status ENUM('submitted', 'graded') NOT NULL DEFAULT 'submitted' COMMENT '提交状态：已提交/已批改',
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    graded_at DATETIME COMMENT '批改时间',
    
    UNIQUE KEY uk_homework_student (homework_id, student_id),
    INDEX idx_student_id (student_id),
    INDEX idx_status (status),
    INDEX idx_submit_time (submit_time),
    CONSTRAINT fk_submission_homework FOREIGN KEY (homework_id) REFERENCES homeworks(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作业提交表';

INSERT INTO users (username, password, name, role, email, phone, department) VALUES
('admin', '123456', '系统管理员', 'admin', 'admin@teaching.edu.cn', '13800138000', '信息中心'),
('teacher1', '123456', '张明教授', 'teacher', 'zhangming@teaching.edu.cn', '13900139001', '计算机学院'),
('teacher2', '123456', '李华副教授', 'teacher', 'lihua@teaching.edu.cn', '13900139002', '计算机学院'),
('teacher3', '123456', '王芳讲师', 'teacher', 'wangfang@teaching.edu.cn', '13900139003', '软件工程系'),
('student1', '123456', '赵小明', 'student', 'zhaoxiaoming@stu.teaching.edu.cn', '13900139101', '计算机学院'),
('student2', '123456', '李晓红', 'student', 'lixiaohong@stu.teaching.edu.cn', '13900139102', '计算机学院'),
('student3', '123456', '陈伟', 'student', 'chenwei@stu.teaching.edu.cn', '13900139103', '软件工程系'),
('student4', '123456', '刘洋', 'student', 'liuyang@stu.teaching.edu.cn', '13900139104', '软件工程系'),
('student5', '123456', '赵敏', 'student', 'zhaomin@stu.teaching.edu.cn', '13900139105', '计算机学院'),
('student6', '123456', '孙磊', 'student', 'sunlei@stu.teaching.edu.cn', '13900139106', '计算机学院');

INSERT INTO courses (name, code, teacher_id, teacher_name, description, category, department, cover_image, syllabus, credit, max_students, semester) VALUES
('Java程序设计', 'CS101', 2, '张明教授', '本课程介绍Java编程语言的基础知识，包括面向对象编程、异常处理、集合框架等核心内容。', '专业必修', '计算机学院', 'https://picsum.photos/seed/java-course/800/450', '第1章 Java开发环境\n第2章 基础语法\n第3章 面向对象编程\n第4章 集合框架\n第5章 异常处理与实践项目', 3, 40, '2024-2025-1'),
('数据结构与算法', 'CS102', 2, '张明教授', '学习常用数据结构（数组、链表、栈、队列、树、图等）及经典算法设计与分析。', '专业必修', '计算机学院', 'https://picsum.photos/seed/algorithm-course/800/450', '第1章 算法复杂度\n第2章 线性表\n第3章 栈与队列\n第4章 树和图\n第5章 排序与查找', 4, 40, '2024-2025-1'),
('数据库原理与应用', 'CS103', 3, '李华副教授', '介绍关系型数据库理论、SQL语言、数据库设计及MySQL数据库的实践应用。', '通识选修', '计算机学院', 'https://picsum.photos/seed/database-course/800/450', '第1章 数据库系统概述\n第2章 关系模型\n第3章 SQL查询\n第4章 数据库设计\n第5章 事务与索引', 3, 50, '2024-2025-1'),
('Web前端开发', 'CS104', 4, '王芳讲师', '学习HTML5、CSS3、JavaScript及Vue.js框架，掌握现代Web前端开发技术。', '通识选修', '软件工程系', 'https://picsum.photos/seed/frontend-course/800/450', '第1章 HTML与CSS基础\n第2章 JavaScript语法\n第3章 DOM与事件\n第4章 Vue基础\n第5章 前端工程化', 3, 45, '2024-2025-1'),
('操作系统', 'CS201', 2, '张明教授', '深入学习操作系统原理，包括进程管理、内存管理、文件系统等核心概念。', '专业选修', '计算机学院', 'https://picsum.photos/seed/os-course/800/450', '第1章 操作系统概述\n第2章 进程与线程\n第3章 调度算法\n第4章 内存管理\n第5章 文件系统', 4, 35, '2024-2025-2'),
('计算机网络', 'CS202', 3, '李华副教授', '介绍计算机网络的基本原理，包括TCP/IP协议栈、网络安全等内容。', '专业选修', '计算机学院', 'https://picsum.photos/seed/network-course/800/450', '第1章 网络体系结构\n第2章 数据链路层\n第3章 网络层\n第4章 传输层\n第5章 应用层协议', 3, 40, '2024-2025-2');

INSERT INTO course_selections (course_id, student_id, selected_at) VALUES
(1, 5, '2024-09-01 08:00:00'),
(1, 6, '2024-09-01 08:30:00'),
(1, 7, '2024-09-01 09:00:00'),
(2, 5, '2024-09-01 08:15:00'),
(2, 6, '2024-09-01 08:45:00'),
(3, 5, '2024-09-01 09:30:00'),
(3, 8, '2024-09-01 10:00:00'),
(3, 9, '2024-09-01 10:30:00'),
(4, 7, '2024-09-01 11:00:00'),
(4, 8, '2024-09-01 11:30:00'),
(4, 10, '2024-09-01 12:00:00');

INSERT INTO course_comments (course_id, user_id, username, user_name, user_role, parent_id, content, rating, created_at, updated_at) VALUES
(1, 5, 'student1', '赵小明', 'student', NULL, '老师讲得很清楚，Java环境配置部分很适合入门。', 5, '2024-09-10 09:30:00', '2024-09-10 09:30:00'),
(1, 6, 'student2', '李晓红', 'student', NULL, '希望后面能多安排一些面向对象的练习。', 4, '2024-09-11 10:20:00', '2024-09-11 10:20:00'),
(3, 8, 'student4', '刘洋', 'student', NULL, 'SQL查询部分案例很实用。', 5, '2024-09-12 14:10:00', '2024-09-12 14:10:00'),
(4, 7, 'student3', '陈伟', 'student', NULL, 'Vue课程内容比较新，适合做项目。', 5, '2024-09-13 16:45:00', '2024-09-13 16:45:00');

INSERT INTO course_comments (course_id, user_id, username, user_name, user_role, parent_id, content, rating, created_at, updated_at) VALUES
(1, 2, 'teacher1', '张明教授', 'teacher', 2, '后续章节会安排类设计和综合项目练习。', NULL, '2024-09-11 11:00:00', '2024-09-11 11:00:00');

INSERT INTO course_chapters (course_id, parent_id, title, sort_order) VALUES
(1, NULL, 'Java入门', 1),
(1, NULL, '面向对象基础', 2),
(1, 2, '类与对象', 1),
(1, 2, '继承与多态', 2),
(2, NULL, '线性结构', 1),
(2, NULL, '排序算法', 2),
(3, NULL, 'SQL基础', 1),
(4, NULL, 'Vue基础', 1);

INSERT INTO course_videos (course_id, chapter_id, title, description, video_url, duration, sort_order, status) VALUES
(1, 1, 'Java课程导学', '介绍课程目标、学习方式和开发环境。', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', 30, 1, 'published'),
(1, 3, '类与对象示例', '通过简单案例理解类、对象、字段和方法。', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.webm', 30, 2, 'published'),
(2, 5, '链表结构演示', '演示单链表节点关系和基本操作。', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', 30, 1, 'published'),
(3, 7, 'SQL查询入门', '演示SELECT、WHERE和ORDER BY基础用法。', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', 30, 1, 'published'),
(4, 8, 'Vue组件基础', '介绍Vue组件结构和基础交互。', 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', 30, 1, 'published');

INSERT INTO course_materials (course_id, chapter_id, title, file_type, resource_url, sort_order) VALUES
(1, 1, 'Java开发环境安装指南', 'PDF', 'https://example.com/materials/java-setup.pdf', 1),
(1, 3, '类与对象课堂讲义', 'PPT', 'https://example.com/materials/java-oop.pptx', 2),
(2, 5, '链表练习题', 'DOC', 'https://example.com/materials/linked-list-exercises.docx', 1),
(3, 7, 'SQL练习数据集', 'URL', 'https://example.com/materials/sql-practice', 1),
(4, 8, 'Vue项目模板', 'URL', 'https://example.com/materials/vue-template', 1);

INSERT INTO course_announcements (course_id, title, content, published_at) VALUES
(1, '课程开课通知', 'Java程序设计课程已开放，请同学们先完成环境配置。', '2024-09-01 08:00:00'),
(1, '第一次作业提醒', 'Java基础练习截止时间为2024-10-15 23:59:59，请按时提交。', '2024-10-10 09:00:00'),
(2, '实验课安排', '本周实验课将练习链表实现，请提前阅读讲义。', '2024-10-12 09:00:00'),
(3, 'SQL练习说明', '数据库原理课程已发布SQL练习作业，请下载练习数据集。', '2024-10-12 10:00:00'),
(4, '前端项目说明', 'Web前端课程将以个人简历页面作为第一次实践项目。', '2024-10-13 10:00:00');

INSERT INTO homeworks (title, course_id, course_name, description, max_score, deadline) VALUES
('Java基础练习', 1, 'Java程序设计', '1. 编写Hello World程序\n2. 实现两个数的加减乘除运算\n3. 使用循环打印九九乘法表', 100, '2024-10-15 23:59:59'),
('数组与方法作业', 1, 'Java程序设计', '1. 实现数组的反转\n2. 编写方法求数组最大值和最小值\n3. 实现冒泡排序算法', 100, '2024-10-25 23:59:59'),
('链表实现', 2, '数据结构与算法', '使用Java实现单链表的增删改查操作', 100, '2024-10-20 23:59:59'),
('快速排序实现', 2, '数据结构与算法', '实现快速排序算法，并分析时间复杂度', 100, '2024-11-05 23:59:59'),
('SQL查询练习', 3, '数据库原理与应用', '基于给定的学生数据库完成10个SQL查询题目', 100, '2024-10-18 23:59:59'),
('数据库设计', 3, '数据库原理与应用', '设计一个图书管理系统的数据库ER图，并写出建表语句', 100, '2024-11-10 23:59:59'),
('HTML页面设计', 4, 'Web前端开发', '使用HTML5和CSS3设计一个个人简历页面', 100, '2024-10-22 23:59:59');

INSERT INTO homework_submissions (homework_id, student_id, student_name, content, score, comment, status, submit_time, graded_at) VALUES
(1, 5, '赵小明', '```java\npublic class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello World!\");\n    }\n}\n```\n\n加减乘除运算和九九乘法表已完成。', 95, '代码结构清晰，注释规范，完成所有要求。', 'graded', '2024-10-14 20:30:00', '2024-10-16 10:00:00'),
(1, 6, '李晓红', 'Hello World程序和运算功能已完成。', 88, '完成基本功能，但代码格式不够规范。', 'graded', '2024-10-15 22:45:00', '2024-10-16 11:00:00'),
(3, 5, '赵小明', '```java\npublic class LinkedList {\n    // 链表实现代码...\n}\n```', 92, '实现完整，算法正确。', 'graded', '2024-10-19 18:00:00', '2024-10-21 09:30:00'),
(5, 8, '刘洋', 'SELECT * FROM students WHERE age > 18;\n-- 其他查询语句...', 85, '大部分查询正确，第5题有小错误。', 'graded', '2024-10-17 23:00:00', '2024-10-19 14:00:00'),
(7, 8, '刘洋', '已完成个人简历页面设计，包含教育经历、技能介绍等模块。', 90, '页面设计美观，布局合理。', 'graded', '2024-10-21 21:00:00', '2024-10-23 10:30:00');

INSERT INTO video_progress (student_id, video_id, last_position, duration) VALUES
(5, 1, 12, 30),
(6, 1, 30, 30),
(5, 2, 8, 30),
(5, 3, 15, 30),
(8, 4, 20, 30);

INSERT INTO video_danmaku (course_id, video_id, student_id, student_name, time_seconds, content, color, status, created_at) VALUES
(1, 1, 5, '赵小明', 3, '这里的环境配置步骤很清楚', '#ffffff', 'visible', '2024-09-10 09:35:00'),
(1, 1, 6, '李晓红', 10, '建议暂停跟着操作一遍', '#67c23a', 'visible', '2024-09-10 09:40:00'),
(1, 2, 5, '赵小明', 6, '类和对象的例子很好理解', '#409eff', 'visible', '2024-09-12 14:30:00'),
(3, 4, 8, '刘洋', 12, 'WHERE条件这里要注意顺序', '#e6a23c', 'visible', '2024-09-13 10:15:00');

UPDATE courses c 
JOIN (
    SELECT course_id, COUNT(*) as cnt 
    FROM course_selections 
    WHERE status = 'selected' 
    GROUP BY course_id
) s ON c.id = s.course_id 
SET c.current_students = s.cnt,
    c.popularity = s.cnt * 10;

UPDATE courses c
JOIN (
    SELECT course_id, COUNT(*) as cnt
    FROM course_comments
    WHERE parent_id IS NULL AND rating IS NOT NULL
    GROUP BY course_id
) r ON c.id = r.course_id
SET c.popularity = c.popularity + r.cnt * 5;

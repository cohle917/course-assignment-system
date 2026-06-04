-- 课程评论表
CREATE TABLE IF NOT EXISTS course_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    user_role VARCHAR(20) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES course_comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入一些测试评论数据
INSERT INTO course_comments (course_id, user_id, username, user_name, user_role, parent_id, content, created_at, updated_at)
VALUES 
    (1, 1, 'admin', '管理员', 'admin', NULL, '欢迎大家来到这门课程，有问题可以在评论区讨论！', NOW(), NOW()),
    (1, 2, 'teacher_zhang', '张老师', 'teacher', NULL, '课程将于下周正式开始，请同学们提前预习。', NOW(), NOW()),
    (1, 3, 'student_001', '小明', 'student', NULL, '老师好，这门课需要什么基础吗？', NOW(), NOW()),
    (1, 2, 'teacher_zhang', '张老师', 'teacher', 3, '需要掌握基本的编程知识，我们会从基础讲起。', NOW(), NOW());

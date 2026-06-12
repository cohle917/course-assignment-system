-- 课程浏览功能增强 - 数据库迁移 v2
-- 执行方式: mysql -u root -p123456 --default-character-set=utf8mb4 < sql/migration_v2.sql

USE teaching_system;

-- 1. 课程表新增字段
ALTER TABLE courses ADD COLUMN category VARCHAR(50) COMMENT '课程分类' AFTER description;
ALTER TABLE courses ADD COLUMN department VARCHAR(100) COMMENT '开课院系' AFTER category;
ALTER TABLE courses ADD COLUMN cover_image VARCHAR(255) COMMENT '封面图URL' AFTER department;
ALTER TABLE courses ADD COLUMN syllabus TEXT COMMENT '课程大纲' AFTER cover_image;
ALTER TABLE courses ADD COLUMN popularity INT DEFAULT 0 COMMENT '热度值' AFTER current_students;

-- 2. 评论表新增评分字段
ALTER TABLE course_comments ADD COLUMN rating INT COMMENT '评分1-5' AFTER content;

-- 3. 为已有课程补充分类和院系数据
UPDATE courses SET category = '专业必修', department = '计算机学院' WHERE id IN (1, 2);
UPDATE courses SET category = '通识选修', department = '计算机学院' WHERE id IN (3, 4);
UPDATE courses SET category = '专业选修', department = '软件工程系' WHERE id IN (5, 6);

-- 4. 初始化热度值（选课人数 × 10）
UPDATE courses SET popularity = current_students * 10;

-- 5. 为新字段添加索引
ALTER TABLE courses ADD INDEX idx_category (category);
ALTER TABLE courses ADD INDEX idx_department (department);
ALTER TABLE courses ADD INDEX idx_popularity (popularity);

-- 6. Course content, video learning progress, and danmaku tables
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

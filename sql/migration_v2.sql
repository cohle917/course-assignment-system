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
